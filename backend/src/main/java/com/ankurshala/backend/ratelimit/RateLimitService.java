package com.ankurshala.backend.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

/**
 * Redis-based distributed rate limiting service.
 * Implements sliding window algorithm for accurate rate limiting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String BLOCK_LIST_PREFIX = "blocked:";
    private static final String VIOLATION_PREFIX = "violations:";

    /**
     * Check if a request is allowed under rate limits.
     *
     * @param key Unique identifier (user ID or IP address)
     * @param endpoint The endpoint being accessed
     * @param isAuthenticated Whether the user is authenticated
     * @return RateLimitResult with allow/deny and remaining quota
     */
    public RateLimitResult checkRateLimit(String key, String endpoint, boolean isAuthenticated) {
        if (!rateLimitProperties.isEnabled()) {
            return new RateLimitResult(true, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        }

        // Check if IP is blocked
        if (isBlocked(key)) {
            log.warn("Request from blocked key: {}", key);
            return new RateLimitResult(false, 0, 0, rateLimitProperties.getBlockDurationSeconds());
        }

        // Get rate limit config for endpoint
        RateLimitProperties.RateLimitConfig config = getConfigForEndpoint(endpoint, isAuthenticated);

        String rateLimitKey = RATE_LIMIT_PREFIX + key + ":" + endpoint;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (config.getWindowSeconds() * 1000L);

        try {
            // Remove old entries outside the window
            redisTemplate.opsForZSet().removeRangeByScore(rateLimitKey, 0, windowStart);

            // Count requests in current window
            Long requestCount = redisTemplate.opsForZSet().count(rateLimitKey, windowStart, currentTime);
            if (requestCount == null) {
                requestCount = 0L;
            }

            int remaining = config.getLimit() - requestCount.intValue();

            if (requestCount >= config.getLimit()) {
                // Rate limit exceeded
                recordViolation(key);
                log.warn("Rate limit exceeded for key: {} on endpoint: {}. Count: {}, Limit: {}", 
                         key, endpoint, requestCount, config.getLimit());
                return new RateLimitResult(false, 0, config.getLimit(), config.getWindowSeconds());
            }

            // Add current request
            redisTemplate.opsForZSet().add(rateLimitKey, String.valueOf(currentTime), currentTime);
            redisTemplate.expire(rateLimitKey, Duration.ofSeconds(config.getWindowSeconds()));

            log.debug("Rate limit check passed for key: {} on endpoint: {}. Remaining: {}/{}", 
                     key, endpoint, remaining - 1, config.getLimit());

            return new RateLimitResult(true, remaining - 1, config.getLimit(), 0);

        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            // Fail open - allow request on Redis errors
            return new RateLimitResult(true, config.getLimit(), config.getLimit(), 0);
        }
    }

    /**
     * Check if a key (IP or user) is blocked.
     */
    private boolean isBlocked(String key) {
        String blockKey = BLOCK_LIST_PREFIX + key;
        Boolean isBlocked = redisTemplate.hasKey(blockKey);
        return Boolean.TRUE.equals(isBlocked);
    }

    /**
     * Record a rate limit violation.
     * Blocks the key if violations exceed threshold.
     */
    private void recordViolation(String key) {
        String violationKey = VIOLATION_PREFIX + key;
        Long violations = redisTemplate.opsForValue().increment(violationKey);
        
        if (violations == null) {
            violations = 1L;
        }

        redisTemplate.expire(violationKey, Duration.ofHours(1));

        if (violations >= rateLimitProperties.getMaxViolationsBeforeBlock()) {
            blockKey(key);
        }
    }

    /**
     * Block a key from making requests.
     */
    private void blockKey(String key) {
        String blockKey = BLOCK_LIST_PREFIX + key;
        redisTemplate.opsForValue().set(blockKey, String.valueOf(System.currentTimeMillis()));
        redisTemplate.expire(blockKey, Duration.ofSeconds(rateLimitProperties.getBlockDurationSeconds()));
        
        log.warn("Blocked key due to excessive violations: {}", key);
    }

    /**
     * Manually unblock a key.
     */
    public void unblockKey(String key) {
        String blockKey = BLOCK_LIST_PREFIX + key;
        String violationKey = VIOLATION_PREFIX + key;
        
        redisTemplate.delete(blockKey);
        redisTemplate.delete(violationKey);
        
        log.info("Manually unblocked key: {}", key);
    }

    /**
     * Get rate limit configuration for a specific endpoint.
     */
    private RateLimitProperties.RateLimitConfig getConfigForEndpoint(String endpoint, boolean isAuthenticated) {
        // Check for endpoint-specific config
        for (Map.Entry<String, RateLimitProperties.RateLimitConfig> entry : 
             rateLimitProperties.getEndpoints().entrySet()) {
            if (endpoint.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Return default config based on authentication
        return isAuthenticated ? 
               rateLimitProperties.getDefaultAuthenticated() : 
               rateLimitProperties.getDefaultAnonymous();
    }

    /**
     * Get remaining quota for a key.
     */
    public int getRemainingQuota(String key, String endpoint, boolean isAuthenticated) {
        RateLimitProperties.RateLimitConfig config = getConfigForEndpoint(endpoint, isAuthenticated);
        String rateLimitKey = RATE_LIMIT_PREFIX + key + ":" + endpoint;
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - (config.getWindowSeconds() * 1000L);

        try {
            Long requestCount = redisTemplate.opsForZSet().count(rateLimitKey, windowStart, currentTime);
            if (requestCount == null) {
                requestCount = 0L;
            }
            return Math.max(0, config.getLimit() - requestCount.intValue());
        } catch (Exception e) {
            log.error("Error getting remaining quota for key: {}", key, e);
            return config.getLimit();
        }
    }

    /**
     * Clear all rate limit data for a key.
     */
    public void clearRateLimitData(String key) {
        String pattern = RATE_LIMIT_PREFIX + key + ":*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared rate limit data for key: {}", key);
        }
    }
}

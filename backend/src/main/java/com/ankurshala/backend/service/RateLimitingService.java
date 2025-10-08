package com.ankurshala.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Service using Redis
 */
@Component
public class RateLimitingService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    
    // Rate limit configurations
    private static final int AUTH_REQUESTS_PER_MINUTE = 5;
    private static final int SIGNUP_REQUESTS_PER_HOUR = 3;
    private static final int GENERAL_REQUESTS_PER_MINUTE = 100;

    /**
     * Check if request is within rate limit
     */
    public boolean isAllowed(String key, int maxRequests, Duration window) {
        try {
            String redisKey = RATE_LIMIT_PREFIX + key;
            String current = redisTemplate.opsForValue().get(redisKey);
            
            if (current == null) {
                // First request
                redisTemplate.opsForValue().set(redisKey, "1", window);
                return true;
            }
            
            int currentCount = Integer.parseInt(current);
            if (currentCount >= maxRequests) {
                return false;
            }
            
            // Increment counter
            redisTemplate.opsForValue().increment(redisKey);
            return true;
            
        } catch (Exception e) {
            // If Redis is down, allow the request (fail open)
            return true;
        }
    }

    /**
     * Check rate limit for authentication endpoints
     */
    public boolean isAuthRequestAllowed(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String key = "auth:" + clientIp;
        return isAllowed(key, AUTH_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));
    }

    /**
     * Check rate limit for signup endpoints
     */
    public boolean isSignupRequestAllowed(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String key = "signup:" + clientIp;
        return isAllowed(key, SIGNUP_REQUESTS_PER_HOUR, Duration.ofHours(1));
    }

    /**
     * Check rate limit for general API endpoints
     */
    public boolean isGeneralRequestAllowed(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String key = "general:" + clientIp;
        return isAllowed(key, GENERAL_REQUESTS_PER_MINUTE, Duration.ofMinutes(1));
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Get remaining requests for a key
     */
    public int getRemainingRequests(String key, int maxRequests) {
        try {
            String redisKey = RATE_LIMIT_PREFIX + key;
            String current = redisTemplate.opsForValue().get(redisKey);
            
            if (current == null) {
                return maxRequests;
            }
            
            int currentCount = Integer.parseInt(current);
            return Math.max(0, maxRequests - currentCount);
            
        } catch (Exception e) {
            return maxRequests;
        }
    }

    /**
     * Get TTL for a rate limit key
     */
    public long getTTL(String key) {
        try {
            String redisKey = RATE_LIMIT_PREFIX + key;
            return redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        } catch (Exception e) {
            return -1;
        }
    }
}

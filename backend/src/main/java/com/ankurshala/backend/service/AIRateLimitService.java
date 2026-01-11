package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIRateLimitConfig;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Service for rate limiting AI endpoint usage.
 * Tracks per-student usage with Redis for distributed rate limiting.
 */
@Service
@Slf4j
public class AIRateLimitService {

    private static final String RATE_LIMIT_PREFIX = "ai:rate:";
    private static final String TOKEN_COUNT_PREFIX = "ai:tokens:";
    private static final String DAILY_COUNT_PREFIX = "ai:daily:";
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Autowired
    private AIRateLimitConfig rateLimitConfig;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Check if a chat request is allowed
     */
    public RateLimitResult checkChatLimit(Long studentId) {
        return checkLimit(studentId, "chat", rateLimitConfig.getChat());
    }

    /**
     * Check if a streaming chat request is allowed
     */
    public RateLimitResult checkStreamChatLimit(Long studentId) {
        return checkLimit(studentId, "stream", rateLimitConfig.getStreamChat());
    }

    /**
     * Check if a quiz generation request is allowed
     */
    public RateLimitResult checkQuizGenerationLimit(Long studentId) {
        return checkLimit(studentId, "quiz", rateLimitConfig.getQuizGeneration());
    }

    /**
     * Check if a content search request is allowed
     */
    public RateLimitResult checkContentSearchLimit(Long studentId) {
        return checkLimit(studentId, "search", rateLimitConfig.getContentSearch());
    }

    /**
     * Check rate limit for a specific operation type
     */
    public RateLimitResult checkLimit(Long studentId, String operationType, AIRateLimitConfig.LimitConfig config) {
        String traceId = TraceUtil.getTraceId();

        if (!rateLimitConfig.isEnabled()) {
            return RateLimitResult.allowed();
        }

        if (redisTemplate == null) {
            log.warn("Redis not available, skipping rate limit check - TraceId: {}", traceId);
            return RateLimitResult.allowed();
        }

        try {
            String windowKey = buildWindowKey(studentId, operationType);
            String dailyKey = buildDailyKey(studentId, operationType);

            // Check window rate limit
            Long windowCount = increment(windowKey, config.getWindowSeconds());
            int effectiveLimit = calculateEffectiveLimit(config.getLimit(), studentId);

            if (windowCount > effectiveLimit) {
                log.warn("Rate limit exceeded for student {} on {} - Count: {}, Limit: {}", 
                        studentId, operationType, windowCount, effectiveLimit);
                return RateLimitResult.limited(
                        "Too many requests. Please wait before trying again.",
                        rateLimitConfig.getCooldownSeconds(),
                        windowCount.intValue(),
                        effectiveLimit
                );
            }

            // Check daily limit
            Long dailyCount = increment(dailyKey, getSecondsUntilMidnight());
            if (dailyCount > config.getDailyLimit()) {
                log.warn("Daily limit exceeded for student {} on {} - Count: {}, Limit: {}", 
                        studentId, operationType, dailyCount, config.getDailyLimit());
                return RateLimitResult.dailyLimitExceeded(
                        "You've reached your daily AI usage limit. Try again tomorrow!",
                        dailyCount.intValue(),
                        config.getDailyLimit()
                );
            }

            // Check token budget if applicable
            RateLimitResult tokenCheck = checkTokenBudget(studentId);
            if (!tokenCheck.isAllowed()) {
                return tokenCheck;
            }

            return RateLimitResult.allowed(
                    effectiveLimit - windowCount.intValue(),
                    config.getDailyLimit() - dailyCount.intValue()
            );

        } catch (Exception e) {
            log.error("Error checking rate limit - TraceId: {}, Error: {}", traceId, e.getMessage());
            // Fail open - allow request if rate limiting fails
            return RateLimitResult.allowed();
        }
    }

    /**
     * Record token usage for a student
     */
    public void recordTokenUsage(Long studentId, int promptTokens, int completionTokens) {
        if (redisTemplate == null) return;

        try {
            int totalTokens = promptTokens + completionTokens;
            String hourlyKey = buildTokenKey(studentId, "hourly");
            String dailyKey = buildTokenKey(studentId, "daily");

            // Increment hourly counter
            increment(hourlyKey, totalTokens, 3600);  // 1 hour expiry

            // Increment daily counter
            increment(dailyKey, totalTokens, getSecondsUntilMidnight());

            log.debug("Recorded {} tokens for student {}", totalTokens, studentId);

        } catch (Exception e) {
            log.error("Failed to record token usage for student {}: {}", studentId, e.getMessage());
        }
    }

    /**
     * Check if student is within token budget
     */
    public RateLimitResult checkTokenBudget(Long studentId) {
        if (redisTemplate == null) return RateLimitResult.allowed();

        try {
            AIRateLimitConfig.TokenBudget budget = rateLimitConfig.getTokenBudget();
            
            String hourlyKey = buildTokenKey(studentId, "hourly");
            String dailyKey = buildTokenKey(studentId, "daily");

            Long hourlyTokens = getCount(hourlyKey);
            Long dailyTokens = getCount(dailyKey);

            // Check hourly limit
            if (hourlyTokens > budget.getHourlyLimit()) {
                return RateLimitResult.tokenBudgetExceeded(
                        "You've used a lot of AI features this hour. Take a short break and try again soon!",
                        hourlyTokens.intValue(),
                        budget.getHourlyLimit(),
                        "hourly"
                );
            }

            // Check daily limit
            if (dailyTokens > budget.getDailyLimit()) {
                return RateLimitResult.tokenBudgetExceeded(
                        "You've reached your daily AI usage limit. Great job learning today! Try again tomorrow.",
                        dailyTokens.intValue(),
                        budget.getDailyLimit(),
                        "daily"
                );
            }

            // Warn if approaching limit
            double usagePercent = (double) dailyTokens / budget.getDailyLimit();
            boolean isWarning = usagePercent >= budget.getWarningThreshold();

            return RateLimitResult.allowed(
                    budget.getDailyLimit() - dailyTokens.intValue(),
                    isWarning
            );

        } catch (Exception e) {
            log.error("Failed to check token budget for student {}: {}", studentId, e.getMessage());
            return RateLimitResult.allowed();
        }
    }

    /**
     * Get current usage stats for a student
     */
    public UsageStats getUsageStats(Long studentId) {
        UsageStats stats = new UsageStats();
        
        if (redisTemplate == null) {
            return stats;
        }

        try {
            // Get counts for each operation type
            stats.setChatCount(getCount(buildDailyKey(studentId, "chat")).intValue());
            stats.setStreamCount(getCount(buildDailyKey(studentId, "stream")).intValue());
            stats.setQuizCount(getCount(buildDailyKey(studentId, "quiz")).intValue());
            stats.setSearchCount(getCount(buildDailyKey(studentId, "search")).intValue());

            // Get token usage
            stats.setHourlyTokens(getCount(buildTokenKey(studentId, "hourly")).intValue());
            stats.setDailyTokens(getCount(buildTokenKey(studentId, "daily")).intValue());

            // Calculate limits
            AIRateLimitConfig.TokenBudget budget = rateLimitConfig.getTokenBudget();
            stats.setDailyTokenLimit(budget.getDailyLimit());
            stats.setHourlyTokenLimit(budget.getHourlyLimit());

            stats.setChatLimit(rateLimitConfig.getChat().getDailyLimit());
            stats.setQuizLimit(rateLimitConfig.getQuizGeneration().getDailyLimit());

            // Calculate estimated cost
            stats.setEstimatedCost(stats.getDailyTokens() * budget.getCostPer1KTokens() / 1000.0);

        } catch (Exception e) {
            log.error("Failed to get usage stats for student {}: {}", studentId, e.getMessage());
        }

        return stats;
    }

    /**
     * Reset rate limit for a student (admin action)
     */
    public void resetLimits(Long studentId) {
        if (redisTemplate == null) return;

        try {
            String pattern = RATE_LIMIT_PREFIX + studentId + ":*";
            String tokenPattern = TOKEN_COUNT_PREFIX + studentId + ":*";
            String dailyPattern = DAILY_COUNT_PREFIX + studentId + ":*";

            // Note: In production, use SCAN instead of KEYS
            redisTemplate.delete(redisTemplate.keys(pattern));
            redisTemplate.delete(redisTemplate.keys(tokenPattern));
            redisTemplate.delete(redisTemplate.keys(dailyPattern));

            log.info("Reset rate limits for student {}", studentId);

        } catch (Exception e) {
            log.error("Failed to reset limits for student {}: {}", studentId, e.getMessage());
        }
    }

    // Helper methods

    private String buildWindowKey(Long studentId, String operationType) {
        long windowStart = System.currentTimeMillis() / 1000 / 60;  // Current minute
        return RATE_LIMIT_PREFIX + studentId + ":" + operationType + ":" + windowStart;
    }

    private String buildDailyKey(Long studentId, String operationType) {
        String date = LocalDate.now(IST).toString();
        return DAILY_COUNT_PREFIX + studentId + ":" + operationType + ":" + date;
    }

    private String buildTokenKey(Long studentId, String period) {
        if ("hourly".equals(period)) {
            int hour = LocalDateTime.now(IST).getHour();
            return TOKEN_COUNT_PREFIX + studentId + ":hour:" + hour;
        } else {
            String date = LocalDate.now(IST).toString();
            return TOKEN_COUNT_PREFIX + studentId + ":day:" + date;
        }
    }

    private Long increment(String key, int expireSeconds) {
        return increment(key, 1, expireSeconds);
    }

    private Long increment(String key, int amount, int expireSeconds) {
        Long count = redisTemplate.opsForValue().increment(key, amount);
        if (count != null && count == amount) {
            redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
        }
        return count != null ? count : 0L;
    }

    private Long getCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    private int getSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now(IST);
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return (int) ChronoUnit.SECONDS.between(now, midnight);
    }

    private int calculateEffectiveLimit(int baseLimit, Long studentId) {
        // Apply burst multiplier if enabled
        if (rateLimitConfig.isAllowBurst()) {
            return (int) (baseLimit * rateLimitConfig.getBurstMultiplier());
        }
        return baseLimit;
    }

    // Result classes

    public static class RateLimitResult {
        private final boolean allowed;
        private final String message;
        private final int retryAfterSeconds;
        private final int currentCount;
        private final int limit;
        private final int remainingRequests;
        private final int remainingDaily;
        private final boolean isWarning;
        private final String limitType;

        private RateLimitResult(boolean allowed, String message, int retryAfterSeconds, 
                               int currentCount, int limit, int remainingRequests, 
                               int remainingDaily, boolean isWarning, String limitType) {
            this.allowed = allowed;
            this.message = message;
            this.retryAfterSeconds = retryAfterSeconds;
            this.currentCount = currentCount;
            this.limit = limit;
            this.remainingRequests = remainingRequests;
            this.remainingDaily = remainingDaily;
            this.isWarning = isWarning;
            this.limitType = limitType;
        }

        public static RateLimitResult allowed() {
            return new RateLimitResult(true, null, 0, 0, 0, -1, -1, false, null);
        }

        public static RateLimitResult allowed(int remaining, int remainingDaily) {
            return new RateLimitResult(true, null, 0, 0, 0, remaining, remainingDaily, false, null);
        }

        public static RateLimitResult allowed(int remainingDaily, boolean isWarning) {
            return new RateLimitResult(true, null, 0, 0, 0, -1, remainingDaily, isWarning, null);
        }

        public static RateLimitResult limited(String message, int retryAfterSeconds, int currentCount, int limit) {
            return new RateLimitResult(false, message, retryAfterSeconds, currentCount, limit, 0, -1, false, "rate");
        }

        public static RateLimitResult dailyLimitExceeded(String message, int currentCount, int limit) {
            return new RateLimitResult(false, message, 0, currentCount, limit, 0, 0, false, "daily");
        }

        public static RateLimitResult tokenBudgetExceeded(String message, int currentTokens, int limit, String period) {
            return new RateLimitResult(false, message, 0, currentTokens, limit, 0, 0, false, "token_" + period);
        }

        // Getters
        public boolean isAllowed() { return allowed; }
        public String getMessage() { return message; }
        public int getRetryAfterSeconds() { return retryAfterSeconds; }
        public int getCurrentCount() { return currentCount; }
        public int getLimit() { return limit; }
        public int getRemainingRequests() { return remainingRequests; }
        public int getRemainingDaily() { return remainingDaily; }
        public boolean isWarning() { return isWarning; }
        public String getLimitType() { return limitType; }
    }

    public static class UsageStats {
        private int chatCount;
        private int streamCount;
        private int quizCount;
        private int searchCount;
        private int hourlyTokens;
        private int dailyTokens;
        private int dailyTokenLimit;
        private int hourlyTokenLimit;
        private int chatLimit;
        private int quizLimit;
        private double estimatedCost;

        // Getters and setters
        public int getChatCount() { return chatCount; }
        public void setChatCount(int chatCount) { this.chatCount = chatCount; }
        public int getStreamCount() { return streamCount; }
        public void setStreamCount(int streamCount) { this.streamCount = streamCount; }
        public int getQuizCount() { return quizCount; }
        public void setQuizCount(int quizCount) { this.quizCount = quizCount; }
        public int getSearchCount() { return searchCount; }
        public void setSearchCount(int searchCount) { this.searchCount = searchCount; }
        public int getHourlyTokens() { return hourlyTokens; }
        public void setHourlyTokens(int hourlyTokens) { this.hourlyTokens = hourlyTokens; }
        public int getDailyTokens() { return dailyTokens; }
        public void setDailyTokens(int dailyTokens) { this.dailyTokens = dailyTokens; }
        public int getDailyTokenLimit() { return dailyTokenLimit; }
        public void setDailyTokenLimit(int dailyTokenLimit) { this.dailyTokenLimit = dailyTokenLimit; }
        public int getHourlyTokenLimit() { return hourlyTokenLimit; }
        public void setHourlyTokenLimit(int hourlyTokenLimit) { this.hourlyTokenLimit = hourlyTokenLimit; }
        public int getChatLimit() { return chatLimit; }
        public void setChatLimit(int chatLimit) { this.chatLimit = chatLimit; }
        public int getQuizLimit() { return quizLimit; }
        public void setQuizLimit(int quizLimit) { this.quizLimit = quizLimit; }
        public double getEstimatedCost() { return estimatedCost; }
        public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }
    }
}


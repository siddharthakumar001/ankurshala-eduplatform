package com.ankurshala.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for AI-specific rate limiting.
 * Provides fine-grained control over AI endpoint usage per student.
 */
@Configuration
@ConfigurationProperties(prefix = "app.ai.rate-limit")
@Data
public class AIRateLimitConfig {

    /**
     * Whether AI rate limiting is enabled
     */
    private boolean enabled = true;

    /**
     * Default limits for chat interactions
     */
    private LimitConfig chat = new LimitConfig(30, 60, 500);  // 30 chats per minute, 500 daily

    /**
     * Default limits for quiz generation
     */
    private LimitConfig quizGeneration = new LimitConfig(10, 60, 50);  // 10 per minute, 50 daily

    /**
     * Default limits for streaming chat
     */
    private LimitConfig streamChat = new LimitConfig(20, 60, 300);  // 20 per minute, 300 daily

    /**
     * Default limits for content search
     */
    private LimitConfig contentSearch = new LimitConfig(60, 60, 1000);  // 60 per minute, 1000 daily

    /**
     * Token budget per student per day
     */
    private TokenBudget tokenBudget = new TokenBudget();

    /**
     * Cooldown period in seconds after hitting rate limit
     */
    private int cooldownSeconds = 60;

    /**
     * Whether to allow burst requests above normal limit
     */
    private boolean allowBurst = true;

    /**
     * Burst multiplier (e.g., 1.5 means 50% above normal limit)
     */
    private double burstMultiplier = 1.5;

    /**
     * Grade-specific overrides (some grades may need more AI usage)
     */
    private Map<Integer, Double> gradeMultipliers = new HashMap<>();

    @Data
    public static class LimitConfig {
        private int limit;
        private int windowSeconds;
        private int dailyLimit;

        public LimitConfig() {
            this.limit = 30;
            this.windowSeconds = 60;
            this.dailyLimit = 500;
        }

        public LimitConfig(int limit, int windowSeconds, int dailyLimit) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
            this.dailyLimit = dailyLimit;
        }
    }

    @Data
    public static class TokenBudget {
        /**
         * Daily token limit per student
         */
        private int dailyLimit = 50000;

        /**
         * Hourly token limit per student
         */
        private int hourlyLimit = 10000;

        /**
         * Token limit per request
         */
        private int perRequestLimit = 4000;

        /**
         * Warning threshold (percentage of daily limit)
         */
        private double warningThreshold = 0.8;

        /**
         * Cost per 1K tokens (for monitoring)
         */
        private double costPer1KTokens = 0.002;  // GPT-4o-mini pricing
    }
}


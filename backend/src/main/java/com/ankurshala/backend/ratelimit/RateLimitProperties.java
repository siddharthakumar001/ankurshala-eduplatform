package com.ankurshala.backend.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for rate limiting.
 * Supports per-endpoint and global rate limits.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /**
     * Enable/disable rate limiting globally.
     */
    private boolean enabled = true;

    /**
     * Default limits for authenticated users (per user ID).
     */
    private RateLimitConfig defaultAuthenticated = new RateLimitConfig(100, 60); // 100 requests per minute

    /**
     * Default limits for anonymous users (per IP).
     */
    private RateLimitConfig defaultAnonymous = new RateLimitConfig(20, 60); // 20 requests per minute

    /**
     * Custom limits for specific endpoints.
     * Key: endpoint pattern (e.g., "/auth/login", "/student/bookings")
     * Value: rate limit configuration
     */
    private Map<String, RateLimitConfig> endpoints = new HashMap<>();

    /**
     * Time in seconds to block an IP after exceeding limits.
     */
    private long blockDurationSeconds = 300; // 5 minutes

    /**
     * Maximum violations before blocking an IP.
     */
    private int maxViolationsBeforeBlock = 10;

    @Data
    public static class RateLimitConfig {
        /**
         * Maximum number of requests allowed.
         */
        private int limit;

        /**
         * Time window in seconds.
         */
        private int windowSeconds;

        public RateLimitConfig() {
        }

        public RateLimitConfig(int limit, int windowSeconds) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }
    }
}

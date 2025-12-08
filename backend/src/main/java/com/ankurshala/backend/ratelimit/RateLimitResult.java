package com.ankurshala.backend.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Result of a rate limit check.
 */
@Data
@AllArgsConstructor
public class RateLimitResult {
    
    /**
     * Whether the request is allowed.
     */
    private boolean allowed;
    
    /**
     * Remaining requests in current window.
     */
    private int remaining;
    
    /**
     * Total limit for the window.
     */
    private int limit;
    
    /**
     * Seconds until rate limit resets.
     * 0 if request is allowed.
     */
    private long retryAfterSeconds;
}

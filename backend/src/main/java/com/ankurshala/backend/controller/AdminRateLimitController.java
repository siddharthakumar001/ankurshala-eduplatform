package com.ankurshala.backend.controller;

import com.ankurshala.backend.ratelimit.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin endpoints for managing rate limits.
 */
@Slf4j
@RestController
@RequestMapping("/admin/rate-limits")
@RequiredArgsConstructor
@Tag(name = "Admin - Rate Limits", description = "Manage API rate limits and blocked IPs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRateLimitController {

    private final RateLimitService rateLimitService;

    @Operation(summary = "Unblock an IP or user", description = "Remove rate limit block for a specific key")
    @PostMapping("/unblock")
    public ResponseEntity<Map<String, Object>> unblockKey(@RequestParam String key) {
        log.info("Admin unblocking key: {}", key);
        
        rateLimitService.unblockKey(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Key unblocked successfully");
        response.put("key", key);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Clear rate limit data", description = "Clear all rate limit counters for a key")
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearRateLimitData(@RequestParam String key) {
        log.info("Admin clearing rate limit data for key: {}", key);
        
        rateLimitService.clearRateLimitData(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Rate limit data cleared successfully");
        response.put("key", key);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get remaining quota", description = "Check remaining requests for a key")
    @GetMapping("/quota")
    public ResponseEntity<Map<String, Object>> getQuota(
            @RequestParam String key,
            @RequestParam String endpoint,
            @RequestParam(defaultValue = "true") boolean isAuthenticated) {
        
        int remaining = rateLimitService.getRemainingQuota(key, endpoint, isAuthenticated);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("key", key);
        response.put("endpoint", endpoint);
        response.put("remaining", remaining);
        
        return ResponseEntity.ok(response);
    }
}

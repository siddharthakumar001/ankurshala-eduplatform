package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.*;
import com.ankurshala.backend.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin endpoints for monitoring AI service health, costs, and usage metrics.
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", 
        "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin AI Metrics", description = "AI service monitoring and management APIs")
public class AdminAIMetricsController {

    @Autowired
    private AICostMonitoringService costMonitoringService;

    @Autowired
    private AIProviderService providerService;

    @Autowired
    private AICacheService cacheService;

    @Autowired
    private AIRateLimitService rateLimitService;

    @Autowired
    private EmbeddingService embeddingService;

    @GetMapping("/health")
    @Operation(summary = "Get comprehensive AI service health status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAIHealth() {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        Map<String, Object> health = new HashMap<>();

        // Provider health
        health.put("providers", providerService.getProviderHealth());
        health.put("activeProvider", providerService.getActiveProvider());

        // Budget status
        health.put("budgetStatus", costMonitoringService.checkBudgetStatus());

        // Cache stats
        health.put("cacheStats", cacheService.getCacheStats());

        // Embedding service status
        health.put("embeddingServiceAvailable", embeddingService.isAvailable());

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(health, "AI health status retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get AI usage metrics for a date range")
    public ResponseEntity<ApiResponse<AICostMonitoringService.UsageMetrics>> getMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        AICostMonitoringService.UsageMetrics metrics = costMonitoringService.getUsageMetrics(startDate, endDate);

        ApiResponse<AICostMonitoringService.UsageMetrics> apiResponse = ApiResponse.success(metrics, "Metrics retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/costs/daily")
    @Operation(summary = "Get daily cost breakdown")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDailyCosts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        LocalDate targetDate = date != null ? date : LocalDate.now();

        Map<String, Object> costs = new HashMap<>();
        costs.put("date", targetDate.toString());
        costs.put("totalSpending", costMonitoringService.getDailySpending());
        costs.put("byType", costMonitoringService.getSpendingByType(targetDate));
        costs.put("budgetStatus", costMonitoringService.checkBudgetStatus());

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(costs, "Daily costs retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/costs/monthly")
    @Operation(summary = "Get monthly cost summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyCosts() {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        Map<String, Object> costs = new HashMap<>();
        costs.put("monthlySpending", costMonitoringService.getMonthlySpending());
        costs.put("budgetStatus", costMonitoringService.checkBudgetStatus());

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(costs, "Monthly costs retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/providers")
    @Operation(summary = "Get AI provider status and health")
    public ResponseEntity<ApiResponse<Map<String, AIProviderService.ProviderHealth>>> getProviderStatus() {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        Map<String, AIProviderService.ProviderHealth> providers = providerService.getProviderHealth();

        ApiResponse<Map<String, AIProviderService.ProviderHealth>> apiResponse = 
                ApiResponse.success(providers, "Provider status retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/providers/{provider}/reset-circuit")
    @Operation(summary = "Reset circuit breaker for a specific provider")
    public ResponseEntity<ApiResponse<String>> resetCircuitBreaker(@PathVariable String provider) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        providerService.resetCircuitBreaker(provider);

        ApiResponse<String> apiResponse = ApiResponse.success(
                "Circuit breaker reset for " + provider, 
                "Circuit breaker reset successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "Get cache statistics")
    public ResponseEntity<ApiResponse<AICacheService.CacheStats>> getCacheStats() {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        AICacheService.CacheStats stats = cacheService.getCacheStats();

        ApiResponse<AICacheService.CacheStats> apiResponse = ApiResponse.success(stats, "Cache stats retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/cache/clear")
    @Operation(summary = "Clear all AI caches")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        cacheService.clearAllCaches();

        ApiResponse<String> apiResponse = ApiResponse.success("Cache cleared", "All AI caches have been cleared");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/cache/topic/{topicId}")
    @Operation(summary = "Invalidate cache for a specific topic")
    public ResponseEntity<ApiResponse<String>> invalidateTopicCache(@PathVariable Long topicId) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        cacheService.invalidateTopicCache(topicId);

        ApiResponse<String> apiResponse = ApiResponse.success(
                "Topic cache invalidated", 
                "Cache for topic " + topicId + " has been invalidated");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/rate-limits/student/{studentId}")
    @Operation(summary = "Get rate limit status for a specific student")
    public ResponseEntity<ApiResponse<AIRateLimitService.UsageStats>> getStudentRateLimits(
            @PathVariable Long studentId) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        AIRateLimitService.UsageStats stats = rateLimitService.getUsageStats(studentId);

        ApiResponse<AIRateLimitService.UsageStats> apiResponse = 
                ApiResponse.success(stats, "Student rate limit stats retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/rate-limits/student/{studentId}/reset")
    @Operation(summary = "Reset rate limits for a specific student")
    public ResponseEntity<ApiResponse<String>> resetStudentRateLimits(@PathVariable Long studentId) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        rateLimitService.resetLimits(studentId);

        ApiResponse<String> apiResponse = ApiResponse.success(
                "Rate limits reset", 
                "Rate limits for student " + studentId + " have been reset");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get AI admin dashboard data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        Map<String, Object> dashboard = new HashMap<>();

        // Summary stats
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        dashboard.put("todaySpending", costMonitoringService.getDailySpending());
        dashboard.put("monthlySpending", costMonitoringService.getMonthlySpending());
        dashboard.put("budgetStatus", costMonitoringService.checkBudgetStatus());

        // Provider health
        Map<String, AIProviderService.ProviderHealth> providers = providerService.getProviderHealth();
        long healthyProviders = providers.values().stream().filter(AIProviderService.ProviderHealth::isAvailable).count();
        dashboard.put("totalProviders", providers.size());
        dashboard.put("healthyProviders", healthyProviders);
        dashboard.put("providers", providers);

        // Cache stats
        AICacheService.CacheStats cacheStats = cacheService.getCacheStats();
        dashboard.put("cacheStats", cacheStats);

        // Usage metrics
        AICostMonitoringService.UsageMetrics metrics = costMonitoringService.getUsageMetrics(monthStart, today);
        dashboard.put("monthlyMetrics", metrics);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(dashboard, "Dashboard data retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }
}


package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Multi-provider LLM service with circuit breaker and fallback support.
 * Provides resilient AI operations with automatic failover.
 */
@Service
@Slf4j
public class AIProviderService {

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private AIConfig.AIProperties aiProperties;

    @Value("${app.ai.circuit-breaker.failure-threshold:5}")
    private int failureThreshold;

    @Value("${app.ai.circuit-breaker.recovery-timeout-seconds:60}")
    private int recoveryTimeoutSeconds;

    @Value("${app.ai.circuit-breaker.half-open-requests:3}")
    private int halfOpenRequests;

    @Value("${app.ai.timeout-seconds:30}")
    private int timeoutSeconds;

    // Circuit breaker state per provider
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();

    // Provider health metrics
    private final Map<String, ProviderMetrics> providerMetrics = new ConcurrentHashMap<>();

    // Supported providers (can be extended)
    private static final List<String> PROVIDER_PRIORITY = Arrays.asList("openai", "anthropic", "gemini");

    /**
     * Execute a chat request with circuit breaker and fallback
     */
    public ChatResponse executeWithFallback(Prompt prompt) {
        String traceId = TraceUtil.getTraceId();
        String primaryProvider = aiProperties.getProvider();

        log.debug("Executing AI request - TraceId: {}, Provider: {}", traceId, primaryProvider);

        // Try primary provider first
        if (isProviderAvailable(primaryProvider)) {
            try {
                return executeWithCircuitBreaker(primaryProvider, prompt);
            } catch (Exception e) {
                log.warn("Primary provider {} failed - TraceId: {}, Error: {}", 
                        primaryProvider, traceId, e.getMessage());
                recordFailure(primaryProvider);
            }
        }

        // Try fallback providers
        for (String provider : PROVIDER_PRIORITY) {
            if (!provider.equals(primaryProvider) && isProviderAvailable(provider)) {
                try {
                    log.info("Attempting fallback to provider {} - TraceId: {}", provider, traceId);
                    return executeWithCircuitBreaker(provider, prompt);
                } catch (Exception e) {
                    log.warn("Fallback provider {} failed - TraceId: {}", provider, traceId);
                    recordFailure(provider);
                }
            }
        }

        // All providers failed
        throw new AIServiceUnavailableException("All AI providers are currently unavailable. Please try again later.");
    }

    /**
     * Execute request with circuit breaker protection
     */
    private ChatResponse executeWithCircuitBreaker(String provider, Prompt prompt) {
        CircuitBreakerState state = getCircuitBreakerState(provider);

        switch (state.getStatus()) {
            case OPEN:
                if (shouldAttemptReset(state)) {
                    state.setStatus(CircuitStatus.HALF_OPEN);
                    state.setHalfOpenAttempts(0);
                } else {
                    throw new CircuitBreakerOpenException("Circuit breaker is open for provider: " + provider);
                }
                break;
            case HALF_OPEN:
                if (state.getHalfOpenAttempts() >= halfOpenRequests) {
                    state.setStatus(CircuitStatus.OPEN);
                    state.setLastFailureTime(Instant.now());
                    throw new CircuitBreakerOpenException("Circuit breaker returning to open state for: " + provider);
                }
                state.incrementHalfOpenAttempts();
                break;
            default:
                break;
        }

        long startTime = System.currentTimeMillis();
        try {
            ChatResponse response = executeRequest(provider, prompt);
            recordSuccess(provider, System.currentTimeMillis() - startTime);
            
            // Reset circuit breaker on success
            if (state.getStatus() == CircuitStatus.HALF_OPEN) {
                state.setStatus(CircuitStatus.CLOSED);
                state.resetFailureCount();
                log.info("Circuit breaker closed for provider: {}", provider);
            }
            
            return response;
        } catch (Exception e) {
            recordFailure(provider);
            throw e;
        }
    }

    /**
     * Execute the actual AI request
     */
    private ChatResponse executeRequest(String provider, Prompt prompt) {
        if (chatModel == null) {
            throw new AIServiceUnavailableException("No chat model configured");
        }

        // In a real implementation, you'd have multiple ChatModel beans for different providers
        // For now, we use the single configured model
        return chatModel.call(prompt);
    }

    /**
     * Check if a provider is available
     */
    public boolean isProviderAvailable(String provider) {
        if (!aiProperties.isAvailable()) {
            return false;
        }

        CircuitBreakerState state = getCircuitBreakerState(provider);
        
        if (state.getStatus() == CircuitStatus.OPEN) {
            return shouldAttemptReset(state);
        }

        return true;
    }

    /**
     * Record a successful request
     */
    private void recordSuccess(String provider, long latencyMs) {
        ProviderMetrics metrics = getOrCreateMetrics(provider);
        metrics.recordSuccess(latencyMs);
        
        CircuitBreakerState state = getCircuitBreakerState(provider);
        if (state.getStatus() != CircuitStatus.CLOSED) {
            state.setStatus(CircuitStatus.CLOSED);
            state.resetFailureCount();
        }
    }

    /**
     * Record a failed request
     */
    private void recordFailure(String provider) {
        ProviderMetrics metrics = getOrCreateMetrics(provider);
        metrics.recordFailure();

        CircuitBreakerState state = getCircuitBreakerState(provider);
        state.incrementFailureCount();
        state.setLastFailureTime(Instant.now());

        if (state.getFailureCount() >= failureThreshold) {
            state.setStatus(CircuitStatus.OPEN);
            log.warn("Circuit breaker opened for provider: {} after {} failures", 
                    provider, state.getFailureCount());
        }
    }

    /**
     * Check if circuit breaker should attempt reset
     */
    private boolean shouldAttemptReset(CircuitBreakerState state) {
        if (state.getLastFailureTime() == null) {
            return true;
        }
        Duration elapsed = Duration.between(state.getLastFailureTime(), Instant.now());
        return elapsed.getSeconds() >= recoveryTimeoutSeconds;
    }

    /**
     * Get circuit breaker state for a provider
     */
    private CircuitBreakerState getCircuitBreakerState(String provider) {
        return circuitBreakers.computeIfAbsent(provider, k -> new CircuitBreakerState());
    }

    /**
     * Get or create metrics for a provider
     */
    private ProviderMetrics getOrCreateMetrics(String provider) {
        return providerMetrics.computeIfAbsent(provider, k -> new ProviderMetrics(provider));
    }

    /**
     * Get health status of all providers
     */
    public Map<String, ProviderHealth> getProviderHealth() {
        Map<String, ProviderHealth> health = new HashMap<>();
        
        for (String provider : PROVIDER_PRIORITY) {
            CircuitBreakerState state = circuitBreakers.get(provider);
            ProviderMetrics metrics = providerMetrics.get(provider);
            
            ProviderHealth providerHealth = new ProviderHealth();
            providerHealth.setProvider(provider);
            providerHealth.setAvailable(isProviderAvailable(provider));
            providerHealth.setCircuitStatus(state != null ? state.getStatus().name() : "CLOSED");
            
            if (metrics != null) {
                providerHealth.setSuccessRate(metrics.getSuccessRate());
                providerHealth.setAverageLatencyMs(metrics.getAverageLatency());
                providerHealth.setTotalRequests(metrics.getTotalRequests());
                providerHealth.setFailedRequests(metrics.getFailedRequests());
            }
            
            health.put(provider, providerHealth);
        }
        
        return health;
    }

    /**
     * Reset circuit breaker for a provider (admin action)
     */
    public void resetCircuitBreaker(String provider) {
        CircuitBreakerState state = circuitBreakers.get(provider);
        if (state != null) {
            state.setStatus(CircuitStatus.CLOSED);
            state.resetFailureCount();
            log.info("Circuit breaker manually reset for provider: {}", provider);
        }
    }

    /**
     * Get current active provider
     */
    public String getActiveProvider() {
        String primary = aiProperties.getProvider();
        if (isProviderAvailable(primary)) {
            return primary;
        }
        for (String provider : PROVIDER_PRIORITY) {
            if (isProviderAvailable(provider)) {
                return provider;
            }
        }
        return null;
    }

    // Inner classes

    public enum CircuitStatus {
        CLOSED,    // Normal operation
        OPEN,      // Blocking requests
        HALF_OPEN  // Testing if service recovered
    }

    private static class CircuitBreakerState {
        private CircuitStatus status = CircuitStatus.CLOSED;
        private AtomicInteger failureCount = new AtomicInteger(0);
        private AtomicInteger halfOpenAttempts = new AtomicInteger(0);
        private Instant lastFailureTime;

        public CircuitStatus getStatus() { return status; }
        public void setStatus(CircuitStatus status) { this.status = status; }
        public int getFailureCount() { return failureCount.get(); }
        public void incrementFailureCount() { failureCount.incrementAndGet(); }
        public void resetFailureCount() { failureCount.set(0); }
        public int getHalfOpenAttempts() { return halfOpenAttempts.get(); }
        public void setHalfOpenAttempts(int attempts) { halfOpenAttempts.set(attempts); }
        public void incrementHalfOpenAttempts() { halfOpenAttempts.incrementAndGet(); }
        public Instant getLastFailureTime() { return lastFailureTime; }
        public void setLastFailureTime(Instant time) { this.lastFailureTime = time; }
    }

    private static class ProviderMetrics {
        private final String provider;
        private final AtomicLong totalRequests = new AtomicLong(0);
        private final AtomicLong successfulRequests = new AtomicLong(0);
        private final AtomicLong failedRequests = new AtomicLong(0);
        private final AtomicLong totalLatency = new AtomicLong(0);

        public ProviderMetrics(String provider) {
            this.provider = provider;
        }

        public void recordSuccess(long latencyMs) {
            totalRequests.incrementAndGet();
            successfulRequests.incrementAndGet();
            totalLatency.addAndGet(latencyMs);
        }

        public void recordFailure() {
            totalRequests.incrementAndGet();
            failedRequests.incrementAndGet();
        }

        public double getSuccessRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) successfulRequests.get() / total * 100 : 100.0;
        }

        public double getAverageLatency() {
            long successful = successfulRequests.get();
            return successful > 0 ? (double) totalLatency.get() / successful : 0.0;
        }

        public long getTotalRequests() { return totalRequests.get(); }
        public long getFailedRequests() { return failedRequests.get(); }
    }

    public static class ProviderHealth {
        private String provider;
        private boolean available;
        private String circuitStatus;
        private double successRate;
        private double averageLatencyMs;
        private long totalRequests;
        private long failedRequests;

        // Getters and setters
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public String getCircuitStatus() { return circuitStatus; }
        public void setCircuitStatus(String status) { this.circuitStatus = status; }
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double rate) { this.successRate = rate; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public void setAverageLatencyMs(double latency) { this.averageLatencyMs = latency; }
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long requests) { this.totalRequests = requests; }
        public long getFailedRequests() { return failedRequests; }
        public void setFailedRequests(long failed) { this.failedRequests = failed; }
    }

    // Custom exceptions

    public static class AIServiceUnavailableException extends RuntimeException {
        public AIServiceUnavailableException(String message) {
            super(message);
        }
    }

    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}


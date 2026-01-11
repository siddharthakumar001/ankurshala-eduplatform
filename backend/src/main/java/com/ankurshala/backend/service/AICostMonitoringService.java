package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.AIInteraction;
import com.ankurshala.backend.repository.AIInteractionRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service for monitoring AI costs and usage metrics.
 * Provides real-time tracking and alerting for AI expenditure.
 */
@Service
@Slf4j
public class AICostMonitoringService {

    private static final String COST_PREFIX = "ai:cost:";
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Autowired
    private AIInteractionRepository aiInteractionRepository;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    // Pricing per 1K tokens (can be updated based on provider)
    @Value("${app.ai.pricing.input-per-1k:0.00015}")  // GPT-4o-mini input
    private double inputPricePer1K;

    @Value("${app.ai.pricing.output-per-1k:0.0006}")  // GPT-4o-mini output
    private double outputPricePer1K;

    @Value("${app.ai.pricing.embedding-per-1k:0.00002}")  // Ada-002
    private double embeddingPricePer1K;

    // Budget limits
    @Value("${app.ai.budget.daily-limit:100.0}")  // $100 daily limit
    private double dailyBudgetLimit;

    @Value("${app.ai.budget.monthly-limit:2000.0}")  // $2000 monthly limit
    private double monthlyBudgetLimit;

    @Value("${app.ai.budget.alert-threshold:0.8}")  // Alert at 80% usage
    private double alertThreshold;

    /**
     * Record token usage and calculate cost
     */
    public CostRecord recordUsage(Long studentId, String interactionType, 
                                   int promptTokens, int completionTokens, String model) {
        double inputCost = (promptTokens / 1000.0) * inputPricePer1K;
        double outputCost = (completionTokens / 1000.0) * outputPricePer1K;
        double totalCost = inputCost + outputCost;

        CostRecord record = new CostRecord();
        record.setStudentId(studentId);
        record.setInteractionType(interactionType);
        record.setPromptTokens(promptTokens);
        record.setCompletionTokens(completionTokens);
        record.setTotalTokens(promptTokens + completionTokens);
        record.setInputCost(inputCost);
        record.setOutputCost(outputCost);
        record.setTotalCost(totalCost);
        record.setModel(model);
        record.setTimestamp(LocalDateTime.now(IST));

        // Update Redis counters
        if (redisTemplate != null) {
            try {
                String today = LocalDate.now(IST).toString();
                String month = LocalDate.now(IST).toString().substring(0, 7);

                // Increment daily cost (in cents to avoid floating point issues)
                long costCents = Math.round(totalCost * 100);
                incrementCost(COST_PREFIX + "daily:" + today, costCents);
                incrementCost(COST_PREFIX + "monthly:" + month, costCents);
                incrementCost(COST_PREFIX + "total", costCents);

                // Increment by interaction type
                incrementCost(COST_PREFIX + "type:" + interactionType + ":" + today, costCents);

                // Increment by student
                incrementCost(COST_PREFIX + "student:" + studentId + ":" + today, costCents);

                // Token counters
                incrementTokens(COST_PREFIX + "tokens:daily:" + today, promptTokens + completionTokens);
                incrementTokens(COST_PREFIX + "tokens:monthly:" + month, promptTokens + completionTokens);

            } catch (Exception e) {
                log.warn("Failed to update cost counters: {}", e.getMessage());
            }
        }

        log.debug("Recorded AI usage - StudentId: {}, Type: {}, Tokens: {}, Cost: ${}", 
                studentId, interactionType, record.getTotalTokens(), 
                String.format("%.4f", totalCost));

        return record;
    }

    /**
     * Get current daily spending
     */
    public double getDailySpending() {
        if (redisTemplate == null) return 0.0;

        String today = LocalDate.now(IST).toString();
        String key = COST_PREFIX + "daily:" + today;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) / 100.0 : 0.0;
    }

    /**
     * Get current monthly spending
     */
    public double getMonthlySpending() {
        if (redisTemplate == null) return 0.0;

        String month = LocalDate.now(IST).toString().substring(0, 7);
        String key = COST_PREFIX + "monthly:" + month;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) / 100.0 : 0.0;
    }

    /**
     * Check if budget limits are exceeded
     */
    public BudgetStatus checkBudgetStatus() {
        BudgetStatus status = new BudgetStatus();
        
        double dailySpending = getDailySpending();
        double monthlySpending = getMonthlySpending();

        status.setDailySpending(dailySpending);
        status.setMonthlySpending(monthlySpending);
        status.setDailyLimit(dailyBudgetLimit);
        status.setMonthlyLimit(monthlyBudgetLimit);
        status.setDailyUsagePercent(dailySpending / dailyBudgetLimit * 100);
        status.setMonthlyUsagePercent(monthlySpending / monthlyBudgetLimit * 100);

        // Check alerts
        if (dailySpending >= dailyBudgetLimit) {
            status.setStatus("DAILY_EXCEEDED");
            status.setMessage("Daily budget limit exceeded!");
        } else if (monthlySpending >= monthlyBudgetLimit) {
            status.setStatus("MONTHLY_EXCEEDED");
            status.setMessage("Monthly budget limit exceeded!");
        } else if (dailySpending >= dailyBudgetLimit * alertThreshold) {
            status.setStatus("DAILY_WARNING");
            status.setMessage("Approaching daily budget limit");
        } else if (monthlySpending >= monthlyBudgetLimit * alertThreshold) {
            status.setStatus("MONTHLY_WARNING");
            status.setMessage("Approaching monthly budget limit");
        } else {
            status.setStatus("OK");
            status.setMessage("Budget within limits");
        }

        return status;
    }

    /**
     * Get spending by interaction type
     */
    public Map<String, Double> getSpendingByType(LocalDate date) {
        Map<String, Double> spending = new HashMap<>();
        
        if (redisTemplate == null) return spending;

        String dateStr = date.toString();
        for (String type : Arrays.asList("CHAT", "QUIZ_GENERATE", "QUIZ_GRADE", "RECOMMENDATION")) {
            String key = COST_PREFIX + "type:" + type + ":" + dateStr;
            String value = redisTemplate.opsForValue().get(key);
            spending.put(type, value != null ? Long.parseLong(value) / 100.0 : 0.0);
        }

        return spending;
    }

    /**
     * Get top students by AI usage
     */
    public List<StudentUsage> getTopStudentsByUsage(int limit) {
        LocalDateTime startOfDay = LocalDate.now(IST).atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Query from database for detailed analysis
        List<Object[]> results = aiInteractionRepository.countByTypeInRange(startOfDay, endOfDay);
        
        // This would need a custom query for per-student aggregation
        // For now, return empty list - implement with proper query
        return new ArrayList<>();
    }

    /**
     * Get comprehensive usage metrics
     */
    public UsageMetrics getUsageMetrics(LocalDate startDate, LocalDate endDate) {
        UsageMetrics metrics = new UsageMetrics();
        
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        // Get from database
        List<Object[]> typeBreakdown = aiInteractionRepository.countByTypeInRange(start, end);
        
        Map<String, Long> byType = new HashMap<>();
        long totalInteractions = 0;
        for (Object[] row : typeBreakdown) {
            AIInteraction.InteractionType type = (AIInteraction.InteractionType) row[0];
            Long count = (Long) row[1];
            byType.put(type.name(), count);
            totalInteractions += count;
        }
        metrics.setInteractionsByType(byType);
        metrics.setTotalInteractions(totalInteractions);

        // Get error stats
        List<Object[]> errorBreakdown = aiInteractionRepository.countErrorsByType(start, end);
        Map<String, Long> byError = new HashMap<>();
        long totalErrors = 0;
        for (Object[] row : errorBreakdown) {
            String errorType = (String) row[0];
            Long count = (Long) row[1];
            if (errorType != null) {
                byError.put(errorType, count);
                totalErrors += count;
            }
        }
        metrics.setErrorsByType(byError);
        metrics.setTotalErrors(totalErrors);

        // Get latency metrics
        Double avgLatency = aiInteractionRepository.calculateAverageLatency(
                AIInteraction.InteractionType.CHAT, start);
        metrics.setAverageLatencyMs(avgLatency != null ? avgLatency : 0.0);

        // Get retrieval metrics
        Double avgRetrievalScore = aiInteractionRepository.calculateAverageRetrievalScore(start);
        metrics.setAverageRetrievalScore(avgRetrievalScore != null ? avgRetrievalScore : 0.0);

        // Cost metrics
        metrics.setTotalCost(getMonthlySpending());
        metrics.setDailyCost(getDailySpending());

        return metrics;
    }

    /**
     * Scheduled job to check budget and send alerts
     */
    @Scheduled(fixedRate = 300000)  // Every 5 minutes
    public void checkBudgetAlerts() {
        BudgetStatus status = checkBudgetStatus();
        
        if (status.getStatus().contains("EXCEEDED")) {
            log.error("BUDGET ALERT: {} - Daily: ${}, Monthly: ${}", 
                    status.getMessage(), 
                    String.format("%.2f", status.getDailySpending()),
                    String.format("%.2f", status.getMonthlySpending()));
            // TODO: Send notification to admin
        } else if (status.getStatus().contains("WARNING")) {
            log.warn("Budget Warning: {} - Daily usage: {}%, Monthly usage: {}%",
                    status.getMessage(),
                    String.format("%.1f", status.getDailyUsagePercent()),
                    String.format("%.1f", status.getMonthlyUsagePercent()));
        }
    }

    // Helper methods

    private void incrementCost(String key, long cents) {
        redisTemplate.opsForValue().increment(key, cents);
        // Set expiry for daily keys (32 days to allow month-end queries)
        if (key.contains(":daily:")) {
            redisTemplate.expire(key, 32, TimeUnit.DAYS);
        }
    }

    private void incrementTokens(String key, int tokens) {
        redisTemplate.opsForValue().increment(key, tokens);
        if (key.contains(":daily:")) {
            redisTemplate.expire(key, 32, TimeUnit.DAYS);
        }
    }

    // Data classes

    public static class CostRecord {
        private Long studentId;
        private String interactionType;
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        private double inputCost;
        private double outputCost;
        private double totalCost;
        private String model;
        private LocalDateTime timestamp;

        // Getters and setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long id) { this.studentId = id; }
        public String getInteractionType() { return interactionType; }
        public void setInteractionType(String type) { this.interactionType = type; }
        public int getPromptTokens() { return promptTokens; }
        public void setPromptTokens(int tokens) { this.promptTokens = tokens; }
        public int getCompletionTokens() { return completionTokens; }
        public void setCompletionTokens(int tokens) { this.completionTokens = tokens; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int tokens) { this.totalTokens = tokens; }
        public double getInputCost() { return inputCost; }
        public void setInputCost(double cost) { this.inputCost = cost; }
        public double getOutputCost() { return outputCost; }
        public void setOutputCost(double cost) { this.outputCost = cost; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double cost) { this.totalCost = cost; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime time) { this.timestamp = time; }
    }

    public static class BudgetStatus {
        private double dailySpending;
        private double monthlySpending;
        private double dailyLimit;
        private double monthlyLimit;
        private double dailyUsagePercent;
        private double monthlyUsagePercent;
        private String status;
        private String message;

        // Getters and setters
        public double getDailySpending() { return dailySpending; }
        public void setDailySpending(double spending) { this.dailySpending = spending; }
        public double getMonthlySpending() { return monthlySpending; }
        public void setMonthlySpending(double spending) { this.monthlySpending = spending; }
        public double getDailyLimit() { return dailyLimit; }
        public void setDailyLimit(double limit) { this.dailyLimit = limit; }
        public double getMonthlyLimit() { return monthlyLimit; }
        public void setMonthlyLimit(double limit) { this.monthlyLimit = limit; }
        public double getDailyUsagePercent() { return dailyUsagePercent; }
        public void setDailyUsagePercent(double percent) { this.dailyUsagePercent = percent; }
        public double getMonthlyUsagePercent() { return monthlyUsagePercent; }
        public void setMonthlyUsagePercent(double percent) { this.monthlyUsagePercent = percent; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class StudentUsage {
        private Long studentId;
        private String studentName;
        private int totalInteractions;
        private int totalTokens;
        private double totalCost;

        // Getters and setters
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long id) { this.studentId = id; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String name) { this.studentName = name; }
        public int getTotalInteractions() { return totalInteractions; }
        public void setTotalInteractions(int count) { this.totalInteractions = count; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int tokens) { this.totalTokens = tokens; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double cost) { this.totalCost = cost; }
    }

    public static class UsageMetrics {
        private long totalInteractions;
        private long totalErrors;
        private double averageLatencyMs;
        private double averageRetrievalScore;
        private double totalCost;
        private double dailyCost;
        private Map<String, Long> interactionsByType;
        private Map<String, Long> errorsByType;

        // Getters and setters
        public long getTotalInteractions() { return totalInteractions; }
        public void setTotalInteractions(long count) { this.totalInteractions = count; }
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long errors) { this.totalErrors = errors; }
        public double getAverageLatencyMs() { return averageLatencyMs; }
        public void setAverageLatencyMs(double latency) { this.averageLatencyMs = latency; }
        public double getAverageRetrievalScore() { return averageRetrievalScore; }
        public void setAverageRetrievalScore(double score) { this.averageRetrievalScore = score; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double cost) { this.totalCost = cost; }
        public double getDailyCost() { return dailyCost; }
        public void setDailyCost(double cost) { this.dailyCost = cost; }
        public Map<String, Long> getInteractionsByType() { return interactionsByType; }
        public void setInteractionsByType(Map<String, Long> map) { this.interactionsByType = map; }
        public Map<String, Long> getErrorsByType() { return errorsByType; }
        public void setErrorsByType(Map<String, Long> map) { this.errorsByType = map; }

        public double getErrorRate() {
            return totalInteractions > 0 ? (double) totalErrors / totalInteractions * 100 : 0;
        }
    }
}


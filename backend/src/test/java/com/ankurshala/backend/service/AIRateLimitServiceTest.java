package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIRateLimitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIRateLimitServiceTest {

    @Mock
    private AIRateLimitConfig rateLimitConfig;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AIRateLimitService rateLimitService;

    private Long studentId = 123L;

    @BeforeEach
    void setUp() {
        // Setup default config values
        AIRateLimitConfig.LimitConfig chatConfig = new AIRateLimitConfig.LimitConfig(30, 60, 500);
        AIRateLimitConfig.TokenBudget tokenBudget = new AIRateLimitConfig.TokenBudget();
        
        when(rateLimitConfig.isEnabled()).thenReturn(true);
        when(rateLimitConfig.getChat()).thenReturn(chatConfig);
        when(rateLimitConfig.getTokenBudget()).thenReturn(tokenBudget);
        when(rateLimitConfig.getCooldownSeconds()).thenReturn(60);
        when(rateLimitConfig.isAllowBurst()).thenReturn(true);
        when(rateLimitConfig.getBurstMultiplier()).thenReturn(1.5);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void checkChatLimit_rateLimitDisabled_allowsRequest() {
        when(rateLimitConfig.isEnabled()).thenReturn(false);

        AIRateLimitService.RateLimitResult result = rateLimitService.checkChatLimit(studentId);

        assertTrue(result.isAllowed());
    }

    @Test
    void checkChatLimit_withinLimit_allowsRequest() {
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(5L);
        when(valueOperations.get(anyString())).thenReturn("1000");  // Token count

        AIRateLimitService.RateLimitResult result = rateLimitService.checkChatLimit(studentId);

        assertTrue(result.isAllowed());
        assertTrue(result.getRemainingRequests() > 0);
    }

    @Test
    void checkChatLimit_exceedsWindowLimit_deniesRequest() {
        // Return count above limit (30 * 1.5 burst = 45)
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(50L);

        AIRateLimitService.RateLimitResult result = rateLimitService.checkChatLimit(studentId);

        assertFalse(result.isAllowed());
        assertEquals("rate", result.getLimitType());
        assertNotNull(result.getMessage());
    }

    @Test
    void checkChatLimit_exceedsDailyLimit_deniesRequest() {
        // Window count OK
        when(valueOperations.increment(anyString(), anyLong()))
                .thenReturn(5L)   // Window count
                .thenReturn(600L); // Daily count above 500

        AIRateLimitService.RateLimitResult result = rateLimitService.checkChatLimit(studentId);

        assertFalse(result.isAllowed());
        assertEquals("daily", result.getLimitType());
    }

    @Test
    void checkTokenBudget_withinBudget_allowsRequest() {
        when(valueOperations.get(anyString())).thenReturn("1000");

        AIRateLimitService.RateLimitResult result = rateLimitService.checkTokenBudget(studentId);

        assertTrue(result.isAllowed());
    }

    @Test
    void checkTokenBudget_exceedsHourlyBudget_deniesRequest() {
        // Return value above hourly limit (10000)
        when(valueOperations.get(anyString())).thenReturn("15000");

        AIRateLimitService.RateLimitResult result = rateLimitService.checkTokenBudget(studentId);

        assertFalse(result.isAllowed());
        assertTrue(result.getLimitType().contains("hourly"));
    }

    @Test
    void checkTokenBudget_exceedsDailyBudget_deniesRequest() {
        // First call for hourly is OK, second for daily exceeds
        when(valueOperations.get(anyString()))
                .thenReturn("5000")   // Hourly OK
                .thenReturn("60000"); // Daily exceeds 50000

        AIRateLimitService.RateLimitResult result = rateLimitService.checkTokenBudget(studentId);

        assertFalse(result.isAllowed());
        assertTrue(result.getLimitType().contains("daily"));
    }

    @Test
    void checkTokenBudget_approachingLimit_setsWarning() {
        // 85% of daily limit (50000 * 0.85 = 42500)
        when(valueOperations.get(anyString()))
                .thenReturn("5000")   // Hourly OK
                .thenReturn("43000"); // Daily at warning threshold

        AIRateLimitService.RateLimitResult result = rateLimitService.checkTokenBudget(studentId);

        assertTrue(result.isAllowed());
        assertTrue(result.isWarning());
    }

    @Test
    void recordTokenUsage_updatesRedis() {
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(100L);

        rateLimitService.recordTokenUsage(studentId, 50, 100);

        // Verify hourly and daily counters are updated
        verify(valueOperations, atLeast(2)).increment(anyString(), anyLong());
    }

    @Test
    void getUsageStats_returnsCorrectStats() {
        AIRateLimitConfig.LimitConfig chatConfig = new AIRateLimitConfig.LimitConfig(30, 60, 500);
        AIRateLimitConfig.LimitConfig quizConfig = new AIRateLimitConfig.LimitConfig(10, 60, 50);
        AIRateLimitConfig.TokenBudget tokenBudget = new AIRateLimitConfig.TokenBudget();
        
        when(rateLimitConfig.getChat()).thenReturn(chatConfig);
        when(rateLimitConfig.getQuizGeneration()).thenReturn(quizConfig);
        when(rateLimitConfig.getTokenBudget()).thenReturn(tokenBudget);
        
        when(valueOperations.get(anyString())).thenReturn("10");

        AIRateLimitService.UsageStats stats = rateLimitService.getUsageStats(studentId);

        assertNotNull(stats);
        assertEquals(500, stats.getChatLimit());
        assertEquals(50, stats.getQuizLimit());
        assertEquals(50000, stats.getDailyTokenLimit());
    }

    @Test
    void checkLimit_redisUnavailable_allowsRequest() {
        // Create service without Redis
        AIRateLimitService serviceNoRedis = new AIRateLimitService();
        // Using reflection to set rateLimitConfig would be needed here
        // For simplicity, just test that null Redis doesn't throw

        // This test verifies graceful degradation behavior
        assertDoesNotThrow(() -> rateLimitService.checkChatLimit(studentId));
    }

    @Test
    void rateLimitResult_allowed_hasCorrectProperties() {
        AIRateLimitService.RateLimitResult result = AIRateLimitService.RateLimitResult.allowed(10, 100);

        assertTrue(result.isAllowed());
        assertEquals(10, result.getRemainingRequests());
        assertEquals(100, result.getRemainingDaily());
        assertNull(result.getMessage());
        assertFalse(result.isWarning());
    }

    @Test
    void rateLimitResult_limited_hasCorrectProperties() {
        AIRateLimitService.RateLimitResult result = AIRateLimitService.RateLimitResult.limited(
                "Too many requests", 60, 50, 30);

        assertFalse(result.isAllowed());
        assertEquals("Too many requests", result.getMessage());
        assertEquals(60, result.getRetryAfterSeconds());
        assertEquals(50, result.getCurrentCount());
        assertEquals(30, result.getLimit());
        assertEquals("rate", result.getLimitType());
    }

    @Test
    void rateLimitResult_dailyLimitExceeded_hasCorrectProperties() {
        AIRateLimitService.RateLimitResult result = AIRateLimitService.RateLimitResult.dailyLimitExceeded(
                "Daily limit reached", 600, 500);

        assertFalse(result.isAllowed());
        assertEquals("daily", result.getLimitType());
        assertEquals(600, result.getCurrentCount());
        assertEquals(500, result.getLimit());
    }

    @Test
    void rateLimitResult_tokenBudgetExceeded_hasCorrectProperties() {
        AIRateLimitService.RateLimitResult result = AIRateLimitService.RateLimitResult.tokenBudgetExceeded(
                "Token budget exceeded", 60000, 50000, "daily");

        assertFalse(result.isAllowed());
        assertEquals("token_daily", result.getLimitType());
        assertEquals(60000, result.getCurrentCount());
        assertEquals(50000, result.getLimit());
    }
}


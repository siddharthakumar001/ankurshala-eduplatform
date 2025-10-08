package com.ankurshala.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    void testIsAllowed_WhenFirstRequest_ShouldReturnTrue() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        Duration window = Duration.ofMinutes(1);
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        boolean result = rateLimitingService.isAllowed(key, maxRequests, window);

        // Assert
        assertTrue(result);
        verify(valueOperations).set(anyString(), eq("1"), eq(window));
    }

    @Test
    void testIsAllowed_WhenWithinLimit_ShouldReturnTrue() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        Duration window = Duration.ofMinutes(1);
        when(valueOperations.get(anyString())).thenReturn("50");

        // Act
        boolean result = rateLimitingService.isAllowed(key, maxRequests, window);

        // Assert
        assertTrue(result);
        verify(valueOperations).increment(anyString());
    }

    @Test
    void testIsAllowed_WhenExceedsLimit_ShouldReturnFalse() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        Duration window = Duration.ofMinutes(1);
        when(valueOperations.get(anyString())).thenReturn("100");

        // Act
        boolean result = rateLimitingService.isAllowed(key, maxRequests, window);

        // Assert
        assertFalse(result);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    void testIsAllowed_WhenRedisError_ShouldReturnTrue() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        Duration window = Duration.ofMinutes(1);
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection error"));

        // Act
        boolean result = rateLimitingService.isAllowed(key, maxRequests, window);

        // Assert
        assertTrue(result); // Fail open for availability
    }

    @Test
    void testIsAuthRequestAllowed_WhenWithinLimit_ShouldReturnTrue() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("3");

        // Act
        boolean result = rateLimitingService.isAuthRequestAllowed(request);

        // Assert
        assertTrue(result);
        verify(valueOperations).increment(anyString());
    }

    @Test
    void testIsAuthRequestAllowed_WhenExceedsLimit_ShouldReturnFalse() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("5");

        // Act
        boolean result = rateLimitingService.isAuthRequestAllowed(request);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsSignupRequestAllowed_WhenWithinLimit_ShouldReturnTrue() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("2");

        // Act
        boolean result = rateLimitingService.isSignupRequestAllowed(request);

        // Assert
        assertTrue(result);
        verify(valueOperations).increment(anyString());
    }

    @Test
    void testIsSignupRequestAllowed_WhenExceedsLimit_ShouldReturnFalse() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("3");

        // Act
        boolean result = rateLimitingService.isSignupRequestAllowed(request);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsGeneralRequestAllowed_WhenWithinLimit_ShouldReturnTrue() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("50");

        // Act
        boolean result = rateLimitingService.isGeneralRequestAllowed(request);

        // Assert
        assertTrue(result);
        verify(valueOperations).increment(anyString());
    }

    @Test
    void testIsGeneralRequestAllowed_WhenExceedsLimit_ShouldReturnFalse() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("100");

        // Act
        boolean result = rateLimitingService.isGeneralRequestAllowed(request);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetRemainingRequests_WhenWithinLimit_ShouldReturnCorrectCount() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        when(valueOperations.get(anyString())).thenReturn("25");

        // Act
        int remaining = rateLimitingService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(75, remaining); // 100 - 25 = 75
    }

    @Test
    void testGetRemainingRequests_WhenExceedsLimit_ShouldReturnZero() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        when(valueOperations.get(anyString())).thenReturn("150");

        // Act
        int remaining = rateLimitingService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(0, remaining);
    }

    @Test
    void testGetRemainingRequests_WhenFirstRequest_ShouldReturnFullLimit() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        int remaining = rateLimitingService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(100, remaining);
    }

    @Test
    void testGetRemainingRequests_WhenRedisError_ShouldReturnFullLimit() {
        // Arrange
        String key = "test_key";
        int maxRequests = 100;
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        // Act
        int remaining = rateLimitingService.getRemainingRequests(key, maxRequests);

        // Assert
        assertEquals(100, remaining);
    }

    @Test
    void testGetTTL_ShouldReturnCorrectTTL() {
        // Arrange
        String key = "test_key";
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(30L);

        // Act
        long ttl = rateLimitingService.getTTL(key);

        // Assert
        assertEquals(30L, ttl);
    }

    @Test
    void testGetTTL_WhenRedisError_ShouldReturnMinusOne() {
        // Arrange
        String key = "test_key";
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenThrow(new RuntimeException("Redis error"));

        // Act
        long ttl = rateLimitingService.getTTL(key);

        // Assert
        assertEquals(-1L, ttl);
    }

    @Test
    void testGetClientIp_WithXForwardedFor_ShouldReturnFirstIp() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");

        // Act
        rateLimitingService.isAuthRequestAllowed(request);

        // This test verifies that the IP extraction works correctly
        // by checking that the method executes without error
        verify(valueOperations, atLeastOnce()).get(contains("10.0.0.1"));
    }

    @Test
    void testGetClientIp_WithXRealIp_ShouldReturnRealIp() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("172.16.0.1");

        // Act
        rateLimitingService.isAuthRequestAllowed(request);

        // This test verifies that the IP extraction works correctly
        verify(valueOperations, atLeastOnce()).get(contains("172.16.0.1"));
    }

    @Test
    void testGetClientIp_WithRemoteAddr_ShouldReturnRemoteAddr() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        // Act
        rateLimitingService.isAuthRequestAllowed(request);

        // This test verifies that the IP extraction works correctly
        verify(valueOperations, atLeastOnce()).get(contains("192.168.1.100"));
    }
}

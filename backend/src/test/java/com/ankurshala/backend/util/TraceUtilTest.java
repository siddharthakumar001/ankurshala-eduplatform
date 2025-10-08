package com.ankurshala.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TraceUtil Tests")
class TraceUtilTest {

    @BeforeEach
    void setUp() {
        // Clear MDC before each test
        TraceUtil.clear();
    }

    @AfterEach
    void tearDown() {
        // Clear MDC after each test
        TraceUtil.clear();
    }

    @Test
    @DisplayName("Should generate valid trace ID")
    void testGenerateTraceId() {
        String traceId = TraceUtil.generateTraceId();
        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
        assertTrue(traceId.contains("-"));
        assertTrue(traceId.length() > 10);
    }

    @Test
    @DisplayName("Should generate valid request ID")
    void testGenerateRequestId() {
        String requestId = TraceUtil.generateRequestId();
        assertNotNull(requestId);
        assertFalse(requestId.isEmpty());
        assertTrue(requestId.length() > 0);
    }

    @Test
    @DisplayName("Should set and get trace ID")
    void testSetAndGetTraceId() {
        String traceId = "test-trace-id";
        TraceUtil.setTraceId(traceId);
        assertEquals(traceId, TraceUtil.getTraceId());
    }

    @Test
    @DisplayName("Should set and get request ID")
    void testSetAndGetRequestId() {
        String requestId = "test-request-id";
        TraceUtil.setRequestId(requestId);
        assertEquals(requestId, TraceUtil.getRequestId());
    }

    @Test
    @DisplayName("Should set and get user ID")
    void testSetAndGetUserId() {
        String userId = "test-user-id";
        TraceUtil.setUserId(userId);
        assertEquals(userId, TraceUtil.getUserId());
    }

    @Test
    @DisplayName("Should set and get request path")
    void testSetAndGetRequestPath() {
        String requestPath = "/api/test";
        TraceUtil.setRequestPath(requestPath);
        assertEquals(requestPath, TraceUtil.getRequestPath());
    }

    @Test
    @DisplayName("Should set and get request method")
    void testSetAndGetRequestMethod() {
        String requestMethod = "GET";
        TraceUtil.setRequestMethod(requestMethod);
        assertEquals(requestMethod, TraceUtil.getRequestMethod());
    }

    @Test
    @DisplayName("Should set and get start time")
    void testSetAndGetStartTime() {
        long startTime = System.currentTimeMillis();
        TraceUtil.setStartTime(startTime);
        assertEquals(startTime, TraceUtil.getStartTime());
    }

    @Test
    @DisplayName("Should set and get session ID")
    void testSetAndGetSessionId() {
        String sessionId = "test-session-id";
        TraceUtil.setSessionId(sessionId);
        assertEquals(sessionId, TraceUtil.getSessionId());
    }

    @Test
    @DisplayName("Should set and get client IP")
    void testSetAndGetClientIp() {
        String clientIp = "127.0.0.1";
        TraceUtil.setClientIp(clientIp);
        assertEquals(clientIp, TraceUtil.getClientIp());
    }

    @Test
    @DisplayName("Should set and get user agent")
    void testSetAndGetUserAgent() {
        String userAgent = "Test-Agent";
        TraceUtil.setUserAgent(userAgent);
        assertEquals(userAgent, TraceUtil.getUserAgent());
    }

    @Test
    @DisplayName("Should clear all MDC values")
    void testClear() {
        TraceUtil.setTraceId("test-trace");
        TraceUtil.setRequestId("test-request");
        TraceUtil.setUserId("test-user");
        
        TraceUtil.clear();
        
        assertNull(TraceUtil.getTraceId());
        assertNull(TraceUtil.getRequestId());
        assertNull(TraceUtil.getUserId());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        TraceUtil.setTraceId(null);
        TraceUtil.setRequestId(null);
        TraceUtil.setUserId(null);
        
        assertNull(TraceUtil.getTraceId());
        assertNull(TraceUtil.getRequestId());
        assertNull(TraceUtil.getUserId());
    }

    @Test
    @DisplayName("Should generate unique trace IDs")
    void testUniqueTraceIds() {
        String traceId1 = TraceUtil.generateTraceId();
        String traceId2 = TraceUtil.generateTraceId();
        
        assertNotEquals(traceId1, traceId2);
    }

    @Test
    @DisplayName("Should generate unique request IDs")
    void testUniqueRequestIds() {
        String requestId1 = TraceUtil.generateRequestId();
        String requestId2 = TraceUtil.generateRequestId();
        
        assertNotEquals(requestId1, requestId2);
    }

    @Test
    @DisplayName("Should calculate execution time")
    void testGetExecutionTime() {
        long startTime = System.currentTimeMillis();
        TraceUtil.setStartTime(startTime);
        
        // Wait a bit
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long executionTime = TraceUtil.getExecutionTime();
        assertTrue(executionTime > 0);
    }

    @Test
    @DisplayName("Should get formatted timestamp")
    void testGetFormattedTimestamp() {
        String timestamp = TraceUtil.getFormattedTimestamp();
        assertNotNull(timestamp);
        assertFalse(timestamp.isEmpty());
        assertTrue(timestamp.contains("-"));
    }

    @Test
    @DisplayName("Should initialize trace context")
    void testInitializeTraceContext() {
        String path = "/api/test";
        String method = "GET";
        String clientIp = "127.0.0.1";
        String userAgent = "Test-Agent";
        
        TraceUtil.initializeTraceContext(path, method, clientIp, userAgent);
        
        assertNotNull(TraceUtil.getTraceId());
        assertNotNull(TraceUtil.getRequestId());
        assertEquals(path, TraceUtil.getRequestPath());
        assertEquals(method, TraceUtil.getRequestMethod());
        assertEquals(clientIp, TraceUtil.getClientIp());
        assertEquals(userAgent, TraceUtil.getUserAgent());
        assertTrue(TraceUtil.getStartTime() > 0);
    }
}
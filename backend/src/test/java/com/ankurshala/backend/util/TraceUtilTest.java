package com.ankurshala.backend.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit tests for TraceUtil
 * Tests all trace ID and context management functionality
 */
class TraceUtilTest {

    @BeforeEach
    void setUp() {
        // Clear MDC before each test
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        // Clear MDC after each test
        MDC.clear();
    }

    @Test
    void testGenerateTraceId() {
        // When
        String traceId1 = TraceUtil.generateTraceId();
        String traceId2 = TraceUtil.generateTraceId();

        // Then
        assertNotNull(traceId1);
        assertNotNull(traceId2);
        assertNotEquals(traceId1, traceId2);
        assertTrue(traceId1.length() > 0);
        assertTrue(traceId2.length() > 0);
        
        // Trace ID should contain timestamp and UUID parts
        assertTrue(traceId1.contains("-"));
        assertTrue(traceId2.contains("-"));
    }

    @Test
    void testGenerateRequestId() {
        // When
        String requestId1 = TraceUtil.generateRequestId();
        String requestId2 = TraceUtil.generateRequestId();

        // Then
        assertNotNull(requestId1);
        assertNotNull(requestId2);
        assertNotEquals(requestId1, requestId2);
        
        // Request ID should be a valid UUID
        assertDoesNotThrow(() -> UUID.fromString(requestId1));
        assertDoesNotThrow(() -> UUID.fromString(requestId2));
    }

    @Test
    void testSetAndGetTraceId() {
        // Given
        String traceId = "test-trace-id-123";

        // When
        TraceUtil.setTraceId(traceId);
        String retrievedTraceId = TraceUtil.getTraceId();

        // Then
        assertEquals(traceId, retrievedTraceId);
    }

    @Test
    void testSetAndGetRequestId() {
        // Given
        String requestId = "test-request-id-456";

        // When
        TraceUtil.setRequestId(requestId);
        String retrievedRequestId = TraceUtil.getRequestId();

        // Then
        assertEquals(requestId, retrievedRequestId);
    }

    @Test
    void testSetAndGetUserId() {
        // Given
        String userId = "user-123";

        // When
        TraceUtil.setUserId(userId);
        String retrievedUserId = TraceUtil.getUserId();

        // Then
        assertEquals(userId, retrievedUserId);
    }

    @Test
    void testSetAndGetRequestPath() {
        // Given
        String path = "/api/test/endpoint";

        // When
        TraceUtil.setRequestPath(path);
        String retrievedPath = TraceUtil.getRequestPath();

        // Then
        assertEquals(path, retrievedPath);
    }

    @Test
    void testSetAndGetRequestMethod() {
        // Given
        String method = "POST";

        // When
        TraceUtil.setRequestMethod(method);
        String retrievedMethod = TraceUtil.getRequestMethod();

        // Then
        assertEquals(method, retrievedMethod);
    }

    @Test
    void testSetAndGetStartTime() {
        // Given
        long startTime = System.currentTimeMillis();

        // When
        TraceUtil.setStartTime(startTime);
        long retrievedStartTime = TraceUtil.getStartTime();

        // Then
        assertEquals(startTime, retrievedStartTime);
    }

    @Test
    void testGetExecutionTime() {
        // Given
        long startTime = System.currentTimeMillis() - 1000; // 1 second ago
        TraceUtil.setStartTime(startTime);

        // When
        long executionTime = TraceUtil.getExecutionTime();

        // Then
        assertTrue(executionTime >= 1000);
        assertTrue(executionTime < 2000); // Should be close to 1000ms
    }

    @Test
    void testGetExecutionTime_NoStartTime() {
        // When
        long executionTime = TraceUtil.getExecutionTime();

        // Then
        assertEquals(0, executionTime);
    }

    @Test
    void testSetAndGetSessionId() {
        // Given
        String sessionId = "session-abc-123";

        // When
        TraceUtil.setSessionId(sessionId);
        String retrievedSessionId = TraceUtil.getSessionId();

        // Then
        assertEquals(sessionId, retrievedSessionId);
    }

    @Test
    void testSetAndGetClientIp() {
        // Given
        String clientIp = "192.168.1.100";

        // When
        TraceUtil.setClientIp(clientIp);
        String retrievedClientIp = TraceUtil.getClientIp();

        // Then
        assertEquals(clientIp, retrievedClientIp);
    }

    @Test
    void testSetAndGetUserAgent() {
        // Given
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

        // When
        TraceUtil.setUserAgent(userAgent);
        String retrievedUserAgent = TraceUtil.getUserAgent();

        // Then
        assertEquals(userAgent, retrievedUserAgent);
    }

    @Test
    void testClearTraceId() {
        // Given
        TraceUtil.setTraceId("test-trace-id");
        assertNotNull(TraceUtil.getTraceId());

        // When
        TraceUtil.clearTraceId();

        // Then
        assertNull(TraceUtil.getTraceId());
    }

    @Test
    void testClearRequestId() {
        // Given
        TraceUtil.setRequestId("test-request-id");
        assertNotNull(TraceUtil.getRequestId());

        // When
        TraceUtil.clearRequestId();

        // Then
        assertNull(TraceUtil.getRequestId());
    }

    @Test
    void testClear() {
        // Given
        TraceUtil.setTraceId("test-trace-id");
        TraceUtil.setRequestId("test-request-id");
        TraceUtil.setUserId("test-user-id");
        TraceUtil.setRequestPath("/api/test");
        TraceUtil.setRequestMethod("GET");
        TraceUtil.setStartTime(System.currentTimeMillis());
        TraceUtil.setSessionId("test-session-id");
        TraceUtil.setClientIp("127.0.0.1");
        TraceUtil.setUserAgent("Test-Agent");

        // Verify all values are set
        assertNotNull(TraceUtil.getTraceId());
        assertNotNull(TraceUtil.getRequestId());
        assertNotNull(TraceUtil.getUserId());
        assertNotNull(TraceUtil.getRequestPath());
        assertNotNull(TraceUtil.getRequestMethod());
        assertTrue(TraceUtil.getStartTime() > 0);
        assertNotNull(TraceUtil.getSessionId());
        assertNotNull(TraceUtil.getClientIp());
        assertNotNull(TraceUtil.getUserAgent());

        // When
        TraceUtil.clear();

        // Then
        assertNull(TraceUtil.getTraceId());
        assertNull(TraceUtil.getRequestId());
        assertNull(TraceUtil.getUserId());
        assertNull(TraceUtil.getRequestPath());
        assertNull(TraceUtil.getRequestMethod());
        assertEquals(0, TraceUtil.getStartTime());
        assertNull(TraceUtil.getSessionId());
        assertNull(TraceUtil.getClientIp());
        assertNull(TraceUtil.getUserAgent());
    }

    @Test
    void testClearSpecificKey() {
        // Given
        TraceUtil.setTraceId("test-trace-id");
        TraceUtil.setRequestId("test-request-id");
        TraceUtil.setUserId("test-user-id");

        // Verify all values are set
        assertNotNull(TraceUtil.getTraceId());
        assertNotNull(TraceUtil.getRequestId());
        assertNotNull(TraceUtil.getUserId());

        // When
        TraceUtil.clear("traceId");

        // Then
        assertNull(TraceUtil.getTraceId());
        assertNotNull(TraceUtil.getRequestId()); // Should still be there
        assertNotNull(TraceUtil.getUserId()); // Should still be there
    }

    @Test
    void testGetFormattedTimestamp() {
        // When
        String timestamp = TraceUtil.getFormattedTimestamp();

        // Then
        assertNotNull(timestamp);
        assertTrue(timestamp.length() > 0);
        
        // Should contain date and time components
        assertTrue(timestamp.contains("-"));
        assertTrue(timestamp.contains(":"));
        assertTrue(timestamp.contains("."));
    }

    @Test
    void testInitializeTraceContext() {
        // Given
        String path = "/api/test/endpoint";
        String method = "POST";
        String clientIp = "192.168.1.100";
        String userAgent = "Test-Agent/1.0";

        // When
        TraceUtil.initializeTraceContext(path, method, clientIp, userAgent);

        // Then
        assertNotNull(TraceUtil.getTraceId());
        assertNotNull(TraceUtil.getRequestId());
        assertEquals(path, TraceUtil.getRequestPath());
        assertEquals(method, TraceUtil.getRequestMethod());
        assertEquals(clientIp, TraceUtil.getClientIp());
        assertEquals(userAgent, TraceUtil.getUserAgent());
        assertTrue(TraceUtil.getStartTime() > 0);
    }

    @Test
    void testInitializeTraceContext_NullValues() {
        // When
        TraceUtil.initializeTraceContext(null, null, null, null);

        // Then
        assertNotNull(TraceUtil.getTraceId());
        assertNotNull(TraceUtil.getRequestId());
        assertNull(TraceUtil.getRequestPath());
        assertNull(TraceUtil.getRequestMethod());
        assertNull(TraceUtil.getClientIp());
        assertNull(TraceUtil.getUserAgent());
        assertTrue(TraceUtil.getStartTime() > 0);
    }

    @Test
    void testConcurrentTraceIdGeneration() throws InterruptedException {
        // Given
        int threadCount = 10;
        String[] traceIds = new String[threadCount];
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                traceIds[index] = TraceUtil.generateTraceId();
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(traceIds[i]);
            for (int j = i + 1; j < threadCount; j++) {
                assertNotEquals(traceIds[i], traceIds[j], 
                    "Trace IDs should be unique across threads");
            }
        }
    }

    @Test
    void testConcurrentRequestIdGeneration() throws InterruptedException {
        // Given
        int threadCount = 10;
        String[] requestIds = new String[threadCount];
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                requestIds[index] = TraceUtil.generateRequestId();
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        for (int i = 0; i < threadCount; i++) {
            assertNotNull(requestIds[i]);
            for (int j = i + 1; j < threadCount; j++) {
                assertNotEquals(requestIds[i], requestIds[j], 
                    "Request IDs should be unique across threads");
            }
        }
    }

    @Test
    void testConcurrentContextManagement() throws InterruptedException {
        // Given
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                // Each thread sets its own context
                TraceUtil.setTraceId("trace-" + threadId);
                TraceUtil.setRequestId("request-" + threadId);
                TraceUtil.setUserId("user-" + threadId);
                
                // Verify the context is set correctly
                assertEquals("trace-" + threadId, TraceUtil.getTraceId());
                assertEquals("request-" + threadId, TraceUtil.getRequestId());
                assertEquals("user-" + threadId, TraceUtil.getUserId());
                
                // Simulate some work
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Verify context is still correct
                assertEquals("trace-" + threadId, TraceUtil.getTraceId());
                assertEquals("request-" + threadId, TraceUtil.getRequestId());
                assertEquals("user-" + threadId, TraceUtil.getUserId());
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        // All threads should have completed without exceptions
        // The context should be isolated per thread
    }

    @Test
    void testTraceIdFormat() {
        // When
        String traceId = TraceUtil.generateTraceId();

        // Then
        assertNotNull(traceId);
        
        // Should contain timestamp and UUID parts separated by dash
        String[] parts = traceId.split("-");
        assertEquals(2, parts.length);
        
        // First part should be timestamp (14 digits: YYYYMMDDHHMMSS)
        assertEquals(14, parts[0].length());
        assertTrue(parts[0].matches("\\d{14}"));
        
        // Second part should be UUID fragment (8 characters)
        assertEquals(8, parts[1].length());
    }

    @Test
    void testRequestIdFormat() {
        // When
        String requestId = TraceUtil.generateRequestId();

        // Then
        assertNotNull(requestId);
        
        // Should be a valid UUID
        assertDoesNotThrow(() -> UUID.fromString(requestId));
        
        // Should be 36 characters (standard UUID format)
        assertEquals(36, requestId.length());
    }

    @Test
    void testExecutionTimeAccuracy() {
        // Given
        long startTime = System.currentTimeMillis();
        TraceUtil.setStartTime(startTime);

        // When
        try {
            Thread.sleep(100); // Sleep for 100ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long executionTime = TraceUtil.getExecutionTime();

        // Then
        assertTrue(executionTime >= 100);
        assertTrue(executionTime < 200); // Allow some tolerance
    }

    @Test
    void testNullHandling() {
        // When setting null values
        TraceUtil.setTraceId(null);
        TraceUtil.setRequestId(null);
        TraceUtil.setUserId(null);
        TraceUtil.setRequestPath(null);
        TraceUtil.setRequestMethod(null);
        TraceUtil.setSessionId(null);
        TraceUtil.setClientIp(null);
        TraceUtil.setUserAgent(null);

        // Then
        assertNull(TraceUtil.getTraceId());
        assertNull(TraceUtil.getRequestId());
        assertNull(TraceUtil.getUserId());
        assertNull(TraceUtil.getRequestPath());
        assertNull(TraceUtil.getRequestMethod());
        assertNull(TraceUtil.getSessionId());
        assertNull(TraceUtil.getClientIp());
        assertNull(TraceUtil.getUserAgent());
    }

    @Test
    void testEmptyStringHandling() {
        // When setting empty strings
        TraceUtil.setTraceId("");
        TraceUtil.setRequestId("");
        TraceUtil.setUserId("");
        TraceUtil.setRequestPath("");
        TraceUtil.setRequestMethod("");
        TraceUtil.setSessionId("");
        TraceUtil.setClientIp("");
        TraceUtil.setUserAgent("");

        // Then
        assertEquals("", TraceUtil.getTraceId());
        assertEquals("", TraceUtil.getRequestId());
        assertEquals("", TraceUtil.getUserId());
        assertEquals("", TraceUtil.getRequestPath());
        assertEquals("", TraceUtil.getRequestMethod());
        assertEquals("", TraceUtil.getSessionId());
        assertEquals("", TraceUtil.getClientIp());
        assertEquals("", TraceUtil.getUserAgent());
    }

    @Test
    void testSpecialCharacterHandling() {
        // Given
        String specialTraceId = "trace-id-with-special-chars!@#$%^&*()";
        String specialRequestId = "request-id-with-unicode-测试";
        String specialUserId = "user-id-with-emoji-🚀";

        // When
        TraceUtil.setTraceId(specialTraceId);
        TraceUtil.setRequestId(specialRequestId);
        TraceUtil.setUserId(specialUserId);

        // Then
        assertEquals(specialTraceId, TraceUtil.getTraceId());
        assertEquals(specialRequestId, TraceUtil.getRequestId());
        assertEquals(specialUserId, TraceUtil.getUserId());
    }
}

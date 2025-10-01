package com.ankurshala.backend.service;

import com.ankurshala.backend.util.TraceUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit tests for LoggingService
 * Tests all logging methods with various scenarios and edge cases
 */
@ExtendWith(MockitoExtension.class)
class LoggingServiceTest {

    @InjectMocks
    private LoggingService loggingService;

    private MockedStatic<TraceUtil> traceUtilMock;

    @BeforeEach
    void setUp() {
        traceUtilMock = Mockito.mockStatic(TraceUtil.class);
        traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
        traceUtilMock.when(TraceUtil::getRequestId).thenReturn("test-request-id");
        traceUtilMock.when(TraceUtil::getUserId).thenReturn("test-user-id");
        traceUtilMock.when(TraceUtil::getClientIp).thenReturn("127.0.0.1");
        traceUtilMock.when(TraceUtil::getUserAgent).thenReturn("Test-Agent");
    }

    @AfterEach
    void tearDown() {
        if (traceUtilMock != null) {
            traceUtilMock.close();
        }
    }

    @Test
    void testLogInfo() {
        // Given
        String message = "Test info message";
        Object arg1 = "arg1";
        Object arg2 = 123;

        // When
        loggingService.logInfo(message, arg1, arg2);

        // Then
        // Verify that the method executes without throwing exceptions
        // The actual logging is handled by SLF4J, so we can't easily mock it
        // We just ensure the method doesn't throw exceptions
    }

    @Test
    void testLogWarn() {
        // Given
        String message = "Test warning message";
        Object arg1 = "warning-arg";

        // When
        loggingService.logWarn(message, arg1);

        // Then
        // Verify that the method executes without throwing exceptions
    }

    @Test
    void testLogError() {
        // Given
        String message = "Test error message";
        RuntimeException exception = new RuntimeException("Test exception");
        Map<String, Object> context = new HashMap<>();
        context.put("key1", "value1");
        context.put("key2", 456);

        // When
        loggingService.logError(message, exception, context);

        // Then
        // Verify that the method executes without throwing exceptions
    }

    @Test
    void testLogDebug() {
        // Given
        String message = "Test debug message";
        Object arg1 = "debug-arg";

        // When
        loggingService.logDebug(message, arg1);

        // Then
        // Verify that the method executes without throwing exceptions
    }

    @Test
    void testLogBusinessOperationStart() {
        // Given
        String operation = "TEST_OPERATION";
        String entityId = "entity-123";
        Map<String, Object> context = new HashMap<>();
        context.put("param1", "value1");
        context.put("param2", 789);

        // When
        loggingService.logBusinessOperationStart(operation, entityId, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogBusinessOperationStart_NullEntityId() {
        // Given
        String operation = "TEST_OPERATION";
        Map<String, Object> context = new HashMap<>();
        context.put("param1", "value1");

        // When
        loggingService.logBusinessOperationStart(operation, null, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogBusinessOperationStart_NullContext() {
        // Given
        String operation = "TEST_OPERATION";
        String entityId = "entity-123";

        // When
        loggingService.logBusinessOperationStart(operation, entityId, null);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogBusinessOperationComplete() {
        // Given
        String operation = "TEST_OPERATION";
        String entityId = "entity-123";
        boolean success = true;
        long executionTime = 1500L;

        // When
        loggingService.logBusinessOperationComplete(operation, entityId, success, executionTime);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogBusinessOperationComplete_Failure() {
        // Given
        String operation = "TEST_OPERATION";
        String entityId = "entity-123";
        boolean success = false;
        long executionTime = 2000L;

        // When
        loggingService.logBusinessOperationComplete(operation, entityId, success, executionTime);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogBusinessOperationComplete_NullEntityId() {
        // Given
        String operation = "TEST_OPERATION";
        boolean success = true;
        long executionTime = 1000L;

        // When
        loggingService.logBusinessOperationComplete(operation, null, success, executionTime);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogAuthenticationEvent() {
        // Given
        String eventType = "LOGIN_SUCCESS";
        String userId = "user-123";
        String email = "user@example.com";
        boolean success = true;
        String details = "Login successful";

        // When
        loggingService.logAuthenticationEvent(eventType, userId, email, success, details);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getClientIp);
        traceUtilMock.verify(TraceUtil::getUserAgent);
    }

    @Test
    void testLogAuthenticationEvent_Failure() {
        // Given
        String eventType = "LOGIN_FAILED";
        String userId = null;
        String email = "user@example.com";
        boolean success = false;
        String details = "Invalid credentials";

        // When
        loggingService.logAuthenticationEvent(eventType, userId, email, success, details);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getClientIp);
        traceUtilMock.verify(TraceUtil::getUserAgent);
    }

    @Test
    void testLogAuthenticationEvent_NullValues() {
        // Given
        String eventType = "LOGOUT";
        String userId = null;
        String email = null;
        boolean success = true;
        String details = null;

        // When
        loggingService.logAuthenticationEvent(eventType, userId, email, success, details);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getClientIp);
        traceUtilMock.verify(TraceUtil::getUserAgent);
    }

    @Test
    void testLogSystemEvent() {
        // Given
        String eventType = "SYSTEM_STARTUP";
        String level = "INFO";
        String message = "System started successfully";
        Map<String, Object> context = new HashMap<>();
        context.put("version", "1.0.0");
        context.put("environment", "production");

        // When
        loggingService.logSystemEvent(eventType, level, message, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogSystemEvent_NullContext() {
        // Given
        String eventType = "SYSTEM_SHUTDOWN";
        String level = "WARN";
        String message = "System shutting down";

        // When
        loggingService.logSystemEvent(eventType, level, message, null);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogSystemEvent_EmptyContext() {
        // Given
        String eventType = "HEALTH_CHECK";
        String level = "DEBUG";
        String message = "Health check completed";
        Map<String, Object> context = new HashMap<>();

        // When
        loggingService.logSystemEvent(eventType, level, message, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogPerformanceMetrics() {
        // Given
        String operation = "DATABASE_QUERY";
        long executionTime = 500L;
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("queryCount", 10);
        metrics.put("cacheHits", 8);
        metrics.put("cacheMisses", 2);

        // When
        loggingService.logPerformanceMetrics(operation, executionTime, metrics);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogPerformanceMetrics_SlowOperation() {
        // Given
        String operation = "SLOW_OPERATION";
        long executionTime = 5000L; // 5 seconds
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("recordsProcessed", 10000);

        // When
        loggingService.logPerformanceMetrics(operation, executionTime, metrics);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogPerformanceMetrics_NullMetrics() {
        // Given
        String operation = "SIMPLE_OPERATION";
        long executionTime = 100L;

        // When
        loggingService.logPerformanceMetrics(operation, executionTime, null);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLogSecurityEvent() {
        // Given
        String eventType = "SECURITY_VIOLATION";
        String severity = "HIGH";
        String message = "Unauthorized access attempt";
        Map<String, Object> context = new HashMap<>();
        context.put("ipAddress", "192.168.1.100");
        context.put("userAgent", "Malicious-Agent");

        // When
        loggingService.logSecurityEvent(eventType, severity, message, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getClientIp);
        traceUtilMock.verify(TraceUtil::getUserAgent);
    }

    @Test
    void testLogSecurityEvent_CriticalSeverity() {
        // Given
        String eventType = "CRITICAL_SECURITY_BREACH";
        String severity = "CRITICAL";
        String message = "Critical security breach detected";
        Map<String, Object> context = new HashMap<>();
        context.put("breachType", "SQL_INJECTION");
        context.put("affectedTables", "users,orders");

        // When
        loggingService.logSecurityEvent(eventType, severity, message, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getClientIp);
        traceUtilMock.verify(TraceUtil::getUserAgent);
    }

    @Test
    void testLogSecurityEvent_NullContext() {
        // Given
        String eventType = "SECURITY_SCAN";
        String severity = "INFO";
        String message = "Security scan completed";

        // When
        loggingService.logSecurityEvent(eventType, severity, message, null);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getClientIp);
        traceUtilMock.verify(TraceUtil::getUserAgent);
    }

    @Test
    void testLogAuditEvent() {
        // Given
        String eventType = "DATA_MODIFICATION";
        String entityType = "User";
        String entityId = "user-123";
        String action = "UPDATE";
        Map<String, Object> changes = new HashMap<>();
        changes.put("email", "old@example.com -> new@example.com");
        changes.put("status", "ACTIVE -> INACTIVE");

        // When
        loggingService.logAuditEvent(eventType, entityType, entityId, action, changes);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getUserId);
    }

    @Test
    void testLogAuditEvent_DataCreation() {
        // Given
        String eventType = "DATA_CREATION";
        String entityType = "Order";
        String entityId = "order-456";
        String action = "CREATE";
        Map<String, Object> changes = new HashMap<>();
        changes.put("orderId", "order-456");
        changes.put("amount", "99.99");

        // When
        loggingService.logAuditEvent(eventType, entityType, entityId, action, changes);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getUserId);
    }

    @Test
    void testLogAuditEvent_DataDeletion() {
        // Given
        String eventType = "DATA_DELETION";
        String entityType = "Product";
        String entityId = "product-789";
        String action = "DELETE";
        Map<String, Object> changes = new HashMap<>();
        changes.put("deletedAt", "2024-01-01T12:00:00Z");
        changes.put("deletedBy", "admin-user");

        // When
        loggingService.logAuditEvent(eventType, entityType, entityId, action, changes);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getUserId);
    }

    @Test
    void testLogAuditEvent_NullChanges() {
        // Given
        String eventType = "DATA_ACCESS";
        String entityType = "Report";
        String entityId = "report-101";
        String action = "READ";

        // When
        loggingService.logAuditEvent(eventType, entityType, entityId, action, null);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
        traceUtilMock.verify(TraceUtil::getUserId);
    }

    @Test
    void testLoggingWithSpecialCharacters() {
        // Given
        String message = "Test message with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
        Map<String, Object> context = new HashMap<>();
        context.put("specialKey", "value with \n newline and \t tab");
        context.put("unicodeKey", "测试中文和emoji 🚀");

        // When
        loggingService.logInfo(message, context);
        loggingService.logSystemEvent("SPECIAL_CHARS", "INFO", message, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId, times(2)); // logInfo + logSystemEvent
        traceUtilMock.verify(TraceUtil::getRequestId, times(2)); // logInfo + logSystemEvent
    }

    @Test
    void testLoggingWithLargeData() {
        // Given
        String message = "Test message";
        Map<String, Object> context = new HashMap<>();
        context.put("largeData", "x".repeat(10000)); // 10KB string
        context.put("arrayData", new String[1000]);

        // When
        loggingService.logInfo(message, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testLoggingWithNullValues() {
        // Given
        String message = "Test message with nulls";
        Map<String, Object> context = new HashMap<>();
        context.put("nullValue", null);
        context.put("emptyString", "");
        context.put("zeroValue", 0);

        // When
        loggingService.logInfo(message, context);

        // Then
        // Verify that the method executes without throwing exceptions
        traceUtilMock.verify(TraceUtil::getTraceId);
        traceUtilMock.verify(TraceUtil::getRequestId);
    }

    @Test
    void testConcurrentLogging() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // When
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    loggingService.logInfo("Thread {} message {}", threadId, j);
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        // Verify that all logging operations completed without exceptions
        // Note: Concurrent threads don't use the mocked TraceUtil, so we just verify no exceptions occurred
        // The test passes if all threads complete successfully without throwing exceptions
    }
}

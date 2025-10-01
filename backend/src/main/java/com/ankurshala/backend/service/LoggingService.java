package com.ankurshala.backend.service;

import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Comprehensive logging service for application-wide logging
 * Provides structured logging with performance metrics and error tracking
 */
@Slf4j
@Service
public class LoggingService {
    
    /**
     * Basic logging methods for compatibility with tests
     */
    public void logInfo(String message, Object... args) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        // Handle case where message contains format characters but no args are provided
        if (args.length == 0) {
            log.info("[{}] [{}] {}", traceId, requestId, message);
        } else {
            try {
                log.info("[{}] [{}] {}", traceId, requestId, String.format(message, args));
            } catch (Exception e) {
                // If formatting fails, log the message as-is
                log.info("[{}] [{}] {}", traceId, requestId, message);
            }
        }
    }

    public void logWarn(String message, Object... args) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        log.warn("[{}] [{}] {}", traceId, requestId, String.format(message, args));
    }

    public void logError(String message, Throwable t, Object... args) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        log.error("[{}] [{}] {}", traceId, requestId, String.format(message, args), t);
    }

    public void logErrorWithContext(String message, Throwable t, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        log.error("[{}] [{}] {} - Context: {}", traceId, requestId, message, context, t);
    }

    public void logDebug(String message, Object... args) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        log.debug("[{}] [{}] {}", traceId, requestId, String.format(message, args));
    }

    /**
     * Log API request start
     */
    public void logRequestStart(String endpoint, String method, Map<String, Object> parameters) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        log.info("API_REQUEST_START - TraceId: {}, RequestId: {}, Endpoint: {}, Method: {}, Parameters: {}", 
                traceId, requestId, endpoint, method, parameters);
    }
    
    /**
     * Log API request completion
     */
    public void logRequestComplete(String endpoint, String method, int statusCode, long executionTime) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        log.info("API_REQUEST_COMPLETE - TraceId: {}, RequestId: {}, Endpoint: {}, Method: {}, Status: {}, ExecutionTime: {}ms", 
                traceId, requestId, endpoint, method, statusCode, executionTime);
    }
    
    /**
     * Log business operation start
     */
    public void logBusinessOperationStart(String operation, String userId, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        log.info("BUSINESS_OPERATION_START - TraceId: {}, RequestId: {}, Operation: {}, UserId: {}, Context: {}", 
                traceId, requestId, operation, userId, context);
    }
    
    /**
     * Log business operation completion
     */
    public void logBusinessOperationComplete(String operation, String userId, boolean success, long executionTime) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        if (success) {
            log.info("BUSINESS_OPERATION_COMPLETE - TraceId: {}, RequestId: {}, Operation: {}, UserId: {}, Success: true, ExecutionTime: {}ms", 
                    traceId, requestId, operation, userId, executionTime);
        } else {
            log.warn("BUSINESS_OPERATION_COMPLETE - TraceId: {}, RequestId: {}, Operation: {}, UserId: {}, Success: false, ExecutionTime: {}ms", 
                    traceId, requestId, operation, userId, executionTime);
        }
    }
    
    /**
     * Log database operation
     */
    public void logDatabaseOperation(String operation, String table, String query, long executionTime) {
        String traceId = TraceUtil.getTraceId();
        
        log.debug("DATABASE_OPERATION - TraceId: {}, Operation: {}, Table: {}, Query: {}, ExecutionTime: {}ms", 
                traceId, operation, table, query, executionTime);
    }
    
    /**
     * Log authentication event
     */
    public void logAuthenticationEvent(String event, String userId, String email, boolean success, String reason) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        String clientIp = TraceUtil.getClientIp();
        String userAgent = TraceUtil.getUserAgent();
        
        if (success) {
            log.info("AUTH_EVENT - TraceId: {}, RequestId: {}, Event: {}, UserId: {}, Email: {}, Success: true, ClientIp: {}, UserAgent: {}", 
                    traceId, requestId, event, userId, email, clientIp, userAgent);
        } else {
            log.warn("AUTH_EVENT - TraceId: {}, RequestId: {}, Event: {}, UserId: {}, Email: {}, Success: false, Reason: {}, ClientIp: {}, UserAgent: {}", 
                    traceId, requestId, event, userId, email, reason, clientIp, userAgent);
        }
    }
    
    /**
     * Log authorization event
     */
    public void logAuthorizationEvent(String event, String userId, String resource, String action, boolean authorized) {
        String traceId = TraceUtil.getTraceId();
        
        if (authorized) {
            log.info("AUTHZ_EVENT - TraceId: {}, Event: {}, UserId: {}, Resource: {}, Action: {}, Authorized: true", 
                    traceId, event, userId, resource, action);
        } else {
            log.warn("AUTHZ_EVENT - TraceId: {}, Event: {}, UserId: {}, Resource: {}, Action: {}, Authorized: false", 
                    traceId, event, userId, resource, action);
        }
    }
    
    /**
     * Log performance metrics
     */
    public void logPerformanceMetrics(String operation, long executionTime, Map<String, Object> metrics) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        if (executionTime > 1000) { // Log slow operations as warnings
            log.warn("PERFORMANCE_WARNING - TraceId: {}, RequestId: {}, Operation: {}, ExecutionTime: {}ms, Metrics: {}", 
                    traceId, requestId, operation, executionTime, metrics);
        } else {
            log.info("PERFORMANCE_METRICS - TraceId: {}, RequestId: {}, Operation: {}, ExecutionTime: {}ms, Metrics: {}", 
                    traceId, requestId, operation, executionTime, metrics);
        }
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String event, String severity, String description, Map<String, Object> details) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        String clientIp = TraceUtil.getClientIp();
        String userAgent = TraceUtil.getUserAgent();
        
        switch (severity.toUpperCase()) {
            case "CRITICAL":
                log.error("SECURITY_EVENT_CRITICAL - TraceId: {}, RequestId: {}, Event: {}, Description: {}, ClientIp: {}, UserAgent: {}, Details: {}", 
                        traceId, requestId, event, description, clientIp, userAgent, details);
                break;
            case "HIGH":
                log.error("SECURITY_EVENT_HIGH - TraceId: {}, RequestId: {}, Event: {}, Description: {}, ClientIp: {}, UserAgent: {}, Details: {}", 
                        traceId, requestId, event, description, clientIp, userAgent, details);
                break;
            case "MEDIUM":
                log.warn("SECURITY_EVENT_MEDIUM - TraceId: {}, RequestId: {}, Event: {}, Description: {}, ClientIp: {}, UserAgent: {}, Details: {}", 
                        traceId, requestId, event, description, clientIp, userAgent, details);
                break;
            case "LOW":
                log.info("SECURITY_EVENT_LOW - TraceId: {}, RequestId: {}, Event: {}, Description: {}, ClientIp: {}, UserAgent: {}, Details: {}", 
                        traceId, requestId, event, description, clientIp, userAgent, details);
                break;
            default:
                log.warn("SECURITY_EVENT - TraceId: {}, RequestId: {}, Event: {}, Severity: {}, Description: {}, ClientIp: {}, UserAgent: {}, Details: {}", 
                        traceId, requestId, event, severity, description, clientIp, userAgent, details);
        }
    }
    
    /**
     * Log system event
     */
    public void logSystemEvent(String event, String level, String message, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        switch (level.toUpperCase()) {
            case "ERROR":
                log.error("SYSTEM_EVENT_ERROR - TraceId: {}, RequestId: {}, Event: {}, Message: {}, Context: {}", 
                        traceId, requestId, event, message, context);
                break;
            case "WARN":
                log.warn("SYSTEM_EVENT_WARN - TraceId: {}, RequestId: {}, Event: {}, Message: {}, Context: {}", 
                        traceId, requestId, event, message, context);
                break;
            case "INFO":
                log.info("SYSTEM_EVENT_INFO - TraceId: {}, RequestId: {}, Event: {}, Message: {}, Context: {}", 
                        traceId, requestId, event, message, context);
                break;
            case "DEBUG":
                log.debug("SYSTEM_EVENT_DEBUG - TraceId: {}, RequestId: {}, Event: {}, Message: {}, Context: {}", 
                        traceId, requestId, event, message, context);
                break;
            default:
                log.info("SYSTEM_EVENT - TraceId: {}, RequestId: {}, Event: {}, Level: {}, Message: {}, Context: {}", 
                        traceId, requestId, event, level, message, context);
        }
    }
    
    
    /**
     * Log audit event
     */
    public void logAuditEvent(String action, String resource, String resourceId, String userId, Map<String, Object> changes) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        String currentUserId = TraceUtil.getUserId();
        String timestamp = TraceUtil.getFormattedTimestamp();
        
        log.info("AUDIT_EVENT - TraceId: {}, RequestId: {}, Timestamp: {}, Action: {}, Resource: {}, ResourceId: {}, UserId: {}, CurrentUserId: {}, Changes: {}", 
                traceId, requestId, timestamp, action, resource, resourceId, userId, currentUserId, changes);
    }
}

package com.ankurshala.backend.util;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Enhanced utility class for managing trace IDs and logging context
 * Provides consistent tracing across the application with performance metrics
 */
@Component
public class TraceUtil {
    
    private static final String TRACE_ID_KEY = "traceId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String USER_ID_KEY = "userId";
    private static final String REQUEST_PATH_KEY = "requestPath";
    private static final String REQUEST_METHOD_KEY = "requestMethod";
    private static final String START_TIME_KEY = "startTime";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String CLIENT_IP_KEY = "clientIp";
    private static final String USER_AGENT_KEY = "userAgent";
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Generate a new trace ID with timestamp prefix
     */
    public static String generateTraceId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s-%s", timestamp, uuid);
    }
    
    /**
     * Generate a new request ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Set trace ID in MDC for logging context
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }
    
    /**
     * Get current trace ID from MDC
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
    
    /**
     * Set request ID in MDC for logging context
     */
    public static void setRequestId(String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }
    
    /**
     * Get current request ID from MDC
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }
    
    /**
     * Set user ID in MDC for logging context
     */
    public static void setUserId(String userId) {
        MDC.put(USER_ID_KEY, userId);
    }
    
    /**
     * Get current user ID from MDC
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }
    
    /**
     * Set request path in MDC for logging context
     */
    public static void setRequestPath(String path) {
        MDC.put(REQUEST_PATH_KEY, path);
    }
    
    /**
     * Get current request path from MDC
     */
    public static String getRequestPath() {
        return MDC.get(REQUEST_PATH_KEY);
    }
    
    /**
     * Set request method in MDC for logging context
     */
    public static void setRequestMethod(String method) {
        MDC.put(REQUEST_METHOD_KEY, method);
    }
    
    /**
     * Get current request method from MDC
     */
    public static String getRequestMethod() {
        return MDC.get(REQUEST_METHOD_KEY);
    }
    
    /**
     * Set start time for performance tracking
     */
    public static void setStartTime(long startTime) {
        MDC.put(START_TIME_KEY, String.valueOf(startTime));
    }
    
    /**
     * Get start time for performance tracking
     */
    public static long getStartTime() {
        String startTimeStr = MDC.get(START_TIME_KEY);
        return startTimeStr != null ? Long.parseLong(startTimeStr) : 0;
    }
    
    /**
     * Calculate execution time
     */
    public static long getExecutionTime() {
        long startTime = getStartTime();
        return startTime > 0 ? System.currentTimeMillis() - startTime : 0;
    }
    
    /**
     * Set session ID in MDC for logging context
     */
    public static void setSessionId(String sessionId) {
        MDC.put(SESSION_ID_KEY, sessionId);
    }
    
    /**
     * Get current session ID from MDC
     */
    public static String getSessionId() {
        return MDC.get(SESSION_ID_KEY);
    }
    
    /**
     * Set client IP in MDC for logging context
     */
    public static void setClientIp(String clientIp) {
        MDC.put(CLIENT_IP_KEY, clientIp);
    }
    
    /**
     * Get current client IP from MDC
     */
    public static String getClientIp() {
        return MDC.get(CLIENT_IP_KEY);
    }
    
    /**
     * Set user agent in MDC for logging context
     */
    public static void setUserAgent(String userAgent) {
        MDC.put(USER_AGENT_KEY, userAgent);
    }
    
    /**
     * Get current user agent from MDC
     */
    public static String getUserAgent() {
        return MDC.get(USER_AGENT_KEY);
    }
    
    /**
     * Clear trace ID from MDC
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }
    
    /**
     * Clear request ID from MDC
     */
    public static void clearRequestId() {
        MDC.remove(REQUEST_ID_KEY);
    }
    
    /**
     * Clear all MDC values
     */
    public static void clear() {
        MDC.clear();
    }
    
    /**
     * Clear specific MDC value
     */
    public static void clear(String key) {
        MDC.remove(key);
    }
    
    /**
     * Get formatted timestamp
     */
    public static String getFormattedTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
    
    /**
     * Initialize trace context for a new request
     */
    public static void initializeTraceContext(String path, String method, String clientIp, String userAgent) {
        setTraceId(generateTraceId());
        setRequestId(generateRequestId());
        setRequestPath(path);
        setRequestMethod(method);
        setClientIp(clientIp);
        setUserAgent(userAgent);
        setStartTime(System.currentTimeMillis());
    }
}
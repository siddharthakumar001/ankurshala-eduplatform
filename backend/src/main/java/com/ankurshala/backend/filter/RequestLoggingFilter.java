package com.ankurshala.backend.filter;

import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Request Logging Filter
 * Logs all incoming requests with comprehensive context information
 * Implements structured logging with performance metrics
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Autowired
    private LoggingService loggingService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        String traceId = TraceUtil.generateTraceId();
        String requestId = TraceUtil.generateRequestId();
        
        TraceUtil.setTraceId(traceId);
        TraceUtil.setRequestId(requestId);

        try {
            // Log request start
            Map<String, Object> requestContext = new HashMap<>();
            requestContext.put("method", request.getMethod());
            requestContext.put("path", request.getRequestURI());
            requestContext.put("queryString", request.getQueryString());
            requestContext.put("remoteAddr", request.getRemoteAddr());
            requestContext.put("userAgent", request.getHeader("User-Agent"));
            requestContext.put("contentType", request.getContentType());
            requestContext.put("contentLength", request.getContentLength());
            requestContext.put("referer", request.getHeader("Referer"));
            requestContext.put("accept", request.getHeader("Accept"));
            requestContext.put("acceptLanguage", request.getHeader("Accept-Language"));
            requestContext.put("acceptEncoding", request.getHeader("Accept-Encoding"));
            
            loggingService.logSystemEvent("REQUEST_START", "INFO", 
                    "Incoming request: " + request.getMethod() + " " + request.getRequestURI(), requestContext);

            // Process request
            filterChain.doFilter(request, response);
            
        } finally {
            // Log request completion
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> responseContext = new HashMap<>();
            responseContext.put("method", request.getMethod());
            responseContext.put("path", request.getRequestURI());
            responseContext.put("status", response.getStatus());
            responseContext.put("duration", duration);
            responseContext.put("contentType", response.getContentType());
            
            String logLevel = getLogLevel(response.getStatus());
            String message = String.format("Request completed: %s %s -> %d (%dms)", 
                    request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
            
            loggingService.logSystemEvent("REQUEST_COMPLETE", logLevel, message, responseContext);
            
            // Log performance metrics
            if (duration > 1000) { // Log slow requests (>1s)
                Map<String, Object> performanceContext = new HashMap<>();
                performanceContext.put("method", request.getMethod());
                performanceContext.put("path", request.getRequestURI());
                performanceContext.put("duration", duration);
                performanceContext.put("status", response.getStatus());
                
                loggingService.logSystemEvent("SLOW_REQUEST", "WARN", 
                        "Slow request detected: " + duration + "ms", performanceContext);
            }
            
            // Clear trace context
            TraceUtil.clearTraceId();
            TraceUtil.clearRequestId();
        }
    }

    private String getLogLevel(int status) {
        if (status >= 500) return "ERROR";
        if (status >= 400) return "WARN";
        return "INFO";
    }
}
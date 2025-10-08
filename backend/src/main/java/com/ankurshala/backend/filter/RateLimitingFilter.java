package com.ankurshala.backend.filter;

import com.ankurshala.backend.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate Limiting Filter
 * Applies rate limiting to API endpoints based on client IP
 */
@Slf4j
@Component
@Order(1) // Execute before security filters
public class RateLimitingFilter implements Filter {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // Skip rate limiting for health checks and static resources
        if (shouldSkipRateLimit(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        boolean isAllowed = true;
        String rateLimitType = "general";
        
        try {
            // Apply specific rate limits based on endpoint
            if (requestURI.startsWith("/api/auth/signin")) {
                isAllowed = rateLimitingService.isAuthRequestAllowed(httpRequest);
                rateLimitType = "auth";
            } else if (requestURI.startsWith("/api/auth/signup")) {
                isAllowed = rateLimitingService.isSignupRequestAllowed(httpRequest);
                rateLimitType = "signup";
            } else {
                isAllowed = rateLimitingService.isGeneralRequestAllowed(httpRequest);
                rateLimitType = "general";
            }

            if (!isAllowed) {
                handleRateLimitExceeded(httpRequest, httpResponse, rateLimitType);
                return;
            }

            // Add rate limit headers to response
            addRateLimitHeaders(httpResponse, httpRequest, rateLimitType);
            
        } catch (Exception e) {
            log.error("Error in rate limiting filter: {}", e.getMessage());
            // On error, allow the request to proceed (fail open)
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSkipRateLimit(String requestURI) {
        return requestURI.startsWith("/api/actuator/") ||
               requestURI.equals("/api/csrf") ||
               requestURI.startsWith("/api/test/") ||
               requestURI.endsWith(".css") ||
               requestURI.endsWith(".js") ||
               requestURI.endsWith(".ico") ||
               requestURI.endsWith(".png") ||
               requestURI.endsWith(".jpg") ||
               requestURI.endsWith(".jpeg") ||
               requestURI.endsWith(".gif");
    }

    private void handleRateLimitExceeded(HttpServletRequest request, HttpServletResponse response, String rateLimitType)
            throws IOException {
        
        String clientIp = getClientIp(request);
        log.warn("Rate limit exceeded for IP: {} on endpoint: {} (type: {})", 
                clientIp, request.getRequestURI(), rateLimitType);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "RATE_LIMIT_EXCEEDED");
        errorResponse.put("message", "Too many requests. Please try again later.");
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("timestamp", System.currentTimeMillis());

        // Add retry-after header
        long ttl = rateLimitingService.getTTL(rateLimitType + ":" + clientIp);
        if (ttl > 0) {
            response.setHeader("Retry-After", String.valueOf(ttl));
            errorResponse.put("retryAfter", ttl);
        }

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void addRateLimitHeaders(HttpServletResponse response, HttpServletRequest request, String rateLimitType) {
        String clientIp = getClientIp(request);
        String key = rateLimitType + ":" + clientIp;
        
        int maxRequests = getMaxRequestsForType(rateLimitType);
        int remaining = rateLimitingService.getRemainingRequests(key, maxRequests);
        long resetTime = System.currentTimeMillis() / 1000 + rateLimitingService.getTTL(key);

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
        response.setHeader("X-RateLimit-Type", rateLimitType);
    }

    private int getMaxRequestsForType(String rateLimitType) {
        switch (rateLimitType) {
            case "auth":
                return 5;
            case "signup":
                return 3;
            case "general":
            default:
                return 100;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

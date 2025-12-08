package com.ankurshala.backend.filter;

import com.ankurshala.backend.ratelimit.RateLimitProperties;
import com.ankurshala.backend.ratelimit.RateLimitResult;
import com.ankurshala.backend.ratelimit.RateLimitService;
import com.ankurshala.backend.service.MetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter for enforcing rate limits on API requests.
 * Applied to all requests before authentication.
 */
@Slf4j
@Component
@Order(2) // After RequestLoggingFilter
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final MetricsService metricsService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!rateLimitProperties.isEnabled()) {
            chain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for actuator endpoints
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.startsWith("/api/actuator/")) {
            chain.doFilter(request, response);
            return;
        }

        // Get identifier (user ID or IP)
        String key = getIdentifier(httpRequest);
        boolean isAuthenticated = isAuthenticated();

        // Check rate limit
        RateLimitResult result = rateLimitService.checkRateLimit(key, requestURI, isAuthenticated);

        // Add rate limit headers
        httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
        
        if (!result.isAllowed()) {
            httpResponse.setHeader("X-RateLimit-Retry-After", String.valueOf(result.getRetryAfterSeconds()));
            
            // Record metric
            metricsService.recordApiError(requestURI, "RateLimitExceeded");
            
            // Return 429 Too Many Requests
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Rate limit exceeded. Please try again later.");
            errorResponse.put("retryAfter", result.getRetryAfterSeconds());
            errorResponse.put("limit", result.getLimit());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            
            log.warn("Rate limit exceeded for {} on {}", key, requestURI);
            return;
        }

        // Continue with request
        chain.doFilter(request, response);
    }

    /**
     * Get unique identifier for rate limiting.
     * Uses user ID if authenticated, otherwise IP address.
     */
    private String getIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getPrincipal().equals("anonymousUser")) {
            return "user:" + authentication.getName();
        }
        
        // Use IP address for anonymous users
        String ip = getClientIP(request);
        return "ip:" + ip;
    }

    /**
     * Check if current request is authenticated.
     */
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !authentication.getPrincipal().equals("anonymousUser");
    }

    /**
     * Get client IP address, considering proxy headers.
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("RateLimitFilter initialized");
    }

    @Override
    public void destroy() {
        log.info("RateLimitFilter destroyed");
    }
}

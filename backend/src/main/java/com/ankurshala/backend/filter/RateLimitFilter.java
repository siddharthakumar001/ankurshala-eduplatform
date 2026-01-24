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

        // Skip rate limiting for non-transactional (read-only) endpoints
        if (isNonTransactionalEndpoint(requestURI, httpRequest.getMethod())) {
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
     * Check if endpoint is non-transactional (read-only) and should skip rate limiting.
     * Rate limiting should only apply to transactional endpoints like:
     * - Authentication (login, signup)
     * - Content upload/import
     * - Payment processing
     * - Booking creation
     * - Notification sending
     */
    private boolean isNonTransactionalEndpoint(String uri, String method) {
        // Only apply rate limiting to POST, PUT, DELETE, PATCH methods on specific patterns
        // GET requests are generally read-only and don't need strict rate limiting
        if ("GET".equalsIgnoreCase(method)) {
            // Still rate limit these GET endpoints
            if (uri.contains("/admin/") && uri.contains("/export")) {
                return false; // Rate limit admin exports
            }
            return true; // Skip rate limiting for all other GET requests
        }

        // For POST/PUT/DELETE/PATCH, only rate limit transactional endpoints
        // Skip rate limiting for these non-transactional patterns
        if (uri.matches(".*/user/profile.*") && !"DELETE".equalsIgnoreCase(method)) {
            return true; // Profile updates are not critical to rate limit
        }

        // Apply rate limiting to these transactional endpoints
        return !isTransactionalEndpoint(uri);
    }

    /**
     * Check if endpoint is transactional and needs rate limiting.
     */
    private boolean isTransactionalEndpoint(String uri) {
        // Authentication endpoints
        if (uri.contains("/auth/login") || uri.contains("/auth/signin") ||
            uri.contains("/auth/signup") || uri.contains("/auth/register") ||
            uri.contains("/auth/refresh") || uri.contains("/auth/logout")) {
            return true;
        }

        // Content upload/import endpoints
        if ((uri.contains("/content/upload") || uri.contains("/content/import")) &&
            uri.contains("/admin/")) {
            return true;
        }

        // Payment and wallet endpoints
        if (uri.contains("/payment") || uri.contains("/payments") || uri.contains("/wallet")) {
            return true;
        }

        // Booking creation/updates
        if (uri.contains("/booking") || uri.contains("/bookings")) {
            return true;
        }

        // Notification sending
        if (uri.contains("/notifications/broadcast") || (uri.contains("/notification") && uri.contains("/send"))) {
            return true;
        }

        return false;
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

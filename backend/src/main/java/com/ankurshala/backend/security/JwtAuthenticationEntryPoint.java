package com.ankurshala.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException e) throws IOException {
        
        String requestURI = httpServletRequest.getRequestURI();
        
        // Skip authentication entry point for permitAll endpoints
        if (requestURI.startsWith("/api/auth/") || 
            requestURI.startsWith("/api/actuator/") || 
            requestURI.startsWith("/api/test/")) {
            // For permitAll endpoints, let Spring Security handle the request normally
            // Don't send any response here, let the filter chain continue
            return;
        }
        
        logger.error("Responding with unauthorized error. Message - {}", e.getMessage());
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Sorry, You're not authorized to access this resource.");
    }
}

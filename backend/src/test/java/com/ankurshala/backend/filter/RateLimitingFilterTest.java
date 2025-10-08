package com.ankurshala.backend.filter;

import com.ankurshala.backend.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testDoFilter_WhenHealthCheckEndpoint_ShouldSkipRateLimit() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/actuator/health");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService, never()).isGeneralRequestAllowed(any());
        verify(rateLimitingService, never()).isAuthRequestAllowed(any());
        verify(rateLimitingService, never()).isSignupRequestAllowed(any());
    }

    @Test
    void testDoFilter_WhenStaticResourceEndpoint_ShouldSkipRateLimit() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/static/css/style.css");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService, never()).isGeneralRequestAllowed(any());
    }

    @Test
    void testDoFilter_WhenAuthEndpointAllowed_ShouldProceed() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/signin");
        when(rateLimitingService.isAuthRequestAllowed(request)).thenReturn(true);

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService).isAuthRequestAllowed(request);
    }

    @Test
    void testDoFilter_WhenAuthEndpointBlocked_ShouldReturnTooManyRequests() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/signin");
        when(rateLimitingService.isAuthRequestAllowed(request)).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Rate limit exceeded\"}");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        verify(rateLimitingService).isAuthRequestAllowed(request);
    }

    @Test
    void testDoFilter_WhenSignupEndpointAllowed_ShouldProceed() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/signup");
        when(rateLimitingService.isSignupRequestAllowed(request)).thenReturn(true);

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService).isSignupRequestAllowed(request);
    }

    @Test
    void testDoFilter_WhenSignupEndpointBlocked_ShouldReturnTooManyRequests() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/signup");
        when(rateLimitingService.isSignupRequestAllowed(request)).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Rate limit exceeded\"}");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        verify(rateLimitingService).isSignupRequestAllowed(request);
    }

    @Test
    void testDoFilter_WhenGeneralEndpointAllowed_ShouldProceed() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/student/profile");
        when(rateLimitingService.isGeneralRequestAllowed(request)).thenReturn(true);

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService).isGeneralRequestAllowed(request);
    }

    @Test
    void testDoFilter_WhenGeneralEndpointBlocked_ShouldReturnTooManyRequests() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/student/profile");
        when(rateLimitingService.isGeneralRequestAllowed(request)).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"error\":\"Rate limit exceeded\"}");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        verify(rateLimitingService).isGeneralRequestAllowed(request);
    }

    @Test
    void testDoFilter_WhenExceptionInObjectMapper_ShouldStillReturnTooManyRequests() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/student/profile");
        when(rateLimitingService.isGeneralRequestAllowed(request)).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON error"));

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        // The filter should still call chain.doFilter even on exception due to fail-open behavior
        verify(filterChain).doFilter(request, response);
        // Should still write fallback response
        verify(response).getWriter();
    }

    @Test
    void testShouldSkipRateLimit_WithActuatorEndpoint_ShouldReturnTrue() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/actuator/prometheus");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService, never()).isGeneralRequestAllowed(any());
    }

    @Test
    void testShouldSkipRateLimit_WithSwaggerEndpoint_ShouldReturnTrue() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test/swagger");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService, never()).isGeneralRequestAllowed(any());
    }

    @Test
    void testShouldSkipRateLimit_WithWebjarsEndpoint_ShouldReturnTrue() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/webjars/springfox-swagger-ui/swagger-ui.js");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService, never()).isGeneralRequestAllowed(any());
    }

    @Test
    void testShouldSkipRateLimit_WithErrorEndpoint_ShouldReturnTrue() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/test/error");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService, never()).isGeneralRequestAllowed(any());
    }

    @Test
    void testShouldSkipRateLimit_WithCsrfEndpoint_ShouldReturnTrue() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/csrf");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(rateLimitingService, never()).isGeneralRequestAllowed(any());
    }
}

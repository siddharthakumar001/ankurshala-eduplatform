package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.RefreshTokenRequest;
import com.ankurshala.backend.dto.auth.SigninRequest;
import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.service.AuthService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private LoggingService loggingService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;
    private SigninRequest signinRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setName("Test User");
        signupRequest.setPassword("password123");

        signinRequest = new SigninRequest();
        signinRequest.setEmail("test@example.com");
        signinRequest.setPassword("password123");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-token");

        authResponse = new AuthResponse();
        authResponse.setUserId(1L);
        authResponse.setEmail("test@example.com");
        authResponse.setRole(Role.STUDENT);
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
    }

    @Test
    void testSignupStudent_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Mock AuthService
            when(authService.signupStudent(any(SignupRequest.class))).thenReturn(authResponse);

            // Execute
            ResponseEntity<ApiResponse<AuthResponse>> response = authController.signupStudent(signupRequest, request);

            // Verify
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Student registered successfully", response.getBody().getMessage());
            assertEquals(authResponse, response.getBody().getData());
            assertEquals("trace-123", response.getBody().getTraceId());
            assertEquals("req-123", response.getBody().getRequestId());

            // Verify service calls
            verify(authService).signupStudent(signupRequest);
            verify(loggingService).logBusinessOperationStart(eq("STUDENT_SIGNUP"), isNull(), any(Map.class));
            verify(loggingService).logBusinessOperationComplete(eq("STUDENT_SIGNUP"), eq("1"), eq(true), anyLong());
            verify(loggingService).logAuthenticationEvent(eq("SIGNUP_SUCCESS"), eq("1"), eq("test@example.com"), eq(true), isNull());
        }
    }

    @Test
    void testSignupStudent_Exception() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Mock AuthService to throw exception
            RuntimeException exception = new RuntimeException("Signup failed");
            when(authService.signupStudent(any(SignupRequest.class))).thenThrow(exception);

            // Execute and verify exception is thrown
            assertThrows(RuntimeException.class, () -> {
                authController.signupStudent(signupRequest, request);
            });

            // Verify error logging
            verify(loggingService).logBusinessOperationComplete(eq("STUDENT_SIGNUP"), isNull(), eq(false), anyLong());
            verify(loggingService).logError(eq("STUDENT_SIGNUP"), eq(exception), any(Map.class));
        }
    }

    @Test
    void testSignupTeacher_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Update authResponse for teacher
            authResponse.setRole(Role.TEACHER);

            // Mock AuthService
            when(authService.signupTeacher(any(SignupRequest.class))).thenReturn(authResponse);

            // Execute
            ResponseEntity<ApiResponse<AuthResponse>> response = authController.signupTeacher(signupRequest, request);

            // Verify
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Teacher registered successfully", response.getBody().getMessage());
            assertEquals(authResponse, response.getBody().getData());

            // Verify service calls
            verify(authService).signupTeacher(signupRequest);
            verify(loggingService).logBusinessOperationStart(eq("TEACHER_SIGNUP"), isNull(), any(Map.class));
            verify(loggingService).logBusinessOperationComplete(eq("TEACHER_SIGNUP"), eq("1"), eq(true), anyLong());
            verify(loggingService).logAuthenticationEvent(eq("SIGNUP_SUCCESS"), eq("1"), eq("test@example.com"), eq(true), isNull());
        }
    }

    @Test
    void testSignin_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");
            traceUtilMock.when(TraceUtil::getClientIp).thenReturn("127.0.0.1");
            traceUtilMock.when(TraceUtil::getUserAgent).thenReturn("Mozilla/5.0");

            // Mock AuthService
            when(authService.signin(any(SigninRequest.class))).thenReturn(authResponse);

            // Execute
            ResponseEntity<ApiResponse<AuthResponse>> response = authController.signin(signinRequest, request);

            // Verify
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Login successful", response.getBody().getMessage());
            assertEquals(authResponse, response.getBody().getData());

            // Verify service calls
            verify(authService).signin(signinRequest);
            verify(loggingService).logBusinessOperationStart(eq("SIGNIN"), isNull(), any(Map.class));
            verify(loggingService).logBusinessOperationComplete(eq("SIGNIN"), eq("1"), eq(true), anyLong());
            verify(loggingService).logAuthenticationEvent(eq("SIGNIN_SUCCESS"), eq("1"), eq("test@example.com"), eq(true), isNull());
        }
    }

    @Test
    void testSignin_Exception() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");
            traceUtilMock.when(TraceUtil::getClientIp).thenReturn("127.0.0.1");

            // Mock AuthService to throw exception
            RuntimeException exception = new RuntimeException("Signin failed");
            when(authService.signin(any(SigninRequest.class))).thenThrow(exception);

            // Execute and verify exception is thrown
            assertThrows(RuntimeException.class, () -> {
                authController.signin(signinRequest, request);
            });

            // Verify error logging
            verify(loggingService).logBusinessOperationComplete(eq("SIGNIN"), isNull(), eq(false), anyLong());
            verify(loggingService).logAuthenticationEvent(eq("SIGNIN_FAILED"), isNull(), eq("test@example.com"), eq(false), eq("Signin failed"));
            verify(loggingService).logError(eq("SIGNIN"), eq(exception), any(Map.class));
        }
    }

    @Test
    void testRefreshToken_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Mock AuthService
            when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

            // Execute
            ResponseEntity<ApiResponse<AuthResponse>> response = authController.refreshToken(refreshTokenRequest, request);

            // Verify
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Token refreshed successfully", response.getBody().getMessage());
            assertEquals(authResponse, response.getBody().getData());

            // Verify service calls
            verify(authService).refreshToken(refreshTokenRequest);
            verify(loggingService).logBusinessOperationStart(eq("TOKEN_REFRESH"), isNull(), any(Map.class));
            verify(loggingService).logBusinessOperationComplete(eq("TOKEN_REFRESH"), eq("1"), eq(true), anyLong());
            verify(loggingService).logAuthenticationEvent(eq("TOKEN_REFRESH_SUCCESS"), eq("1"), eq("test@example.com"), eq(true), isNull());
        }
    }

    @Test
    void testRefreshToken_Exception() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Mock AuthService to throw exception
            RuntimeException exception = new RuntimeException("Token refresh failed");
            when(authService.refreshToken(any(RefreshTokenRequest.class))).thenThrow(exception);

            // Execute and verify exception is thrown
            assertThrows(RuntimeException.class, () -> {
                authController.refreshToken(refreshTokenRequest, request);
            });

            // Verify error logging
            verify(loggingService).logBusinessOperationComplete(eq("TOKEN_REFRESH"), isNull(), eq(false), anyLong());
            verify(loggingService).logAuthenticationEvent(eq("TOKEN_REFRESH_FAILED"), isNull(), isNull(), eq(false), eq("Token refresh failed"));
            verify(loggingService).logError(eq("TOKEN_REFRESH"), eq(exception), any(Map.class));
        }
    }

    @Test
    void testLogout_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Mock AuthService
            doNothing().when(authService).logout(anyString());

            // Execute
            ResponseEntity<ApiResponse<Void>> response = authController.logout(refreshTokenRequest, request);

            // Verify
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Logout successful", response.getBody().getMessage());
            assertNull(response.getBody().getData());

            // Verify service calls
            verify(authService).logout("refresh-token");
            verify(loggingService).logBusinessOperationStart(eq("LOGOUT"), isNull(), any(Map.class));
            verify(loggingService).logBusinessOperationComplete(eq("LOGOUT"), isNull(), eq(true), anyLong());
            verify(loggingService).logAuthenticationEvent(eq("LOGOUT_SUCCESS"), isNull(), isNull(), eq(true), isNull());
        }
    }

    @Test
    void testLogout_Exception() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Mock AuthService to throw exception
            RuntimeException exception = new RuntimeException("Logout failed");
            doThrow(exception).when(authService).logout(anyString());

            // Execute and verify exception is thrown
            assertThrows(RuntimeException.class, () -> {
                authController.logout(refreshTokenRequest, request);
            });

            // Verify error logging
            verify(loggingService).logBusinessOperationComplete(eq("LOGOUT"), isNull(), eq(false), anyLong());
            verify(loggingService).logError(eq("LOGOUT"), eq(exception), any(Map.class));
        }
    }

    @Test
    void testTestEndpoint_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Mock TraceUtil static methods
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("trace-123");
            traceUtilMock.when(TraceUtil::getRequestId).thenReturn("req-123");

            // Execute
            ResponseEntity<ApiResponse<String>> response = authController.test(request);

            // Verify
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Test endpoint successful", response.getBody().getMessage());
            assertEquals("AuthController is working", response.getBody().getData());
            assertEquals("trace-123", response.getBody().getTraceId());
            assertEquals("req-123", response.getBody().getRequestId());

            // Verify logging
            verify(loggingService).logSystemEvent(eq("AUTH_TEST_ENDPOINT"), eq("INFO"), eq("Auth test endpoint called"), any(Map.class));
        }
    }
}
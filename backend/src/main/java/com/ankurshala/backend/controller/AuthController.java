package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.RefreshTokenRequest;
import com.ankurshala.backend.dto.auth.SigninRequest;
import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.AuthService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Authentication Controller
 * Handles user authentication, registration, and token management
 * Implements proper design patterns with comprehensive logging and error handling
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private LoggingService loggingService;

    @PostMapping("/signup/student")
    public ResponseEntity<ApiResponse<AuthResponse>> signupStudent(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("email", signupRequest.getEmail());
        context.put("name", signupRequest.getName());
        context.put("userType", "STUDENT");
        
        loggingService.logBusinessOperationStart("STUDENT_SIGNUP", null, context);
        
        try {
            AuthResponse response = authService.signupStudent(signupRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("STUDENT_SIGNUP", response.getUserId().toString(), true, executionTime);
            
            Map<String, Object> authContext = new HashMap<>();
            authContext.put("userId", response.getUserId());
            authContext.put("email", response.getEmail());
            authContext.put("role", response.getRole());
            
            loggingService.logAuthenticationEvent("SIGNUP_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Student registered successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("STUDENT_SIGNUP", null, false, executionTime);
            loggingService.logError("STUDENT_SIGNUP", e, context);
            throw e;
        }
    }

    @PostMapping("/signup/teacher")
    public ResponseEntity<ApiResponse<AuthResponse>> signupTeacher(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("email", signupRequest.getEmail());
        context.put("name", signupRequest.getName());
        context.put("userType", "TEACHER");
        
        loggingService.logBusinessOperationStart("TEACHER_SIGNUP", null, context);
        
        try {
            AuthResponse response = authService.signupTeacher(signupRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEACHER_SIGNUP", response.getUserId().toString(), true, executionTime);
            
            loggingService.logAuthenticationEvent("SIGNUP_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Teacher registered successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEACHER_SIGNUP", null, false, executionTime);
            loggingService.logError("TEACHER_SIGNUP", e, context);
            throw e;
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> signin(
            @Valid @RequestBody SigninRequest signinRequest,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("email", signinRequest.getEmail());
        context.put("clientIp", TraceUtil.getClientIp());
        context.put("userAgent", TraceUtil.getUserAgent());
        
        loggingService.logBusinessOperationStart("SIGNIN", null, context);
        
        try {
            AuthResponse response = authService.signin(signinRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("SIGNIN", response.getUserId().toString(), true, executionTime);
            
            loggingService.logAuthenticationEvent("SIGNIN_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Login successful");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("SIGNIN", null, false, executionTime);
            
            Map<String, Object> errorContext = new HashMap<>();
            errorContext.put("email", signinRequest.getEmail());
            errorContext.put("clientIp", TraceUtil.getClientIp());
            
            loggingService.logAuthenticationEvent("SIGNIN_FAILED", null, signinRequest.getEmail(), false, e.getMessage());
            loggingService.logError("SIGNIN", e, errorContext);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("hasRefreshToken", refreshTokenRequest.getRefreshToken() != null);
        
        loggingService.logBusinessOperationStart("TOKEN_REFRESH", null, context);
        
        try {
            AuthResponse response = authService.refreshToken(refreshTokenRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TOKEN_REFRESH", response.getUserId().toString(), true, executionTime);
            
            loggingService.logAuthenticationEvent("TOKEN_REFRESH_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Token refreshed successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TOKEN_REFRESH", null, false, executionTime);
            
            loggingService.logAuthenticationEvent("TOKEN_REFRESH_FAILED", null, null, false, e.getMessage());
            loggingService.logError("TOKEN_REFRESH", e, context);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("hasRefreshToken", refreshTokenRequest.getRefreshToken() != null);
        
        loggingService.logBusinessOperationStart("LOGOUT", null, context);
        
        try {
            authService.logout(refreshTokenRequest.getRefreshToken());
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("LOGOUT", null, true, executionTime);
            
            loggingService.logAuthenticationEvent("LOGOUT_SUCCESS", null, null, true, null);
            
            ApiResponse<Void> apiResponse = ApiResponse.success(null, "Logout successful");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("LOGOUT", null, false, executionTime);
            loggingService.logError("LOGOUT", e, context);
            throw e;
        }
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        loggingService.logSystemEvent("AUTH_TEST_ENDPOINT", "INFO", "Auth test endpoint called", new HashMap<>());
        
        ApiResponse<String> response = ApiResponse.success("AuthController is working", "Test endpoint successful");
        response.setTraceId(traceId);
        response.setRequestId(requestId);
        
        return ResponseEntity.ok(response);
    }
}
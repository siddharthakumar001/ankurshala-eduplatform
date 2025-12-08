package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.auth.*;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.EnhancedAuthService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Authentication Controller
 * Handles strict student and teacher registration with mandatory personalization fields
 * Implements proper design patterns with comprehensive logging and error handling
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class EnhancedAuthController {

    @Autowired
    private EnhancedAuthService enhancedAuthService;
    
    @Autowired
    private LoggingService loggingService;

    @PostMapping("/signup/student")
    public ResponseEntity<ApiResponse<AuthResponse>> signupStudent(
            @Valid @RequestBody StudentSignupRequest signupRequest,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("email", signupRequest.getEmail());
        context.put("name", signupRequest.getName());
        context.put("board", signupRequest.getBoard());
        context.put("grade", signupRequest.getGrade());
        context.put("userType", "STUDENT");
        
        loggingService.logBusinessOperationStart("ENHANCED_STUDENT_SIGNUP", null, context);
        
        try {
            AuthResponse response = enhancedAuthService.signupStudent(signupRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_STUDENT_SIGNUP", response.getUserId().toString(), true, executionTime);
            
            Map<String, Object> authContext = new HashMap<>();
            authContext.put("userId", response.getUserId());
            authContext.put("email", response.getEmail());
            authContext.put("role", response.getRole());
            
            loggingService.logAuthenticationEvent("ENHANCED_SIGNUP_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Student registered successfully with enhanced profile");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_STUDENT_SIGNUP", null, false, executionTime);
            loggingService.logError("ENHANCED_STUDENT_SIGNUP", e, context);
            throw e;
        }
    }

    @PostMapping("/signup/teacher")
    public ResponseEntity<ApiResponse<AuthResponse>> signupTeacher(
            @Valid @RequestBody TeacherSignupRequest signupRequest,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("email", signupRequest.getEmail());
        context.put("name", signupRequest.getName());
        context.put("categories", signupRequest.getCategories());
        context.put("hourlyRate", signupRequest.getHourlyRate());
        context.put("userType", "TEACHER");
        
        loggingService.logBusinessOperationStart("ENHANCED_TEACHER_SIGNUP", null, context);
        
        try {
            AuthResponse response = enhancedAuthService.signupTeacher(signupRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_TEACHER_SIGNUP", response.getUserId().toString(), true, executionTime);
            
            loggingService.logAuthenticationEvent("ENHANCED_SIGNUP_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Teacher registered successfully with enhanced profile");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_TEACHER_SIGNUP", null, false, executionTime);
            loggingService.logError("ENHANCED_TEACHER_SIGNUP", e, context);
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
        
        loggingService.logBusinessOperationStart("ENHANCED_SIGNIN", null, context);
        
        try {
            AuthResponse response = enhancedAuthService.signin(signinRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_SIGNIN", response.getUserId().toString(), true, executionTime);
            
            loggingService.logAuthenticationEvent("ENHANCED_SIGNIN_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Login successful");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_SIGNIN", null, false, executionTime);
            
            Map<String, Object> errorContext = new HashMap<>();
            errorContext.put("email", signinRequest.getEmail());
            errorContext.put("clientIp", TraceUtil.getClientIp());
            
            loggingService.logAuthenticationEvent("ENHANCED_SIGNIN_FAILED", null, signinRequest.getEmail(), false, e.getMessage());
            loggingService.logError("ENHANCED_SIGNIN", e, errorContext);
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
        
        loggingService.logBusinessOperationStart("ENHANCED_TOKEN_REFRESH", null, context);
        
        try {
            AuthResponse response = enhancedAuthService.refreshToken(refreshTokenRequest);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_TOKEN_REFRESH", response.getUserId().toString(), true, executionTime);
            
            loggingService.logAuthenticationEvent("ENHANCED_TOKEN_REFRESH_SUCCESS", response.getUserId().toString(), response.getEmail(), true, null);
            
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(response, "Token refreshed successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_TOKEN_REFRESH", null, false, executionTime);
            
            loggingService.logAuthenticationEvent("ENHANCED_TOKEN_REFRESH_FAILED", null, null, false, e.getMessage());
            loggingService.logError("ENHANCED_TOKEN_REFRESH", e, context);
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
        
        loggingService.logBusinessOperationStart("ENHANCED_LOGOUT", null, context);
        
        try {
            enhancedAuthService.logout(refreshTokenRequest.getRefreshToken());
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_LOGOUT", null, true, executionTime);
            
            loggingService.logAuthenticationEvent("ENHANCED_LOGOUT_SUCCESS", null, null, true, null);
            
            ApiResponse<Void> apiResponse = ApiResponse.success(null, "Logout successful");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_LOGOUT", null, false, executionTime);
            loggingService.logError("ENHANCED_LOGOUT", e, context);
            throw e;
        }
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<ApiResponse<Map<String, Object>>> heartbeat(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("clientIp", TraceUtil.getClientIp());
        context.put("userAgent", TraceUtil.getUserAgent());
        
        loggingService.logBusinessOperationStart("ENHANCED_HEARTBEAT", null, context);
        
        try {
            // Get current user from security context
            String currentUser = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
            
            Map<String, Object> heartbeatData = new HashMap<>();
            heartbeatData.put("timestamp", System.currentTimeMillis());
            heartbeatData.put("status", "active");
            heartbeatData.put("user", currentUser);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_HEARTBEAT", currentUser, true, executionTime);
            
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(heartbeatData, "Heartbeat successful");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ENHANCED_HEARTBEAT", null, false, executionTime);
            loggingService.logError("ENHANCED_HEARTBEAT", e, context);
            throw e;
        }
    }

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        loggingService.logSystemEvent("ENHANCED_AUTH_TEST_ENDPOINT", "INFO", "Enhanced auth test endpoint called", new HashMap<>());
        
        ApiResponse<String> response = ApiResponse.success("EnhancedAuthController is working", "Test endpoint successful");
        response.setTraceId(traceId);
        response.setRequestId(requestId);
        
        return ResponseEntity.ok(response);
    }
}

package com.ankurshala.backend.exception;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enhanced Global Exception Handler
 * Handles all exceptions globally and returns standardized error responses
 * Implements comprehensive logging and error tracking
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private LoggingService loggingService;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "RESOURCE_NOT_FOUND");
        
        loggingService.logErrorWithContext("RESOURCE_NOT_FOUND", ex, context);
        loggingService.logSystemEvent("RESOURCE_NOT_FOUND", "WARN", 
                "Resource not found: " + ex.getMessage(), context);
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "BUSINESS_EXCEPTION");
        
        loggingService.logErrorWithContext("BUSINESS_EXCEPTION", ex, context);
        loggingService.logSystemEvent("BUSINESS_EXCEPTION", "WARN", 
                "Business logic error: " + ex.getMessage(), context);
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            ValidationException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "VALIDATION_EXCEPTION");
        context.put("fieldName", ex.getFieldName());
        context.put("fieldValue", ex.getFieldValue());
        
        loggingService.logErrorWithContext("VALIDATION_EXCEPTION", ex, context);
        loggingService.logSystemEvent("VALIDATION_EXCEPTION", "WARN", 
                "Validation failed: " + ex.getMessage(), context);
        
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "METHOD_ARGUMENT_NOT_VALID");
        context.put("validationErrors", errors);
        
        loggingService.logErrorWithContext("METHOD_ARGUMENT_NOT_VALID", ex, context);
        loggingService.logSystemEvent("METHOD_ARGUMENT_NOT_VALID", "WARN", 
                "Method argument validation failed", context);
        
        ApiResponse<Object> response = ApiResponse.error("Validation failed", errors, request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "BAD_CREDENTIALS");
        context.put("clientIp", TraceUtil.getClientIp());
        context.put("userAgent", TraceUtil.getUserAgent());
        
        loggingService.logErrorWithContext("BAD_CREDENTIALS", ex, context);
        loggingService.logAuthenticationEvent("AUTHENTICATION_FAILED", null, null, false, ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error("Invalid email or password", request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "ACCESS_DENIED");
        context.put("clientIp", TraceUtil.getClientIp());
        
        loggingService.logErrorWithContext("ACCESS_DENIED", ex, context);
        loggingService.logSystemEvent("ACCESS_DENIED", "WARN", 
                "Access denied for resource: " + request.getRequestURI(), context);
        
        ApiResponse<Object> response = ApiResponse.error("Access Denied: You do not have permission to access this resource", request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(
            NoResourceFoundException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "NO_RESOURCE_FOUND");
        
        loggingService.logErrorWithContext("NO_RESOURCE_FOUND", ex, context);
        loggingService.logSystemEvent("NO_RESOURCE_FOUND", "WARN", 
                "No resource found: " + request.getRequestURI(), context);
        
        ApiResponse<Object> response = ApiResponse.error("Resource not found", request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "METHOD_NOT_SUPPORTED");
        context.put("supportedMethods", ex.getSupportedMethods());
        
        loggingService.logErrorWithContext("METHOD_NOT_SUPPORTED", ex, context);
        loggingService.logSystemEvent("METHOD_NOT_SUPPORTED", "WARN", 
                "HTTP method not supported: " + ex.getMethod(), context);
        
        ApiResponse<Object> response = ApiResponse.error("HTTP method '" + ex.getMethod() + "' not supported for this endpoint", request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "ILLEGAL_ARGUMENT");
        
        loggingService.logErrorWithContext("ILLEGAL_ARGUMENT", ex, context);
        loggingService.logSystemEvent("ILLEGAL_ARGUMENT", "WARN", 
                "Illegal argument provided: " + ex.getMessage(), context);
        
        ApiResponse<Object> response = ApiResponse.error("Invalid argument: " + ex.getMessage(), request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllUncaughtException(
            Exception ex, 
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("path", request.getRequestURI());
        context.put("method", request.getMethod());
        context.put("errorType", "UNEXPECTED_ERROR");
        context.put("exceptionClass", ex.getClass().getSimpleName());
        
        loggingService.logErrorWithContext("UNEXPECTED_ERROR", ex, context);
        loggingService.logSystemEvent("UNEXPECTED_ERROR", "ERROR", 
                "Unexpected error occurred: " + ex.getMessage(), context);
        
        ApiResponse<Object> response = ApiResponse.error("An unexpected error occurred", request.getRequestURI(), traceId);
        response.setRequestId(requestId);
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
package com.ankurshala.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Custom business exception for application-specific errors
 * Provides structured error handling with HTTP status codes and error details
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String errorType;
    private final Map<String, Object> errorDetails;
    private final LocalDateTime timestamp;
    
    public BusinessException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.errorCode = "BUSINESS_ERROR";
        this.errorType = "ValidationError";
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }
    
    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = "BUSINESS_ERROR";
        this.errorType = "BusinessError";
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }
    
    public BusinessException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorType = "BusinessError";
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }
    
    public BusinessException(String message, HttpStatus httpStatus, String errorCode, String errorType) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }
    
    public BusinessException(String message, HttpStatus httpStatus, String errorCode, String errorType, Map<String, Object> errorDetails) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.errorDetails = errorDetails;
        this.timestamp = LocalDateTime.now();
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "INTERNAL_ERROR";
        this.errorType = "SystemError";
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }
    
    public BusinessException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = "BUSINESS_ERROR";
        this.errorType = "BusinessError";
        this.errorDetails = null;
        this.timestamp = LocalDateTime.now();
    }
}
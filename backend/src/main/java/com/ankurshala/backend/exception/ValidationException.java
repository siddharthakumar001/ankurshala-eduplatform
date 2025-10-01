package com.ankurshala.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Validation exception for input validation errors
 * Thrown when request data doesn't meet validation requirements
 */
@Getter
public class ValidationException extends BusinessException {
    
    private final String fieldName;
    private final Object fieldValue;
    
    public ValidationException(String fieldName, Object fieldValue, String message) {
        super(String.format("Validation failed for field '%s': %s", fieldName, message), 
              HttpStatus.BAD_REQUEST, 
              "VALIDATION_ERROR", 
              "ValidationError");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "ValidationError");
        this.fieldName = null;
        this.fieldValue = null;
    }
}

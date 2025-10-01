package com.ankurshala.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Resource not found exception
 * Thrown when a requested resource doesn't exist
 */
@Getter
public class ResourceNotFoundException extends BusinessException {
    
    private final String resourceType;
    private final String resourceId;
    
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s with id '%s' not found", resourceType, resourceId), 
              HttpStatus.NOT_FOUND, 
              "RESOURCE_NOT_FOUND", 
              "NotFoundError");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String resourceType, Long resourceId) {
        this(resourceType, String.valueOf(resourceId));
    }
    
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "NotFoundError");
        this.resourceType = null;
        this.resourceId = null;
    }
}
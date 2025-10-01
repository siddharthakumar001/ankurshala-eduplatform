package com.ankurshala.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized API Response wrapper for all endpoints
 * Implements proper JSON design with consistent structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("errors")
    private List<String> errors;
    
    @JsonProperty("metadata")
    private ResponseMetadata metadata;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("traceId")
    private String traceId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("version")
    private String version;
    
    /**
     * Response metadata for pagination, filtering, etc.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseMetadata {
        @JsonProperty("pagination")
        private PaginationInfo pagination;
        
        @JsonProperty("filters")
        private Map<String, Object> filters;
        
        @JsonProperty("sorting")
        private SortingInfo sorting;
        
        @JsonProperty("totalCount")
        private Long totalCount;
        
        @JsonProperty("executionTime")
        private Long executionTimeMs;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationInfo {
        @JsonProperty("page")
        private int page;
        
        @JsonProperty("size")
        private int size;
        
        @JsonProperty("totalPages")
        private int totalPages;
        
        @JsonProperty("totalElements")
        private long totalElements;
        
        @JsonProperty("hasNext")
        private boolean hasNext;
        
        @JsonProperty("hasPrevious")
        private boolean hasPrevious;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SortingInfo {
        @JsonProperty("sortBy")
        private String sortBy;
        
        @JsonProperty("sortDirection")
        private String sortDirection;
    }
    
    // Factory methods for common response types
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .version("1.0")
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .version("1.0")
                .build();
    }
    
    public static <T> ApiResponse<T> success(T data, String message, ResponseMetadata metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(LocalDateTime.now().toString())
                .version("1.0")
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .version("1.0")
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now().toString())
                .version("1.0")
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String path, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .path(path)
                .traceId(traceId)
                .timestamp(LocalDateTime.now().toString())
                .version("1.0")
                .build();
    }
    
    public static <T> ApiResponse<T> error(String message, List<String> errors, String path, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .path(path)
                .traceId(traceId)
                .timestamp(LocalDateTime.now().toString())
                .version("1.0")
                .build();
    }
}
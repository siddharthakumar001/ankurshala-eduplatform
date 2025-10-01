package com.ankurshala.backend.exception;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ankurshala.backend.util.TraceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit tests for GlobalExceptionHandler
 * Tests all exception handling scenarios and response formatting
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private LoggingService loggingService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private ObjectMapper objectMapper;
    private MockedStatic<TraceUtil> traceUtilMock;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
        
        // Mock TraceUtil static methods to return different values on subsequent calls
        traceUtilMock = Mockito.mockStatic(TraceUtil.class);
        traceUtilMock.when(TraceUtil::getTraceId)
            .thenReturn("test-trace-id-1")
            .thenReturn("test-trace-id-2");
        traceUtilMock.when(TraceUtil::getRequestId)
            .thenReturn("test-request-id-1")
            .thenReturn("test-request-id-2");
    }

    @AfterEach
    void tearDown() {
        if (traceUtilMock != null) {
            traceUtilMock.close();
        }
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleResourceNotFoundException(exception, request);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTraceId());
        assertNotNull(response.getBody().getRequestId());
        
        verify(loggingService).logErrorWithContext(eq("RESOURCE_NOT_FOUND"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("RESOURCE_NOT_FOUND"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleBusinessException() {
        // Given
        BusinessException exception = new BusinessException("Business logic error");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBusinessException(exception, request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Business logic error", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("BUSINESS_EXCEPTION"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("BUSINESS_EXCEPTION"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleValidationException() {
        // Given
        List<String> errors = Arrays.asList("Field 'name' is required", "Field 'email' is invalid");
        ValidationException exception = new ValidationException("name", "invalid", "Validation failed");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleValidationException(exception, request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("VALIDATION_EXCEPTION"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("VALIDATION_EXCEPTION"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(exception.getBindingResult().getFieldErrors()).thenReturn(Arrays.asList(
                mock(org.springframework.validation.FieldError.class),
                mock(org.springframework.validation.FieldError.class)
        ));
        
        // Mock field errors
        org.springframework.validation.FieldError fieldError1 = mock(org.springframework.validation.FieldError.class);
        when(fieldError1.getField()).thenReturn("name");
        when(fieldError1.getDefaultMessage()).thenReturn("Name is required");
        
        org.springframework.validation.FieldError fieldError2 = mock(org.springframework.validation.FieldError.class);
        when(fieldError2.getField()).thenReturn("email");
        when(fieldError2.getDefaultMessage()).thenReturn("Email is invalid");
        
        when(exception.getBindingResult().getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleMethodArgumentNotValid(exception, request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getErrors());
        assertEquals(2, response.getBody().getErrors().size());
        
        verify(loggingService).logErrorWithContext(eq("METHOD_ARGUMENT_NOT_VALID"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("METHOD_ARGUMENT_NOT_VALID"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleBadCredentialsException() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBadCredentialsException(exception, request);
        
        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid email or password", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("BAD_CREDENTIALS"), eq(exception), any());
        verify(loggingService).logAuthenticationEvent(eq("AUTHENTICATION_FAILED"), isNull(), isNull(), eq(false), anyString());
    }

    @Test
    void testHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAccessDeniedException(exception, request);
        
        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Access Denied"));
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("ACCESS_DENIED"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("ACCESS_DENIED"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleNoResourceFoundException() {
        // Given
        NoResourceFoundException exception = new NoResourceFoundException(org.springframework.http.HttpMethod.GET, "/api/nonexistent");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleNoResourceFoundException(exception, request);
        
        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("NO_RESOURCE_FOUND"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("NO_RESOURCE_FOUND"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleHttpRequestMethodNotSupportedException() {
        // Given
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST", Arrays.asList("GET"));
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleHttpRequestMethodNotSupported(exception, request);
        
        // Then
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("HTTP method 'POST' not supported"));
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("METHOD_NOT_SUPPORTED"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("METHOD_NOT_SUPPORTED"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalArgumentException(exception, request);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Invalid argument"));
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("ILLEGAL_ARGUMENT"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("ILLEGAL_ARGUMENT"), eq("WARN"), anyString(), any());
    }

    @Test
    void testHandleAllUncaughtException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error occurred");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testHandleNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Null pointer exception");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testHandleArithmeticException() {
        // Given
        ArithmeticException exception = new ArithmeticException("Division by zero");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testHandleIndexOutOfBoundsException() {
        // Given
        IndexOutOfBoundsException exception = new IndexOutOfBoundsException("Index out of bounds");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testHandleClassCastException() {
        // Given
        ClassCastException exception = new ClassCastException("Class cast exception");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testHandleSecurityException() {
        // Given
        SecurityException exception = new SecurityException("Security violation");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(exception, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(exception), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testHandleOutOfMemoryError() {
        // Given
        RuntimeException error = new RuntimeException("Out of memory");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(error, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(error), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testHandleStackOverflowError() {
        // Given
        RuntimeException error = new RuntimeException("Stack overflow");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAllUncaughtException(error, request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        
        verify(loggingService).logErrorWithContext(eq("UNEXPECTED_ERROR"), eq(error), any());
        verify(loggingService).logSystemEvent(eq("UNEXPECTED_ERROR"), eq("ERROR"), anyString(), any());
    }

    @Test
    void testResponseStructureConsistency() {
        // Given
        BusinessException exception = new BusinessException("Test error");
        
        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBusinessException(exception, request);
        
        // Then
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertNotNull(body.getMessage());
        assertNotNull(body.getPath());
        assertNotNull(body.getTraceId());
        assertNotNull(body.getRequestId());
        assertNotNull(body.getTimestamp());
    }

    @Test
    void testTraceIdGeneration() {
        // Given
        ResourceNotFoundException exception1 = new ResourceNotFoundException("Error 1");
        BusinessException exception2 = new BusinessException("Error 2");
        
        // When
        ResponseEntity<ApiResponse<Object>> response1 = exceptionHandler.handleResourceNotFoundException(exception1, request);
        ResponseEntity<ApiResponse<Object>> response2 = exceptionHandler.handleBusinessException(exception2, request);
        
        // Then
        assertNotNull(response1.getBody().getTraceId());
        assertNotNull(response2.getBody().getTraceId());
        // Trace IDs should be different for different requests
        assertNotEquals(response1.getBody().getTraceId(), response2.getBody().getTraceId());
    }

    @Test
    void testRequestIdGeneration() {
        // Given
        ResourceNotFoundException exception1 = new ResourceNotFoundException("Error 1");
        BusinessException exception2 = new BusinessException("Error 2");
        
        // When
        ResponseEntity<ApiResponse<Object>> response1 = exceptionHandler.handleResourceNotFoundException(exception1, request);
        ResponseEntity<ApiResponse<Object>> response2 = exceptionHandler.handleBusinessException(exception2, request);
        
        // Then
        assertNotNull(response1.getBody().getRequestId());
        assertNotNull(response2.getBody().getRequestId());
        // Request IDs should be different for different requests
        assertNotEquals(response1.getBody().getRequestId(), response2.getBody().getRequestId());
    }
}

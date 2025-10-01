package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.RefreshTokenRequest;
import com.ankurshala.backend.dto.auth.SigninRequest;
import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.GlobalExceptionHandler;
import com.ankurshala.backend.service.AuthService;
import com.ankurshala.backend.service.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit tests for AuthController
 * Tests all endpoints with various scenarios including edge cases
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        // Inject LoggingService into GlobalExceptionHandler
        try {
            java.lang.reflect.Field loggingServiceField = GlobalExceptionHandler.class.getDeclaredField("loggingService");
            loggingServiceField.setAccessible(true);
            loggingServiceField.set(globalExceptionHandler, loggingService);
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSignupStudent_Success() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("john.doe@example.com");
        signupRequest.setPassword("Password123!");

        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        AuthResponse authResponse = new AuthResponse(
                "access-token", "refresh-token", 1L, "John Doe", "john.doe@example.com", Role.STUDENT
        );

        when(authService.signupStudent(any(SignupRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student registered successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.name").value("John Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));

        verify(authService).signupStudent(any(SignupRequest.class));
        verify(loggingService).logBusinessOperationStart(eq("STUDENT_SIGNUP"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("STUDENT_SIGNUP"), eq("1"), eq(true), anyLong());
        verify(loggingService).logAuthenticationEvent(eq("SIGNUP_SUCCESS"), eq("1"), eq("john.doe@example.com"), eq(true), isNull());
    }

    @Test
    void testSignupStudent_ValidationError() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName(""); // Invalid: empty name
        signupRequest.setEmail("invalid-email"); // Invalid: malformed email
        signupRequest.setPassword("123"); // Invalid: too short password

        // When & Then
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signupStudent(any(SignupRequest.class));
    }

    @Test
    void testSignupStudent_EmailAlreadyExists() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("existing@example.com");
        signupRequest.setPassword("Password123!");

        when(authService.signupStudent(any(SignupRequest.class)))
                .thenThrow(new BusinessException("Email is already in use!"));

        // When & Then
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).signupStudent(any(SignupRequest.class));
        verify(loggingService).logError(eq("STUDENT_SIGNUP"), any(Exception.class), any());
    }

    @Test
    void testSignupTeacher_Success() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Jane Smith");
        signupRequest.setEmail("jane.smith@example.com");
        signupRequest.setPassword("Password123!");

        AuthResponse authResponse = new AuthResponse(
                "access-token", "refresh-token", 2L, "Jane Smith", "jane.smith@example.com", Role.TEACHER
        );

        when(authService.signupTeacher(any(SignupRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/signup/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher registered successfully"))
                .andExpect(jsonPath("$.data.role").value("TEACHER"));

        verify(authService).signupTeacher(any(SignupRequest.class));
        verify(loggingService).logBusinessOperationStart(eq("TEACHER_SIGNUP"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("TEACHER_SIGNUP"), eq("2"), eq(true), anyLong());
    }

    @Test
    void testSignin_Success() throws Exception {
        // Given
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setEmail("john.doe@example.com");
        signinRequest.setPassword("password123");

        AuthResponse authResponse = new AuthResponse(
                "access-token", "refresh-token", 1L, "John Doe", "john.doe@example.com", Role.STUDENT
        );

        when(authService.signin(any(SigninRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(authService).signin(any(SigninRequest.class));
        verify(loggingService).logBusinessOperationStart(eq("SIGNIN"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("SIGNIN"), eq("1"), eq(true), anyLong());
        verify(loggingService).logAuthenticationEvent(eq("SIGNIN_SUCCESS"), eq("1"), eq("john.doe@example.com"), eq(true), isNull());
    }

    @Test
    void testSignin_InvalidCredentials() throws Exception {
        // Given
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setEmail("john.doe@example.com");
        signinRequest.setPassword("wrongpassword");

        when(authService.signin(any(SigninRequest.class)))
                .thenThrow(new BusinessException("Invalid email or password"));

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).signin(any(SigninRequest.class));
        verify(loggingService).logAuthenticationEvent(eq("SIGNIN_FAILED"), isNull(), eq("john.doe@example.com"), eq(false), anyString());
        verify(loggingService).logError(eq("SIGNIN"), any(Exception.class), any());
    }

    @Test
    void testSignin_ValidationError() throws Exception {
        // Given
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setEmail(""); // Invalid: empty email
        signinRequest.setPassword(""); // Invalid: empty password

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signin(any(SigninRequest.class));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");

        AuthResponse authResponse = new AuthResponse(
                "new-access-token", "new-refresh-token", 1L, "John Doe", "john.doe@example.com", Role.STUDENT
        );

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
        verify(loggingService).logBusinessOperationStart(eq("TOKEN_REFRESH"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("TOKEN_REFRESH"), eq("1"), eq(true), anyLong());
    }

    @Test
    void testRefreshToken_InvalidToken() throws Exception {
        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("invalid-refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new BusinessException("Invalid refresh token"));

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
        verify(loggingService).logAuthenticationEvent(eq("TOKEN_REFRESH_FAILED"), isNull(), isNull(), eq(false), anyString());
        verify(loggingService).logError(eq("TOKEN_REFRESH"), any(Exception.class), any());
    }

    @Test
    void testLogout_Success() throws Exception {
        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");

        doNothing().when(authService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authService).logout(anyString());
        verify(loggingService).logBusinessOperationStart(eq("LOGOUT"), isNull(), any());
        verify(loggingService).logBusinessOperationComplete(eq("LOGOUT"), isNull(), eq(true), anyLong());
        verify(loggingService).logAuthenticationEvent(eq("LOGOUT_SUCCESS"), isNull(), isNull(), eq(true), isNull());
    }

    @Test
    void testLogout_ServiceException() throws Exception {
        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("invalid-refresh-token");

        doThrow(new BusinessException("Logout failed")).when(authService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).logout(anyString());
        verify(loggingService).logError(eq("LOGOUT"), any(Exception.class), any());
    }

    @Test
    void testTestEndpoint_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Test endpoint successful"))
                .andExpect(jsonPath("$.data").value("AuthController is working"));

        verify(loggingService).logSystemEvent(eq("AUTH_TEST_ENDPOINT"), eq("INFO"), anyString(), any());
    }

    @Test
    void testSignupStudent_MissingRequiredFields() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        // Missing all required fields

        // When & Then
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signupStudent(any(SignupRequest.class));
    }

    @Test
    void testSignupStudent_EmailTooLong() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("a".repeat(300) + "@example.com"); // Too long email
        signupRequest.setPassword("Password123!");

        // When & Then
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).signupStudent(any(SignupRequest.class));
    }

    @Test
    void testSignin_UserDisabled() throws Exception {
        // Given
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setEmail("disabled@example.com");
        signinRequest.setPassword("password123");

        when(authService.signin(any(SigninRequest.class)))
                .thenThrow(new BusinessException("User account is disabled"));

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).signin(any(SigninRequest.class));
        verify(loggingService).logAuthenticationEvent(eq("SIGNIN_FAILED"), isNull(), eq("disabled@example.com"), eq(false), anyString());
    }

    @Test
    void testRefreshToken_ExpiredToken() throws Exception {
        // Given
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("expired-refresh-token");

        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new BusinessException("Refresh token has expired"));

        // When & Then
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).refreshToken(any(RefreshTokenRequest.class));
        verify(loggingService).logAuthenticationEvent(eq("TOKEN_REFRESH_FAILED"), isNull(), isNull(), eq(false), anyString());
    }

    @Test
    void testSignupStudent_ServiceException() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Doe");
        signupRequest.setEmail("john.doe@example.com");
        signupRequest.setPassword("Password123!");

        when(authService.signupStudent(any(SignupRequest.class)))
                .thenThrow(new BusinessException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).signupStudent(any(SignupRequest.class));
        verify(loggingService).logError(eq("STUDENT_SIGNUP"), any(Exception.class), any());
    }

    @Test
    void testSignupTeacher_ServiceException() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Jane Smith");
        signupRequest.setEmail("jane.smith@example.com");
        signupRequest.setPassword("Password123!");

        when(authService.signupTeacher(any(SignupRequest.class)))
                .thenThrow(new BusinessException("Service unavailable"));

        // When & Then
        mockMvc.perform(post("/auth/signup/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).signupTeacher(any(SignupRequest.class));
        verify(loggingService).logError(eq("TEACHER_SIGNUP"), any(Exception.class), any());
    }
}

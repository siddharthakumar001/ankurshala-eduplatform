package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.RefreshTokenRequest;
import com.ankurshala.backend.dto.auth.SigninRequest;
import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.JwtTokenProvider;
import com.ankurshala.backend.util.TraceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TeacherService teacherService;

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignupRequest signupRequest;
    private SigninRequest signinRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.STUDENT);
        testUser.setEnabled(true);

        signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        signinRequest = new SigninRequest();
        signinRequest.setEmail("test@example.com");
        signinRequest.setPassword("password123");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("validRefreshToken");
    }

    @Test
    @DisplayName("Should signup student successfully")
    void testSignupStudent_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(testUser)).thenReturn("refreshToken");
            when(studentProfileService.createStudentProfile(testUser, testUser.getName())).thenReturn(new StudentProfile());

            // When
            AuthResponse response = authService.signupStudent(signupRequest);

            // Then
            assertNotNull(response);
            assertEquals("accessToken", response.getAccessToken());
            assertEquals("refreshToken", response.getRefreshToken());
            assertEquals(testUser.getId(), response.getUserId());
            assertEquals(testUser.getName(), response.getName());
            assertEquals(testUser.getEmail(), response.getEmail());
            assertEquals(testUser.getRole(), response.getRole());

            verify(userRepository).existsByEmail(signupRequest.getEmail());
            verify(passwordEncoder).encode(signupRequest.getPassword());
            verify(userRepository).save(any(User.class));
            verify(studentProfileService).createStudentProfile(testUser, testUser.getName());
            verify(jwtTokenProvider).generateAccessToken(testUser);
            verify(jwtTokenProvider).generateRefreshToken(testUser);
        }
    }

    @Test
    @DisplayName("Should throw exception when student signup with existing email")
    void testSignupStudent_UserExists() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.signupStudent(signupRequest);
            });

            assertEquals("User with this email already exists", exception.getMessage());
            verify(userRepository).existsByEmail(signupRequest.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    @DisplayName("Should signup teacher successfully")
    void testSignupTeacher_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            testUser.setRole(Role.TEACHER);
            when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(testUser)).thenReturn("refreshToken");
            when(teacherService.createTeacher(testUser)).thenReturn(new Teacher());

            // When
            AuthResponse response = authService.signupTeacher(signupRequest);

            // Then
            assertNotNull(response);
            assertEquals("accessToken", response.getAccessToken());
            assertEquals("refreshToken", response.getRefreshToken());
            assertEquals(testUser.getId(), response.getUserId());
            assertEquals(testUser.getName(), response.getName());
            assertEquals(testUser.getEmail(), response.getEmail());
            assertEquals(Role.TEACHER, response.getRole());

            verify(userRepository).existsByEmail(signupRequest.getEmail());
            verify(passwordEncoder).encode(signupRequest.getPassword());
            verify(userRepository).save(any(User.class));
            verify(teacherService).createTeacher(testUser);
            verify(jwtTokenProvider).generateAccessToken(testUser);
            verify(jwtTokenProvider).generateRefreshToken(testUser);
        }
    }

    @Test
    @DisplayName("Should signin successfully")
    void testSignin_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByEmail(signinRequest.getEmail())).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("accessToken");
            when(jwtTokenProvider.generateRefreshToken(testUser)).thenReturn("refreshToken");

            // When
            AuthResponse response = authService.signin(signinRequest);

            // Then
            assertNotNull(response);
            assertEquals("accessToken", response.getAccessToken());
            assertEquals("refreshToken", response.getRefreshToken());
            assertEquals(testUser.getId(), response.getUserId());
            assertEquals(testUser.getName(), response.getName());
            assertEquals(testUser.getEmail(), response.getEmail());
            assertEquals(testUser.getRole(), response.getRole());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail(signinRequest.getEmail());
            verify(jwtTokenProvider).generateAccessToken(testUser);
            verify(jwtTokenProvider).generateRefreshToken(testUser);
        }
    }

    @Test
    @DisplayName("Should throw exception when signin with bad credentials")
    void testSignin_BadCredentials() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.signin(signinRequest);
            });

            assertEquals("Invalid email or password", exception.getMessage());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Test
    @DisplayName("Should throw exception when signin with inactive user")
    void testSignin_InactiveUser() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            testUser.setEnabled(false);
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByEmail(signinRequest.getEmail())).thenReturn(Optional.of(testUser));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.signin(signinRequest);
            });

            assertEquals("User account is inactive", exception.getMessage());
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail(signinRequest.getEmail());
        }
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshToken_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            when(jwtTokenProvider.validateToken(refreshTokenRequest.getRefreshToken())).thenReturn(true);
            when(jwtTokenProvider.getEmailFromToken(refreshTokenRequest.getRefreshToken())).thenReturn(testUser.getEmail());
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("newAccessToken");
            when(jwtTokenProvider.generateRefreshToken(testUser)).thenReturn("newRefreshToken");

            // When
            AuthResponse response = authService.refreshToken(refreshTokenRequest);

            // Then
            assertNotNull(response);
            assertEquals("newAccessToken", response.getAccessToken());
            assertEquals("newRefreshToken", response.getRefreshToken());
            assertEquals(testUser.getId(), response.getUserId());
            assertEquals(testUser.getName(), response.getName());
            assertEquals(testUser.getEmail(), response.getEmail());
            assertEquals(testUser.getRole(), response.getRole());

            verify(jwtTokenProvider).validateToken(refreshTokenRequest.getRefreshToken());
            verify(jwtTokenProvider).getEmailFromToken(refreshTokenRequest.getRefreshToken());
            verify(userRepository).findByEmail(testUser.getEmail());
            verify(jwtTokenProvider).generateAccessToken(testUser);
            verify(jwtTokenProvider).generateRefreshToken(testUser);
        }
    }

    @Test
    @DisplayName("Should throw exception when refresh token is invalid")
    void testRefreshToken_InvalidToken() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            when(jwtTokenProvider.validateToken(refreshTokenRequest.getRefreshToken())).thenReturn(false);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.refreshToken(refreshTokenRequest);
            });

            assertEquals("Invalid refresh token", exception.getMessage());
            verify(jwtTokenProvider).validateToken(refreshTokenRequest.getRefreshToken());
            verify(userRepository, never()).findByEmail(anyString());
        }
    }

    @Test
    @DisplayName("Should logout successfully")
    void testLogout_Success() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            String refreshToken = "validRefreshToken";
            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtTokenProvider.getEmailFromToken(refreshToken)).thenReturn(testUser.getEmail());

            // When
            assertDoesNotThrow(() -> {
                authService.logout(refreshToken);
            });

            // Then
            verify(jwtTokenProvider).validateToken(refreshToken);
            verify(jwtTokenProvider).getEmailFromToken(refreshToken);
        }
    }

    @Test
    @DisplayName("Should handle logout with invalid token gracefully")
    void testLogout_InvalidToken() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");
            String refreshToken = "invalidRefreshToken";
            when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

            // When
            assertDoesNotThrow(() -> {
                authService.logout(refreshToken);
            });

            // Then
            verify(jwtTokenProvider).validateToken(refreshToken);
            verify(jwtTokenProvider, never()).getEmailFromToken(anyString());
        }
    }

    @Test
    @DisplayName("Should handle logout with null token gracefully")
    void testLogout_NullToken() {
        try (MockedStatic<TraceUtil> traceUtilMock = mockStatic(TraceUtil.class)) {
            // Given
            traceUtilMock.when(TraceUtil::getTraceId).thenReturn("test-trace-id");

            // When
            assertDoesNotThrow(() -> {
                authService.logout(null);
            });

            // Then
            verify(jwtTokenProvider, never()).validateToken(anyString());
        }
    }
}

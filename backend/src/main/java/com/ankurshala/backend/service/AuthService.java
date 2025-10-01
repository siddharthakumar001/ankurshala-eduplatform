package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.RefreshTokenRequest;
import com.ankurshala.backend.dto.auth.SigninRequest;
import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.JwtTokenProvider;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Authentication Service
 * Handles user authentication, registration, and token management
 * Provides comprehensive logging and error handling
 */
@Slf4j
@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Register a new student
     */
    public AuthResponse signupStudent(SignupRequest signupRequest) {
        String traceId = TraceUtil.getTraceId();
        log.info("Student signup started - TraceId: {}, Email: {}", traceId, signupRequest.getEmail());
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                log.warn("Student signup failed - user already exists - TraceId: {}, Email: {}", 
                        traceId, signupRequest.getEmail());
                throw new BusinessException("User with this email already exists", 
                        org.springframework.http.HttpStatus.CONFLICT, "USER_EXISTS");
            }

            // Create new user
            User user = new User();
            user.setName(signupRequest.getName());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setRole(Role.STUDENT);
            user.setEnabled(true);

            User savedUser = userRepository.save(user);
            log.info("Student user created - TraceId: {}, UserId: {}, Email: {}", 
                    traceId, savedUser.getId(), savedUser.getEmail());

            // Create student profile
            studentProfileService.createStudentProfile(savedUser, savedUser.getName());
            log.info("Student profile created - TraceId: {}, UserId: {}", 
                    traceId, savedUser.getId());

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
            String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

            AuthResponse response = new AuthResponse(
                    accessToken,
                    refreshToken,
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

            log.info("Student signup completed successfully - TraceId: {}, UserId: {}", 
                    traceId, savedUser.getId());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Student signup failed with unexpected error - TraceId: {}, Email: {}, Error: {}", 
                    traceId, signupRequest.getEmail(), e.getMessage(), e);
            throw new BusinessException("Failed to register student", e);
        }
    }

    /**
     * Register a new teacher
     */
    public AuthResponse signupTeacher(SignupRequest signupRequest) {
        String traceId = TraceUtil.getTraceId();
        log.info("Teacher signup started - TraceId: {}, Email: {}", traceId, signupRequest.getEmail());
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                log.warn("Teacher signup failed - user already exists - TraceId: {}, Email: {}", 
                        traceId, signupRequest.getEmail());
                throw new BusinessException("User with this email already exists", 
                        org.springframework.http.HttpStatus.CONFLICT, "USER_EXISTS");
            }

            // Create new user
            User user = new User();
            user.setName(signupRequest.getName());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            user.setRole(Role.TEACHER);
            user.setEnabled(true);

            User savedUser = userRepository.save(user);
            log.info("Teacher user created - TraceId: {}, UserId: {}, Email: {}", 
                    traceId, savedUser.getId(), savedUser.getEmail());

            // Create teacher profile
            teacherService.createTeacher(savedUser);
            log.info("Teacher profile created - TraceId: {}, UserId: {}", 
                    traceId, savedUser.getId());

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
            String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

            AuthResponse response = new AuthResponse(
                    accessToken,
                    refreshToken,
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

            log.info("Teacher signup completed successfully - TraceId: {}, UserId: {}", 
                    traceId, savedUser.getId());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Teacher signup failed with unexpected error - TraceId: {}, Email: {}, Error: {}", 
                    traceId, signupRequest.getEmail(), e.getMessage(), e);
            throw new BusinessException("Failed to register teacher", e);
        }
    }

    /**
     * Authenticate user and generate tokens
     */
    public AuthResponse signin(SigninRequest signinRequest) {
        String traceId = TraceUtil.getTraceId();
        log.info("Signin started - TraceId: {}, Email: {}", traceId, signinRequest.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signinRequest.getEmail(),
                            signinRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = userRepository.findByEmail(signinRequest.getEmail())
                    .orElseThrow(() -> new BusinessException("User not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

            if (!user.getEnabled()) {
                log.warn("Signin failed - user inactive - TraceId: {}, Email: {}", 
                        traceId, signinRequest.getEmail());
                throw new BusinessException("User account is inactive", 
                        org.springframework.http.HttpStatus.FORBIDDEN, "USER_INACTIVE");
            }

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            AuthResponse response = new AuthResponse(
                    accessToken,
                    refreshToken,
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            );

            log.info("Signin completed successfully - TraceId: {}, UserId: {}, Role: {}", 
                    traceId, user.getId(), user.getRole());
            return response;

        } catch (BadCredentialsException e) {
            log.warn("Signin failed - bad credentials - TraceId: {}, Email: {}", 
                    traceId, signinRequest.getEmail());
            throw new BusinessException("Invalid email or password", 
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Signin failed with unexpected error - TraceId: {}, Email: {}, Error: {}", 
                    traceId, signinRequest.getEmail(), e.getMessage(), e);
            throw new BusinessException("Authentication failed", e);
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String traceId = TraceUtil.getTraceId();
        log.info("Token refresh started - TraceId: {}", traceId);
        
        try {
            String refreshToken = refreshTokenRequest.getRefreshToken();
            
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                log.warn("Token refresh failed - invalid refresh token - TraceId: {}", traceId);
                throw new BusinessException("Invalid refresh token", 
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
            }

            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("User not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

            if (!user.getEnabled()) {
                log.warn("Token refresh failed - user inactive - TraceId: {}, Email: {}", 
                        traceId, email);
                throw new BusinessException("User account is inactive", 
                        org.springframework.http.HttpStatus.FORBIDDEN, "USER_INACTIVE");
            }

            // Generate new tokens
            String newAccessToken = jwtTokenProvider.generateAccessToken(user);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

            AuthResponse response = new AuthResponse(
                    newAccessToken,
                    newRefreshToken,
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            );

            log.info("Token refresh completed successfully - TraceId: {}, UserId: {}", 
                    traceId, user.getId());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed with unexpected error - TraceId: {}, Error: {}", 
                    traceId, e.getMessage(), e);
            throw new BusinessException("Token refresh failed", e);
        }
    }

    /**
     * Logout user (invalidate refresh token)
     */
    public void logout(String refreshToken) {
        String traceId = TraceUtil.getTraceId();
        log.info("Logout started - TraceId: {}", traceId);
        
        try {
            // In a real implementation, you would invalidate the refresh token
            // For now, we'll just log the logout
            if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                String email = jwtTokenProvider.getEmailFromToken(refreshToken);
                log.info("User logged out successfully - TraceId: {}, Email: {}", traceId, email);
            } else {
                log.warn("Logout attempted with invalid refresh token - TraceId: {}", traceId);
            }
        } catch (Exception e) {
            log.error("Logout failed with unexpected error - TraceId: {}, Error: {}", 
                    traceId, e.getMessage(), e);
            // Don't throw exception for logout failures
        }
    }
}
package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.auth.*;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Enhanced Authentication Service
 * Handles strict student and teacher registration with mandatory personalization fields
 * Provides comprehensive logging and error handling
 */
@Slf4j
@Service
@Transactional
public class EnhancedAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EnhancedTeacherService enhancedTeacherService;

    @Autowired
    private EnhancedStudentService enhancedStudentService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Register a new student with strict validation
     */
    public AuthResponse signupStudent(StudentSignupRequest signupRequest) {
        String traceId = TraceUtil.getTraceId();
        log.info("Enhanced student signup started - TraceId: {}, Email: {}", traceId, signupRequest.getEmail());
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                log.warn("Student signup failed - user already exists - TraceId: {}, Email: {}", 
                        traceId, signupRequest.getEmail());
                throw new BusinessException("User with this email already exists", 
                        org.springframework.http.HttpStatus.CONFLICT, "USER_EXISTS");
            }

            // Validate age (must be between 5 and 25)
            LocalDate dob = signupRequest.getDob();
            LocalDate now = LocalDate.now();
            int age = now.getYear() - dob.getYear();
            if (age < 5 || age > 25) {
                throw new BusinessException("Student age must be between 5 and 25 years", 
                        org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_AGE");
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

            // Create enhanced student profile with all mandatory fields
            enhancedStudentService.createStudentProfile(savedUser, signupRequest);
            log.info("Enhanced student profile created - TraceId: {}, UserId: {}", 
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

            log.info("Enhanced student signup completed successfully - TraceId: {}, UserId: {}", 
                    traceId, savedUser.getId());
            return response;

        } catch (BusinessException e) {
            log.error("Enhanced student signup failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Enhanced student signup failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Student registration failed", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "SIGNUP_FAILED");
        }
    }

    /**
     * Register a new teacher with strict validation
     */
    public AuthResponse signupTeacher(TeacherSignupRequest signupRequest) {
        String traceId = TraceUtil.getTraceId();
        log.info("Enhanced teacher signup started - TraceId: {}, Email: {}", traceId, signupRequest.getEmail());
        
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(signupRequest.getEmail())) {
                log.warn("Teacher signup failed - user already exists - TraceId: {}, Email: {}", 
                        traceId, signupRequest.getEmail());
                throw new BusinessException("User with this email already exists", 
                        org.springframework.http.HttpStatus.CONFLICT, "USER_EXISTS");
            }

            // Validate teacher categories
            List<String> categories = signupRequest.getCategories();
            for (String category : categories) {
                if (!category.equals("STANDARD") && !category.equals("PREMIUM")) {
                    throw new BusinessException("Invalid teacher category: " + category, 
                            org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_CATEGORY");
                }
            }

            // Validate availability slots
            validateAvailabilitySlots(signupRequest.getAvailability());

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

            // Create enhanced teacher profile with all mandatory fields
            enhancedTeacherService.createTeacherProfile(savedUser, signupRequest);
            log.info("Enhanced teacher profile created - TraceId: {}, UserId: {}", 
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

            log.info("Enhanced teacher signup completed successfully - TraceId: {}, UserId: {}", 
                    traceId, savedUser.getId());
            return response;

        } catch (BusinessException e) {
            log.error("Enhanced teacher signup failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Enhanced teacher signup failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Teacher registration failed", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "SIGNUP_FAILED");
        }
    }

    /**
     * Validate availability slots for overlap and time constraints
     */
    private void validateAvailabilitySlots(List<TeacherSignupRequest.AvailabilitySlotDto> availability) {
        for (TeacherSignupRequest.AvailabilitySlotDto slot : availability) {
            // Parse times
            String[] startParts = slot.getStartTime().split(":");
            String[] endParts = slot.getEndTime().split(":");
            
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);
            
            int startMinutes = startHour * 60 + startMinute;
            int endMinutes = endHour * 60 + endMinute;
            
            // Validate slot duration (minimum 1 hour)
            if (endMinutes - startMinutes < 60) {
                throw new BusinessException("Availability slot must be at least 1 hour long", 
                        org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_SLOT_DURATION");
            }
            
            // Validate time range (6 AM to 11 PM)
            if (startMinutes < 360 || endMinutes > 1380) {
                throw new BusinessException("Availability slots must be between 6:00 AM and 11:00 PM", 
                        org.springframework.http.HttpStatus.BAD_REQUEST, "INVALID_TIME_RANGE");
            }
        }
        
        // Check for overlapping slots on the same weekday
        for (int i = 0; i < availability.size(); i++) {
            for (int j = i + 1; j < availability.size(); j++) {
                TeacherSignupRequest.AvailabilitySlotDto slot1 = availability.get(i);
                TeacherSignupRequest.AvailabilitySlotDto slot2 = availability.get(j);
                
                if (slot1.getWeekday().equals(slot2.getWeekday())) {
                    if (isOverlapping(slot1, slot2)) {
                        throw new BusinessException("Overlapping availability slots found for weekday " + slot1.getWeekday(), 
                                org.springframework.http.HttpStatus.BAD_REQUEST, "OVERLAPPING_SLOTS");
                    }
                }
            }
        }
    }

    /**
     * Check if two availability slots overlap
     */
    private boolean isOverlapping(TeacherSignupRequest.AvailabilitySlotDto slot1, 
                                 TeacherSignupRequest.AvailabilitySlotDto slot2) {
        String[] start1Parts = slot1.getStartTime().split(":");
        String[] end1Parts = slot1.getEndTime().split(":");
        String[] start2Parts = slot2.getStartTime().split(":");
        String[] end2Parts = slot2.getEndTime().split(":");
        
        int start1Minutes = Integer.parseInt(start1Parts[0]) * 60 + Integer.parseInt(start1Parts[1]);
        int end1Minutes = Integer.parseInt(end1Parts[0]) * 60 + Integer.parseInt(end1Parts[1]);
        int start2Minutes = Integer.parseInt(start2Parts[0]) * 60 + Integer.parseInt(start2Parts[1]);
        int end2Minutes = Integer.parseInt(end2Parts[0]) * 60 + Integer.parseInt(end2Parts[1]);
        
        return !(end1Minutes <= start2Minutes || end2Minutes <= start1Minutes);
    }

    /**
     * Sign in user (existing implementation)
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
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Signin failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Signin failed", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "SIGNIN_FAILED");
        }
    }

    /**
     * Refresh token (existing implementation)
     */
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String traceId = TraceUtil.getTraceId();
        log.info("Token refresh started - TraceId: {}", traceId);
        
        try {
            // Implementation for token refresh
            // This would validate the refresh token and generate new access token
            throw new BusinessException("Token refresh not implemented yet", 
                    org.springframework.http.HttpStatus.NOT_IMPLEMENTED, "NOT_IMPLEMENTED");
        } catch (Exception e) {
            log.error("Token refresh failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Token refresh failed", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "REFRESH_FAILED");
        }
    }

    /**
     * Logout user (existing implementation)
     */
    public void logout(String refreshToken) {
        String traceId = TraceUtil.getTraceId();
        log.info("Logout started - TraceId: {}", traceId);
        
        try {
            // Implementation for logout
            // This would invalidate the refresh token
            log.info("Logout completed successfully - TraceId: {}", traceId);
        } catch (Exception e) {
            log.error("Logout failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Logout failed", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "LOGOUT_FAILED");
        }
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.RefreshTokenRequest;
import com.ankurshala.backend.dto.auth.SigninRequest;
import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.entity.RefreshToken;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.RefreshTokenRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.JwtTokenProvider;
import com.ankurshala.backend.exception.DuplicateResourceException;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private StudentProfileService studentProfileService;

    @Autowired
    private TeacherService teacherService;

    public AuthResponse signupStudent(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateResourceException("Email is already in use!");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // Create student profile
        studentProfileService.createStudentProfile(savedUser, signupRequest.getName());

        return generateAuthResponse(savedUser);
    }

    public AuthResponse signupTeacher(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateResourceException("Email is already in use!");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(Role.TEACHER);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // Create teacher and teacher profile
        teacherService.createTeacher(savedUser);

        return generateAuthResponse(savedUser);
    }

    public AuthResponse signin(SigninRequest signinRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signinRequest.getEmail(), signinRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(signinRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify refresh token exists and is not revoked
        Optional<RefreshToken> tokenEntity = refreshTokenRepository.findByTokenHash(
                String.valueOf(refreshToken.hashCode()));
        
        if (tokenEntity.isEmpty() || tokenEntity.get().isRevoked() || tokenEntity.get().isExpired()) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Revoke old refresh token
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

        return generateAuthResponse(user);
    }

    public void logout(String refreshToken) {
        if (jwtTokenProvider.validateToken(refreshToken) && jwtTokenProvider.isRefreshToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId).orElse(null);
            
            if (user != null) {
                refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());
            }
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Store refresh token hash in database
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setTokenHash(String.valueOf(refreshToken.hashCode()));
        refreshTokenEntity.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}

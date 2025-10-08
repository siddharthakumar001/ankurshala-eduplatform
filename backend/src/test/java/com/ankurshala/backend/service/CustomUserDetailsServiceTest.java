package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.STUDENT);
        testUser.setEnabled(true);
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void testLoadUserByUsernameSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        var userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("1", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLoadUserByUsernameNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent@example.com");
        });

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should handle disabled user")
    void testLoadUserByUsernameDisabledUser() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        var userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should handle different user roles")
    void testLoadUserByUsernameDifferentRoles() {
        // Arrange
        testUser.setRole(Role.TEACHER);
        when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(testUser));

        // Act
        var userDetails = customUserDetailsService.loadUserByUsername("teacher@example.com");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER")));
        verify(userRepository).findByEmail("teacher@example.com");
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceAuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ResourceAuthorizationService resourceAuthorizationService;

    private UserPrincipal userPrincipal;
    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword("password");
        testUser.setRole(Role.STUDENT);
        testUser.setEnabled(true);
        
        userPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    void testIsOwner_WhenUserIsOwner_ShouldReturnTrue() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        boolean result = resourceAuthorizationService.isOwner(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsOwner_WhenUserIsNotOwner_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        boolean result = resourceAuthorizationService.isOwner(2L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOwner_WhenNotAuthenticated_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        boolean result = resourceAuthorizationService.isOwner(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOwner_WhenAuthenticationIsNull_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        boolean result = resourceAuthorizationService.isOwner(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOwner_WhenExceptionOccurs_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Test exception"));

        // Act
        boolean result = resourceAuthorizationService.isOwner(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOwnerWithRole_WhenUserIsOwnerAndHasRole_ShouldReturnTrue() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authentication.getAuthorities()).thenAnswer(invocation -> 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        // Act
        boolean result = resourceAuthorizationService.isOwnerWithRole(1L, "STUDENT");

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsOwnerWithRole_WhenUserIsOwnerButDoesNotHaveRole_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authentication.getAuthorities()).thenAnswer(invocation -> 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        // Act
        boolean result = resourceAuthorizationService.isOwnerWithRole(1L, "TEACHER");

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsOwnerWithRole_WhenUserIsNotOwner_ShouldReturnFalse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        boolean result = resourceAuthorizationService.isOwnerWithRole(2L, "STUDENT");

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsAdminOrOwner_WhenUserIsAdmin_ShouldReturnTrue() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenAnswer(invocation -> 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // Act
        boolean result = resourceAuthorizationService.isAdminOrOwner(2L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsAdminOrOwner_WhenUserIsOwner_ShouldReturnTrue() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authentication.getAuthorities()).thenAnswer(invocation -> 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        // Act
        boolean result = resourceAuthorizationService.isAdminOrOwner(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testGetCurrentUserId_WhenAuthenticated_ShouldReturnUserId() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        Long result = resourceAuthorizationService.getCurrentUserId();

        // Assert
        assertEquals(1L, result);
    }

    @Test
    void testGetCurrentUserId_WhenNotAuthenticated_ShouldReturnNull() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        Long result = resourceAuthorizationService.getCurrentUserId();

        // Assert
        assertNull(result);
    }

    @Test
    void testIsValidUser_WhenUserExistsAndEnabled_ShouldReturnTrue() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = resourceAuthorizationService.isValidUser(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsValidUser_WhenUserExistsButDisabled_ShouldReturnFalse() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        boolean result = resourceAuthorizationService.isValidUser(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidUser_WhenUserDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = resourceAuthorizationService.isValidUser(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsValidUser_WhenExceptionOccurs_ShouldReturnFalse() {
        // Arrange
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = resourceAuthorizationService.isValidUser(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCanAccessStudentDocument_ShouldReturnOwnershipResult() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        boolean result = resourceAuthorizationService.canAccessStudentDocument(1L, 100L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanAccessTeacherResource_ShouldReturnOwnershipResult() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        boolean result = resourceAuthorizationService.canAccessTeacherResource(1L, 200L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanAccessStudentProfile_ShouldReturnOwnershipResult() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        boolean result = resourceAuthorizationService.canAccessStudentProfile(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCanAccessTeacherProfile_ShouldReturnOwnershipResult() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // Act
        boolean result = resourceAuthorizationService.canAccessTeacherProfile(1L);

        // Assert
        assertTrue(result);
    }
}

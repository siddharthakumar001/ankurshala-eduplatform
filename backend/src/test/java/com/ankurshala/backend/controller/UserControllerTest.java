package com.ankurshala.backend.controller;

import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private UserController userController;
    private Authentication authentication;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userController = new UserController();

        // Setup user principal
        userPrincipal = new UserPrincipal(1L, "test@example.com", "Test User", "password", Role.STUDENT, true);

        // Setup authentication
        authentication = new TestingAuthenticationToken(userPrincipal, null);
        authentication.setAuthenticated(true);
    }

    @Test
    void testGetCurrentUser_Success() {
        // When
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(1L, responseBody.get("id"));
        assertEquals("test@example.com", responseBody.get("email"));
        assertEquals("Test User", responseBody.get("name"));
        assertEquals(Role.STUDENT, responseBody.get("role"));
        assertEquals(true, responseBody.get("enabled"));
    }

    @Test
    void testGetCurrentUser_TeacherRole() {
        // Given
        userPrincipal = new UserPrincipal(1L, "teacher@example.com", "Teacher User", "password", Role.TEACHER, true);
        authentication = new TestingAuthenticationToken(userPrincipal, null);
        authentication.setAuthenticated(true);

        // When
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(Role.TEACHER, responseBody.get("role"));
        assertEquals("Teacher User", responseBody.get("name"));
    }

    @Test
    void testGetCurrentUser_AdminRole() {
        // Given
        userPrincipal = new UserPrincipal(1L, "admin@example.com", "Admin User", "password", Role.ADMIN, true);
        authentication = new TestingAuthenticationToken(userPrincipal, null);
        authentication.setAuthenticated(true);

        // When
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(Role.ADMIN, responseBody.get("role"));
        assertEquals("Admin User", responseBody.get("name"));
    }

    @Test
    void testGetCurrentUser_DisabledUser() {
        // Given
        userPrincipal = new UserPrincipal(1L, "test@example.com", "Test User", "password", Role.STUDENT, false);
        authentication = new TestingAuthenticationToken(userPrincipal, null);
        authentication.setAuthenticated(true);

        // When
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(Role.STUDENT, responseBody.get("role"));
        assertEquals(false, responseBody.get("enabled"));
    }

    @Test
    void testGetCurrentUser_NullAuthentication() {
        // When
        ResponseEntity<?> response = userController.getCurrentUser(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Not authenticated", responseBody.get("error"));
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        // Given
        Authentication notAuthenticated = new TestingAuthenticationToken(null, null);
        notAuthenticated.setAuthenticated(false);

        // When
        ResponseEntity<?> response = userController.getCurrentUser(notAuthenticated);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Not authenticated", responseBody.get("error"));
    }

    @Test
    void testGetCurrentUser_NullPrincipal() {
        // Given
        Authentication authWithNullPrincipal = new TestingAuthenticationToken(null, null);

        // When
        ResponseEntity<?> response = userController.getCurrentUser(authWithNullPrincipal);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Not authenticated", responseBody.get("error"));
    }

    @Test
    void testGetCurrentUser_WithNullValues() {
        // Given
        userPrincipal = new UserPrincipal(1L, null, null, "password", Role.STUDENT, true);
        authentication = new TestingAuthenticationToken(userPrincipal, null);
        authentication.setAuthenticated(true);

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            userController.getCurrentUser(authentication);
        });
    }

    @Test
    void testGetCurrentUser_WithEmptyValues() {
        // Given
        userPrincipal = new UserPrincipal(1L, "", "", "password", Role.STUDENT, true);
        authentication = new TestingAuthenticationToken(userPrincipal, null);
        authentication.setAuthenticated(true);

        // When
        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(1L, responseBody.get("id"));
        assertEquals("", responseBody.get("email"));
        assertEquals("", responseBody.get("name"));
        assertEquals(Role.STUDENT, responseBody.get("role"));
        assertEquals(true, responseBody.get("enabled"));
    }
}

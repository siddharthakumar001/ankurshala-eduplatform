package com.ankurshala.backend.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtResponse Tests")
class JwtResponseTest {

    @Test
    @DisplayName("Should create JwtResponse with all-args constructor")
    void testAllArgsConstructor() {
        String accessToken = "access-token";
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        JwtResponse response = new JwtResponse(accessToken, id, username, email, firstName, lastName);

        assertEquals(accessToken, response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(id, response.getId());
        assertEquals(username, response.getUsername());
        assertEquals(email, response.getEmail());
        assertEquals(firstName, response.getFirstName());
        assertEquals(lastName, response.getLastName());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        String accessToken = "access-token";
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";

        JwtResponse response = new JwtResponse(accessToken, id, username, email, firstName, lastName);
        
        String newToken = "new-token";
        String newTokenType = "JWT";
        Long newId = 2L;
        String newUsername = "newuser";
        String newEmail = "new@example.com";
        String newFirstName = "Jane";
        String newLastName = "Smith";

        response.setAccessToken(newToken);
        response.setTokenType(newTokenType);
        response.setId(newId);
        response.setUsername(newUsername);
        response.setEmail(newEmail);
        response.setFirstName(newFirstName);
        response.setLastName(newLastName);

        assertEquals(newToken, response.getAccessToken());
        assertEquals(newTokenType, response.getTokenType());
        assertEquals(newId, response.getId());
        assertEquals(newUsername, response.getUsername());
        assertEquals(newEmail, response.getEmail());
        assertEquals(newFirstName, response.getFirstName());
        assertEquals(newLastName, response.getLastName());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        JwtResponse response = new JwtResponse("token", 1L, "user", "email", "first", "last");
        
        response.setAccessToken(null);
        response.setTokenType(null);
        response.setId(null);
        response.setUsername(null);
        response.setEmail(null);
        response.setFirstName(null);
        response.setLastName(null);

        assertNull(response.getAccessToken());
        assertNull(response.getTokenType());
        assertNull(response.getId());
        assertNull(response.getUsername());
        assertNull(response.getEmail());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
    }
}

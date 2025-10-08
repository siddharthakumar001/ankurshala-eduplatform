package com.ankurshala.backend.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoginRequest Tests")
class LoginRequestTest {

    @Test
    @DisplayName("Should create LoginRequest with no-args constructor")
    void testNoArgsConstructor() {
        LoginRequest request = new LoginRequest();
        assertNotNull(request);
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        LoginRequest request = new LoginRequest();
        String username = "testuser";
        String password = "password123";

        request.setUsername(username);
        request.setPassword(password);

        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        LoginRequest request = new LoginRequest();
        request.setUsername(null);
        request.setPassword(null);

        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }
}

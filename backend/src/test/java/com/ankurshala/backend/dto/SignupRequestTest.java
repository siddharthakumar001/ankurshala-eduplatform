package com.ankurshala.backend.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SignupRequest Tests")
class SignupRequestTest {

    @Test
    @DisplayName("Should create SignupRequest with no-args constructor")
    void testNoArgsConstructor() {
        SignupRequest request = new SignupRequest();
        assertNotNull(request);
        assertNull(request.getUsername());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        SignupRequest request = new SignupRequest();
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Doe";

        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);

        assertEquals(username, request.getUsername());
        assertEquals(email, request.getEmail());
        assertEquals(password, request.getPassword());
        assertEquals(firstName, request.getFirstName());
        assertEquals(lastName, request.getLastName());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        SignupRequest request = new SignupRequest();
        request.setUsername(null);
        request.setEmail(null);
        request.setPassword(null);
        request.setFirstName(null);
        request.setLastName(null);

        assertNull(request.getUsername());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
    }
}

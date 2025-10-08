package com.ankurshala.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create User with no-args constructor")
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
        assertTrue(user.getEnabled()); // Default value is true
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create User with all-args constructor")
    void testAllArgsConstructor() {
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";
        Role role = Role.STUDENT;

        User user = new User(name, email, password, role);

        assertNull(user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
        assertTrue(user.getEnabled());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        User user = new User();
        Long id = 1L;
        String name = "Jane Doe";
        String email = "jane@example.com";
        String password = "newpassword";
        Role role = Role.TEACHER;
        Boolean enabled = false;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setEnabled(enabled);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(role, user.getRole());
        assertEquals(enabled, user.getEnabled());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        User user = new User();
        user.setId(null);
        user.setName(null);
        user.setEmail(null);
        user.setPassword(null);
        user.setRole(null);
        user.setEnabled(null);
        user.setCreatedAt(null);
        user.setUpdatedAt(null);

        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
        assertNull(user.getEnabled());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }
}

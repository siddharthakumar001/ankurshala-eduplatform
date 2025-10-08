package com.ankurshala.backend.dto.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminProfileDto Tests")
class AdminProfileDtoTest {

    @Test
    @DisplayName("Should create AdminProfileDto with no-args constructor")
    void testNoArgsConstructor() {
        AdminProfileDto dto = new AdminProfileDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getPhoneNumber());
        assertFalse(dto.getIsSuperAdmin());
        assertNull(dto.getLastLogin());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        AdminProfileDto dto = new AdminProfileDto();
        Long id = 1L;
        String phoneNumber = "9876543210";
        Boolean isSuperAdmin = true;
        LocalDateTime lastLogin = LocalDateTime.now();

        dto.setId(id);
        dto.setPhoneNumber(phoneNumber);
        dto.setIsSuperAdmin(isSuperAdmin);
        dto.setLastLogin(lastLogin);

        assertEquals(id, dto.getId());
        assertEquals(phoneNumber, dto.getPhoneNumber());
        assertEquals(isSuperAdmin, dto.getIsSuperAdmin());
        assertEquals(lastLogin, dto.getLastLogin());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        AdminProfileDto dto = new AdminProfileDto();
        dto.setId(null);
        dto.setPhoneNumber(null);
        dto.setIsSuperAdmin(null);
        dto.setLastLogin(null);

        assertNull(dto.getId());
        assertNull(dto.getPhoneNumber());
        assertNull(dto.getIsSuperAdmin());
        assertNull(dto.getLastLogin());
    }

    @Test
    @DisplayName("Should handle default super admin value")
    void testDefaultSuperAdminValue() {
        AdminProfileDto dto = new AdminProfileDto();
        assertEquals(false, dto.getIsSuperAdmin());
    }
}

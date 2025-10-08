package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.AdminProfileDto;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.AdminProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProfileControllerTest {

    @Mock
    private AdminProfileService adminProfileService;

    @InjectMocks
    private AdminProfileController controller;

    private UserPrincipal userPrincipal;
    private Authentication authentication;
    private AdminProfileDto adminProfileDto;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "admin@example.com", "Admin Name", "password", Role.ADMIN, true);
        authentication = new TestingAuthenticationToken(userPrincipal, null);
        authentication.setAuthenticated(true);

        adminProfileDto = new AdminProfileDto();
        adminProfileDto.setId(1L);
        adminProfileDto.setPhoneNumber("+91-9876543210");
        adminProfileDto.setIsSuperAdmin(false);
        adminProfileDto.setLastLogin(java.time.LocalDateTime.now());
    }

    @Test
    void testGetProfile_Success() {
        // Mock service
        when(adminProfileService.getAdminProfile(anyLong())).thenReturn(adminProfileDto);

        // Execute
        ResponseEntity<AdminProfileDto> response = controller.getProfile(authentication);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adminProfileDto, response.getBody());
        verify(adminProfileService).getAdminProfile(1L);
    }

    @Test
    void testUpdateProfile_Success() {
        // Setup
        AdminProfileDto updatedProfile = new AdminProfileDto();
        updatedProfile.setId(1L);
        updatedProfile.setPhoneNumber("+91-9876543211");
        updatedProfile.setIsSuperAdmin(true);
        updatedProfile.setLastLogin(java.time.LocalDateTime.now());

        // Mock service
        when(adminProfileService.updateAdminProfile(anyLong(), any(AdminProfileDto.class)))
                .thenReturn(updatedProfile);

        // Execute
        ResponseEntity<AdminProfileDto> response = controller.updateProfile(adminProfileDto, authentication);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProfile, response.getBody());
        verify(adminProfileService).updateAdminProfile(1L, adminProfileDto);
    }

    @Test
    void testGetUserIdFromAuthentication_Success() {
        // This is a private method, but we can test it indirectly through the public methods
        // The method is called by getProfile and updateProfile methods
        
        // Mock service
        when(adminProfileService.getAdminProfile(anyLong())).thenReturn(adminProfileDto);

        // Execute
        ResponseEntity<AdminProfileDto> response = controller.getProfile(authentication);

        // Verify that the correct user ID (1L) was passed to the service
        verify(adminProfileService).getAdminProfile(1L);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetProfile_WithDifferentUser() {
        // Setup
        UserPrincipal differentUser = new UserPrincipal(2L, "admin2@example.com", "Admin 2", "password", Role.ADMIN, true);
        Authentication differentAuth = new TestingAuthenticationToken(differentUser, null);
        differentAuth.setAuthenticated(true);

        // Mock service
        when(adminProfileService.getAdminProfile(anyLong())).thenReturn(adminProfileDto);

        // Execute
        ResponseEntity<AdminProfileDto> response = controller.getProfile(differentAuth);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminProfileService).getAdminProfile(2L); // Should use the different user's ID
    }

    @Test
    void testUpdateProfile_WithDifferentUser() {
        // Setup
        UserPrincipal differentUser = new UserPrincipal(2L, "admin2@example.com", "Admin 2", "password", Role.ADMIN, true);
        Authentication differentAuth = new TestingAuthenticationToken(differentUser, null);
        differentAuth.setAuthenticated(true);

        AdminProfileDto updatedProfile = new AdminProfileDto();
        updatedProfile.setId(2L);
        updatedProfile.setPhoneNumber("+91-9876543212");
        updatedProfile.setIsSuperAdmin(false);

        // Mock service
        when(adminProfileService.updateAdminProfile(anyLong(), any(AdminProfileDto.class)))
                .thenReturn(updatedProfile);

        // Execute
        ResponseEntity<AdminProfileDto> response = controller.updateProfile(adminProfileDto, differentAuth);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminProfileService).updateAdminProfile(2L, adminProfileDto); // Should use the different user's ID
    }
}

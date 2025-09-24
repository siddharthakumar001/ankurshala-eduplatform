package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.AdminProfileDto;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.AdminProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    @Autowired
    private AdminProfileService adminProfileService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfileDto> getProfile(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        AdminProfileDto profile = adminProfileService.getAdminProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminProfileDto> updateProfile(
            @Valid @RequestBody AdminProfileDto profileDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        AdminProfileDto updatedProfile = adminProfileService.updateAdminProfile(userId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}

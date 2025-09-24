package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.AdminProfileDto;
import com.ankurshala.backend.entity.AdminProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.AdminProfileRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AdminProfileService {

    @Autowired
    private AdminProfileRepository adminProfileRepository;
    
    @Autowired
    private UserRepository userRepository;

    public AdminProfileDto getAdminProfile(Long userId) {
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Admin profile not found"));
        return convertToDto(profile);
    }

    public AdminProfileDto updateAdminProfile(Long userId, AdminProfileDto profileDto) {
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Admin profile not found"));
        
        updateProfileFromDto(profile, profileDto);
        AdminProfile updatedProfile = adminProfileRepository.save(profile);
        return convertToDto(updatedProfile);
    }

    public AdminProfile createAdminProfile(User user) {
        AdminProfile profile = new AdminProfile();
        profile.setUser(user);
        profile.setIsSuperAdmin(false);
        return adminProfileRepository.save(profile);
    }

    public void updateLastLogin(Long userId) {
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Admin profile not found"));
        profile.setLastLogin(LocalDateTime.now());
        adminProfileRepository.save(profile);
    }

    // DTO Conversion methods
    private AdminProfileDto convertToDto(AdminProfile profile) {
        AdminProfileDto dto = new AdminProfileDto();
        dto.setId(profile.getId());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setIsSuperAdmin(profile.getIsSuperAdmin());
        dto.setLastLogin(profile.getLastLogin());
        return dto;
    }

    private void updateProfileFromDto(AdminProfile profile, AdminProfileDto dto) {
        profile.setPhoneNumber(dto.getPhoneNumber());
        // Note: isSuperAdmin is typically not updated by user, only by super admin
        // profile.setIsSuperAdmin(dto.getIsSuperAdmin());
    }
}

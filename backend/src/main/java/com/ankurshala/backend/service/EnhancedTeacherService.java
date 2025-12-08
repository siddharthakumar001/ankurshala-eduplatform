package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.auth.TeacherSignupRequest;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Enhanced Teacher Service
 * Handles comprehensive teacher profile management with mandatory professional fields
 */
@Slf4j
@Service
@Transactional
public class EnhancedTeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    /**
     * Create enhanced teacher profile with all mandatory fields
     */
    public Teacher createTeacherProfile(User user, TeacherSignupRequest signupRequest) {
        log.info("Creating enhanced teacher profile for user ID: {}, email: {}", user.getId(), user.getEmail());
        
        // Create teacher entity
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setName(signupRequest.getName());
        teacher.setEmail(signupRequest.getEmail());
        teacher.setBio(signupRequest.getBio());
        teacher.setYearsExperience(signupRequest.getYearsExperience());
        teacher.setLanguagesArray(signupRequest.getLanguages().toArray(new String[0]));
        teacher.setCategoriesArray(signupRequest.getCategories().toArray(new String[0]));
        teacher.setHourlyRate(signupRequest.getHourlyRate());
        teacher.setStatus(com.ankurshala.backend.entity.TeacherStatus.PENDING);
        
        // Set optional fields
        if (signupRequest.getPhoneNumber() != null) {
            teacher.setPhoneNumber(signupRequest.getPhoneNumber());
        }
        if (signupRequest.getLinkedinProfile() != null) {
            teacher.setLinkedinProfile(signupRequest.getLinkedinProfile());
        }
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Enhanced teacher created with ID: {} for user ID: {}", savedTeacher.getId(), user.getId());
        
        // Create teacher profile
        TeacherProfile profile = new TeacherProfile();
        profile.setUser(user);
        profile.setTeacher(savedTeacher);
        profile.setBio(signupRequest.getBio());
        profile.setYearsExperience(signupRequest.getYearsExperience());
        profile.setHourlyRate(signupRequest.getHourlyRate());
        
        TeacherProfile savedProfile = teacherProfileRepository.save(profile);
        log.info("Enhanced teacher profile created with ID: {} for user ID: {}", savedProfile.getId(), user.getId());
        
        // TODO: Create teacher availability slots
        // TODO: Create teacher subject mappings
        // TODO: Create teacher wallet if wallet feature is enabled
        
        return savedTeacher;
    }

    /**
     * Get teacher by user ID
     */
    public Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher not found for user ID: " + userId));
    }

    /**
     * Get teacher profile by user ID
     */
    public TeacherProfile getTeacherProfileByUserId(Long userId) {
        return teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for user ID: " + userId));
    }

    /**
     * Update teacher profile
     */
    public Teacher updateTeacherProfile(Long userId, TeacherSignupRequest updateRequest) {
        Teacher teacher = getTeacherByUserId(userId);
        
        // Update basic fields
        if (updateRequest.getName() != null) {
            teacher.setName(updateRequest.getName());
        }
        if (updateRequest.getBio() != null) {
            teacher.setBio(updateRequest.getBio());
        }
        if (updateRequest.getYearsExperience() != null) {
            teacher.setYearsExperience(updateRequest.getYearsExperience());
        }
        if (updateRequest.getLanguages() != null) {
            teacher.setLanguagesArray(updateRequest.getLanguages().toArray(new String[0]));
        }
        if (updateRequest.getCategories() != null) {
            teacher.setCategoriesArray(updateRequest.getCategories().toArray(new String[0]));
        }
        if (updateRequest.getHourlyRate() != null) {
            teacher.setHourlyRate(updateRequest.getHourlyRate());
        }
        
        // Update optional fields
        if (updateRequest.getPhoneNumber() != null) {
            teacher.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getLinkedinProfile() != null) {
            teacher.setLinkedinProfile(updateRequest.getLinkedinProfile());
        }
        
        Teacher savedTeacher = teacherRepository.save(teacher);
        
        // Update teacher profile
        TeacherProfile profile = getTeacherProfileByUserId(userId);
        if (updateRequest.getBio() != null) {
            profile.setBio(updateRequest.getBio());
        }
        if (updateRequest.getYearsExperience() != null) {
            profile.setYearsExperience(updateRequest.getYearsExperience());
        }
        if (updateRequest.getHourlyRate() != null) {
            profile.setHourlyRate(updateRequest.getHourlyRate());
        }
        
        teacherProfileRepository.save(profile);
        
        return savedTeacher;
    }

    /**
     * Validate teacher categories
     */
    public boolean isValidCategory(String category) {
        return "STANDARD".equals(category) || "PREMIUM".equals(category);
    }

    /**
     * Validate teacher hourly rate
     */
    public boolean isValidHourlyRate(BigDecimal rate) {
        return rate != null && rate.compareTo(BigDecimal.valueOf(100)) >= 0 && 
               rate.compareTo(BigDecimal.valueOf(10000)) <= 0;
    }
}
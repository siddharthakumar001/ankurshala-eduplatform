package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.auth.StudentSignupRequest;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.StudentProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Enhanced Student Service
 * Handles comprehensive student profile management with mandatory personalization fields
 */
@Slf4j
@Service
@Transactional
public class EnhancedStudentService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    /**
     * Create enhanced student profile with all mandatory fields
     */
    public StudentProfile createStudentProfile(User user, StudentSignupRequest signupRequest) {
        log.info("Creating enhanced student profile for user ID: {}, email: {}", user.getId(), user.getEmail());
        log.info("Received signup request - board: '{}', grade: '{}'", signupRequest.getBoard(), signupRequest.getGrade());
        
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        
        // Set basic name fields
        String[] nameParts = signupRequest.getName().split(" ", 2);
        profile.setFirstName(nameParts[0]);
        profile.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        // Set mandatory personalization fields
        // Convert board string to enum (handle variations like "State Board" -> "STATE_BOARD")
        try {
            String boardStr = signupRequest.getBoard() != null ? signupRequest.getBoard().trim().toUpperCase() : "";
            log.info("Processing board: original='{}', uppercase='{}'", signupRequest.getBoard(), boardStr);
            if (boardStr.equals("STATE BOARD") || boardStr.equals("STATEBOARD")) {
                boardStr = "STATE_BOARD";
            }
            profile.setEducationalBoard(com.ankurshala.backend.entity.EducationalBoard.valueOf(boardStr));
            log.info("Successfully set educational board to: {}", boardStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid educational board: '{}'. Error: {}", signupRequest.getBoard(), e.getMessage(), e);
            throw new RuntimeException("Invalid educational board: " + signupRequest.getBoard() + 
                    ". Valid values are: CBSE, ICSE, STATE_BOARD, IB, CAMBRIDGE, OTHER", e);
        }
        
        // Convert grade string to enum (handle "7" -> "GRADE_7")
        // This MUST happen before any other operations that might trigger JPA
        com.ankurshala.backend.entity.ClassLevel classLevel = null;
        try {
            if (signupRequest.getGrade() == null || signupRequest.getGrade().trim().isEmpty()) {
                throw new RuntimeException("Grade is required and cannot be empty");
            }
            
            String originalGrade = signupRequest.getGrade().trim();
            String gradeStr = originalGrade.toUpperCase();
            log.info("Processing grade: original='{}', uppercase='{}'", originalGrade, gradeStr);
            
            // Check if it's a numeric grade (e.g., "7", "10", "12") - handle both string and numeric formats
            // Also handle if it already starts with "GRADE_"
            if (gradeStr.startsWith("GRADE_")) {
                // Already in correct format, use as is
                log.info("Grade already in GRADE_X format: '{}'", gradeStr);
            } else if (gradeStr.matches("^\\d+$")) {
                // It's a numeric grade, convert to GRADE_X format
                gradeStr = "GRADE_" + gradeStr;
                log.info("Converted numeric grade '{}' to: '{}'", originalGrade, gradeStr);
            } else {
                // Try to parse as number and convert
                try {
                    int gradeNum = Integer.parseInt(gradeStr);
                    if (gradeNum >= 1 && gradeNum <= 12) {
                        gradeStr = "GRADE_" + gradeNum;
                        log.info("Parsed and converted grade '{}' to: '{}'", originalGrade, gradeStr);
                    } else {
                        throw new RuntimeException("Grade number must be between 1 and 12, got: " + gradeNum);
                    }
                } catch (NumberFormatException nfe) {
                    // Not a number, will try to use as-is (might be UNDERGRADUATE, etc.)
                    log.info("Grade '{}' is not numeric, using as-is: '{}'", originalGrade, gradeStr);
                }
            }
            
            log.info("Final grade string before enum conversion: '{}'", gradeStr);
            classLevel = com.ankurshala.backend.entity.ClassLevel.valueOf(gradeStr);
            log.info("Successfully converted to ClassLevel enum: {}", classLevel);
        } catch (IllegalArgumentException e) {
            log.error("Invalid class level: '{}'. Error: {}", signupRequest.getGrade(), e.getMessage(), e);
            throw new RuntimeException("Invalid grade: " + signupRequest.getGrade() + 
                    ". Valid values are: 1-12 (will be converted to GRADE_1 through GRADE_12), or GRADE_1 through GRADE_12, UNDERGRADUATE, POSTGRADUATE, DOCTORATE, OTHER", e);
        } catch (Exception e) {
            log.error("Error converting grade '{}': {}", signupRequest.getGrade(), e.getMessage(), e);
            throw new RuntimeException("Error processing grade: " + signupRequest.getGrade() + " - " + e.getMessage(), e);
        }
        
        // Now set the class level on the profile (classLevel is guaranteed to be non-null at this point)
        profile.setClassLevel(classLevel);
        log.info("Set class level on profile to: {}", classLevel);
        profile.setSchoolName(signupRequest.getSchool());
        profile.setDateOfBirth(signupRequest.getDob());
        profile.setGuardianName(signupRequest.getGuardianName());
        
        // Set language and goals (from onboarding fields)
        profile.setLanguage(signupRequest.getLanguage());
        if (signupRequest.getGoals() != null && !signupRequest.getGoals().isEmpty()) {
            profile.setGoals(String.join(", ", signupRequest.getGoals()));
        }
        
        // Set guardian contact as alternate mobile number
        if (signupRequest.getGuardianContact() != null) {
            profile.setAlternateMobileNumber(signupRequest.getGuardianContact());
        }
        
        // Set optional fields if provided
        if (signupRequest.getFatherName() != null) {
            profile.setFatherName(signupRequest.getFatherName());
        }
        if (signupRequest.getMotherName() != null) {
            profile.setMotherName(signupRequest.getMotherName());
        }
        if (signupRequest.getMobileNumber() != null) {
            profile.setMobileNumber(signupRequest.getMobileNumber());
        }
        if (signupRequest.getEmergencyContact() != null) {
            profile.setEmergencyContact(signupRequest.getEmergencyContact());
        }
        
        StudentProfile savedProfile = studentProfileRepository.save(profile);
        log.info("Enhanced student profile created with ID: {} for user ID: {}", savedProfile.getId(), user.getId());
        
        // TODO: Create student wallet if wallet feature is enabled
        // TODO: Initialize student goals in a separate table
        
        return savedProfile;
    }

    /**
     * Get student profile by user ID
     */
    public StudentProfile getStudentProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found for user ID: " + userId));
    }

    /**
     * Update student profile
     */
    public StudentProfile updateStudentProfile(Long userId, StudentSignupRequest updateRequest) {
        StudentProfile profile = getStudentProfile(userId);
        
        // Update fields
        if (updateRequest.getName() != null) {
            String[] nameParts = updateRequest.getName().split(" ", 2);
            profile.setFirstName(nameParts[0]);
            profile.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        }
        
        if (updateRequest.getBoard() != null) {
            String boardStr = updateRequest.getBoard().trim().toUpperCase();
            if (boardStr.equals("STATE BOARD") || boardStr.equals("STATEBOARD")) {
                boardStr = "STATE_BOARD";
            }
            profile.setEducationalBoard(com.ankurshala.backend.entity.EducationalBoard.valueOf(boardStr));
        }
        
        if (updateRequest.getGrade() != null) {
            String gradeStr = updateRequest.getGrade().trim().toUpperCase();
            if (gradeStr.matches("^\\d+$")) {
                gradeStr = "GRADE_" + gradeStr;
            }
            profile.setClassLevel(com.ankurshala.backend.entity.ClassLevel.valueOf(gradeStr));
        }
        
        if (updateRequest.getSchool() != null) {
            profile.setSchoolName(updateRequest.getSchool());
        }
        
        if (updateRequest.getDob() != null) {
            profile.setDateOfBirth(updateRequest.getDob());
        }
        
        if (updateRequest.getGuardianName() != null) {
            profile.setGuardianName(updateRequest.getGuardianName());
        }
        
        // Update optional fields
        if (updateRequest.getFatherName() != null) {
            profile.setFatherName(updateRequest.getFatherName());
        }
        if (updateRequest.getMotherName() != null) {
            profile.setMotherName(updateRequest.getMotherName());
        }
        if (updateRequest.getMobileNumber() != null) {
            profile.setMobileNumber(updateRequest.getMobileNumber());
        }
        if (updateRequest.getEmergencyContact() != null) {
            profile.setEmergencyContact(updateRequest.getEmergencyContact());
        }
        
        return studentProfileRepository.save(profile);
    }
}

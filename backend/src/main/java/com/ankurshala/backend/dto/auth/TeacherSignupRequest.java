package com.ankurshala.backend.dto.auth;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TeacherSignupRequest {
    
    // Basic user info
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d|.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$", 
             message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit or special character")
    private String password;

    // Mandatory professional fields
    @NotBlank(message = "Bio is required")
    @Size(min = 50, max = 1000, message = "Bio must be between 50 and 1000 characters")
    private String bio;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    private Integer yearsExperience;

    @NotNull(message = "Languages are required")
    @Size(min = 1, message = "At least one language must be specified")
    private List<String> languages;

    @NotNull(message = "Categories are required")
    @Size(min = 1, message = "At least one category must be specified")
    private List<String> categories;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "100.00", message = "Hourly rate must be at least ₹100")
    @DecimalMax(value = "10000.00", message = "Hourly rate cannot exceed ₹10,000")
    private BigDecimal hourlyRate;

    // Mandatory subject expertise (at least one)
    @NotNull(message = "Subject expertise is required")
    @Size(min = 1, message = "At least one subject expertise must be specified")
    @Valid
    private List<SubjectExpertiseDto> subjectExpertise;

    // Mandatory availability (at least one slot)
    @NotNull(message = "Availability is required")
    @Size(min = 1, message = "At least one availability slot must be specified")
    @Valid
    private List<AvailabilitySlotDto> availability;

    // Optional fields
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @Size(max = 255, message = "LinkedIn profile must not exceed 255 characters")
    private String linkedinProfile;

    @Data
    public static class SubjectExpertiseDto {
        @NotBlank(message = "Board is required")
        @Size(max = 50, message = "Board must not exceed 50 characters")
        private String board;

        @NotBlank(message = "Grade is required")
        @Size(max = 50, message = "Grade must not exceed 50 characters")
        private String grade;

        @NotNull(message = "Subject ID is required")
        private Long subjectId;

        @NotBlank(message = "Language is required")
        @Size(max = 50, message = "Language must not exceed 50 characters")
        private String language = "English";
    }

    @Data
    public static class AvailabilitySlotDto {
        @NotNull(message = "Weekday is required")
        @Min(value = 0, message = "Weekday must be between 0 (Sunday) and 6 (Saturday)")
        @Max(value = 6, message = "Weekday must be between 0 (Sunday) and 6 (Saturday)")
        private Integer weekday;

        @NotBlank(message = "Start time is required")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:MM format")
        private String startTime;

        @NotBlank(message = "End time is required")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:MM format")
        private String endTime;

        @NotBlank(message = "Timezone is required")
        @Size(max = 64, message = "Timezone must not exceed 64 characters")
        private String timezone = "Asia/Kolkata";
    }
}

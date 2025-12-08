package com.ankurshala.backend.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StudentSignupRequest {
    
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

    // Mandatory personalization fields
    @NotBlank(message = "Board is required")
    @Size(max = 50, message = "Board must not exceed 50 characters")
    private String board;

    @NotBlank(message = "Grade is required")
    @Size(max = 50, message = "Grade must not exceed 50 characters")
    private String grade;

    @NotBlank(message = "Language is required")
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language = "English";

    @NotNull(message = "Goals are required")
    @Size(min = 1, message = "At least one goal must be specified")
    private List<String> goals;

    @NotBlank(message = "School name is required")
    @Size(max = 200, message = "School name must not exceed 200 characters")
    private String school;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a valid 6-digit Indian pincode")
    private String pincode;

    @NotBlank(message = "Guardian name is required")
    @Size(max = 100, message = "Guardian name must not exceed 100 characters")
    private String guardianName;

    @NotBlank(message = "Guardian contact is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Guardian contact must be a valid 10-digit Indian mobile number")
    private String guardianContact;

    // Optional fields
    @Size(max = 100, message = "Father name must not exceed 100 characters")
    private String fatherName;

    @Size(max = 100, message = "Mother name must not exceed 100 characters")
    private String motherName;

    @Size(max = 20, message = "Mobile number must not exceed 20 characters")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit Indian mobile number")
    private String mobileNumber;

    @Size(max = 100, message = "Emergency contact must not exceed 100 characters")
    private String emergencyContact;
}

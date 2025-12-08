package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSearchRequest {
    
    private Long subjectId;
    
    private Long chapterId;
    
    private Long topicId;
    
    @FutureOrPresent(message = "Date must be present or future")
    private LocalDate date;
    
    private String timeSlot; // e.g., "MORNING", "AFTERNOON", "EVENING"
    
    @Min(value = 0, message = "Minimum rating must be at least 0")
    @Max(value = 5, message = "Maximum rating cannot exceed 5")
    private Double minRating;
    
    @Min(value = 0, message = "Maximum hourly rate must be positive")
    private Double maxHourlyRate;
    
    private List<String> languages; // e.g., ["English", "Hindi"]
    
    private String teacherCategory; // "STANDARD", "PREMIUM", "PLATINUM"
    
    private Boolean verifiedOnly; // Only show verified teachers
    
    private Boolean availableNow; // Show only currently available teachers
    
    private String search; // Search by teacher name or bio
}

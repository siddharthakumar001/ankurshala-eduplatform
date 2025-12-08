package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSearchResponse {
    
    private Long id;
    
    private String name;
    
    private String email;
    
    private String profilePictureUrl;
    
    private String bio;
    
    private Double rating;
    
    private Integer totalRatings;
    
    private BigDecimal hourlyRate;
    
    private String teacherCategory; // "STANDARD", "PREMIUM", "PLATINUM"
    
    private List<String> languages;
    
    private List<String> specializations; // Subject names
    
    private Integer yearsOfExperience;
    
    private Boolean verified;
    
    private Boolean currentlyAvailable;
    
    private Integer completedSessions;
    
    private String qualifications;
    
    private Double responseRate; // Percentage of booking requests responded to
    
    private String responseTime; // Average response time (e.g., "Within 2 hours")
    
    private List<TeacherReviewDto> recentReviews;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherReviewDto {
        private String studentName;
        private Double rating;
        private String comment;
        private String date;
    }
}

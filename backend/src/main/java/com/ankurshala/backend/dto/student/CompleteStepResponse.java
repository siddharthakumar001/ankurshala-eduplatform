package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for step completion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteStepResponse {
    
    private String message;
    private Boolean success;
    private LocalDateTime completedAt;
    private Integer totalStepsCompleted;
    private Integer todayStepsCompleted;
    private String nextRecommendation; // What to do next
    private String motivationalMessage;
}

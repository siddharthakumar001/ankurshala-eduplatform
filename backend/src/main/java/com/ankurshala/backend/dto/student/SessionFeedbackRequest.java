package com.ankurshala.backend.dto.student;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Data
public class SessionFeedbackRequest {
    @NotNull
    private Long bookingId;
    
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    
    private String feedback;
    private String teacherFeedback;
    private String sessionNotes;
    private boolean wouldRecommend;
    private String improvementSuggestions;
}

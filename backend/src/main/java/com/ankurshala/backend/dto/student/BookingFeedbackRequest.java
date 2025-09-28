package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookingFeedbackRequest {
    
    @Min(1)
    @Max(5)
    private Integer rating;
    
    private String feedback;
}

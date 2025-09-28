package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RescheduleBookingRequest {
    
    @NotNull
    @Future
    private LocalDateTime newStartTime;
    
    @Min(30)
    @Max(120)
    private Integer newDurationMinutes;
    
    private String reason;
    private String timezone;
}

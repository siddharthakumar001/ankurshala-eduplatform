package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeePreviewRequest {
    
    @NotNull
    private String action; // RESCHEDULE or CANCEL
    
    private LocalDateTime newStart;
    private Integer newDurationMinutes;
}

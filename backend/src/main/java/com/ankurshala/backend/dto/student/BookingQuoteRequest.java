package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingQuoteRequest {
    
    @NotNull
    private Long topicId;
    
    @NotNull
    @Future
    private LocalDateTime startTime;
    
    @Min(30)
    @Max(120)
    private Integer durationMinutes;
    
    private String timezone;
}

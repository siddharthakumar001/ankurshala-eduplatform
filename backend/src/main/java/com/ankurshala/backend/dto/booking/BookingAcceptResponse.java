package com.ankurshala.backend.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class BookingAcceptResponse {
    
    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Start time is required")
    private ZonedDateTime startTime;

    @NotNull(message = "End time is required")
    private ZonedDateTime endTime;

    @NotBlank(message = "Zoom host link is required")
    private String zoomHostLink;
}

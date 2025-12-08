package com.ankurshala.backend.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class FeePreviewRequest {
    
    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(RESCHEDULE|CANCEL)$", message = "Action must be RESCHEDULE or CANCEL")
    private String action;

    private ZonedDateTime newStart;
}

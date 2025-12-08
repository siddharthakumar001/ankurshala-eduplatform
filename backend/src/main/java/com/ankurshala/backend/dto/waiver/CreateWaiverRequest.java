package com.ankurshala.backend.dto.waiver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateWaiverRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    private Long bookingId;

    @Min(value = 0, message = "Percent waiver cannot be negative")
    @Max(value = 100, message = "Percent waiver cannot exceed 100%")
    private BigDecimal percent;

    @Min(value = 0, message = "Flat waiver cannot be negative")
    private Integer flatCents;

    @NotBlank(message = "Reason is required")
    private String reason;

    private LocalDateTime expiresAt;
}

package com.ankurshala.backend.dto.waiver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequestWaiverRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Reason is required")
    private String reason;

    @Min(value = 0, message = "Percent waiver cannot be negative")
    @Max(value = 100, message = "Percent waiver cannot exceed 100%")
    private BigDecimal percentWaiver;

    @Min(value = 0, message = "Flat waiver cannot be negative")
    private Integer flatWaiverCents;
}

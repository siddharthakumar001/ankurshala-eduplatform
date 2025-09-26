package com.ankurshala.backend.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CreateFeeWaiverRequest {
    private Long bookingId; // Optional, for future booking integration

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}

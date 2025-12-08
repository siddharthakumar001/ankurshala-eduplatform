package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for initiating a payment order
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentOrderRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String currency; // Default: INR

    private String notes;

    private Map<String, String> metadata;
}

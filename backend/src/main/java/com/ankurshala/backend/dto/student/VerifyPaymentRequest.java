package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying a payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Signature is required")
    private String signature;

    @NotNull(message = "Booking ID is required")
    private Long bookingId;
}

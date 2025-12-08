package com.ankurshala.backend.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePaymentIntentRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Amount in cents is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Integer amountCents;

    @Size(max = 3, message = "Currency code must not exceed 3 characters")
    private String currency = "INR";

    private Long paymentMethodId;
}

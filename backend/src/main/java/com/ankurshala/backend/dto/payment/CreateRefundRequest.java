package com.ankurshala.backend.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRefundRequest {
    @NotNull(message = "Payment intent ID is required")
    private Long paymentIntentId;

    @NotNull(message = "Amount in cents is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Integer amountCents;

    @NotBlank(message = "Refund reason is required")
    @Size(max = 1000, message = "Refund reason must not exceed 1000 characters")
    private String reason;
}

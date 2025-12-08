package com.ankurshala.backend.dto.payment;

import com.ankurshala.backend.entity.PaymentMethodType;
import com.ankurshala.backend.entity.PaymentProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddPaymentMethodRequest {
    @NotNull(message = "Method type is required")
    private PaymentMethodType methodType;

    @NotNull(message = "Provider is required")
    private PaymentProvider provider;

    @NotBlank(message = "Provider ID is required")
    @Size(max = 255, message = "Provider ID must not exceed 255 characters")
    private String providerId;

    @NotBlank(message = "Masked details are required")
    @Size(max = 255, message = "Masked details must not exceed 255 characters")
    private String maskedDetails;
}

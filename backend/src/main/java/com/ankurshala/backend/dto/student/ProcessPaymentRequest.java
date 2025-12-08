package com.ankurshala.backend.dto.student;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
public class ProcessPaymentRequest {
    @NotNull
    private Long bookingId;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull
    private String paymentMethodId;
    
    private String couponCode;
    private String notes;
}

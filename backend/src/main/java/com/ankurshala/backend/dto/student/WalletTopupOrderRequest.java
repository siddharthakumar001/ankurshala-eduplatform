package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletTopupOrderRequest {
    @NotNull
    @DecimalMin(value = "1.00", message = "Top-up amount must be at least 1.00")
    private BigDecimal amount;
    private String currency;
    private String notes;
}

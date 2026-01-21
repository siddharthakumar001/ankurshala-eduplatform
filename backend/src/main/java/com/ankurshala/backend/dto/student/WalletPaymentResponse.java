package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletPaymentResponse {
    private boolean success;
    private String status;
    private String message;
    private Long bookingId;
    private BigDecimal amount;
    private Long balanceCents;
    private LocalDateTime paidAt;
}

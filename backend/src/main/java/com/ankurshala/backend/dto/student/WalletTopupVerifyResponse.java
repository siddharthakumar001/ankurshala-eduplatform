package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletTopupVerifyResponse {
    private boolean success;
    private String status;
    private String message;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private Long balanceCents;
    private LocalDateTime paidAt;
}

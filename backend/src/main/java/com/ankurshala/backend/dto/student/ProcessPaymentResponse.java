package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProcessPaymentResponse {
    private boolean success;
    private String transactionId;
    private String paymentUrl;
    private BigDecimal amount;
    private String status;
    private String message;
    private LocalDateTime processedAt;
    private String receiptUrl;
}

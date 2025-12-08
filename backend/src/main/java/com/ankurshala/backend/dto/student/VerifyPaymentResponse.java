package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentResponse {

    private boolean success;
    private String status; // VERIFIED, FAILED, PENDING
    private String message;
    private String transactionId;
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime paidAt;
    private Long bookingId;
    private String receiptUrl;
}

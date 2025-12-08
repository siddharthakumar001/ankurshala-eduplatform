package com.ankurshala.backend.dto.payment;

import com.ankurshala.backend.entity.PaymentIntentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PaymentIntentDto {
    private Long id;
    private Long userId;
    private Long bookingId;
    private Integer amountCents;
    private String currency;
    private PaymentIntentStatus status;
    private Long paymentMethodId;
    private String providerPaymentId;
    private String providerOrderId;
    private Map<String, Object> providerResponse;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

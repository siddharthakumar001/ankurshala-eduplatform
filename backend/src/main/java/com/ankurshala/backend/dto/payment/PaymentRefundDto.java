package com.ankurshala.backend.dto.payment;

import com.ankurshala.backend.entity.PaymentRefundStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PaymentRefundDto {
    private Long id;
    private Long paymentIntentId;
    private Integer amountCents;
    private String reason;
    private PaymentRefundStatus status;
    private String providerRefundId;
    private Map<String, Object> providerResponse;
    private String failureReason;
    private Long processedBy;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

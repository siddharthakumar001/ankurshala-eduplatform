package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment_refunds")
@Data
public class PaymentRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_intent_id", nullable = false)
    private Long paymentIntentId;

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentRefundStatus status = PaymentRefundStatus.PENDING;

    @Column(name = "provider_refund_id")
    private String providerRefundId;

    @Column(name = "provider_response", columnDefinition = "JSONB")
    @Convert(converter = com.ankurshala.backend.util.HashMapConverter.class)
    private Map<String, Object> providerResponse;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment_transactions")
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_intent_id", nullable = false)
    private PaymentIntent paymentIntent;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "provider_response", columnDefinition = "JSONB")
    @Convert(converter = com.ankurshala.backend.util.HashMapConverter.class)
    private Map<String, Object> providerResponse;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionType {
        PAYMENT, REFUND, PARTIAL_REFUND, CHARGEBACK
    }

    public enum TransactionStatus {
        PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELLED
    }
}

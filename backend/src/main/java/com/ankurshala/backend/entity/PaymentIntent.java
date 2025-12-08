package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment_intents")
@Data
public class PaymentIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    @Column(name = "currency", nullable = false)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentIntentStatus status = PaymentIntentStatus.CREATED;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(name = "provider_order_id")
    private String providerOrderId;

    @Column(name = "provider_response", columnDefinition = "JSONB")
    @Convert(converter = com.ankurshala.backend.util.HashMapConverter.class)
    private Map<String, Object> providerResponse;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
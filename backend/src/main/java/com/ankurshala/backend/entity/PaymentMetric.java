package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_metrics", indexes = {
    @Index(name = "idx_payment_metric_type", columnList = "metricType"),
    @Index(name = "idx_payment_metric_timestamp", columnList = "timestamp"),
    @Index(name = "idx_payment_metric_user", columnList = "userId"),
    @Index(name = "idx_payment_metric_provider", columnList = "provider")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String metricType;

    @Column(length = 100)
    private String provider;

    @Column(length = 100)
    private String operation;

    @Column(length = 50)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean success;

    private Long executionTime;

    private Long amountCents;

    @Column(length = 10)
    private String currency;

    @Column(length = 500)
    private String details;

    @Column(length = 100)
    private String traceId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(length = 50)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

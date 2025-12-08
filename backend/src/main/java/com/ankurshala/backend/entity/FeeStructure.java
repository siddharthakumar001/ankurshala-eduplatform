package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_structures")
@Data
public class FeeStructure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private FeeCategory category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType;

    @Column(name = "base_amount_cents", nullable = false)
    private Long baseAmountCents = 0L;

    @Column(name = "percentage_rate", precision = 5, scale = 4)
    private BigDecimal percentageRate = BigDecimal.ZERO;

    @Column(name = "min_amount_cents")
    private Long minAmountCents = 0L;

    @Column(name = "max_amount_cents")
    private Long maxAmountCents = 0L;

    @Column(name = "currency", length = 3)
    private String currency = "INR";

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FeeType {
        PERCENTAGE, FIXED, TIERED
    }
}

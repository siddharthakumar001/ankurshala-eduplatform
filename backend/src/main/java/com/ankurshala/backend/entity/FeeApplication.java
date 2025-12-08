package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fee_applications")
@Data
public class FeeApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "fee_amount_cents", nullable = false)
    private Long feeAmountCents;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FeeApplicationStatus status = FeeApplicationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FeeApplicationStatus {
        PENDING, APPLIED, WAIVED, CANCELLED
    }
}

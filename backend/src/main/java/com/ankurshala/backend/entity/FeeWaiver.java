package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_waivers")
@Data
public class FeeWaiver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_application_id")
    private FeeApplication feeApplication;

    @Enumerated(EnumType.STRING)
    @Column(name = "waiver_type", nullable = false)
    private WaiverType waiverType;

    @Column(name = "waiver_amount_cents")
    private Long waiverAmountCents = 0L;

    @Column(name = "waiver_percentage", precision = 5, scale = 2)
    private BigDecimal waiverPercentage = BigDecimal.ZERO;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WaiverStatus status = WaiverStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum WaiverType {
        FULL, PARTIAL, PERCENTAGE
    }

    public enum WaiverStatus {
        PENDING, APPROVED, REJECTED, EXPIRED
    }

    // Helper methods for compatibility
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(Long userId) {
        if (userId != null) {
            this.user = new User();
            this.user.setId(userId);
        }
    }

    public Long getFeeApplicationId() {
        return feeApplication != null ? feeApplication.getId() : null;
    }

    public void setFeeApplicationId(Long feeApplicationId) {
        if (feeApplicationId != null) {
            this.feeApplication = new FeeApplication();
            this.feeApplication.setId(feeApplicationId);
        }
    }
}
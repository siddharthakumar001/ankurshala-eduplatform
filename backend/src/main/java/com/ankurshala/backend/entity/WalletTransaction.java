package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_txns")
@Data
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private WalletOwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private WalletTransactionType type;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private WalletTransactionSource source;

    @Column(name = "meta", columnDefinition = "JSONB")
    private String meta;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

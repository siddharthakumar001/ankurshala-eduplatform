package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_waivers")
@Data
@NoArgsConstructor
public class FeeWaiver {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;
    
    @NotNull
    @DecimalMin("0.00")
    @Column(name = "amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal amount;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public FeeWaiver(User user, String reason, BigDecimal amount) {
        this.user = user;
        this.reason = reason;
        this.amount = amount;
    }
}
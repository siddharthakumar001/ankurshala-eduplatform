package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_earnings")
public class TeacherEarnings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session_duration_minutes", nullable = false)
    private Integer sessionDurationMinutes;

    @Column(name = "hourly_rate", nullable = false, precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "earnings_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal earningsAmount;

    @Column(name = "platform_fee", nullable = false, precision = 8, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "net_earnings", nullable = false, precision = 8, scale = 2)
    private BigDecimal netEarnings;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TeacherEarnings() {}

    public TeacherEarnings(Teacher teacher, Booking booking, LocalDate sessionDate, 
                          Integer sessionDurationMinutes, BigDecimal hourlyRate, 
                          BigDecimal earningsAmount, BigDecimal platformFee) {
        this.teacher = teacher;
        this.booking = booking;
        this.sessionDate = sessionDate;
        this.sessionDurationMinutes = sessionDurationMinutes;
        this.hourlyRate = hourlyRate;
        this.earningsAmount = earningsAmount;
        this.platformFee = platformFee;
        this.netEarnings = earningsAmount.subtract(platformFee);
    }

    public enum PaymentStatus {
        PENDING, PROCESSED, PAID, FAILED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public Integer getSessionDurationMinutes() { return sessionDurationMinutes; }
    public void setSessionDurationMinutes(Integer sessionDurationMinutes) { this.sessionDurationMinutes = sessionDurationMinutes; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public BigDecimal getEarningsAmount() { return earningsAmount; }
    public void setEarningsAmount(BigDecimal earningsAmount) { this.earningsAmount = earningsAmount; }

    public BigDecimal getPlatformFee() { return platformFee; }
    public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }

    public BigDecimal getNetEarnings() { return netEarnings; }
    public void setNetEarnings(BigDecimal netEarnings) { this.netEarnings = netEarnings; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

package com.ankurshala.backend.dto.teacher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TeacherEarningsDto {
    private Long id;
    private Long bookingId;
    private LocalDate sessionDate;
    private Integer sessionDurationMinutes;
    private BigDecimal hourlyRate;
    private BigDecimal earningsAmount;
    private BigDecimal platformFee;
    private BigDecimal netEarnings;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    
    public TeacherEarningsDto() {}
    
    public TeacherEarningsDto(Long bookingId, LocalDate sessionDate, Integer sessionDurationMinutes, 
                            BigDecimal hourlyRate, BigDecimal earningsAmount, BigDecimal platformFee, 
                            BigDecimal netEarnings, String paymentStatus) {
        this.bookingId = bookingId;
        this.sessionDate = sessionDate;
        this.sessionDurationMinutes = sessionDurationMinutes;
        this.hourlyRate = hourlyRate;
        this.earningsAmount = earningsAmount;
        this.platformFee = platformFee;
        this.netEarnings = netEarnings;
        this.paymentStatus = paymentStatus;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
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
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

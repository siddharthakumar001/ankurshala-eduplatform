package com.ankurshala.backend.dto.teacher;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TeacherRecentEarningsDto {
    private Long id;
    private Long bookingId;
    private LocalDate sessionDate;
    private Integer sessionDurationMinutes;
    private BigDecimal hourlyRate;
    private BigDecimal netEarnings;
    private String paymentStatus;
    private String studentName;
    private String topicTitle;
    
    public TeacherRecentEarningsDto() {}
    
    public TeacherRecentEarningsDto(Long id, Long bookingId, LocalDate sessionDate, Integer sessionDurationMinutes, 
                                  BigDecimal hourlyRate, BigDecimal netEarnings, String paymentStatus, 
                                  String studentName, String topicTitle) {
        this.id = id;
        this.bookingId = bookingId;
        this.sessionDate = sessionDate;
        this.sessionDurationMinutes = sessionDurationMinutes;
        this.hourlyRate = hourlyRate;
        this.netEarnings = netEarnings;
        this.paymentStatus = paymentStatus;
        this.studentName = studentName;
        this.topicTitle = topicTitle;
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
    
    public BigDecimal getNetEarnings() { return netEarnings; }
    public void setNetEarnings(BigDecimal netEarnings) { this.netEarnings = netEarnings; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getTopicTitle() { return topicTitle; }
    public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }
}

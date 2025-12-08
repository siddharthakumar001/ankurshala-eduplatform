package com.ankurshala.backend.dto.teacher;

import java.math.BigDecimal;

public class TeacherDashboardStatsDto {
    private Integer totalBookings;
    private Integer pendingBookings;
    private Integer acceptedBookings;
    private Integer completedSessions;
    private Integer cancelledSessions;
    private BigDecimal totalEarnings;
    private BigDecimal monthlyEarnings;
    private BigDecimal averageRating;
    private Integer totalStudents;
    private Integer activeStudents;
    private Double completionRate;
    private Double cancellationRate;
    
    public TeacherDashboardStatsDto() {}
    
    public TeacherDashboardStatsDto(Integer totalBookings, Integer pendingBookings, Integer acceptedBookings, 
                                  Integer completedSessions, Integer cancelledSessions, BigDecimal totalEarnings, 
                                  BigDecimal monthlyEarnings, BigDecimal averageRating, Integer totalStudents, 
                                  Integer activeStudents, Double completionRate, Double cancellationRate) {
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.acceptedBookings = acceptedBookings;
        this.completedSessions = completedSessions;
        this.cancelledSessions = cancelledSessions;
        this.totalEarnings = totalEarnings;
        this.monthlyEarnings = monthlyEarnings;
        this.averageRating = averageRating;
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.completionRate = completionRate;
        this.cancellationRate = cancellationRate;
    }
    
    // Getters and Setters
    public Integer getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Integer totalBookings) { this.totalBookings = totalBookings; }
    
    public Integer getPendingBookings() { return pendingBookings; }
    public void setPendingBookings(Integer pendingBookings) { this.pendingBookings = pendingBookings; }
    
    public Integer getAcceptedBookings() { return acceptedBookings; }
    public void setAcceptedBookings(Integer acceptedBookings) { this.acceptedBookings = acceptedBookings; }
    
    public Integer getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(Integer completedSessions) { this.completedSessions = completedSessions; }
    
    public Integer getCancelledSessions() { return cancelledSessions; }
    public void setCancelledSessions(Integer cancelledSessions) { this.cancelledSessions = cancelledSessions; }
    
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    
    public BigDecimal getMonthlyEarnings() { return monthlyEarnings; }
    public void setMonthlyEarnings(BigDecimal monthlyEarnings) { this.monthlyEarnings = monthlyEarnings; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public Integer getTotalStudents() { return totalStudents; }
    public void setTotalStudents(Integer totalStudents) { this.totalStudents = totalStudents; }
    
    public Integer getActiveStudents() { return activeStudents; }
    public void setActiveStudents(Integer activeStudents) { this.activeStudents = activeStudents; }
    
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    
    public Double getCancellationRate() { return cancellationRate; }
    public void setCancellationRate(Double cancellationRate) { this.cancellationRate = cancellationRate; }
}

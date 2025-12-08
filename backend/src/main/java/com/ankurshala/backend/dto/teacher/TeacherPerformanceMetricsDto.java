package com.ankurshala.backend.dto.teacher;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TeacherPerformanceMetricsDto {
    private Long id;
    private LocalDate metricDate;
    private Integer totalSessions;
    private Integer completedSessions;
    private Integer cancelledSessions;
    private BigDecimal averageRating;
    private BigDecimal totalEarnings;
    private BigDecimal totalHoursTaught;
    private BigDecimal studentSatisfactionScore;
    
    public TeacherPerformanceMetricsDto() {}
    
    public TeacherPerformanceMetricsDto(LocalDate metricDate, Integer totalSessions, Integer completedSessions, 
                                      Integer cancelledSessions, BigDecimal averageRating, BigDecimal totalEarnings, 
                                      BigDecimal totalHoursTaught, BigDecimal studentSatisfactionScore) {
        this.metricDate = metricDate;
        this.totalSessions = totalSessions;
        this.completedSessions = completedSessions;
        this.cancelledSessions = cancelledSessions;
        this.averageRating = averageRating;
        this.totalEarnings = totalEarnings;
        this.totalHoursTaught = totalHoursTaught;
        this.studentSatisfactionScore = studentSatisfactionScore;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDate getMetricDate() { return metricDate; }
    public void setMetricDate(LocalDate metricDate) { this.metricDate = metricDate; }
    
    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }
    
    public Integer getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(Integer completedSessions) { this.completedSessions = completedSessions; }
    
    public Integer getCancelledSessions() { return cancelledSessions; }
    public void setCancelledSessions(Integer cancelledSessions) { this.cancelledSessions = cancelledSessions; }
    
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
    
    public BigDecimal getTotalEarnings() { return totalEarnings; }
    public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
    
    public BigDecimal getTotalHoursTaught() { return totalHoursTaught; }
    public void setTotalHoursTaught(BigDecimal totalHoursTaught) { this.totalHoursTaught = totalHoursTaught; }
    
    public BigDecimal getStudentSatisfactionScore() { return studentSatisfactionScore; }
    public void setStudentSatisfactionScore(BigDecimal studentSatisfactionScore) { this.studentSatisfactionScore = studentSatisfactionScore; }
}

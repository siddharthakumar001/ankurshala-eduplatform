package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_performance_metrics")
public class TeacherPerformanceMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "total_sessions", nullable = false)
    private Integer totalSessions = 0;

    @Column(name = "completed_sessions", nullable = false)
    private Integer completedSessions = 0;

    @Column(name = "cancelled_sessions", nullable = false)
    private Integer cancelledSessions = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_earnings", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "total_hours_taught", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalHoursTaught = BigDecimal.ZERO;

    @Column(name = "student_satisfaction_score", precision = 3, scale = 2)
    private BigDecimal studentSatisfactionScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TeacherPerformanceMetrics() {}

    public TeacherPerformanceMetrics(Teacher teacher, LocalDate metricDate) {
        this.teacher = teacher;
        this.metricDate = metricDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Additional method for accepted bookings
    public Integer getAcceptedBookings() { return completedSessions; }
    public void setAcceptedBookings(Integer acceptedBookings) { this.completedSessions = acceptedBookings; }
}

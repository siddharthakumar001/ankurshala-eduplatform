package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity for tracking daily plan progress
 * Records which steps a student completed on their daily plan
 */
@Entity
@Table(name = "daily_plan_progress", 
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"student_id", "plan_date", "step_type", "step_identifier"}
       ))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;
    
    @Column(name = "step_type", nullable = false, length = 50)
    private String stepType; // PRACTICE, REVISE_NOTE, FOCUS_SPRINT, BOOKING_COMPANION
    
    @Column(name = "step_identifier", length = 255)
    private String stepIdentifier; // topicId, noteId, bookingId, etc.
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    @Column(name = "completion_metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> completionMetadata;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (completedAt == null) {
            completedAt = now;
        }
        if (planDate == null) {
            planDate = LocalDate.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

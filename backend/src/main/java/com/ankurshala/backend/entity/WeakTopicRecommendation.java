package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for weak topic recommendations
 * Stores system-generated recommendations for topics students should focus on
 */
@Entity
@Table(name = "weak_topic_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeakTopicRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "topic_id", nullable = false)
    private Long topicId;
    
    @Column(name = "recommendation_reason", nullable = false, columnDefinition = "TEXT")
    private String recommendationReason;
    
    @Column(name = "prerequisite_gaps", columnDefinition = "TEXT")
    private String prerequisiteGaps;
    
    @Column(name = "confidence_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal confidenceScore; // 0.00 to 1.00
    
    @Column(name = "last_practice_date")
    private LocalDateTime lastPracticeDate;
    
    @Column(name = "practice_attempts_count", nullable = false)
    private Integer practiceAttemptsCount = 0;
    
    @Column(name = "avg_score", precision = 5, scale = 2)
    private BigDecimal avgScore;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (confidenceScore == null) {
            confidenceScore = BigDecimal.ZERO;
        }
        if (practiceAttemptsCount == null) {
            practiceAttemptsCount = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

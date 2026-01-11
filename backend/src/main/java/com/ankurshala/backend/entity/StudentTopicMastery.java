package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks per-student, per-topic mastery scores for adaptive learning.
 * Mastery score ranges from 0.0 to 1.0, with 0.3 as the default for unassessed topics.
 */
@Entity
@Table(name = "student_topic_mastery",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "topic_id"}))
public class StudentTopicMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    @Column(name = "mastery_score", precision = 4, scale = 3, nullable = false)
    private BigDecimal masteryScore = new BigDecimal("0.300");

    @Column(name = "confidence", precision = 4, scale = 3, nullable = false)
    private BigDecimal confidence = BigDecimal.ZERO;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts = 0;

    @Column(name = "correct_attempts", nullable = false)
    private Integer correctAttempts = 0;

    @Column(name = "last_assessed_at")
    private LocalDateTime lastAssessedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentTopicMastery() {}

    public StudentTopicMastery(Long studentId, Long topicId) {
        this.studentId = studentId;
        this.topicId = topicId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public BigDecimal getMasteryScore() { return masteryScore; }
    public void setMasteryScore(BigDecimal masteryScore) { this.masteryScore = masteryScore; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public Integer getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(Integer totalAttempts) { this.totalAttempts = totalAttempts; }

    public Integer getCorrectAttempts() { return correctAttempts; }
    public void setCorrectAttempts(Integer correctAttempts) { this.correctAttempts = correctAttempts; }

    public LocalDateTime getLastAssessedAt() { return lastAssessedAt; }
    public void setLastAssessedAt(LocalDateTime lastAssessedAt) { this.lastAssessedAt = lastAssessedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Updates mastery after a quiz attempt.
     * Uses exponential moving average for smooth updates.
     */
    public void updateMasteryFromAttempt(BigDecimal attemptScore, int questionsAnswered, int correctAnswers) {
        // Update attempt counts
        this.totalAttempts += questionsAnswered;
        this.correctAttempts += correctAnswers;
        
        // Calculate new mastery using EMA with alpha = 0.3 (more weight to recent performance)
        BigDecimal alpha = new BigDecimal("0.3");
        BigDecimal oneMinusAlpha = BigDecimal.ONE.subtract(alpha);
        
        this.masteryScore = alpha.multiply(attemptScore)
                .add(oneMinusAlpha.multiply(this.masteryScore))
                .setScale(3, java.math.RoundingMode.HALF_UP);
        
        // Clamp mastery between 0 and 1
        if (this.masteryScore.compareTo(BigDecimal.ZERO) < 0) {
            this.masteryScore = BigDecimal.ZERO;
        } else if (this.masteryScore.compareTo(BigDecimal.ONE) > 0) {
            this.masteryScore = BigDecimal.ONE;
        }
        
        // Update confidence based on number of attempts (saturates at ~10 attempts)
        double confidenceValue = 1.0 - Math.exp(-0.2 * this.totalAttempts);
        this.confidence = new BigDecimal(confidenceValue).setScale(3, java.math.RoundingMode.HALF_UP);
        
        this.lastAssessedAt = LocalDateTime.now();
    }
}


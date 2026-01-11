package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Per-student daily practice preferences.
 */
@Entity
@Table(name = "student_practice_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id"}))
public class StudentPracticePreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "daily_question_count", nullable = false)
    private Integer dailyQuestionCount = 5;

    @Column(name = "preferred_time_local", length = 5)
    private String preferredTimeLocal;  // HH:mm format

    @Column(name = "language", nullable = false, length = 20)
    private String language = "en";

    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentPracticePreferences() {}

    public StudentPracticePreferences(Long studentId) {
        this.studentId = studentId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Integer getDailyQuestionCount() { return dailyQuestionCount; }
    public void setDailyQuestionCount(Integer dailyQuestionCount) { this.dailyQuestionCount = dailyQuestionCount; }

    public String getPreferredTimeLocal() { return preferredTimeLocal; }
    public void setPreferredTimeLocal(String preferredTimeLocal) { this.preferredTimeLocal = preferredTimeLocal; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


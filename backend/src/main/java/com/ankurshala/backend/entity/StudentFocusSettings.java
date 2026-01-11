package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Per-student focus mode preferences and settings.
 */
@Entity
@Table(name = "student_focus_settings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id"}))
public class StudentFocusSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "focus_enabled", nullable = false)
    private Boolean focusEnabled = false;

    @Column(name = "default_sprint_minutes", nullable = false)
    private Integer defaultSprintMinutes = 15;

    @Column(name = "language_pref", nullable = false, length = 20)
    private String languagePref = "en";

    @Column(name = "reminder_enabled", nullable = false)
    private Boolean reminderEnabled = true;

    @Column(name = "reminder_before_minutes", nullable = false)
    private Integer reminderBeforeMinutes = 5;

    @Column(name = "sound_enabled", nullable = false)
    private Boolean soundEnabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentFocusSettings() {}

    public StudentFocusSettings(Long studentId) {
        this.studentId = studentId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Boolean getFocusEnabled() { return focusEnabled; }
    public void setFocusEnabled(Boolean focusEnabled) { this.focusEnabled = focusEnabled; }

    public Integer getDefaultSprintMinutes() { return defaultSprintMinutes; }
    public void setDefaultSprintMinutes(Integer defaultSprintMinutes) { this.defaultSprintMinutes = defaultSprintMinutes; }

    public String getLanguagePref() { return languagePref; }
    public void setLanguagePref(String languagePref) { this.languagePref = languagePref; }

    public Boolean getReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(Boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public Integer getReminderBeforeMinutes() { return reminderBeforeMinutes; }
    public void setReminderBeforeMinutes(Integer reminderBeforeMinutes) { this.reminderBeforeMinutes = reminderBeforeMinutes; }

    public Boolean getSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(Boolean soundEnabled) { this.soundEnabled = soundEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


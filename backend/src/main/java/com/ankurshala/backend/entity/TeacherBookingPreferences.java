package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_booking_preferences")
public class TeacherBookingPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false, unique = true)
    private Teacher teacher;

    @Column(name = "auto_accept_bookings", nullable = false)
    private Boolean autoAcceptBookings = false;

    @Column(name = "advance_booking_days", nullable = false)
    private Integer advanceBookingDays = 7;

    @Column(name = "minimum_session_duration", nullable = false)
    private Integer minimumSessionDuration = 30;

    @Column(name = "maximum_session_duration", nullable = false)
    private Integer maximumSessionDuration = 120;

    @Column(name = "cancellation_policy_hours", nullable = false)
    private Integer cancellationPolicyHours = 24;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TeacherBookingPreferences() {}

    public TeacherBookingPreferences(Teacher teacher) {
        this.teacher = teacher;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public Boolean getAutoAcceptBookings() { return autoAcceptBookings; }
    public void setAutoAcceptBookings(Boolean autoAcceptBookings) { this.autoAcceptBookings = autoAcceptBookings; }

    public Integer getAdvanceBookingDays() { return advanceBookingDays; }
    public void setAdvanceBookingDays(Integer advanceBookingDays) { this.advanceBookingDays = advanceBookingDays; }

    public Integer getMinimumSessionDuration() { return minimumSessionDuration; }
    public void setMinimumSessionDuration(Integer minimumSessionDuration) { this.minimumSessionDuration = minimumSessionDuration; }

    public Integer getMaximumSessionDuration() { return maximumSessionDuration; }
    public void setMaximumSessionDuration(Integer maximumSessionDuration) { this.maximumSessionDuration = maximumSessionDuration; }

    public Integer getCancellationPolicyHours() { return cancellationPolicyHours; }
    public void setCancellationPolicyHours(Integer cancellationPolicyHours) { this.cancellationPolicyHours = cancellationPolicyHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

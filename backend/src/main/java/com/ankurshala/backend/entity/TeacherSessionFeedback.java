package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_session_feedback")
public class TeacherSessionFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "session_rating")
    private Integer sessionRating; // 1-5

    @Column(name = "student_engagement", columnDefinition = "TEXT")
    private String studentEngagement;

    @Column(name = "session_notes", columnDefinition = "TEXT")
    private String sessionNotes;

    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Column(name = "would_recommend")
    private Boolean wouldRecommend;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TeacherSessionFeedback() {}

    public TeacherSessionFeedback(Booking booking, Teacher teacher, User student) {
        this.booking = booking;
        this.teacher = teacher;
        this.student = student;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Integer getSessionRating() { return sessionRating; }
    public void setSessionRating(Integer sessionRating) { this.sessionRating = sessionRating; }

    public String getStudentEngagement() { return studentEngagement; }
    public void setStudentEngagement(String studentEngagement) { this.studentEngagement = studentEngagement; }

    public String getSessionNotes() { return sessionNotes; }
    public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }

    public String getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(String improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }

    public Boolean getWouldRecommend() { return wouldRecommend; }
    public void setWouldRecommend(Boolean wouldRecommend) { this.wouldRecommend = wouldRecommend; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Additional methods for student feedback
    public Integer getStudentRating() { return sessionRating; }
    public void setStudentRating(Integer studentRating) { this.sessionRating = studentRating; }
    
    public String getStudentFeedback() { return studentEngagement; }
    public void setStudentFeedback(String studentFeedback) { this.studentEngagement = studentFeedback; }
    
    public String getStudentNotes() { return sessionNotes; }
    public void setStudentNotes(String studentNotes) { this.sessionNotes = studentNotes; }
}

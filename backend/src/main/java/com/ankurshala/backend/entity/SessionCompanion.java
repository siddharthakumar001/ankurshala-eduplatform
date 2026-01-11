package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Session companion artifact for booked sessions.
 * Provides pre/during/post session support content.
 */
@Entity
@Table(name = "session_companion")
public class SessionCompanion {

    public enum CompanionStatus {
        CREATED,        // Initial state
        PREP_READY,     // Pre-session plan generated
        LIVE,           // Session in progress
        POST_READY,     // Post-session summary generated
        COMPLETED       // All content finalized
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "teacher_id")
    private Long teacherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private User teacher;

    @Column(name = "topic_id")
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    @Column(name = "subject_id")
    private Long subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;

    // Pre-session content
    @Column(name = "pre_session_plan_md", columnDefinition = "TEXT")
    private String preSessionPlanMd;

    @Column(name = "warmup_quiz_id")
    private Long warmupQuizId;

    @Column(name = "warmup_completed")
    private Boolean warmupCompleted = false;

    // Live session content
    @Column(name = "live_notes_md", columnDefinition = "TEXT")
    private String liveNotesMd;

    @ElementCollection
    @CollectionTable(name = "session_companion_highlights", joinColumns = @JoinColumn(name = "companion_id"))
    @Column(name = "highlight", columnDefinition = "TEXT")
    private List<String> sessionHighlights = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "session_companion_questions", joinColumns = @JoinColumn(name = "companion_id"))
    @Column(name = "question", columnDefinition = "TEXT")
    private List<String> questionsAsked = new ArrayList<>();

    // Post-session content
    @Column(name = "post_session_summary_md", columnDefinition = "TEXT")
    private String postSessionSummaryMd;

    @Column(name = "homework_plan_md", columnDefinition = "TEXT")
    private String homeworkPlanMd;

    @ElementCollection
    @CollectionTable(name = "session_companion_recommended_topics", joinColumns = @JoinColumn(name = "companion_id"))
    @Column(name = "topic_id")
    private List<Long> recommendedTopics = new ArrayList<>();

    @Column(name = "mastery_suggestions", columnDefinition = "jsonb")
    private String masterySuggestions;

    // Status tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private CompanionStatus status = CompanionStatus.CREATED;

    @Column(name = "prep_generated_at")
    private LocalDateTime prepGeneratedAt;

    @Column(name = "live_started_at")
    private LocalDateTime liveStartedAt;

    @Column(name = "post_generated_at")
    private LocalDateTime postGeneratedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "companion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionCompanionNote> notes = new ArrayList<>();

    public SessionCompanion() {}

    public SessionCompanion(Long bookingId, Long studentId) {
        this.bookingId = bookingId;
        this.studentId = studentId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public String getPreSessionPlanMd() { return preSessionPlanMd; }
    public void setPreSessionPlanMd(String preSessionPlanMd) { this.preSessionPlanMd = preSessionPlanMd; }

    public Long getWarmupQuizId() { return warmupQuizId; }
    public void setWarmupQuizId(Long warmupQuizId) { this.warmupQuizId = warmupQuizId; }

    public Boolean getWarmupCompleted() { return warmupCompleted; }
    public void setWarmupCompleted(Boolean warmupCompleted) { this.warmupCompleted = warmupCompleted; }

    public String getLiveNotesMd() { return liveNotesMd; }
    public void setLiveNotesMd(String liveNotesMd) { this.liveNotesMd = liveNotesMd; }

    public List<String> getSessionHighlights() { return sessionHighlights; }
    public void setSessionHighlights(List<String> sessionHighlights) { this.sessionHighlights = sessionHighlights != null ? sessionHighlights : new ArrayList<>(); }

    public List<String> getQuestionsAsked() { return questionsAsked; }
    public void setQuestionsAsked(List<String> questionsAsked) { this.questionsAsked = questionsAsked != null ? questionsAsked : new ArrayList<>(); }

    public String getPostSessionSummaryMd() { return postSessionSummaryMd; }
    public void setPostSessionSummaryMd(String postSessionSummaryMd) { this.postSessionSummaryMd = postSessionSummaryMd; }

    public String getHomeworkPlanMd() { return homeworkPlanMd; }
    public void setHomeworkPlanMd(String homeworkPlanMd) { this.homeworkPlanMd = homeworkPlanMd; }

    public List<Long> getRecommendedTopics() { return recommendedTopics; }
    public void setRecommendedTopics(List<Long> recommendedTopics) { this.recommendedTopics = recommendedTopics != null ? recommendedTopics : new ArrayList<>(); }

    public String getMasterySuggestions() { return masterySuggestions; }
    public void setMasterySuggestions(String masterySuggestions) { this.masterySuggestions = masterySuggestions; }

    public CompanionStatus getStatus() { return status; }
    public void setStatus(CompanionStatus status) { this.status = status; }

    public LocalDateTime getPrepGeneratedAt() { return prepGeneratedAt; }
    public void setPrepGeneratedAt(LocalDateTime prepGeneratedAt) { this.prepGeneratedAt = prepGeneratedAt; }

    public LocalDateTime getLiveStartedAt() { return liveStartedAt; }
    public void setLiveStartedAt(LocalDateTime liveStartedAt) { this.liveStartedAt = liveStartedAt; }

    public LocalDateTime getPostGeneratedAt() { return postGeneratedAt; }
    public void setPostGeneratedAt(LocalDateTime postGeneratedAt) { this.postGeneratedAt = postGeneratedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SessionCompanionNote> getNotes() { return notes; }
    public void setNotes(List<SessionCompanionNote> notes) { this.notes = notes; }

    // Helper methods
    public void markPrepReady() {
        this.status = CompanionStatus.PREP_READY;
        this.prepGeneratedAt = LocalDateTime.now();
    }

    public void startLive() {
        this.status = CompanionStatus.LIVE;
        this.liveStartedAt = LocalDateTime.now();
    }

    public void markPostReady() {
        this.status = CompanionStatus.POST_READY;
        this.postGeneratedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = CompanionStatus.COMPLETED;
    }

    public void appendLiveNote(String note) {
        if (this.liveNotesMd == null) {
            this.liveNotesMd = note;
        } else {
            this.liveNotesMd = this.liveNotesMd + "\n\n" + note;
        }
    }

    public void addHighlight(String highlight) {
        if (this.sessionHighlights == null) {
            this.sessionHighlights = new ArrayList<>();
        }
        this.sessionHighlights.add(highlight);
    }

    public void addQuestion(String question) {
        if (this.questionsAsked == null) {
            this.questionsAsked = new ArrayList<>();
        }
        this.questionsAsked.add(question);
    }
}


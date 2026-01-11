package com.ankurshala.backend.entity;

import com.ankurshala.backend.util.HashMapConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Active study sessions with goals and AI coaching.
 */
@Entity
@Table(name = "student_focus_sessions")
public class StudentFocusSession {

    public enum SessionStatus {
        ACTIVE,
        PAUSED,
        ENDED,
        ABANDONED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "subject_id")
    private Long subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;

    @Column(name = "topic_id")
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    @Column(name = "goal_text", nullable = false, length = 500)
    private String goalText;

    @Column(name = "sprint_minutes", nullable = false)
    private Integer sprintMinutes = 15;

    @Column(name = "language", nullable = false, length = 20)
    private String language = "en";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep = 0;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_active_seconds")
    private Integer totalActiveSeconds = 0;

    @Column(name = "steps_completed")
    private Integer stepsCompleted = 0;

    @Column(name = "questions_answered")
    private Integer questionsAnswered = 0;

    @Column(name = "questions_correct")
    private Integer questionsCorrect = 0;

    @Convert(converter = HashMapConverter.class)
    @Column(name = "session_plan_json", columnDefinition = "jsonb")
    private Map<String, Object> sessionPlanJson;

    @OneToMany(mappedBy = "focusSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SprintCheckin> checkins = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentFocusSession() {}

    public StudentFocusSession(Long studentId, String goalText) {
        this.studentId = studentId;
        this.goalText = goalText;
        this.startedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public String getGoalText() { return goalText; }
    public void setGoalText(String goalText) { this.goalText = goalText; }

    public Integer getSprintMinutes() { return sprintMinutes; }
    public void setSprintMinutes(Integer sprintMinutes) { this.sprintMinutes = sprintMinutes; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public Integer getCurrentStep() { return currentStep; }
    public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getPausedAt() { return pausedAt; }
    public void setPausedAt(LocalDateTime pausedAt) { this.pausedAt = pausedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public Integer getTotalActiveSeconds() { return totalActiveSeconds; }
    public void setTotalActiveSeconds(Integer totalActiveSeconds) { this.totalActiveSeconds = totalActiveSeconds; }

    public Integer getStepsCompleted() { return stepsCompleted; }
    public void setStepsCompleted(Integer stepsCompleted) { this.stepsCompleted = stepsCompleted; }

    public Integer getQuestionsAnswered() { return questionsAnswered; }
    public void setQuestionsAnswered(Integer questionsAnswered) { this.questionsAnswered = questionsAnswered; }

    public Integer getQuestionsCorrect() { return questionsCorrect; }
    public void setQuestionsCorrect(Integer questionsCorrect) { this.questionsCorrect = questionsCorrect; }

    public Map<String, Object> getSessionPlanJson() { return sessionPlanJson; }
    public void setSessionPlanJson(Map<String, Object> sessionPlanJson) { this.sessionPlanJson = sessionPlanJson; }

    public List<SprintCheckin> getCheckins() { return checkins; }
    public void setCheckins(List<SprintCheckin> checkins) { this.checkins = checkins; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Pause the session
     */
    public void pause() {
        if (this.status == SessionStatus.ACTIVE) {
            this.status = SessionStatus.PAUSED;
            this.pausedAt = LocalDateTime.now();
        }
    }

    /**
     * Resume the session
     */
    public void resume() {
        if (this.status == SessionStatus.PAUSED && this.pausedAt != null) {
            this.status = SessionStatus.ACTIVE;
            this.pausedAt = null;
        }
    }

    /**
     * End the session
     */
    public void end() {
        this.status = SessionStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Mark session as abandoned
     */
    public void abandon() {
        this.status = SessionStatus.ABANDONED;
        this.endedAt = LocalDateTime.now();
    }

    /**
     * Add a check-in to the session
     */
    public void addCheckin(SprintCheckin checkin) {
        checkins.add(checkin);
        checkin.setFocusSession(this);
    }
}


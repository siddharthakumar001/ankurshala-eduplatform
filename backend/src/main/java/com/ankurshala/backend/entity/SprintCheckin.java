package com.ankurshala.backend.entity;

import com.ankurshala.backend.util.HashMapConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Step-by-step AI check-ins within a focus session.
 */
@Entity
@Table(name = "sprint_checkins",
       uniqueConstraints = @UniqueConstraint(columnNames = {"focus_session_id", "step_number"}))
public class SprintCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "focus_session_id", nullable = false)
    private StudentFocusSession focusSession;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Convert(converter = HashMapConverter.class)
    @Column(name = "ai_plan_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> aiPlanJson;

    @Column(name = "student_response_text", columnDefinition = "TEXT")
    private String studentResponseText;

    @Column(name = "response_correct")
    private Boolean responseCorrect;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SprintCheckin() {}

    public SprintCheckin(StudentFocusSession focusSession, Integer stepNumber, Map<String, Object> aiPlanJson) {
        this.focusSession = focusSession;
        this.stepNumber = stepNumber;
        this.aiPlanJson = aiPlanJson;
        this.startedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentFocusSession getFocusSession() { return focusSession; }
    public void setFocusSession(StudentFocusSession focusSession) { this.focusSession = focusSession; }

    public Integer getStepNumber() { return stepNumber; }
    public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }

    public Map<String, Object> getAiPlanJson() { return aiPlanJson; }
    public void setAiPlanJson(Map<String, Object> aiPlanJson) { this.aiPlanJson = aiPlanJson; }

    public String getStudentResponseText() { return studentResponseText; }
    public void setStudentResponseText(String studentResponseText) { this.studentResponseText = studentResponseText; }

    public Boolean getResponseCorrect() { return responseCorrect; }
    public void setResponseCorrect(Boolean responseCorrect) { this.responseCorrect = responseCorrect; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public Integer getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Integer timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Record student response to this check-in
     */
    public void recordResponse(String responseText, Boolean isCorrect) {
        this.studentResponseText = responseText;
        this.responseCorrect = isCorrect;
        this.respondedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.timeSpentSeconds = (int) ChronoUnit.SECONDS.between(this.startedAt, this.respondedAt);
        }
    }
}


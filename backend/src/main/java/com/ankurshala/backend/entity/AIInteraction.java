package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Privacy-preserving AI interaction logging for observability.
 * Does NOT store raw user text - only metadata for analytics and debugging.
 */
@Entity
@Table(name = "ai_interactions")
public class AIInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id")
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 50)
    private InteractionType interactionType;

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

    @Column(name = "model_provider", length = 50)
    private String modelProvider;  // OPENAI, ANTHROPIC, GEMINI, etc.

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "retrieval_count")
    private Integer retrievalCount;

    @Column(name = "retrieval_score_avg", precision = 5, scale = 4)
    private BigDecimal retrievalScoreAvg;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "safety_flags", columnDefinition = "JSONB")
    private Map<String, Object> safetyFlags;

    @Column(name = "was_refused", nullable = false)
    private Boolean wasRefused = false;

    @Column(name = "refusal_reason", length = 500)
    private String refusalReason;

    @Column(name = "had_error", nullable = false)
    private Boolean hadError = false;

    @Column(name = "error_type", length = 100)
    private String errorType;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AIInteraction() {}

    public AIInteraction(InteractionType type, Long studentId) {
        this.interactionType = type;
        this.studentId = studentId;
    }

    // Builder-style setters for fluent API
    public AIInteraction withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public AIInteraction withTopic(Long topicId) {
        this.topicId = topicId;
        return this;
    }

    public AIInteraction withSubject(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public AIInteraction withModel(String provider, String name, String version) {
        this.modelProvider = provider;
        this.modelName = name;
        this.modelVersion = version;
        return this;
    }

    public AIInteraction withLatency(int latencyMs) {
        this.latencyMs = latencyMs;
        return this;
    }

    public AIInteraction withTokens(int prompt, int completion) {
        this.promptTokens = prompt;
        this.completionTokens = completion;
        this.totalTokens = prompt + completion;
        return this;
    }

    public AIInteraction withRetrieval(int count, BigDecimal avgScore) {
        this.retrievalCount = count;
        this.retrievalScoreAvg = avgScore;
        return this;
    }

    public AIInteraction withSafetyFlags(Map<String, Object> flags) {
        this.safetyFlags = flags;
        return this;
    }

    public AIInteraction markRefused(String reason) {
        this.wasRefused = true;
        this.refusalReason = reason;
        return this;
    }

    public AIInteraction markError(String type, String message) {
        this.hadError = true;
        this.errorType = type;
        this.errorMessage = message;
        return this;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public InteractionType getInteractionType() { return interactionType; }
    public void setInteractionType(InteractionType interactionType) { this.interactionType = interactionType; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public String getModelProvider() { return modelProvider; }
    public void setModelProvider(String modelProvider) { this.modelProvider = modelProvider; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }

    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }

    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }

    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }

    public Integer getRetrievalCount() { return retrievalCount; }
    public void setRetrievalCount(Integer retrievalCount) { this.retrievalCount = retrievalCount; }

    public BigDecimal getRetrievalScoreAvg() { return retrievalScoreAvg; }
    public void setRetrievalScoreAvg(BigDecimal retrievalScoreAvg) { this.retrievalScoreAvg = retrievalScoreAvg; }

    public Map<String, Object> getSafetyFlags() { return safetyFlags; }
    public void setSafetyFlags(Map<String, Object> safetyFlags) { this.safetyFlags = safetyFlags; }

    public Boolean getWasRefused() { return wasRefused; }
    public void setWasRefused(Boolean wasRefused) { this.wasRefused = wasRefused; }

    public String getRefusalReason() { return refusalReason; }
    public void setRefusalReason(String refusalReason) { this.refusalReason = refusalReason; }

    public Boolean getHadError() { return hadError; }
    public void setHadError(Boolean hadError) { this.hadError = hadError; }

    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Enum for interaction types
    public enum InteractionType {
        CHAT,
        QUIZ_GENERATE,
        QUIZ_GRADE,
        RECOMMENDATION,
        SUMMARY,
        CONTENT_SEARCH
    }
}


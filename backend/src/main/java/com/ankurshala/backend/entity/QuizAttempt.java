package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Student attempts at quizzes with scoring and AI feedback.
 */
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", insertable = false, updatable = false)
    private Quiz quiz;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "total_score", precision = 6, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "max_score", precision = 6, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "graded_by", length = 100)
    private String gradedBy;  // 'AUTO', 'AI', or admin user ID

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strengths", columnDefinition = "JSONB")
    private List<String> strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weaknesses", columnDefinition = "JSONB")
    private List<String> weaknesses;

    @Column(name = "mastery_delta", precision = 4, scale = 3)
    private BigDecimal masteryDelta;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<QuizAnswer> answers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public QuizAttempt() {
        this.startedAt = LocalDateTime.now();
    }

    public QuizAttempt(Long quizId, Long studentId) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.startedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Integer timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }

    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public String getGradedBy() { return gradedBy; }
    public void setGradedBy(String gradedBy) { this.gradedBy = gradedBy; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

    public BigDecimal getMasteryDelta() { return masteryDelta; }
    public void setMasteryDelta(BigDecimal masteryDelta) { this.masteryDelta = masteryDelta; }

    public List<QuizAnswer> getAnswers() { return answers; }
    public void setAnswers(List<QuizAnswer> answers) { this.answers = answers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addAnswer(QuizAnswer answer) {
        answers.add(answer);
        answer.setAttempt(this);
    }

    /**
     * Submit the attempt and calculate scores
     */
    public void submit() {
        this.submittedAt = LocalDateTime.now();
        this.status = AttemptStatus.SUBMITTED;
        
        if (this.startedAt != null) {
            this.timeSpentSeconds = (int) java.time.Duration.between(startedAt, submittedAt).getSeconds();
        }
    }

    /**
     * Calculate scores from answers
     */
    public void calculateScores() {
        if (answers == null || answers.isEmpty()) {
            this.totalScore = BigDecimal.ZERO;
            this.maxScore = BigDecimal.ZERO;
            this.percentage = BigDecimal.ZERO;
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;

        for (QuizAnswer answer : answers) {
            if (answer.getScore() != null) {
                total = total.add(answer.getScore());
            }
            if (answer.getMaxScore() != null) {
                max = max.add(answer.getMaxScore());
            }
        }

        this.totalScore = total;
        this.maxScore = max;

        if (max.compareTo(BigDecimal.ZERO) > 0) {
            this.percentage = total.multiply(new BigDecimal("100"))
                    .divide(max, 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.percentage = BigDecimal.ZERO;
        }
    }

    // Enum for attempt status
    public enum AttemptStatus {
        IN_PROGRESS,
        SUBMITTED,
        GRADED,
        ABANDONED
    }
}


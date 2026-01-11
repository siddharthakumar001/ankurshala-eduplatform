package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Individual answers within quiz attempts.
 */
@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", insertable = false, updatable = false)
    private QuizAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private QuizQuestion question;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_option_ids", columnDefinition = "JSONB")
    private List<String> selectedOptionIds;  // For MCQ: array of selected option IDs

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "grading_feedback", columnDefinition = "TEXT")
    private String gradingFeedback;

    @Column(name = "grading_rationale", columnDefinition = "TEXT")
    private String gradingRationale;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public QuizAnswer() {}

    public QuizAnswer(Long attemptId, Long questionId) {
        this.attemptId = attemptId;
        this.questionId = questionId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

    public QuizAttempt getAttempt() { return attempt; }
    public void setAttempt(QuizAttempt attempt) { 
        this.attempt = attempt;
        if (attempt != null) {
            this.attemptId = attempt.getId();
        }
    }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public QuizQuestion getQuestion() { return question; }
    public void setQuestion(QuizQuestion question) { this.question = question; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public List<String> getSelectedOptionIds() { return selectedOptionIds; }
    public void setSelectedOptionIds(List<String> selectedOptionIds) { this.selectedOptionIds = selectedOptionIds; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }

    public String getGradingFeedback() { return gradingFeedback; }
    public void setGradingFeedback(String gradingFeedback) { this.gradingFeedback = gradingFeedback; }

    public String getGradingRationale() { return gradingRationale; }
    public void setGradingRationale(String gradingRationale) { this.gradingRationale = gradingRationale; }

    public Integer getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Integer timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Grade this answer for MCQ questions
     */
    public void gradeAsMcq(QuizQuestion question) {
        if (question == null || question.getQuestionType() != QuizQuestion.QuestionType.MCQ) {
            return;
        }

        this.maxScore = BigDecimal.valueOf(question.getPoints());
        this.answeredAt = LocalDateTime.now();

        if (selectedOptionIds != null && question.isAnswerCorrect(selectedOptionIds)) {
            this.isCorrect = true;
            this.score = this.maxScore;
            this.gradingFeedback = "Correct!";
        } else {
            this.isCorrect = false;
            this.score = BigDecimal.ZERO;
            this.gradingFeedback = "Incorrect. " + (question.getExplanation() != null ? question.getExplanation() : "");
        }
    }
}


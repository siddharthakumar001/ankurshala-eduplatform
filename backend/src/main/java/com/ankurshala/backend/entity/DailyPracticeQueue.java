package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Daily practice queue items generated from weak topics.
 */
@Entity
@Table(name = "daily_practice_queue")
public class DailyPracticeQueue {

    public enum PracticeStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        SKIPPED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    @Column(name = "scheduled_for_date", nullable = false)
    private LocalDate scheduledForDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PracticeStatus status = PracticeStatus.PENDING;

    @Column(name = "quiz_id")
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", insertable = false, updatable = false)
    private Quiz quiz;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "questions_answered")
    private Integer questionsAnswered;

    @Column(name = "questions_correct")
    private Integer questionsCorrect;

    @Column(name = "mastery_delta", precision = 4, scale = 3)
    private BigDecimal masteryDelta;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DailyPracticeQueue() {}

    public DailyPracticeQueue(Long studentId, Long topicId, LocalDate scheduledForDate) {
        this.studentId = studentId;
        this.topicId = topicId;
        this.scheduledForDate = scheduledForDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public LocalDate getScheduledForDate() { return scheduledForDate; }
    public void setScheduledForDate(LocalDate scheduledForDate) { this.scheduledForDate = scheduledForDate; }

    public PracticeStatus getStatus() { return status; }
    public void setStatus(PracticeStatus status) { this.status = status; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Integer getQuestionsAnswered() { return questionsAnswered; }
    public void setQuestionsAnswered(Integer questionsAnswered) { this.questionsAnswered = questionsAnswered; }

    public Integer getQuestionsCorrect() { return questionsCorrect; }
    public void setQuestionsCorrect(Integer questionsCorrect) { this.questionsCorrect = questionsCorrect; }

    public BigDecimal getMasteryDelta() { return masteryDelta; }
    public void setMasteryDelta(BigDecimal masteryDelta) { this.masteryDelta = masteryDelta; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Integer timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Start the practice
     */
    public void start() {
        this.status = PracticeStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }

    /**
     * Complete the practice with results
     */
    public void complete(int answered, int correct, BigDecimal scorePct) {
        this.status = PracticeStatus.COMPLETED;
        this.questionsAnswered = answered;
        this.questionsCorrect = correct;
        this.score = scorePct;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.timeSpentSeconds = (int) java.time.Duration.between(this.startedAt, this.completedAt).getSeconds();
        }
    }

    /**
     * Skip this practice
     */
    public void skip() {
        this.status = PracticeStatus.SKIPPED;
    }
}


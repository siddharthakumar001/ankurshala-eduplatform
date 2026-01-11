package com.ankurshala.backend.entity;

import com.ankurshala.backend.util.HashMapConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Individual questions within quizzes, supporting multiple question types.
 */
@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", insertable = false, updatable = false)
    private Quiz quiz;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    private QuestionType questionType = QuestionType.MCQ;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "JSONB")
    private List<Map<String, Object>> options;  // Array of {id, text, isCorrect}

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;  // For non-MCQ types

    @Column(name = "points", nullable = false)
    private Integer points = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rubric", columnDefinition = "JSONB")
    private Map<String, Object> rubric;

    @Column(name = "expected_answer_guide", columnDefinition = "TEXT")
    private String expectedAnswerGuide;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "hint", columnDefinition = "TEXT")
    private String hint;

    @Enumerated(EnumType.STRING)
    @Column(name = "bloom_level", length = 50)
    private Quiz.BloomLevel bloomLevel;

    @Column(name = "difficulty", length = 50)
    private String difficulty;

    @Column(name = "topic_id")
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber = 0;

    @Column(name = "language", nullable = false, length = 20)
    private String language = "en";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public QuizQuestion() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { 
        this.quiz = quiz;
        if (quiz != null) {
            this.quizId = quiz.getId();
        }
    }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public List<Map<String, Object>> getOptions() { return options; }
    public void setOptions(List<Map<String, Object>> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Map<String, Object> getRubric() { return rubric; }
    public void setRubric(Map<String, Object> rubric) { this.rubric = rubric; }

    public String getExpectedAnswerGuide() { return expectedAnswerGuide; }
    public void setExpectedAnswerGuide(String expectedAnswerGuide) { this.expectedAnswerGuide = expectedAnswerGuide; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public Quiz.BloomLevel getBloomLevel() { return bloomLevel; }
    public void setBloomLevel(Quiz.BloomLevel bloomLevel) { this.bloomLevel = bloomLevel; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Check if a given answer is correct for MCQ questions
     */
    public boolean isAnswerCorrect(List<String> selectedOptionIds) {
        if (questionType != QuestionType.MCQ || options == null) {
            return false;
        }
        
        for (Map<String, Object> option : options) {
            String optionId = String.valueOf(option.get("id"));
            Boolean isCorrect = (Boolean) option.get("isCorrect");
            
            if (Boolean.TRUE.equals(isCorrect)) {
                return selectedOptionIds.contains(optionId);
            }
        }
        return false;
    }

    // Enum for question types
    public enum QuestionType {
        MCQ,              // Multiple Choice Question
        SHORT_ANSWER,     // Short text answer
        LONG_ANSWER,      // Long text/essay answer
        TRUE_FALSE,       // True/False
        FILL_BLANK,       // Fill in the blank
        ASSERTION_REASON  // Assertion-Reason type (common in Indian boards)
    }
}


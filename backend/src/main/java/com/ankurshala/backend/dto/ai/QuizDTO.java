package com.ankurshala.backend.dto.ai;

import com.ankurshala.backend.entity.Quiz;
import com.ankurshala.backend.entity.QuizQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for quiz generation, attempts, and grading
 */
public class QuizDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateRequest {
        @NotNull(message = "Topic ID is required")
        private Long topicId;
        
        private Long subjectId;
        
        @Min(value = 1, message = "Must have at least 1 question")
        @Max(value = 20, message = "Maximum 20 questions per quiz")
        private Integer numQuestions = 5;
        
        private String difficulty = "MEDIUM";  // EASY, MEDIUM, HARD, ADAPTIVE
        
        private String bloomLevel;  // REMEMBER, UNDERSTAND, APPLY, ANALYZE, EVALUATE, CREATE
        
        private List<String> questionTypes;  // MCQ, SHORT_ANSWER, TRUE_FALSE, etc.
        
        private String language = "en";
        
        private Integer timeLimitMinutes;
        
        private String quizType = "PRACTICE";  // PRACTICE, ASSESSMENT, DIAGNOSTIC
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizResponse {
        private Long quizId;
        private String title;
        private String description;
        private Long topicId;
        private String topicTitle;
        private Long subjectId;
        private String subjectName;
        private String difficulty;
        private String bloomLevel;
        private Integer totalQuestions;
        private Integer timeLimitMinutes;
        private String quizType;
        private String language;
        private List<QuestionResponse> questions;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResponse {
        private Long questionId;
        private Integer sequenceNumber;
        private String questionText;
        private String questionType;  // MCQ, SHORT_ANSWER, etc.
        private List<OptionResponse> options;  // For MCQ
        private Integer points;
        private String difficulty;
        private String bloomLevel;
        private String hint;  // Optional hint for students
        // Note: correctAnswer and explanation NOT included until after grading
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResponse {
        private String id;
        private String text;
        // Note: isCorrect NOT included in response to student
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartAttemptRequest {
        @NotNull(message = "Quiz ID is required")
        private Long quizId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptResponse {
        private Long attemptId;
        private Long quizId;
        private String quizTitle;
        private Long studentId;
        private LocalDateTime startedAt;
        private LocalDateTime submittedAt;
        private String status;  // IN_PROGRESS, SUBMITTED, GRADED
        private Integer timeSpentSeconds;
        private BigDecimal totalScore;
        private BigDecimal maxScore;
        private BigDecimal percentage;
        private List<AnswerResponse> answers;
        private String aiFeedback;
        private List<String> strengths;
        private List<String> weaknesses;
        private BigDecimal masteryDelta;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitAnswerRequest {
        @NotNull(message = "Question ID is required")
        private Long questionId;
        
        private String answerText;  // For text-based answers
        
        private List<String> selectedOptionIds;  // For MCQ
        
        private Integer timeSpentSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitAttemptRequest {
        @NotNull(message = "Attempt ID is required")
        private Long attemptId;
        
        @NotEmpty(message = "At least one answer is required")
        private List<SubmitAnswerRequest> answers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerResponse {
        private Long answerId;
        private Long questionId;
        private String questionText;
        private String answerText;
        private List<String> selectedOptionIds;
        private Boolean isCorrect;
        private BigDecimal score;
        private BigDecimal maxScore;
        private String correctAnswer;  // Shown after grading
        private String explanation;  // Shown after grading
        private String gradingFeedback;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeAttemptResponse {
        private Long attemptId;
        private BigDecimal totalScore;
        private BigDecimal maxScore;
        private BigDecimal percentage;
        private Integer correctCount;
        private Integer totalQuestions;
        private String overallFeedback;
        private List<String> strengths;
        private List<String> weaknesses;
        private List<String> recommendations;
        private BigDecimal masteryBefore;
        private BigDecimal masteryAfter;
        private BigDecimal masteryDelta;
        private List<AnswerResponse> gradedAnswers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizHistoryItem {
        private Long attemptId;
        private Long quizId;
        private String quizTitle;
        private String topicTitle;
        private String subjectName;
        private LocalDateTime attemptedAt;
        private BigDecimal percentage;
        private Integer correctCount;
        private Integer totalQuestions;
        private String status;
    }
}


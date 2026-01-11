package com.ankurshala.backend.dto.student;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for Daily Practice feature.
 */
public class PracticeDTO {

    // ==================== PREFERENCES ====================

    public static class PracticePreferencesResponse {
        private Boolean enabled;
        private Integer dailyQuestionCount;
        private String preferredTimeLocal;
        private String language;
        private Boolean notificationEnabled;

        public PracticePreferencesResponse() {}

        public PracticePreferencesResponse(Boolean enabled, Integer dailyQuestionCount, 
                                           String preferredTimeLocal, String language,
                                           Boolean notificationEnabled) {
            this.enabled = enabled;
            this.dailyQuestionCount = dailyQuestionCount;
            this.preferredTimeLocal = preferredTimeLocal;
            this.language = language;
            this.notificationEnabled = notificationEnabled;
        }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Integer getDailyQuestionCount() { return dailyQuestionCount; }
        public void setDailyQuestionCount(Integer dailyQuestionCount) { this.dailyQuestionCount = dailyQuestionCount; }

        public String getPreferredTimeLocal() { return preferredTimeLocal; }
        public void setPreferredTimeLocal(String preferredTimeLocal) { this.preferredTimeLocal = preferredTimeLocal; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public Boolean getNotificationEnabled() { return notificationEnabled; }
        public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
    }

    public static class UpdatePracticePreferencesRequest {
        private Boolean enabled;

        @Min(1) @Max(20)
        private Integer dailyQuestionCount;

        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Time must be in HH:mm format")
        private String preferredTimeLocal;

        @Pattern(regexp = "^(en|hi)$", message = "Language must be 'en' or 'hi'")
        private String language;

        private Boolean notificationEnabled;

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Integer getDailyQuestionCount() { return dailyQuestionCount; }
        public void setDailyQuestionCount(Integer dailyQuestionCount) { this.dailyQuestionCount = dailyQuestionCount; }

        public String getPreferredTimeLocal() { return preferredTimeLocal; }
        public void setPreferredTimeLocal(String preferredTimeLocal) { this.preferredTimeLocal = preferredTimeLocal; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public Boolean getNotificationEnabled() { return notificationEnabled; }
        public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
    }

    // ==================== PRACTICE ITEMS ====================

    public static class PracticeItemResponse {
        private Long id;
        private Long topicId;
        private String topicName;
        private String subjectName;
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate scheduledForDate;
        private Long quizId;
        private BigDecimal score;
        private Integer questionsAnswered;
        private Integer questionsCorrect;
        private BigDecimal masteryDelta;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startedAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime completedAt;
        private Integer timeSpentSeconds;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getTopicId() { return topicId; }
        public void setTopicId(Long topicId) { this.topicId = topicId; }

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDate getScheduledForDate() { return scheduledForDate; }
        public void setScheduledForDate(LocalDate scheduledForDate) { this.scheduledForDate = scheduledForDate; }

        public Long getQuizId() { return quizId; }
        public void setQuizId(Long quizId) { this.quizId = quizId; }

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
    }

    // ==================== TODAY'S PRACTICE ====================

    public static class TodayPracticeResponse {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private int totalItems;
        private int completedItems;
        private int pendingItems;
        private List<PracticeItemResponse> items;
        private String nextRecommendedTopicName;
        private Long nextRecommendedTopicId;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

        public int getCompletedItems() { return completedItems; }
        public void setCompletedItems(int completedItems) { this.completedItems = completedItems; }

        public int getPendingItems() { return pendingItems; }
        public void setPendingItems(int pendingItems) { this.pendingItems = pendingItems; }

        public List<PracticeItemResponse> getItems() { return items; }
        public void setItems(List<PracticeItemResponse> items) { this.items = items; }

        public String getNextRecommendedTopicName() { return nextRecommendedTopicName; }
        public void setNextRecommendedTopicName(String nextRecommendedTopicName) { 
            this.nextRecommendedTopicName = nextRecommendedTopicName; 
        }

        public Long getNextRecommendedTopicId() { return nextRecommendedTopicId; }
        public void setNextRecommendedTopicId(Long nextRecommendedTopicId) { 
            this.nextRecommendedTopicId = nextRecommendedTopicId; 
        }
    }

    // ==================== START PRACTICE ====================

    public static class StartPracticeRequest {
        @NotNull
        private Long practiceId;

        public Long getPracticeId() { return practiceId; }
        public void setPracticeId(Long practiceId) { this.practiceId = practiceId; }
    }

    public static class StartPracticeResponse {
        private Long practiceId;
        private Long quizId;
        private String topicName;
        private List<QuizQuestionResponse> questions;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime startedAt;

        public Long getPracticeId() { return practiceId; }
        public void setPracticeId(Long practiceId) { this.practiceId = practiceId; }

        public Long getQuizId() { return quizId; }
        public void setQuizId(Long quizId) { this.quizId = quizId; }

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }

        public List<QuizQuestionResponse> getQuestions() { return questions; }
        public void setQuestions(List<QuizQuestionResponse> questions) { this.questions = questions; }

        public LocalDateTime getStartedAt() { return startedAt; }
        public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    }

    public static class QuizQuestionResponse {
        private Long id;
        private String questionText;
        private List<String> options;
        private String questionType;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }
    }

    // ==================== SUBMIT PRACTICE ====================

    public static class SubmitPracticeRequest {
        @NotNull
        private List<AnswerSubmission> answers;

        public List<AnswerSubmission> getAnswers() { return answers; }
        public void setAnswers(List<AnswerSubmission> answers) { this.answers = answers; }
    }

    public static class AnswerSubmission {
        @NotNull
        private Long questionId;
        @NotNull
        private String selectedAnswer;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getSelectedAnswer() { return selectedAnswer; }
        public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }
    }

    public static class SubmitPracticeResponse {
        private Long practiceId;
        private BigDecimal score;
        private Integer questionsAnswered;
        private Integer questionsCorrect;
        private BigDecimal masteryDelta;
        private BigDecimal newMasteryScore;
        private String feedback;
        private List<QuestionResultResponse> questionResults;
        private NextRecommendation nextRecommendation;

        public Long getPracticeId() { return practiceId; }
        public void setPracticeId(Long practiceId) { this.practiceId = practiceId; }

        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }

        public Integer getQuestionsAnswered() { return questionsAnswered; }
        public void setQuestionsAnswered(Integer questionsAnswered) { this.questionsAnswered = questionsAnswered; }

        public Integer getQuestionsCorrect() { return questionsCorrect; }
        public void setQuestionsCorrect(Integer questionsCorrect) { this.questionsCorrect = questionsCorrect; }

        public BigDecimal getMasteryDelta() { return masteryDelta; }
        public void setMasteryDelta(BigDecimal masteryDelta) { this.masteryDelta = masteryDelta; }

        public BigDecimal getNewMasteryScore() { return newMasteryScore; }
        public void setNewMasteryScore(BigDecimal newMasteryScore) { this.newMasteryScore = newMasteryScore; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }

        public List<QuestionResultResponse> getQuestionResults() { return questionResults; }
        public void setQuestionResults(List<QuestionResultResponse> questionResults) { 
            this.questionResults = questionResults; 
        }

        public NextRecommendation getNextRecommendation() { return nextRecommendation; }
        public void setNextRecommendation(NextRecommendation nextRecommendation) { 
            this.nextRecommendation = nextRecommendation; 
        }
    }

    public static class QuestionResultResponse {
        private Long questionId;
        private String questionText;
        private String selectedAnswer;
        private String correctAnswer;
        private boolean correct;
        private String explanation;

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getSelectedAnswer() { return selectedAnswer; }
        public void setSelectedAnswer(String selectedAnswer) { this.selectedAnswer = selectedAnswer; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }

        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public static class NextRecommendation {
        private String type;  // CONTINUE_TOPIC, MOVE_TO_NEXT, MASTERED
        private Long topicId;
        private String topicName;
        private String message;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Long getTopicId() { return topicId; }
        public void setTopicId(Long topicId) { this.topicId = topicId; }

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // ==================== PRACTICE HISTORY ====================

    public static class PracticeHistoryResponse {
        private List<PracticeDaySummary> days;
        private PracticeStats stats;

        public List<PracticeDaySummary> getDays() { return days; }
        public void setDays(List<PracticeDaySummary> days) { this.days = days; }

        public PracticeStats getStats() { return stats; }
        public void setStats(PracticeStats stats) { this.stats = stats; }
    }

    public static class PracticeDaySummary {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        private int totalItems;
        private int completedItems;
        private BigDecimal averageScore;
        private List<PracticeItemResponse> items;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }

        public int getCompletedItems() { return completedItems; }
        public void setCompletedItems(int completedItems) { this.completedItems = completedItems; }

        public BigDecimal getAverageScore() { return averageScore; }
        public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }

        public List<PracticeItemResponse> getItems() { return items; }
        public void setItems(List<PracticeItemResponse> items) { this.items = items; }
    }

    public static class PracticeStats {
        private int totalDaysActive;
        private int currentStreak;
        private int longestStreak;
        private int totalQuestionsAnswered;
        private int totalQuestionsCorrect;
        private BigDecimal averageAccuracy;

        public int getTotalDaysActive() { return totalDaysActive; }
        public void setTotalDaysActive(int totalDaysActive) { this.totalDaysActive = totalDaysActive; }

        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

        public int getLongestStreak() { return longestStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

        public int getTotalQuestionsAnswered() { return totalQuestionsAnswered; }
        public void setTotalQuestionsAnswered(int totalQuestionsAnswered) { 
            this.totalQuestionsAnswered = totalQuestionsAnswered; 
        }

        public int getTotalQuestionsCorrect() { return totalQuestionsCorrect; }
        public void setTotalQuestionsCorrect(int totalQuestionsCorrect) { 
            this.totalQuestionsCorrect = totalQuestionsCorrect; 
        }

        public BigDecimal getAverageAccuracy() { return averageAccuracy; }
        public void setAverageAccuracy(BigDecimal averageAccuracy) { this.averageAccuracy = averageAccuracy; }
    }
}


package com.ankurshala.backend.dto.student;

import com.ankurshala.backend.entity.StudentFocusSession.SessionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for Focus Mode + Study Sprints feature.
 */
public class FocusDTO {

    // ===================== Settings DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusSettingsResponse {
        private Long id;
        private Long studentId;
        private Boolean focusEnabled;
        private Integer defaultSprintMinutes;
        private String languagePref;
        private Boolean reminderEnabled;
        private Integer reminderBeforeMinutes;
        private Boolean soundEnabled;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateFocusSettingsRequest {
        private Boolean focusEnabled;

        @Min(value = 10, message = "Sprint duration must be at least 10 minutes")
        @Max(value = 30, message = "Sprint duration must not exceed 30 minutes")
        private Integer defaultSprintMinutes;

        private String languagePref;
        private Boolean reminderEnabled;
        private Integer reminderBeforeMinutes;
        private Boolean soundEnabled;
    }

    // ===================== Session DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartSessionRequest {
        @NotNull(message = "Topic ID is required")
        private Long topicId;

        @NotBlank(message = "Goal is required")
        @Size(max = 500, message = "Goal must not exceed 500 characters")
        private String goalText;

        @Min(value = 10, message = "Sprint duration must be at least 10 minutes")
        @Max(value = 30, message = "Sprint duration must not exceed 30 minutes")
        @Builder.Default
        private Integer sprintMinutes = 15;

        @Builder.Default
        private String language = "en";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionResponse {
        private Long id;
        private Long studentId;
        private Long subjectId;
        private String subjectName;
        private Long topicId;
        private String topicTitle;
        private String goalText;
        private Integer sprintMinutes;
        private String language;
        private SessionStatus status;
        private Integer currentStep;
        private LocalDateTime startedAt;
        private LocalDateTime pausedAt;
        private LocalDateTime endedAt;
        private Integer totalActiveSeconds;
        private Integer stepsCompleted;
        private Integer questionsAnswered;
        private Integer questionsCorrect;
        private Map<String, Object> sessionPlan;
        private List<CheckinResponse> checkins;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionListItem {
        private Long id;
        private Long topicId;
        private String topicTitle;
        private String subjectName;
        private String goalText;
        private SessionStatus status;
        private Integer sprintMinutes;
        private Integer stepsCompleted;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionsListResponse {
        private List<SessionListItem> sessions;
        private long totalCount;
        private int page;
        private int size;
    }

    // ===================== Check-in DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckinRequest {
        private String responseText;  // Student's answer to the check-in question
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckinResponse {
        private Long id;
        private Long sessionId;
        private Integer stepNumber;
        private String explanation;
        private String question;
        private String questionType;  // MCQ, SHORT_ANSWER, etc.
        private List<String> options;  // For MCQ questions
        private String correctAnswer;  // Revealed after response
        private List<SuggestedAction> suggestedActions;
        private String studentResponseText;
        private Boolean responseCorrect;
        private String feedback;  // AI feedback on response
        private LocalDateTime startedAt;
        private LocalDateTime respondedAt;
        private Integer timeSpentSeconds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedAction {
        private String actionType;  // TAKE_MICRO_QUIZ, GENERATE_NOTES, VIEW_PREREQ, ASK_TEACHER
        private String actionLabel;
        private Map<String, Object> actionData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateCheckinResponse {
        private CheckinResponse checkin;
        private String message;
        private Integer tokensUsed;
        private Integer latencyMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitCheckinResponse {
        private CheckinResponse checkin;
        private Boolean isCorrect;
        private String feedback;
        private CheckinResponse nextCheckin;  // Optional: pre-loaded next step
        private Boolean sessionComplete;
    }

    // ===================== Stats DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusStatsResponse {
        private Long totalSessions;
        private Long completedSessions;
        private Long totalFocusTimeMinutes;
        private Long totalQuestionsAnswered;
        private Long totalQuestionsCorrect;
        private Double averageAccuracy;
        private List<TopicFocusStats> topicStats;
        private List<DailyFocusStats> dailyStats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicFocusStats {
        private Long topicId;
        private String topicTitle;
        private Long sessionCount;
        private Long totalMinutes;
        private Double accuracy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyFocusStats {
        private String date;
        private Long sessions;
        private Long minutes;
        private Long questionsAnswered;
    }

    // ===================== AI Integration DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionPlan {
        private String overview;
        private List<StepPlan> steps;
        private String summary;
        private List<String> keyTakeaways;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepPlan {
        private Integer stepNumber;
        private String title;
        private String objective;
        private Integer estimatedMinutes;
    }
}


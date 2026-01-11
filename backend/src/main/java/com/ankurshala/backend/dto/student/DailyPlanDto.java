package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Daily Plan DTO - Student's personalized plan for today
 * Contains all recommended activities and next steps for the day
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanDto {
    
    /**
     * Plan generation metadata
     */
    private LocalDateTime generatedAt;
    private LocalDateTime validUntil;
    private String planDate; // YYYY-MM-DD format
    
    /**
     * Next booked class summary
     */
    private UpcomingClassDto nextClass;
    
    /**
     * Weak topic recommendation
     */
    private WeakTopicRecommendationDto weakTopicRecommendation;
    
    /**
     * Today's practice items
     */
    private List<PracticeItemDto> practiceItems;
    
    /**
     * Revise note suggestion
     */
    private ReviseNoteSuggestionDto reviseNoteSuggestion;
    
    /**
     * Focus Sprint quick-start
     */
    private FocusSprintDto focusSprint;
    
    /**
     * Overall progress summary for today
     */
    private DailyProgressSummaryDto progress;
    
    /**
     * Upcoming Class DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingClassDto {
        private Long bookingId;
        private String topicName;
        private String teacherName;
        private LocalDateTime startTime;
        private Integer durationMinutes;
        private String status;
        private String companionCta; // "Prepare for class", "Join now", etc.
        private String companionLink; // Deep link to booking companion
    }
    
    /**
     * Weak Topic Recommendation DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeakTopicRecommendationDto {
        private Long topicId;
        private String topicName;
        private String reason; // Why recommended
        private List<String> prerequisiteGaps; // Missing prerequisites
        private Double confidenceScore;
        private String actionCta; // "Practice now", "Learn basics first", etc.
        private String actionLink; // Deep link to practice/learning flow
    }
    
    /**
     * Practice Item DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PracticeItemDto {
        private Long topicId;
        private String topicName;
        private String source; // SPACED_REPETITION, WEAK_TOPIC, ADAPTIVE
        private Boolean isDueForReview;
        private Integer priority;
        private String difficulty; // EASY, MEDIUM, HARD
        private String actionCta; // "Review now", "Practice more", etc.
        private String actionLink;
    }
    
    /**
     * Revise Note Suggestion DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviseNoteSuggestionDto {
        private Long noteId;
        private String noteTitle;
        private String topicName;
        private LocalDateTime createdAt;
        private String reason; // "From recent weak topic", "High importance", etc.
        private String actionCta; // "Revise now"
        private String actionLink;
    }
    
    /**
     * Focus Sprint DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FocusSprintDto {
        private Long topicId;
        private String topicName;
        private Integer defaultDurationMinutes;
        private String description;
        private String actionCta; // "Start 15-min sprint"
        private String actionLink;
    }
    
    /**
     * Daily Progress Summary DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyProgressSummaryDto {
        private Integer totalSteps;
        private Integer completedSteps;
        private Integer practiceCompleted;
        private Integer notesRevised;
        private Integer sprintsCompleted;
        private String motivationalMessage;
    }
}

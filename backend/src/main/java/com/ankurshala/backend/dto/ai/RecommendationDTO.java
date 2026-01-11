package com.ankurshala.backend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs for personalized recommendations
 */
public class RecommendationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrerequisiteRecommendation {
        private Long topicId;
        private String topicTitle;
        private String chapterName;
        private String subjectName;
        private BigDecimal currentMastery;
        private String masteryLevel;
        private String reason;  // Why this is recommended
        private Integer estimatedTimeMinutes;
        private Boolean isRequired;  // Is this prerequisite critical?
        private Integer priority;  // 1 = highest priority
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicRecommendationResponse {
        private Long targetTopicId;
        private String targetTopicTitle;
        private Boolean isReadyToLearn;
        private String readinessMessage;
        private List<PrerequisiteRecommendation> requiredPrerequisites;
        private List<PrerequisiteRecommendation> recommendedPrerequisites;
        private BigDecimal overallReadinessScore;  // 0.0 to 1.0
        private List<String> suggestedActions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeakTopicResponse {
        private Long topicId;
        private String topicTitle;
        private String chapterName;
        private String subjectName;
        private BigDecimal masteryScore;
        private String masteryLevel;
        private Integer attemptCount;
        private String improvementSuggestion;
        private List<String> relatedResources;
        private Boolean hasAvailableQuiz;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeakTopicsOverview {
        private Long studentId;
        private Integer totalWeakTopics;
        private List<WeakTopicResponse> weakTopics;
        private List<SubjectWeakness> weakestSubjects;
        private String overallRecommendation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectWeakness {
        private Long subjectId;
        private String subjectName;
        private Integer weakTopicCount;
        private BigDecimal averageMastery;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyPlanItem {
        private Integer dayNumber;
        private Long topicId;
        private String topicTitle;
        private String activity;  // REVIEW, PRACTICE, QUIZ, LEARN
        private Integer estimatedMinutes;
        private String reason;
        private Boolean isCompleted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalizedStudyPlan {
        private Long studentId;
        private Long targetTopicId;
        private String targetTopicTitle;
        private String planType;  // PREREQUISITE_FOCUSED, WEAKNESS_FOCUSED, BALANCED
        private Integer totalDays;
        private Integer estimatedTotalMinutes;
        private List<StudyPlanItem> dailyPlan;
        private String summary;
        private List<String> tips;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetPrerequisitesRequest {
        private Long topicId;
        private BigDecimal masteryThreshold;  // Default 0.65
        private Boolean includeOptional;  // Include RELATED topics too
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetWeakTopicsRequest {
        private Long subjectId;  // Optional filter
        private Long gradeId;  // Optional filter
        private BigDecimal threshold;  // Default 0.65
        private Integer limit;  // Default 10
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateStudyPlanRequest {
        private Long targetTopicId;
        private Integer availableDays;  // How many days before the target
        private Integer minutesPerDay;  // Available study time per day
        private String focusArea;  // PREREQUISITES, WEAKNESSES, BALANCED
    }
}


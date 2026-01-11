package com.ankurshala.backend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for student mastery data
 */
public class MasteryDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicMastery {
        private Long topicId;
        private String topicTitle;
        private Long subjectId;
        private String subjectName;
        private Long chapterId;
        private String chapterName;
        private BigDecimal masteryScore;
        private BigDecimal confidence;
        private Integer totalAttempts;
        private Integer correctAttempts;
        private LocalDateTime lastAssessedAt;
        private String masteryLevel;  // WEAK, DEVELOPING, PROFICIENT, MASTERED
        
        public String getMasteryLevel() {
            if (masteryScore == null) return "UNKNOWN";
            double score = masteryScore.doubleValue();
            if (score < 0.4) return "WEAK";
            if (score < 0.65) return "DEVELOPING";
            if (score < 0.85) return "PROFICIENT";
            return "MASTERED";
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectMastery {
        private Long subjectId;
        private String subjectName;
        private BigDecimal averageMastery;
        private Integer totalTopics;
        private Integer masteredTopics;
        private Integer weakTopics;
        private List<TopicMastery> topicDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MasteryOverview {
        private Long studentId;
        private BigDecimal overallMastery;
        private Integer totalTopicsStudied;
        private Integer weakTopicsCount;
        private Integer proficientTopicsCount;
        private Integer masteredTopicsCount;
        private List<SubjectMastery> subjectMasteries;
        private List<TopicMastery> weakestTopics;  // Top 5 weakest
        private List<TopicMastery> recentlyAssessed;  // Recent 5
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MasteryUpdateRequest {
        private Long topicId;
        private BigDecimal scoreAchieved;  // 0.0 to 1.0
        private Integer questionsAnswered;
        private Integer correctAnswers;
        private String source;  // QUIZ, TEACHER_FEEDBACK, COMPLETION
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MasteryUpdateResponse {
        private Long topicId;
        private BigDecimal previousScore;
        private BigDecimal newScore;
        private BigDecimal delta;
        private String newLevel;
    }
}


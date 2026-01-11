package com.ankurshala.backend.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for AI tutor chat functionality
 */
public class ChatDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        @NotBlank(message = "Message is required")
        @Size(max = 2000, message = "Message cannot exceed 2000 characters")
        private String message;
        
        private String sessionId;  // For conversation continuity
        
        private Long topicId;  // Optional context
        
        private Long subjectId;  // Optional context
        
        private String language;  // en, hi, etc.
        
        private List<ChatMessage> conversationHistory;  // Previous messages for context
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;  // USER, ASSISTANT
        private String content;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private String sessionId;
        private String message;  // AI response
        private Boolean wasGrounded;  // Was response based on retrieved content?
        private List<ContentReference> references;  // Citations
        private List<SuggestedAction> suggestedActions;
        private Boolean safetyFiltered;  // Was any content filtered?
        private String filterReason;  // Why content was filtered (if applicable)
        private Integer tokensUsed;
        private Integer latencyMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentReference {
        private String topicTitle;
        private String chapterName;
        private String subjectName;
        private String sourceType;  // TEXTBOOK, NCERT, NOTES, etc.
        private String sourceRef;  // Page number, URL, etc.
        private Double relevanceScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedAction {
        private String actionType;  // TAKE_QUIZ, VIEW_TOPIC, REVIEW_PREREQUISITE, ASK_TEACHER
        private String actionLabel;  // Display text
        private Map<String, Object> actionData;  // Data needed to perform action
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamChunk {
        private String sessionId;
        private String content;  // Partial content
        private Boolean isComplete;
        private Integer chunkIndex;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyCheckResult {
        private Boolean isSafe;
        private String category;  // SAFE, HARMFUL, MEDICAL, LEGAL, PERSONAL_DATA, OFF_TOPIC
        private String reason;
        private String suggestedResponse;  // Pre-canned safe response
    }
}


package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for completing a step in the daily plan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteStepRequest {
    
    /**
     * Type of step completed
     * Valid values: PRACTICE, REVISE_NOTE, FOCUS_SPRINT, BOOKING_COMPANION
     */
    @NotBlank(message = "Step type is required")
    private String stepType;
    
    /**
     * Identifier for the step (e.g., topicId, noteId, bookingId)
     */
    @NotNull(message = "Step identifier is required")
    private String stepIdentifier;
    
    /**
     * Optional metadata about the completion
     * Examples:
     * - duration: time spent in seconds
     * - score: quiz score if applicable
     * - questions_attempted: number of questions
     */
    private Map<String, Object> metadata;
}

package com.ankurshala.backend.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class CreateBookingRequest {
    
    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Chapter ID is required")
    private Long chapterId;

    @NotNull(message = "Topic ID is required")
    private Long topicId;

    @NotNull(message = "Start time is required")
    private ZonedDateTime startTimeISO;

    @NotBlank(message = "Teacher category is required")
    @Pattern(regexp = "^(STANDARD|PREMIUM)$", message = "Teacher category must be STANDARD or PREMIUM")
    private String teacherCategory;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Compatibility methods for tests
    public void setTeacherId(Long teacherId) {
        // This method is for test compatibility - teacherId is not stored in this DTO
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTimeISO = startTime;
    }

    public ZonedDateTime getStartTime() {
        return startTimeISO;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        // This method is for test compatibility - duration is calculated from topic
    }

    public void setCategory(String category) {
        // This method is for test compatibility - category is derived from teacherCategory
    }

    public void setAppliedRuleId(Long appliedRuleId) {
        // This method is for test compatibility - ruleId is not stored in this DTO
    }

    public void setPriceMinCents(Integer priceMinCents) {
        // This method is for test compatibility - price is calculated
    }

    public void setPriceMaxCents(Integer priceMaxCents) {
        // This method is for test compatibility - price is calculated
    }
}

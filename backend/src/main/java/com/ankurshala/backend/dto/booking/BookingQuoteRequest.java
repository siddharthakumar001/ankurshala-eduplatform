package com.ankurshala.backend.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class BookingQuoteRequest {
    
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
}

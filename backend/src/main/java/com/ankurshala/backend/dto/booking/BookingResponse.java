package com.ankurshala.backend.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class BookingResponse {
    
    @NotNull(message = "Booking ID is required")
    private Long id;

    // Compatibility method for tests
    public Long getBookingId() {
        return id;
    }

    public void setBookingId(Long bookingId) {
        this.id = bookingId;
    }

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private Long teacherId;

    @NotBlank(message = "Board is required")
    private String board;

    @NotBlank(message = "Grade is required")
    private String grade;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Topic ID is required")
    private Long topicId;

    @NotNull(message = "Start time is required")
    private ZonedDateTime startTime;

    @NotNull(message = "End time is required")
    private ZonedDateTime endTime;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    private Integer priceCents;

    @NotBlank(message = "State is required")
    private String state;

    private String notes;

    @NotNull(message = "Created at is required")
    private ZonedDateTime createdAt;

    @NotNull(message = "Updated at is required")
    private ZonedDateTime updatedAt;

    // Additional fields
    private Long chapterId;
    private Integer priceMinCents;
    private Integer priceMaxCents;
    private Long appliedRuleId;
}

package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReviewRequest {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must be at most 5.0")
    private Double rating;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
}

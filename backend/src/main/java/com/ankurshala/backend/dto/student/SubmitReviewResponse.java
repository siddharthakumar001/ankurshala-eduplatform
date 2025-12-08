package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitReviewResponse {

    private Long reviewId;
    private Long teacherId;
    private Long bookingId;
    private Double rating;
    private String comment;
    private LocalDateTime createdAt;
    private String message;
}

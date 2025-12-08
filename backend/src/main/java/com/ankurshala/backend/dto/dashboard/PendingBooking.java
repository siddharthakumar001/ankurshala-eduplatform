package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PendingBooking {
    private Long bookingId;
    private String studentName;
    private String topicTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer priceCents;
    private String category;
}

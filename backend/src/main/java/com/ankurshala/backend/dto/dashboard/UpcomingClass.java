package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpcomingClass {
    private Long bookingId;
    private String topicTitle;
    private String teacherName;
    private String studentName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}

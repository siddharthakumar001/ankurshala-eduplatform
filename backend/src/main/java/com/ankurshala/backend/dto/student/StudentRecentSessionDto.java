package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentRecentSessionDto {
    private Long id;
    private Long topicId;
    private String topicTitle;
    private String subjectName;
    private String chapterName;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String status;
    private BigDecimal amountPaid;
    private Integer studentRating;
    private String studentFeedback;
    private String teacherFeedback;
    private String sessionNotes;
    private boolean hasFeedback;
    private LocalDateTime completedAt;
}

package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentUpcomingBookingDto {
    private Long id;
    private Long topicId;
    private String topicTitle;
    private String subjectName;
    private String chapterName;
    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String status;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String studentNotes;
    private boolean canJoin;
    private boolean canCancel;
    private boolean canReschedule;
    private LocalDateTime createdAt;
}

package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentProgressDto {
    private Long id;
    private Long topicId;
    private String topicTitle;
    private String subjectName;
    private String chapterName;
    private int sessionsCompleted;
    private int totalSessions;
    private double completionPercentage;
    private LocalDateTime lastSessionDate;
    private String lastSessionTeacher;
    private String progressStatus; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

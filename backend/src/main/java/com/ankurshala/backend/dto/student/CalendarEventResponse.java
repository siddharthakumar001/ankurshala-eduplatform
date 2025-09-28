package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CalendarEventResponse {
    
    private Long id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private String color;
    private String teacherName;
    private String topicTitle;
    private Boolean canReschedule;
    private Boolean canCancel;
    private Boolean canJoin;
}

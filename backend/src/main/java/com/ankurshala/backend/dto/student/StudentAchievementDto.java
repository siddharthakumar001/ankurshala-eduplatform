package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentAchievementDto {
    private Long id;
    private String title;
    private String description;
    private String type; // MILESTONE, STREAK, COMPLETION, etc.
    private String iconUrl;
    private LocalDateTime earnedAt;
    private boolean isNew;
}

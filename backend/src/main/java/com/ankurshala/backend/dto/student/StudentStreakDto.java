package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentStreakDto {
    private LocalDate date;
    private int sessionsCompleted;
    private boolean isActive;
}

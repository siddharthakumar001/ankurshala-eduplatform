package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentPerformanceDataPoint {
    private LocalDate date;
    private double averageRating;
    private int sessionsCompleted;
    private double completionRate;
}

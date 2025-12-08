package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.util.List;

@Data
public class StudentPerformanceTrendDto {
    private List<StudentPerformanceDataPoint> weeklyTrend;
    private List<StudentPerformanceDataPoint> monthlyTrend;
    private double improvementRate;
    private String trendDirection; // IMPROVING, DECLINING, STABLE
}

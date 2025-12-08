package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StudentLearningAnalyticsDto {
    private double overallProgress;
    private int totalTopicsStudied;
    private int totalSessionsCompleted;
    private double averageSessionRating;
    private Map<String, Integer> subjectProgress; // subject -> completion percentage
    private Map<String, Integer> monthlySessions; // month -> session count
    private List<StudentStreakDto> learningStreaks;
    private StudentPerformanceTrendDto performanceTrend;
    private List<StudentAchievementDto> recentAchievements;
}

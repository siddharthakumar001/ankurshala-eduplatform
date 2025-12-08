package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentDashboardStatsDto {
    private int totalBookings;
    private int upcomingBookings;
    private int completedSessions;
    private int cancelledSessions;
    private int totalHoursSpent;
    private BigDecimal totalAmountSpent;
    private BigDecimal monthlySpending;
    private double averageRating;
    private int totalTeachers;
    private int activeTeachers;
    private int completedTopics;
    private int totalTopics;
    private double completionRate;
    private LocalDateTime nextClassTime;
    private String nextClassTopic;
    private String nextClassTeacher;
    private boolean hasUpcomingClass;
}

package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentDashboardDto {
    private StudentDashboardStatsDto stats;
    private List<StudentUpcomingBookingDto> upcomingBookings;
    private List<StudentRecentSessionDto> recentSessions;
    private List<StudentProgressDto> recentProgress;
    private List<StudentNotificationDto> recentNotifications;
    private StudentLearningAnalyticsDto learningAnalytics;
}

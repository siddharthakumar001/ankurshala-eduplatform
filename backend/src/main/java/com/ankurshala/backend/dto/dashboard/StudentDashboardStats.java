package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.util.List;

@Data
public class StudentDashboardStats {
    private Long upcomingBookings;
    private Long completedBookings;
    private Long totalHoursSpent;
    private List<SubjectMastery> subjectMastery;
    private List<UpcomingClass> upcomingClasses;
    private List<TopicRecommendation> recommendations;
}

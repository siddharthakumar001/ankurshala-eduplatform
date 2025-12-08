package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TeacherDashboardStats {
    private Long pendingRequests;
    private Long upcomingClasses;
    private BigDecimal earningsMTD;
    private BigDecimal totalEarnings;
    private Double averageRating;
    private Long totalRatings;
    private List<PendingBooking> pendingBookings;
    private List<UpcomingClass> upcomingClassesList;
    private EarningsBreakdown earningsBreakdown;
}

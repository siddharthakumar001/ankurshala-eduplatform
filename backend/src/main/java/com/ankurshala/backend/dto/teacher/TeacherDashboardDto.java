package com.ankurshala.backend.dto.teacher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TeacherDashboardDto {
    private TeacherProfileDto profile;
    private TeacherBookingPreferencesDto bookingPreferences;
    private List<TeacherWeeklyAvailabilityDto> weeklyAvailability;
    private TeacherDashboardStatsDto stats;
    private List<TeacherUpcomingBookingDto> upcomingBookings;
    private List<TeacherRecentEarningsDto> recentEarnings;
    private List<TeacherPerformanceMetricsDto> performanceMetrics;
    
    public TeacherDashboardDto() {}
    
    public TeacherDashboardDto(TeacherProfileDto profile, TeacherBookingPreferencesDto bookingPreferences, 
                             List<TeacherWeeklyAvailabilityDto> weeklyAvailability, TeacherDashboardStatsDto stats, 
                             List<TeacherUpcomingBookingDto> upcomingBookings, List<TeacherRecentEarningsDto> recentEarnings, 
                             List<TeacherPerformanceMetricsDto> performanceMetrics) {
        this.profile = profile;
        this.bookingPreferences = bookingPreferences;
        this.weeklyAvailability = weeklyAvailability;
        this.stats = stats;
        this.upcomingBookings = upcomingBookings;
        this.recentEarnings = recentEarnings;
        this.performanceMetrics = performanceMetrics;
    }
    
    // Getters and Setters
    public TeacherProfileDto getProfile() { return profile; }
    public void setProfile(TeacherProfileDto profile) { this.profile = profile; }
    
    public TeacherBookingPreferencesDto getBookingPreferences() { return bookingPreferences; }
    public void setBookingPreferences(TeacherBookingPreferencesDto bookingPreferences) { this.bookingPreferences = bookingPreferences; }
    
    public List<TeacherWeeklyAvailabilityDto> getWeeklyAvailability() { return weeklyAvailability; }
    public void setWeeklyAvailability(List<TeacherWeeklyAvailabilityDto> weeklyAvailability) { this.weeklyAvailability = weeklyAvailability; }
    
    public TeacherDashboardStatsDto getStats() { return stats; }
    public void setStats(TeacherDashboardStatsDto stats) { this.stats = stats; }
    
    public List<TeacherUpcomingBookingDto> getUpcomingBookings() { return upcomingBookings; }
    public void setUpcomingBookings(List<TeacherUpcomingBookingDto> upcomingBookings) { this.upcomingBookings = upcomingBookings; }
    
    public List<TeacherRecentEarningsDto> getRecentEarnings() { return recentEarnings; }
    public void setRecentEarnings(List<TeacherRecentEarningsDto> recentEarnings) { this.recentEarnings = recentEarnings; }
    
    public List<TeacherPerformanceMetricsDto> getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(List<TeacherPerformanceMetricsDto> performanceMetrics) { this.performanceMetrics = performanceMetrics; }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.teacher.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher Dashboard Service
 * Provides comprehensive dashboard data for teachers including stats, bookings, and analytics
 */
@Slf4j
@Service
@Transactional
public class TeacherDashboardService {

    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private TeacherProfileRepository teacherProfileRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private TeacherEarningsRepository teacherEarningsRepository;
    
    @Autowired
    private TeacherSessionFeedbackRepository teacherSessionFeedbackRepository;
    
    @Autowired
    private TeacherPerformanceMetricsRepository teacherPerformanceMetricsRepository;

    public TeacherDashboardDto getTeacherDashboard(Long userId) {
        log.info("Getting teacher dashboard for user ID: {}", userId);
        
        Teacher teacher = getTeacherByUserId(userId);
        TeacherProfile profile = getTeacherProfileByUserId(userId);
        
        // Get dashboard components
        TeacherProfileDto profileDto = convertToProfileDto(profile);
        TeacherDashboardStatsDto statsDto = getDashboardStats(teacher.getId());
        List<TeacherUpcomingBookingDto> upcomingBookings = getUpcomingBookings(teacher.getId());
        List<TeacherRecentEarningsDto> recentEarnings = getRecentEarnings(teacher.getId());
        List<TeacherPerformanceMetricsDto> performanceMetrics = getPerformanceMetrics(teacher.getId());
        
        return new TeacherDashboardDto(
            profileDto, null, null, statsDto, upcomingBookings, recentEarnings, performanceMetrics
        );
    }
    
    private TeacherDashboardStatsDto getDashboardStats(Long teacherId) {
        // Get booking statistics
        List<Booking> allBookings = bookingRepository.findByTeacherIdOrderByStartTsDesc(teacherId);
        int totalBookings = allBookings.size();
        int pendingBookings = (int) allBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        int acceptedBookings = (int) allBookings.stream().filter(b -> b.getStatus() == BookingStatus.ACCEPTED).count();
        int completedSessions = (int) allBookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        int cancelledSessions = (int) allBookings.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        
        // Get earnings statistics
        Double totalEarnings = teacherEarningsRepository.findTotalEarningsByTeacherIdAndStatus(teacherId, TeacherEarnings.PaymentStatus.PAID);
        Double monthlyEarnings = teacherEarningsRepository.findTotalEarningsByTeacherIdAndDateRange(
            teacherId, LocalDate.now().withDayOfMonth(1), LocalDate.now());
        
        // Get rating statistics
        Double averageRating = teacherSessionFeedbackRepository.findAverageRatingByTeacherId(teacherId);
        
        // Get student statistics
        Set<Long> uniqueStudents = allBookings.stream().map(b -> b.getStudent().getId()).collect(Collectors.toSet());
        int totalStudents = uniqueStudents.size();
        int activeStudents = (int) allBookings.stream()
            .filter(b -> b.getStartTs().isAfter(ZonedDateTime.now().minusDays(30)))
            .map(b -> b.getStudent().getId())
            .distinct()
            .count();
        
        // Calculate rates
        double completionRate = totalBookings > 0 ? (double) completedSessions / totalBookings * 100 : 0;
        double cancellationRate = totalBookings > 0 ? (double) cancelledSessions / totalBookings * 100 : 0;
        
        return new TeacherDashboardStatsDto(
            totalBookings, pendingBookings, acceptedBookings, completedSessions, cancelledSessions,
            BigDecimal.valueOf(totalEarnings != null ? totalEarnings : 0),
            BigDecimal.valueOf(monthlyEarnings != null ? monthlyEarnings : 0),
            BigDecimal.valueOf(averageRating != null ? averageRating : 0),
            totalStudents, activeStudents, completionRate, cancellationRate
        );
    }
    
    private List<TeacherUpcomingBookingDto> getUpcomingBookings(Long teacherId) {
        List<Booking> upcomingBookings = bookingRepository.findByTeacherIdOrderByStartTsDesc(teacherId)
            .stream()
            .filter(b -> b.getStartTs().isAfter(ZonedDateTime.now()))
            .collect(Collectors.toList());
        
        return upcomingBookings.stream()
            .limit(10) // Limit to 10 upcoming bookings
            .map(this::convertToUpcomingBookingDto)
            .collect(Collectors.toList());
    }
    
    private List<TeacherRecentEarningsDto> getRecentEarnings(Long teacherId) {
        List<TeacherEarnings> recentEarnings = teacherEarningsRepository.findEarningsByTeacherIdAndDateRange(
            teacherId, LocalDate.now().minusDays(30), LocalDate.now());
        
        return recentEarnings.stream()
            .limit(10) // Limit to 10 recent earnings
            .map(this::convertToRecentEarningsDto)
            .collect(Collectors.toList());
    }
    
    private List<TeacherPerformanceMetricsDto> getPerformanceMetrics(Long teacherId) {
        List<TeacherPerformanceMetrics> metrics = teacherPerformanceMetricsRepository.findByTeacherIdAndDateRange(
            teacherId, LocalDate.now().minusDays(30), LocalDate.now());
        
        return metrics.stream()
            .map(this::convertToPerformanceMetricsDto)
            .collect(Collectors.toList());
    }

    // ============ HELPER METHODS ============
    
    private Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Teacher not found for user ID: " + userId));
    }
    
    private TeacherProfile getTeacherProfileByUserId(Long userId) {
        return teacherProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Teacher profile not found for user ID: " + userId));
    }
    
    // ============ CONVERSION METHODS ============
    
    private TeacherProfileDto convertToProfileDto(TeacherProfile profile) {
        // This is a placeholder - you'll need to implement based on your existing TeacherProfileDto
        TeacherProfileDto dto = new TeacherProfileDto();
        // Map fields from profile to dto
        // dto.setFirstName(profile.getFirstName());
        // dto.setLastName(profile.getLastName());
        // ... etc
        return dto;
    }
    
    private TeacherUpcomingBookingDto convertToUpcomingBookingDto(Booking booking) {
        return new TeacherUpcomingBookingDto(
            booking.getId(),
            booking.getStudent().getId(),
            booking.getStudent().getName(),
            booking.getStudent().getEmail(),
            booking.getTopic().getId(),
            booking.getTopic().getTitle(),
            booking.getTopic().getChapter().getSubject().getName(),
            booking.getStartTs().toLocalDateTime(),
            booking.getEndTs().toLocalDateTime(),
            booking.getDurationMinutes(),
            booking.getStatus().name(),
            booking.getPriceMin(),
            booking.getPriceMax(),
            booking.getStudentNotes(),
            booking.getCreatedAt().toLocalDateTime()
        );
    }
    
    private TeacherRecentEarningsDto convertToRecentEarningsDto(TeacherEarnings earnings) {
        return new TeacherRecentEarningsDto(
            earnings.getId(),
            earnings.getBooking().getId(),
            earnings.getSessionDate(),
            earnings.getSessionDurationMinutes(),
            earnings.getHourlyRate(),
            earnings.getNetEarnings(),
            earnings.getPaymentStatus().name(),
            earnings.getBooking().getStudent().getName(),
            earnings.getBooking().getTopic().getTitle()
        );
    }
    
    private TeacherPerformanceMetricsDto convertToPerformanceMetricsDto(TeacherPerformanceMetrics metrics) {
        return new TeacherPerformanceMetricsDto(
            metrics.getMetricDate(),
            metrics.getTotalSessions(),
            metrics.getCompletedSessions(),
            metrics.getCancelledSessions(),
            metrics.getAverageRating(),
            metrics.getTotalEarnings(),
            metrics.getTotalHoursTaught(),
            metrics.getStudentSatisfactionScore()
        );
    }
}

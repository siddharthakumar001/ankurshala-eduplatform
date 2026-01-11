package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Teacher Booking Management Service
 * Handles booking acceptance, decline, and session management for teachers
 */
@Slf4j
@Service
@Transactional
public class TeacherBookingManagementService {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private TeacherBookingPreferencesRepository teacherBookingPreferencesRepository;
    
    @Autowired
    private TeacherEarningsRepository teacherEarningsRepository;
    
    @Autowired
    private TeacherPerformanceMetricsRepository teacherPerformanceMetricsRepository;
    
    @Autowired
    private TeacherSessionFeedbackRepository teacherSessionFeedbackRepository;

    @Autowired(required = false)
    private WebSocketNotificationService webSocketNotificationService;

    // ============ BOOKING ACCEPTANCE/DECLINE ============
    
    public Booking acceptBooking(Long teacherId, Long bookingId) {
        log.info("Teacher {} accepting booking {}", teacherId, bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Unauthorized to accept this booking");
        }
        
        if (!booking.getState().equals("REQUESTED")) {
            throw new RuntimeException("Booking is not in REQUESTED status");
        }
        
        // Check if teacher has auto-accept enabled
        Optional<TeacherBookingPreferences> preferences = teacherBookingPreferencesRepository.findByTeacherId(teacherId);
        if (preferences.isPresent() && preferences.get().getAutoAcceptBookings()) {
            log.info("Auto-accept is enabled for teacher {}", teacherId);
        }
        
        // Update booking status
        booking.setState("ACCEPTED");
        booking.setAcceptedAt(ZonedDateTime.now());
        
        Booking saved = bookingRepository.save(booking);
        
        // Create earnings record
        createEarningsRecord(saved);
        
        // Update performance metrics
        updatePerformanceMetrics(teacherId, saved, "ACCEPTED");
        
        log.info("Booking {} accepted successfully by teacher {}", bookingId, teacherId);
        return saved;
    }
    
    public Booking declineBooking(Long teacherId, Long bookingId, String reason) {
        log.info("Teacher {} declining booking {} with reason: {}", teacherId, bookingId, reason);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Unauthorized to decline this booking");
        }
        
        if (!booking.getState().equals("REQUESTED")) {
            throw new RuntimeException("Booking is not in REQUESTED status");
        }
        
        // Update booking status
        booking.setState("CANCELLED");
        booking.setCancelledAt(ZonedDateTime.now());
        booking.setCancellationReason(reason);
        
        Booking saved = bookingRepository.save(booking);
        
        // Update performance metrics
        updatePerformanceMetrics(teacherId, saved, "CANCELLED");
        
        log.info("Booking {} declined successfully by teacher {}", bookingId, teacherId);
        return saved;
    }
    
    public Booking rescheduleBooking(Long teacherId, Long bookingId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        log.info("Teacher {} rescheduling booking {} to {}", teacherId, bookingId, newStartTime);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Unauthorized to reschedule this booking");
        }
        
        if (!booking.getState().equals("ACCEPTED")) {
            throw new RuntimeException("Booking must be ACCEPTED to reschedule");
        }
        
        // Update booking times
        booking.setStartTs(ZonedDateTime.of(newStartTime, ZoneId.systemDefault()));
        booking.setEndTs(ZonedDateTime.of(newEndTime, ZoneId.systemDefault()));
        booking.setState("RESCHEDULED");
        
        Booking saved = bookingRepository.save(booking);
        
        log.info("Booking {} rescheduled successfully by teacher {}", bookingId, teacherId);
        return saved;
    }

    // ============ SESSION MANAGEMENT ============
    
    public Booking startSession(Long teacherId, Long bookingId) {
        log.info("Teacher {} starting session for booking {}", teacherId, bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Unauthorized to start this session");
        }
        
        if (!booking.getState().equals("ACCEPTED")) {
            throw new RuntimeException("Booking must be ACCEPTED to start session");
        }
        
        // Check if session time is appropriate (within 15 minutes of start time)
        ZonedDateTime now = ZonedDateTime.now();
        if (now.isBefore(booking.getStartTs().minusMinutes(15)) || now.isAfter(booking.getStartTs().plusMinutes(15))) {
            throw new RuntimeException("Session can only be started within 15 minutes of scheduled time");
        }
        
        // Update booking status to IN_PROGRESS
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setState("ACTIVE");
        
        Booking saved = bookingRepository.save(booking);
        
        // Notify student via WebSocket that session has started
        if (webSocketNotificationService != null) {
            webSocketNotificationService.notifyBookingInProgress(saved);
        }
        
        log.info("Session started successfully for booking {} by teacher {}", bookingId, teacherId);
        return saved;
    }
    
    public Booking endSession(Long teacherId, Long bookingId, String sessionNotes) {
        log.info("Teacher {} ending session for booking {}", teacherId, bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Unauthorized to end this session");
        }
        
        // Update booking with session notes
        booking.setTeacherNotes(sessionNotes);
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setState("COMPLETED");
        
        Booking saved = bookingRepository.save(booking);
        
        // Notify student via WebSocket that session has completed
        if (webSocketNotificationService != null) {
            webSocketNotificationService.notifyBookingCompleted(saved);
        }
        
        // Update performance metrics
        updatePerformanceMetrics(teacherId, saved, "COMPLETED");
        
        log.info("Session ended successfully for booking {} by teacher {}", bookingId, teacherId);
        return saved;
    }

    // ============ HELPER METHODS ============
    
    private void createEarningsRecord(Booking booking) {
        try {
            // Get the teacher entity from the booking's teacher user
            Teacher teacher = teacherRepository.findByUser(booking.getTeacher()).orElse(null);
            if (teacher == null) {
                log.warn("Teacher entity not found for user ID: {}", booking.getTeacher().getId());
                return;
            }
            LocalDate sessionDate = booking.getStartTime().toLocalDate();
            Integer durationMinutes = booking.getDurationMinutes();
            BigDecimal hourlyRate = booking.getPriceMin(); // Use minimum price as hourly rate
            BigDecimal earningsAmount = hourlyRate.multiply(BigDecimal.valueOf(durationMinutes)).divide(BigDecimal.valueOf(60));
            BigDecimal platformFee = earningsAmount.multiply(BigDecimal.valueOf(0.1)); // 10% platform fee
            BigDecimal netEarnings = earningsAmount.subtract(platformFee);
            
            TeacherEarnings earnings = new TeacherEarnings(
                teacher, booking, sessionDate, durationMinutes, hourlyRate, 
                earningsAmount, platformFee);
            
            teacherEarningsRepository.save(earnings);
            log.info("Earnings record created for booking {}", booking.getId());
        } catch (Exception e) {
            log.error("Failed to create earnings record for booking {}: {}", booking.getId(), e.getMessage());
        }
    }
    
    private void updatePerformanceMetrics(Long teacherId, Booking booking, String action) {
        try {
            LocalDate metricDate = LocalDate.now();
            Optional<TeacherPerformanceMetrics> existing = teacherPerformanceMetricsRepository.findByTeacherIdAndMetricDate(teacherId, metricDate);
            
            TeacherPerformanceMetrics metrics;
            if (existing.isPresent()) {
                metrics = existing.get();
            } else {
                Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
                metrics = new TeacherPerformanceMetrics(teacher, metricDate);
            }
            
            // Update metrics based on action
            switch (action) {
                case "ACCEPTED":
                    metrics.setTotalSessions(metrics.getTotalSessions() + 1);
                    metrics.setAcceptedBookings(metrics.getAcceptedBookings() + 1);
                    break;
                case "CANCELLED":
                    metrics.setCancelledSessions(metrics.getCancelledSessions() + 1);
                    break;
                case "COMPLETED":
                    metrics.setCompletedSessions(metrics.getCompletedSessions() + 1);
                    // Update total hours taught
                    BigDecimal currentHours = metrics.getTotalHoursTaught();
                    BigDecimal sessionHours = BigDecimal.valueOf(booking.getDurationMinutes()).divide(BigDecimal.valueOf(60));
                    metrics.setTotalHoursTaught(currentHours.add(sessionHours));
                    break;
            }
            
            teacherPerformanceMetricsRepository.save(metrics);
            log.info("Performance metrics updated for teacher {} with action {}", teacherId, action);
        } catch (Exception e) {
            log.error("Failed to update performance metrics for teacher {}: {}", teacherId, e.getMessage());
        }
    }
}

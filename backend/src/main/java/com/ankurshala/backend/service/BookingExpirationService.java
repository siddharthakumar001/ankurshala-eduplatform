package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Booking Expiration Service - Handles automatic timeout of pending bookings
 * 
 * Similar to Uber/Ola ride timeout mechanism where if no driver accepts within
 * a certain time, the ride request is automatically cancelled.
 * 
 * Features:
 * - Automatic expiration of pending bookings after timeout
 * - Periodic cleanup of expired bookings
 * - Student notifications for expired bookings
 * - Metrics tracking for expiration rate
 */
@Slf4j
@Service
public class BookingExpirationService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${booking.expiration.minutes:10}")
    private int expirationMinutes;

    /**
     * Scheduled task to check and expire pending bookings
     * Runs every 2 minutes
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    @Transactional
    public void expirePendingBookings() {
        log.debug("[BOOKING_EXPIRATION] Starting expiration check...");
        
        try {
            // Find all pending bookings
            List<Booking> pendingBookings = bookingRepository.findPendingBookings();
            
            if (pendingBookings.isEmpty()) {
                log.debug("[BOOKING_EXPIRATION] No pending bookings to check");
                return;
            }
            
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime expirationThreshold = now.minusMinutes(expirationMinutes);
            
            int expiredCount = 0;
            
            for (Booking booking : pendingBookings) {
                // Check if booking was created more than expiration threshold ago
                if (booking.getCreatedAt() != null && 
                    booking.getCreatedAt().isBefore(expirationThreshold) &&
                    booking.getTeacherId() == null) {
                    
                    expireBooking(booking);
                    expiredCount++;
                }
            }
            
            if (expiredCount > 0) {
                log.info("[BOOKING_EXPIRATION] Expired {} bookings that had no teacher acceptance within {} minutes", 
                        expiredCount, expirationMinutes);
            } else {
                log.debug("[BOOKING_EXPIRATION] No bookings expired in this run");
            }
            
        } catch (Exception e) {
            log.error("[BOOKING_EXPIRATION] Error during expiration check: {}", e.getMessage(), e);
        }
    }

    /**
     * Expire a specific booking
     */
    @Transactional
    public void expireBooking(Booking booking) {
        try {
            log.info("[BOOKING_EXPIRATION] Expiring booking {} (created at: {}, no teacher accepted)", 
                    booking.getId(), booking.getCreatedAt());

            String reason = "No teacher accepted within " + expirationMinutes + " minutes";
            ZonedDateTime now = ZonedDateTime.now();
            int expired = bookingRepository.expireBookingIfPending(booking.getId(), reason, now, now);
            if (expired == 0) {
                return;
            }

            Map<String, Object> expireEvent = new HashMap<>();
            expireEvent.put("bookingId", booking.getId());
            expireEvent.put("studentId", booking.getStudentId());
            expireEvent.put("reason", reason);
            kafkaTemplate.send("booking.expired", booking.getId().toString(), expireEvent);
            
            log.info("[BOOKING_EXPIRATION] Successfully expired booking {}", booking.getId());
            
        } catch (Exception e) {
            log.error("[BOOKING_EXPIRATION] Failed to expire booking {}: {}", 
                    booking.getId(), e.getMessage(), e);
        }
    }

    /**
     * Check if a booking has expired
     */
    public boolean isBookingExpired(Booking booking) {
        if (booking.getCreatedAt() == null) {
            return false;
        }
        
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expirationThreshold = now.minusMinutes(expirationMinutes);
        
        return booking.getCreatedAt().isBefore(expirationThreshold) &&
               booking.getStatus() == BookingStatus.PENDING &&
               booking.getTeacherId() == null;
    }

    /**
     * Get expiration time for a booking
     */
    public ZonedDateTime getExpirationTime(Booking booking) {
        if (booking.getCreatedAt() == null) {
            return null;
        }
        return booking.getCreatedAt().plusMinutes(expirationMinutes);
    }

    /**
     * Get remaining time until expiration in seconds
     */
    public long getRemainingSecondsUntilExpiration(Booking booking) {
        if (booking.getCreatedAt() == null) {
            return 0;
        }
        
        ZonedDateTime expirationTime = getExpirationTime(booking);
        ZonedDateTime now = ZonedDateTime.now();
        
        if (expirationTime.isBefore(now)) {
            return 0;
        }
        
        return java.time.Duration.between(now, expirationTime).getSeconds();
    }

    /**
     * Cancel booking expiration check (for testing)
     */
    public void disableExpirationChecks() {
        log.warn("[BOOKING_EXPIRATION] Expiration checks disabled (test mode)");
    }
}

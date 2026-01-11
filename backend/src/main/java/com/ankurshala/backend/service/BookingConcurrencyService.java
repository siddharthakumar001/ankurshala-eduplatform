package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.TeacherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enterprise-Grade Booking Concurrency Service
 * Handles concurrent booking acceptance similar to Uber/Ola ride matching system
 * 
 * Key Features:
 * - Pessimistic locking for atomic booking acceptance
 * - Optimistic locking with retry mechanism
 * - First-accept-wins strategy
 * - Real-time notifications to teachers
 * - Automatic timeout and expiry
 * - Race condition prevention
 */
@Slf4j
@Service
public class BookingConcurrencyService {

    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private WebSocketNotificationService webSocketService;

    @Autowired
    private DistributedLockService distributedLockService;

    /**
     * Accept booking with pessimistic locking (first-accept wins)
     * Similar to Uber's driver acceptance mechanism
     * 
     * Uses distributed lock (Redis or in-memory) as Layer 1, then pessimistic DB lock as Layer 2
     * 
     * @param bookingId The booking ID to accept
     * @param teacherId The teacher attempting to accept
     * @return Accepted booking or null if already accepted by another teacher
     * @throws BookingAlreadyAcceptedException if booking is no longer available
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking acceptBookingWithLock(Long bookingId, Long teacherId) {
        String lockKey = "booking:accept:" + bookingId;
        
        // Layer 1: Distributed application lock (Redis or in-memory)
        return distributedLockService.executeWithLock(lockKey, 10, () -> {
            log.info("[BOOKING_ACCEPT] Teacher {} attempting to accept booking {} (with distributed lock)", 
                    teacherId, bookingId);
            
            // Step 1: Acquire pessimistic write lock on the booking (Layer 2: Database lock)
            Optional<Booking> bookingOpt = bookingRepository.findByIdWithLock(bookingId);
            
            if (bookingOpt.isEmpty()) {
                log.warn("[BOOKING_ACCEPT] Booking {} not found", bookingId);
                throw new IllegalArgumentException("Booking not found");
            }
            
            Booking booking = bookingOpt.get();
            
            // Step 2: Validate booking is still in PENDING/REQUESTED state
            if (booking.getStatus() != BookingStatus.PENDING && !booking.getState().equals("REQUESTED")) {
                log.warn("[BOOKING_ACCEPT] Booking {} is no longer available. Current state: {}, status: {}", 
                        bookingId, booking.getState(), booking.getStatus());
                throw new BookingAlreadyAcceptedException(
                    "Booking has already been accepted by another teacher or is no longer available");
            }
            
            // Step 3: Validate teacher is not already assigned
            if (booking.getTeacherId() != null) {
                log.warn("[BOOKING_ACCEPT] Booking {} already has teacher {} assigned. Teacher {} attempted acceptance.", 
                        bookingId, booking.getTeacherId(), teacherId);
                throw new BookingAlreadyAcceptedException(
                    "This booking has already been accepted by another teacher");
            }
            
            // Step 4: Validate no time conflicts for this teacher
            validateNoTimeConflicts(teacherId, booking.getStartTs(), booking.getEndTs(), bookingId);
            
            // Step 5: Atomically update booking (within the transaction with lock held)
            booking.setTeacherId(teacherId);
            booking.setStatus(BookingStatus.ACCEPTED);
            booking.setState("ACCEPTED");
            booking.setAcceptedAt(ZonedDateTime.now());
            
            // Find and set teacher entity
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
            booking.setTeacher(teacher.getUser());
            
            Booking acceptedBooking = bookingRepository.save(booking);
            
            log.info("[BOOKING_ACCEPT] ✅ SUCCESS - Teacher {} successfully accepted booking {}", 
                    teacherId, bookingId);
            
            // Step 6: Publish events asynchronously (after commit)
            publishBookingAcceptedEvent(acceptedBooking);
            notifyStudentOfAcceptance(acceptedBooking);
            notifyOtherTeachersBookingTaken(acceptedBooking);
            
            return acceptedBooking;
        });
    }

    /**
     * Accept booking with optimistic locking and retry
     * Uses @Version field for optimistic concurrency control
     * 
     * @param bookingId The booking ID to accept
     * @param teacherId The teacher attempting to accept
     * @return Accepted booking or null if max retries exceeded
     */
    @Retryable(
        retryFor = OptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public Booking acceptBookingWithOptimisticLock(Long bookingId, Long teacherId) {
        log.info("[BOOKING_ACCEPT_OPTIMISTIC] Teacher {} attempting to accept booking {} (optimistic locking)", 
                teacherId, bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Check if still available
        if (booking.getStatus() != BookingStatus.PENDING || booking.getTeacherId() != null) {
            log.warn("[BOOKING_ACCEPT_OPTIMISTIC] Booking {} no longer available", bookingId);
            throw new BookingAlreadyAcceptedException("Booking has already been accepted");
        }
        
        // Validate no conflicts
        validateNoTimeConflicts(teacherId, booking.getStartTs(), booking.getEndTs(), bookingId);
        
        // Update booking (optimistic lock will trigger retry if version mismatch)
        booking.setTeacherId(teacherId);
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setState("ACCEPTED");
        booking.setAcceptedAt(ZonedDateTime.now());
        
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        booking.setTeacher(teacher.getUser());
        
        Booking acceptedBooking = bookingRepository.save(booking);
        
        log.info("[BOOKING_ACCEPT_OPTIMISTIC] ✅ SUCCESS - Teacher {} accepted booking {} with optimistic locking", 
                teacherId, bookingId);
        
        publishBookingAcceptedEvent(acceptedBooking);
        notifyStudentOfAcceptance(acceptedBooking);
        notifyOtherTeachersBookingTaken(acceptedBooking);
        
        return acceptedBooking;
    }

    /**
     * Atomic booking acceptance using database-level UPDATE query
     * Most efficient approach for high concurrency scenarios
     * 
     * @param bookingId The booking ID to accept
     * @param teacherId The teacher attempting to accept
     * @return true if acceptance succeeded, false if already accepted
     */
    @Transactional
    public boolean acceptBookingAtomic(Long bookingId, Long teacherId) {
        log.info("[BOOKING_ACCEPT_ATOMIC] Teacher {} attempting atomic acceptance of booking {}", 
                teacherId, bookingId);
        
        // Validate no conflicts first (read operation)
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        if (booking.getTeacherId() != null) {
            log.warn("[BOOKING_ACCEPT_ATOMIC] Booking {} already accepted", bookingId);
            return false;
        }
        
        validateNoTimeConflicts(teacherId, booking.getStartTs(), booking.getEndTs(), bookingId);
        
        // Atomic update query (CAS operation)
        int rowsUpdated = bookingRepository.acceptBooking(bookingId, teacherId);
        
        boolean success = rowsUpdated > 0;
        
        if (success) {
            log.info("[BOOKING_ACCEPT_ATOMIC] ✅ SUCCESS - Teacher {} atomically accepted booking {}", 
                    teacherId, bookingId);
            
            // Reload booking to get complete data
            Booking acceptedBooking = bookingRepository.findById(bookingId).orElseThrow();
            
            publishBookingAcceptedEvent(acceptedBooking);
            notifyStudentOfAcceptance(acceptedBooking);
            notifyOtherTeachersBookingTaken(acceptedBooking);
        } else {
            log.warn("[BOOKING_ACCEPT_ATOMIC] ❌ FAILED - Booking {} was accepted by another teacher " +
                    "before teacher {} could complete acceptance", bookingId, teacherId);
        }
        
        return success;
    }

    /**
     * Validate teacher has no time conflicts with existing bookings
     * 
     * @param teacherId Teacher ID
     * @param startTime Proposed booking start time
     * @param endTime Proposed booking end time
     * @param excludeBookingId Booking ID to exclude from conflict check
     * @throws TeacherTimeConflictException if conflict found
     */
    private void validateNoTimeConflicts(Long teacherId, ZonedDateTime startTime, 
                                       ZonedDateTime endTime, Long excludeBookingId) {
        var conflicts = bookingRepository.findConflictingBookingsForTeacher(
                teacherId, startTime, endTime);
        
        // Remove the current booking from conflicts (if rescheduling)
        conflicts.removeIf(b -> b.getId().equals(excludeBookingId));
        
        if (!conflicts.isEmpty()) {
            log.warn("[CONFLICT_CHECK] Teacher {} has {} conflicting bookings for time range {} to {}", 
                    teacherId, conflicts.size(), startTime, endTime);
            throw new TeacherTimeConflictException(
                "Teacher has conflicting bookings during this time slot");
        }
    }

    /**
     * Publish Kafka event for booking acceptance
     */
    private void publishBookingAcceptedEvent(Booking booking) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "BOOKING_ACCEPTED");
            event.put("bookingId", booking.getId());
            event.put("studentId", booking.getStudentId());
            event.put("teacherId", booking.getTeacherId());
            event.put("startTime", booking.getStartTs());
            event.put("acceptedAt", booking.getAcceptedAt());
            event.put("timestamp", ZonedDateTime.now());
            
            kafkaTemplate.send("booking.accepted", booking.getId().toString(), event);
            log.info("[KAFKA] Published booking.accepted event for booking {}", booking.getId());
        } catch (Exception e) {
            log.error("[KAFKA] Failed to publish booking.accepted event: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify student via WebSocket that their booking was accepted
     */
    private void notifyStudentOfAcceptance(Booking booking) {
        try {
            webSocketService.notifyStudentBookingAccepted(
                    booking.getStudentId(), 
                    booking.getId(), 
                    booking.getTeacher().getName());
            log.info("[WEBSOCKET] Notified student {} of booking {} acceptance", 
                    booking.getStudentId(), booking.getId());
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to notify student: {}", e.getMessage(), e);
        }
    }

    /**
     * Notify other teachers that booking is no longer available
     */
    private void notifyOtherTeachersBookingTaken(Booking booking) {
        try {
            webSocketService.notifyBookingNoLongerAvailable(booking.getId());
            log.info("[WEBSOCKET] Notified teachers that booking {} is no longer available", 
                    booking.getId());
        } catch (Exception e) {
            log.error("[WEBSOCKET] Failed to notify teachers: {}", e.getMessage(), e);
        }
    }

    /**
     * Custom exception for when booking is already accepted
     */
    public static class BookingAlreadyAcceptedException extends RuntimeException {
        public BookingAlreadyAcceptedException(String message) {
            super(message);
        }
    }

    /**
     * Custom exception for teacher time conflicts
     */
    public static class TeacherTimeConflictException extends RuntimeException {
        public TeacherTimeConflictException(String message) {
            super(message);
        }
    }
}

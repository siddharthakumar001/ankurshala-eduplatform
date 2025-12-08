package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyBookingAccepted(Booking booking) {
        log.info("Sending booking accepted notification to student {}", booking.getStudent().getId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.accepted");
        notification.put("bookingId", booking.getId());
        notification.put("teacherName", booking.getTeacher().getName());
        notification.put("startTime", booking.getStartTime());
        notification.put("endTime", booking.getEndTime());
        notification.put("topicTitle", booking.getTopic().getTitle());
        
        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking accepted notification to {}", destination);
    }

    public void notifyBookingRescheduled(Booking booking) {
        log.info("Sending booking rescheduled notification to student {}", booking.getStudent().getId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.rescheduled");
        notification.put("bookingId", booking.getId());
        notification.put("teacherName", booking.getTeacher().getName());
        notification.put("startTime", booking.getStartTime());
        notification.put("endTime", booking.getEndTime());
        notification.put("topicTitle", booking.getTopic().getTitle());
        
        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking rescheduled notification to {}", destination);
    }

    public void notifyBookingCancelled(Booking booking) {
        log.info("Sending booking cancelled notification to student {}", booking.getStudent().getId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.cancelled");
        notification.put("bookingId", booking.getId());
        notification.put("teacherName", booking.getTeacher() != null ? booking.getTeacher().getName() : "TBD");
        notification.put("startTime", booking.getStartTime());
        notification.put("endTime", booking.getEndTime());
        notification.put("topicTitle", booking.getTopic().getTitle());
        notification.put("reason", booking.getCancellationReason());
        
        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking cancelled notification to {}", destination);
    }

    public void notifyBookingReminder(Booking booking) {
        log.info("Sending booking reminder to student {}", booking.getStudent().getId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.reminder");
        notification.put("bookingId", booking.getId());
        notification.put("teacherName", booking.getTeacher().getName());
        notification.put("startTime", booking.getStartTime());
        notification.put("endTime", booking.getEndTime());
        notification.put("topicTitle", booking.getTopic().getTitle());
        notification.put("minutesUntilStart", java.time.Duration.between(
            java.time.LocalDateTime.now(), booking.getStartTime()).toMinutes());
        
        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking reminder to {}", destination);
    }

    public void broadcastBookingRequest(Booking booking) {
        log.info("Broadcasting booking request to eligible teachers");
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.requested");
        notification.put("bookingId", booking.getId());
        notification.put("studentId", booking.getStudent().getId());
        notification.put("studentName", booking.getStudent().getName());
        notification.put("topicId", booking.getTopic().getId());
        notification.put("topicTitle", booking.getTopic().getTitle());
        notification.put("startTime", booking.getStartTime());
        notification.put("endTime", booking.getEndTime());
        notification.put("durationMinutes", booking.getDurationMinutes());
        notification.put("acceptanceToken", booking.getAcceptanceToken());
        
        // Broadcast to all teachers (in a real implementation, this would be filtered by eligibility)
        String destination = "/topic/teachers";
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Broadcasted booking request to {}", destination);
    }

    /**
     * Notify student that their booking was accepted by a teacher
     * Used by BookingConcurrencyService
     */
    public void notifyStudentBookingAccepted(Long studentId, Long bookingId, String teacherName) {
        log.info("Notifying student {} that booking {} was accepted by {}", studentId, bookingId, teacherName);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.accepted");
        notification.put("bookingId", bookingId);
        notification.put("teacherName", teacherName);
        notification.put("message", "Your booking request has been accepted by " + teacherName);
        
        String destination = "/topic/student/" + studentId;
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking accepted notification to student {}", studentId);
    }

    /**
     * Notify teachers that a booking is no longer available
     * Used when one teacher accepts, others need to know it's taken
     */
    public void notifyBookingNoLongerAvailable(Long bookingId) {
        log.info("Notifying teachers that booking {} is no longer available", bookingId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.taken");
        notification.put("bookingId", bookingId);
        notification.put("message", "This booking has been accepted by another teacher");
        
        String destination = "/topic/teachers";
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Broadcasted booking taken notification");
    }

    /**
     * Send notification to a specific user
     * Generic method for any type of notification
     */
    public void sendToUser(Long userId, Map<String, Object> notification) {
        log.info("Sending notification to user {}: {}", userId, notification.get("type"));
        
        String destination = "/topic/user/" + userId;
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent notification to user {}", userId);
    }

    /**
     * Send notification to a specific student
     */
    public void sendToStudent(Long studentId, Map<String, Object> notification) {
        log.info("Sending notification to student {}: {}", studentId, notification.get("type"));
        
        String destination = "/topic/student/" + studentId;
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent notification to student {}", studentId);
    }

    /**
     * Send notification to a specific teacher
     */
    public void sendToTeacher(Long teacherId, Map<String, Object> notification) {
        log.info("Sending notification to teacher {}: {}", teacherId, notification.get("type"));
        
        String destination = "/topic/teacher/" + teacherId;
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent notification to teacher {}", teacherId);
    }

    /**
     * Broadcast to all teachers
     */
    public void broadcastToTeachers(Map<String, Object> notification) {
        log.info("Broadcasting to all teachers: {}", notification.get("type"));
        
        String destination = "/topic/teachers";
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Broadcasted to all teachers");
    }

    /**
     * Broadcast to all students
     */
    public void broadcastToStudents(Map<String, Object> notification) {
        log.info("Broadcasting to all students: {}", notification.get("type"));
        
        String destination = "/topic/students";
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Broadcasted to all students");
    }
}

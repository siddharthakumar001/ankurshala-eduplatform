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
}

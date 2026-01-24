package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.repository.BookingDeclineRepository;
import com.ankurshala.backend.repository.TeacherSubjectExpertiseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private TeacherSubjectExpertiseRepository teacherSubjectExpertiseRepository;

    @Autowired
    private BookingDeclineRepository bookingDeclineRepository;
    
    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

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
        log.info("Broadcasting booking request to eligible teachers based on expertise");
        
        // Get topic details for matching
        Long subjectId = booking.getTopic().getSubjectId();
        Long gradeId = booking.getTopic().getGradeId();
        Long boardId = booking.getTopic().getBoardId();
        
        log.info("Finding teachers with expertise: subject={}, grade={}, board={}", subjectId, gradeId, boardId);
        
        // Find teachers with matching expertise using the eligible teacher query
        List<Long> matchingTeacherIds = 
            teacherSubjectExpertiseRepository.findEligibleTeacherIds(subjectId, gradeId, boardId);
        
        log.info("Found {} teachers with matching expertise", matchingTeacherIds.size());
        
        if (matchingTeacherIds.isEmpty()) {
            log.warn("No teachers found with matching expertise for booking {}.", booking.getId());
            return;
        }
        
        // Build notification payload
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.requested");
        notification.put("bookingId", booking.getId());
        notification.put("studentId", booking.getStudent().getId());
        notification.put("studentName", booking.getStudent().getName());
        notification.put("topicId", booking.getTopic().getId());
        notification.put("topicTitle", booking.getTopic().getTitle());
        notification.put("subjectName", booking.getTopic().getSubject() != null ? booking.getTopic().getSubject().getName() : "Unknown");
        notification.put("gradeName", booking.getTopic().getGrade() != null ? booking.getTopic().getGrade().getName() : "Unknown");
        notification.put("boardName", booking.getTopic().getBoard() != null ? booking.getTopic().getBoard().getName() : "Unknown");
        notification.put("startTime", booking.getStartTime());
        notification.put("endTime", booking.getEndTime());
        notification.put("durationMinutes", booking.getDurationMinutes());
        notification.put("acceptanceToken", booking.getAcceptanceToken());
        
        // Send notification to each matching teacher individually
        int notifiedCount = 0;
        for (Long teacherId : matchingTeacherIds) {
            if (bookingDeclineRepository.existsByBookingIdAndTeacherId(booking.getId(), teacherId)) {
                continue;
            }
            try {
                String destination = "/topic/teacher/" + teacherId;
                messagingTemplate.convertAndSend(destination, notification);
                notifiedCount++;
                log.debug("Sent booking request to teacher {}", teacherId);
            } catch (Exception e) {
                log.error("Failed to send notification to teacher {}: {}", teacherId, e.getMessage());
            }
        }
        
        log.info("Broadcasted booking request {} to {} matching teachers", booking.getId(), notifiedCount);
    }
    
    /**
     * Fallback method to broadcast to all teachers when no matching expertise is found
     */
    private void broadcastToAllTeachers(Booking booking) {
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
        notification.put("fallbackBroadcast", true);
        
        String destination = "/topic/teachers";
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Broadcasted booking request {} to all teachers (fallback)", booking.getId());
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
    public void notifyBookingNoLongerAvailable(Booking booking) {
        if (booking.getTopic() == null) {
            return;
        }

        log.info("Notifying teachers that booking {} is no longer available", booking.getId());

        Long subjectId = booking.getTopic().getSubjectId();
        Long gradeId = booking.getTopic().getGradeId();
        Long boardId = booking.getTopic().getBoardId();

        List<Long> matchingTeacherIds =
            teacherSubjectExpertiseRepository.findEligibleTeacherIds(subjectId, gradeId, boardId);

        if (matchingTeacherIds.isEmpty()) {
            return;
        }

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.taken");
        notification.put("bookingId", booking.getId());
        notification.put("message", "This booking has been accepted by another teacher");

        for (Long teacherId : matchingTeacherIds) {
            try {
                String destination = "/topic/teacher/" + teacherId;
                messagingTemplate.convertAndSend(destination, notification);
            } catch (Exception e) {
                log.error("Failed to send booking taken notification to teacher {}: {}", teacherId, e.getMessage());
            }
        }

        log.info("Broadcasted booking taken notification to {} teachers", matchingTeacherIds.size());
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

    /**
     * Notify student that booking status changed to CONFIRMED
     * Triggered when payment is processed successfully
     */
    public void notifyBookingConfirmed(Booking booking) {
        log.info("Sending booking confirmed notification to student {}", booking.getStudent().getId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.confirmed");
        notification.put("bookingId", booking.getId());
        notification.put("status", "CONFIRMED");
        notification.put("teacherName", booking.getTeacher() != null ? booking.getTeacher().getName() : "TBD");
        notification.put("startTime", booking.getStartTs());
        notification.put("endTime", booking.getEndTs());
        notification.put("topicTitle", booking.getTopic() != null ? booking.getTopic().getTitle() : "Unknown");
        notification.put("message", "Your booking has been confirmed and is ready!");
        
        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking confirmed notification to {}", destination);
    }

    /**
     * Notify student that booking status changed to IN_PROGRESS
     * Triggered when teacher starts the session
     */
    public void notifyBookingInProgress(Booking booking) {
        log.info("Sending booking in-progress notification to student {}", booking.getStudent().getId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.in_progress");
        notification.put("bookingId", booking.getId());
        notification.put("status", "IN_PROGRESS");
        notification.put("teacherName", booking.getTeacher() != null ? booking.getTeacher().getName() : "TBD");
        notification.put("topicTitle", booking.getTopic() != null ? booking.getTopic().getTitle() : "Unknown");
        notification.put("message", "Your session has started! You can now use the live notes feature.");
        notification.put("companionReady", true);
        
        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking in-progress notification to {}", destination);
    }

    /**
     * Notify student that booking status changed to COMPLETED
     * Triggered when teacher ends the session
     */
    public void notifyBookingCompleted(Booking booking) {
        log.info("Sending booking completed notification to student {}", booking.getStudent().getId());
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.completed");
        notification.put("bookingId", booking.getId());
        notification.put("status", "COMPLETED");
        notification.put("teacherName", booking.getTeacher() != null ? booking.getTeacher().getName() : "TBD");
        notification.put("topicTitle", booking.getTopic() != null ? booking.getTopic().getTitle() : "Unknown");
        notification.put("message", "Your session has been completed. Check the post-session summary!");
        notification.put("postSummaryAvailable", true);
        
        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);
        
        log.info("Sent booking completed notification to {}", destination);
    }

    public void notifyBookingExpired(Booking booking, String reason) {
        log.info("Sending booking expired notification to student {}", booking.getStudent().getId());

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "booking.expired");
        notification.put("bookingId", booking.getId());
        notification.put("status", "EXPIRED");
        notification.put("topicTitle", booking.getTopic() != null ? booking.getTopic().getTitle() : "Unknown");
        notification.put("message", reason != null ? reason : "No teachers were available for this slot.");

        String destination = "/topic/student/" + booking.getStudent().getId();
        messagingTemplate.convertAndSend(destination, notification);

        log.info("Sent booking expired notification to {}", destination);
    }
}

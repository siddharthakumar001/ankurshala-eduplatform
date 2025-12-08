package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.entity.NotificationAudience;
import com.ankurshala.backend.entity.NotificationDelivery;
import com.ankurshala.backend.entity.NotificationStatus;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentSessionManagementService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherSessionFeedbackRepository teacherSessionFeedbackRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public JoinSessionResponse joinSession(JoinSessionRequest request, UserPrincipal userPrincipal) {
        log.info("Student {} attempting to join session for booking {}", userPrincipal.getId(), request.getBookingId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Verify student owns this booking
        if (!booking.getStudent().getId().equals(student.getId())) {
            return new JoinSessionResponse() {{
                setSuccess(false);
                setMessage("You are not authorized to join this session");
                setCanJoin(false);
                setReason("UNAUTHORIZED");
            }};
        }
        
        // Check if session can be joined
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionStart = booking.getStartTs().toLocalDateTime();
        LocalDateTime sessionEnd = booking.getEndTs().toLocalDateTime();
        
        // Can join 15 minutes before session starts and during the session
        boolean canJoin = now.isAfter(sessionStart.minusMinutes(15)) && now.isBefore(sessionEnd.plusMinutes(15));
        
        if (!canJoin) {
            return new JoinSessionResponse() {{
                setSuccess(false);
                setMessage("Session is not available for joining at this time");
                setCanJoin(false);
                setReason("SESSION_NOT_AVAILABLE");
            }};
        }
        
        // Check if session is accepted
        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            return new JoinSessionResponse() {{
                setSuccess(false);
                setMessage("Session is not confirmed by the teacher");
                setCanJoin(false);
                setReason("SESSION_NOT_CONFIRMED");
            }};
        }
        
        // Generate session URL (mock for now)
        String sessionUrl = generateSessionUrl(booking);
        String sessionId = "session_" + booking.getId() + "_" + System.currentTimeMillis();
        
        // Send notification to teacher
        sendSessionJoinNotification(booking, student);
        
        return new JoinSessionResponse() {{
            setSuccess(true);
            setSessionUrl(sessionUrl);
            setSessionId(sessionId);
            setSessionStartTime(sessionStart);
            setSessionEndTime(sessionEnd);
            setMessage("Successfully joined the session");
            setCanJoin(true);
        }};
    }

    public SessionFeedbackResponse submitSessionFeedback(SessionFeedbackRequest request, UserPrincipal userPrincipal) {
        log.info("Student {} submitting feedback for booking {}", userPrincipal.getId(), request.getBookingId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Verify student owns this booking
        if (!booking.getStudent().getId().equals(student.getId())) {
            return new SessionFeedbackResponse() {{
                setSuccess(false);
                setMessage("You are not authorized to submit feedback for this session");
            }};
        }
        
        // Check if session is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            return new SessionFeedbackResponse() {{
                setSuccess(false);
                setMessage("Feedback can only be submitted for completed sessions");
            }};
        }
        
        // Create or update session feedback
        Optional<TeacherSessionFeedback> existingFeedback = teacherSessionFeedbackRepository
                .findByBookingId(request.getBookingId());
        
        TeacherSessionFeedback feedback;
        if (existingFeedback.isPresent()) {
            feedback = existingFeedback.get();
            feedback.setStudentRating(request.getRating());
            feedback.setStudentFeedback(request.getFeedback());
            feedback.setStudentNotes(request.getSessionNotes());
            feedback.setWouldRecommend(request.isWouldRecommend());
        } else {
            feedback = new TeacherSessionFeedback();
            feedback.setBooking(booking);
            // Get the teacher entity from the booking's teacher user
            Teacher teacher = teacherRepository.findByUser(booking.getTeacher()).orElse(null);
            feedback.setTeacher(teacher);
            feedback.setStudentRating(request.getRating());
            feedback.setStudentFeedback(request.getFeedback());
            feedback.setStudentNotes(request.getSessionNotes());
            feedback.setWouldRecommend(request.isWouldRecommend());
        }
        
        feedback.setUpdatedAt(LocalDateTime.now());
        teacherSessionFeedbackRepository.save(feedback);
        
        // Send notification to teacher
        sendFeedbackNotification(booking, student, request.getRating());
        
        return new SessionFeedbackResponse() {{
            setSuccess(true);
            setMessage("Feedback submitted successfully");
            setSubmittedAt(LocalDateTime.now());
            setFeedbackId(feedback.getId());
        }};
    }

    private String generateSessionUrl(Booking booking) {
        // Mock session URL generation
        return "https://meet.ankurshala.com/session/" + booking.getId() + "?token=" + 
               generateSessionToken(booking);
    }

    private String generateSessionToken(Booking booking) {
        // Mock token generation
        return "token_" + booking.getId() + "_" + System.currentTimeMillis();
    }

    private void sendSessionJoinNotification(Booking booking, User student) {
        try {
            // Create notification for teacher
            Notification notification = new Notification();
            notification.setUser(booking.getTeacher());
            notification.setTitle("Student Joined Session");
            notification.setBody(student.getName() + " has joined the session for " + booking.getTopic().getTitle());
            notification.setAudience(NotificationAudience.TEACHER);
            notification.setDelivery(NotificationDelivery.IN_APP);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
            // Send Kafka event
            kafkaTemplate.send("session-events", "student-joined", new Object() {{
                // Session join event data
            }});
            
        } catch (Exception e) {
            log.error("Failed to send session join notification", e);
        }
    }

    private void sendFeedbackNotification(Booking booking, User student, Integer rating) {
        try {
            // Create notification for teacher
            Notification notification = new Notification();
            notification.setUser(booking.getTeacher());
            notification.setTitle("Session Feedback Received");
            notification.setBody(student.getName() + " rated your session " + rating + "/5 stars");
            notification.setAudience(NotificationAudience.TEACHER);
            notification.setDelivery(NotificationDelivery.IN_APP);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
            // Send Kafka event
            kafkaTemplate.send("feedback-events", "student-feedback", new Object() {{
                // Feedback event data
            }});
            
        } catch (Exception e) {
            log.error("Failed to send feedback notification", e);
        }
    }
}

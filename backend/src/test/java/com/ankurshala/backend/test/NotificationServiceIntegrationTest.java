package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.notification.MarkNotificationsReadRequest;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NotificationServiceIntegrationTest {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    private User student;
    private User teacher;

    @BeforeEach
    void setUp() {
        // Clean up
        notificationRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test users
        student = createTestStudent();
        teacher = createTestTeacher();
    }

    @Test
    void testCreateNotificationSuccess() {
        // Given
        Long userId = student.getId();
        NotificationType type = NotificationType.BOOKING_REQUESTED;
        String title = "Test Notification";
        String message = "This is a test notification";
        Map<String, Object> meta = Map.of("testKey", "testValue");

        // When
        Notification notification = notificationService.createNotification(userId, type, title, message, meta);

        // Then
        assertNotNull(notification);
        assertNotNull(notification.getId());
        assertEquals(userId, notification.getUserId());
        assertEquals(type, notification.getType());
        assertEquals(title, notification.getTitle());
        assertEquals(message, notification.getMessage());
        assertEquals(false, notification.getRead());
        assertNotNull(notification.getCreatedAt());
        
        // Verify notification is saved
        assertTrue(notificationRepository.existsById(notification.getId()));
    }

    @Test
    void testGetNotificationsForUser() {
        // Given
        Long userId = student.getId();
        notificationService.createNotification(userId, NotificationType.BOOKING_REQUESTED, "Title 1", "Message 1", Map.of());
        notificationService.createNotification(userId, NotificationType.BOOKING_ACCEPTED, "Title 2", "Message 2", Map.of());

        // When
        List<com.ankurshala.backend.dto.notification.NotificationDto> notifications = 
            notificationService.getUserNotifications(userId);

        // Then
        assertEquals(2, notifications.size());
        assertEquals(NotificationType.BOOKING_ACCEPTED, notifications.get(0).getType()); // Most recent first
        assertEquals(NotificationType.BOOKING_REQUESTED, notifications.get(1).getType());
    }

    @Test
    void testGetUnreadNotificationsForUser() {
        // Given
        Long userId = student.getId();
        notificationService.createNotification(userId, NotificationType.BOOKING_REQUESTED, "Title 1", "Message 1", Map.of());
        notificationService.createNotification(userId, NotificationType.BOOKING_ACCEPTED, "Title 2", "Message 2", Map.of());

        // When
        List<com.ankurshala.backend.dto.notification.NotificationDto> notifications = 
            notificationService.getUnreadNotifications(userId);

        // Then
        assertEquals(2, notifications.size());
        assertFalse(notifications.get(0).getRead());
        assertFalse(notifications.get(1).getRead());
    }

    @Test
    void testMarkNotificationsAsRead() {
        // Given
        Long userId = student.getId();
        Notification notification1 = notificationService.createNotification(userId, NotificationType.BOOKING_REQUESTED, "Title 1", "Message 1", Map.of());
        Notification notification2 = notificationService.createNotification(userId, NotificationType.BOOKING_ACCEPTED, "Title 2", "Message 2", Map.of());
        
        MarkNotificationsReadRequest request = new MarkNotificationsReadRequest();
        request.setNotificationIds(List.of(notification1.getId(), notification2.getId()));

        // When
        notificationService.markNotificationsAsRead(userId, request);

        // Then
        List<com.ankurshala.backend.dto.notification.NotificationDto> notifications = 
            notificationService.getUserNotifications(userId);
        
        assertTrue(notifications.get(0).getRead());
        assertTrue(notifications.get(1).getRead());
    }

    @Test
    void testSendBookingNotification() {
        // Given
        Long studentId = student.getId();
        Long teacherId = teacher.getId();
        NotificationType type = NotificationType.BOOKING_ACCEPTED;
        String title = "Booking Accepted";
        String message = "Your booking has been accepted";
        Map<String, Object> meta = Map.of("bookingId", 123L);

        // When
        notificationService.sendBookingNotification(studentId, teacherId, type, title, message, meta);

        // Then
        List<Notification> studentNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(studentId);
        List<Notification> teacherNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(teacherId);
        
        assertEquals(1, studentNotifications.size());
        assertEquals(1, teacherNotifications.size());
        
        assertEquals(type, studentNotifications.get(0).getType());
        assertEquals(type, teacherNotifications.get(0).getType());
    }

    @Test
    void testNotificationOrdering() {
        // Given
        Long userId = student.getId();
        
        // Create notifications with slight delay to ensure different timestamps
        notificationService.createNotification(userId, NotificationType.BOOKING_REQUESTED, "Title 1", "Message 1", Map.of());
        try {
            Thread.sleep(10); // Small delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        notificationService.createNotification(userId, NotificationType.BOOKING_ACCEPTED, "Title 2", "Message 2", Map.of());

        // When
        List<com.ankurshala.backend.dto.notification.NotificationDto> notifications = 
            notificationService.getUserNotifications(userId);

        // Then
        assertEquals(2, notifications.size());
        // Most recent should be first
        assertEquals(NotificationType.BOOKING_ACCEPTED, notifications.get(0).getType());
        assertEquals(NotificationType.BOOKING_REQUESTED, notifications.get(1).getType());
    }

    private User createTestStudent() {
        User user = new User();
        user.setName("Test Student");
        user.setEmail("student@test.com");
        user.setPassword("password");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private User createTestTeacher() {
        User user = new User();
        user.setName("Test Teacher");
        user.setEmail("teacher@test.com");
        user.setPassword("password");
        user.setRole(Role.TEACHER);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}

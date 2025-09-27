package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.BroadcastNotificationRequest;
import com.ankurshala.backend.dto.admin.NotificationDto;
import com.ankurshala.backend.entity.Notification;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.NotificationRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<NotificationDto> getNotifications(Long userId, String audience, String status, Pageable pageable) {
        Notification.NotificationAudience audienceEnum = null;
        if (audience != null) {
            try {
                audienceEnum = Notification.NotificationAudience.valueOf(audience.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid audience, ignore filter
            }
        }

        Notification.NotificationStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = Notification.NotificationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        Page<Notification> notifications = notificationRepository.findAllNotifications(pageable);
        
        return notifications.map(this::convertToDto);
    }

    public Map<String, Object> broadcastNotification(BroadcastNotificationRequest request) {
        Notification.NotificationAudience audience = Notification.NotificationAudience.valueOf(
                request.getAudience().toUpperCase());
        Notification.NotificationDelivery delivery = Notification.NotificationDelivery.valueOf(
                request.getDelivery().toUpperCase());

        // Get target users based on audience
        List<User> targetUsers = getTargetUsers(audience);
        
        int inAppCount = 0;
        int emailCount = 0;
        int failedCount = 0;

        for (User user : targetUsers) {
            try {
                // Create notification record
                Notification notification = new Notification(
                        request.getTitle(),
                        request.getBody(),
                        audience,
                        delivery
                );
                notification.setUser(user);
                notification.setStatus(Notification.NotificationStatus.QUEUED);
                
                notificationRepository.save(notification);

                // Send email if requested
                if (delivery == Notification.NotificationDelivery.EMAIL || 
                    delivery == Notification.NotificationDelivery.BOTH) {
                    // Email sending would be implemented here when mail service is configured
                    emailCount++;
                }

                // Mark as sent for in-app notifications
                if (delivery == Notification.NotificationDelivery.IN_APP || 
                    delivery == Notification.NotificationDelivery.BOTH) {
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    inAppCount++;
                }

            } catch (Exception e) {
                failedCount++;
                // Log error but continue with other users
                System.err.println("Failed to send notification to user " + user.getId() + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", targetUsers.size());
        result.put("inAppSent", inAppCount);
        result.put("emailSent", emailCount);
        result.put("failed", failedCount);
        result.put("message", "Notification broadcast completed");

        return result;
    }

    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalNotifications", notificationRepository.count());
        stats.put("queuedNotifications", notificationRepository.countByStatus(Notification.NotificationStatus.QUEUED));
        stats.put("sentNotifications", notificationRepository.countByStatus(Notification.NotificationStatus.SENT));
        stats.put("failedNotifications", notificationRepository.countByStatus(Notification.NotificationStatus.FAILED));
        
        // Last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("notificationsLast30Days", notificationRepository.countByCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now()));

        return stats;
    }

    private List<User> getTargetUsers(Notification.NotificationAudience audience) {
        switch (audience) {
            case STUDENT:
                return userRepository.findByRole(Role.STUDENT);
            case TEACHER:
                return userRepository.findByRole(Role.TEACHER);
            case BOTH:
                return userRepository.findByRoleIn(List.of(Role.STUDENT, Role.TEACHER));
            default:
                return List.of();
        }
    }

    private void sendEmail(String to, String subject, String body) {
        // Email sending implementation would go here
        // For now, just log the email that would be sent
        System.out.println("Would send email to: " + to + ", Subject: " + subject);
    }

    private NotificationDto convertToDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getUser() != null ? notification.getUser().getEmail() : null,
                notification.getTitle(),
                notification.getBody(),
                notification.getAudience().toString(),
                notification.getDelivery().toString(),
                notification.getStatus().toString(),
                notification.getCreatedAt(),
                notification.getSentAt()
        );
    }
}

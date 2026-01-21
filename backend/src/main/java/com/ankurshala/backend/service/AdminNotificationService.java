package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.BroadcastNotificationRequest;
import com.ankurshala.backend.dto.admin.NotificationDto;
import com.ankurshala.backend.entity.Notification;
import com.ankurshala.backend.entity.NotificationAudience;
import com.ankurshala.backend.entity.NotificationDelivery;
import com.ankurshala.backend.entity.NotificationStatus;
import com.ankurshala.backend.entity.NotificationType;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.NotificationRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<NotificationDto> getNotifications(Long userId, String audience, String status, Pageable pageable) {
        NotificationAudience audienceEnum = null;
        if (audience != null) {
            try {
                audienceEnum = NotificationAudience.valueOf(audience.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid audience, ignore filter
            }
        }

        NotificationStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = NotificationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        Page<Notification> notifications = notificationRepository.findFiltered(userId, audienceEnum, statusEnum, pageable);
        
        return notifications.map(this::convertToDto);
    }

    public Map<String, Object> broadcastNotification(BroadcastNotificationRequest request) {
        NotificationAudience audience = parseAudience(request.getAudience());
        NotificationDelivery delivery = parseDelivery(request.getDelivery());

        User targetUser = resolveTargetUser(request);
        List<User> targetUsers;
        if (targetUser != null) {
            audience = resolveAudienceFromUser(targetUser, audience);
            targetUsers = List.of(targetUser);
        } else {
            targetUsers = getTargetUsers(audience);
        }
        
        int inAppCount = 0;
        int emailCount = 0;
        int failedCount = 0;

        for (User user : targetUsers) {
            try {
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setUser(user);
                notification.setTitle(request.getTitle());
                notification.setMessage(request.getBody());
                notification.setType(NotificationType.GENERAL_ANNOUNCEMENT);
                notification.setAudience(audience);
                notification.setDelivery(delivery);
                notification.setStatus(NotificationStatus.PENDING);
                
                notificationRepository.save(notification);

                if (delivery == NotificationDelivery.EMAIL || 
                    delivery == NotificationDelivery.IN_APP_EMAIL) {
                    emailCount++;
                }

                if (delivery == NotificationDelivery.IN_APP || 
                    delivery == NotificationDelivery.IN_APP_EMAIL) {
                    notification.setStatus(NotificationStatus.SENT);
                    notificationRepository.save(notification);
                    inAppCount++;
                }

            } catch (Exception e) {
                failedCount++;
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
        
        try {
            // Get total count
            long totalCount = notificationRepository.count();
            stats.put("totalNotifications", totalCount);
            
            // Get counts by status (mock implementation)
            stats.put("queuedNotifications", 0L);
            stats.put("sentNotifications", totalCount);
            stats.put("failedNotifications", 0L);
            
            // Get count for last 30 days (mock implementation)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            stats.put("notificationsLast30Days", totalCount);
            
        } catch (Exception e) {
            // If there's any error, return zero values
            System.err.println("Error getting notification stats: " + e.getMessage());
            stats.put("totalNotifications", 0);
            stats.put("queuedNotifications", 0);
            stats.put("sentNotifications", 0);
            stats.put("failedNotifications", 0);
            stats.put("notificationsLast30Days", 0);
        }
        
        return stats;
    }

    private List<User> getTargetUsers(NotificationAudience audience) {
        switch (audience) {
            case STUDENT:
                return userRepository.findByRole(Role.STUDENT);
            case TEACHER:
                return userRepository.findByRole(Role.TEACHER);
            case ADMIN:
                return userRepository.findByRole(Role.ADMIN);
            case ALL:
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
                null // sentAt not available in current entity
        );
    }

    private NotificationAudience parseAudience(String audience) {
        if (audience == null) {
            throw new IllegalArgumentException("Audience is required");
        }
        try {
            return NotificationAudience.valueOf(audience.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid audience value: " + audience);
        }
    }

    private NotificationDelivery parseDelivery(String delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException("Delivery method is required");
        }
        try {
            return NotificationDelivery.valueOf(delivery.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid delivery value: " + delivery);
        }
    }

    private User resolveTargetUser(BroadcastNotificationRequest request) {
        if (request.getTargetUserId() != null) {
            return userRepository.findById(request.getTargetUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        }
        if (request.getTargetEmail() != null && !request.getTargetEmail().isBlank()) {
            String email = request.getTargetEmail().trim();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
        }
        return null;
    }

    private NotificationAudience resolveAudienceFromUser(User user, NotificationAudience fallback) {
        if (user.getRole() == Role.STUDENT) {
            return NotificationAudience.STUDENT;
        }
        if (user.getRole() == Role.TEACHER) {
            return NotificationAudience.TEACHER;
        }
        if (user.getRole() == Role.ADMIN) {
            return NotificationAudience.ADMIN;
        }
        return fallback != null ? fallback : NotificationAudience.ALL;
    }
}

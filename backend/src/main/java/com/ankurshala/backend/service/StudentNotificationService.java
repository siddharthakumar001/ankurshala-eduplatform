package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.StudentNotificationDto;
import com.ankurshala.backend.dto.student.StudentNotificationSettingsDto;
import com.ankurshala.backend.entity.Notification;
import com.ankurshala.backend.entity.NotificationStatus;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.NotificationRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Page<StudentNotificationDto> getNotifications(UserPrincipal userPrincipal, Pageable pageable) {
        log.info("Getting notifications for student {}", userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(student.getId(), pageable);
        
        return notifications.map(this::convertToNotificationDto);
    }

    public StudentNotificationDto markAsRead(Long notificationId, UserPrincipal userPrincipal) {
        log.info("Marking notification {} as read for student {}", notificationId, userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        // Verify student owns this notification
        if (!notification.getUser().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You are not authorized to access this notification");
        }
        
        notification.setStatus(NotificationStatus.READ);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        return convertToNotificationDto(notification);
    }

    public void markAllAsRead(UserPrincipal userPrincipal) {
        log.info("Marking all notifications as read for student {}", userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        // Use the repository method to mark all as read
        notificationRepository.markAllAsRead(student.getId());
    }

    public StudentNotificationSettingsDto getNotificationSettings(UserPrincipal userPrincipal) {
        log.info("Getting notification settings for student {}", userPrincipal.getId());
        
        // Mock notification settings
        return new StudentNotificationSettingsDto() {{
            setEmailNotifications(true);
            setSmsNotifications(true);
            setPushNotifications(true);
            setBookingConfirmations(true);
            setSessionReminders(true);
            setPaymentNotifications(true);
            setFeedbackReminders(true);
            setPromotionalEmails(false);
            setReminderMinutesBeforeSession(15);
            setPreferredNotificationTime("09:00");
        }};
    }

    public StudentNotificationSettingsDto updateNotificationSettings(
            StudentNotificationSettingsDto settings, UserPrincipal userPrincipal) {
        log.info("Updating notification settings for student {}", userPrincipal.getId());
        
        // Mock update - in real implementation, save to database
        return settings;
    }

    public int getUnreadNotificationCount(UserPrincipal userPrincipal) {
        log.info("Getting unread notification count for student {}", userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Long count = notificationRepository.countUnreadByUserId(student.getId());
        return count != null ? count.intValue() : 0;
    }

    private StudentNotificationDto convertToNotificationDto(Notification notification) {
        return new StudentNotificationDto() {{
            setId(notification.getId());
            setTitle(notification.getTitle());
            setMessage(notification.getBody());
            setType(notification.getAudience().toString());
            setRead(notification.getStatus() == NotificationStatus.READ);
            setCreatedAt(notification.getCreatedAt());
            setActionUrl(determineActionUrl(notification));
            setActionText(determineActionText(notification));
        }};
    }

    private String determineActionUrl(Notification notification) {
        // Determine action URL based on notification type
        String title = notification.getTitle().toLowerCase();
        if (title.contains("booking")) {
            return "/student/bookings";
        } else if (title.contains("payment")) {
            return "/student/payments";
        } else if (title.contains("session")) {
            return "/student/sessions";
        } else if (title.contains("feedback")) {
            return "/student/history";
        }
        return "/student/dashboard";
    }

    private String determineActionText(Notification notification) {
        // Determine action text based on notification type
        String title = notification.getTitle().toLowerCase();
        if (title.contains("booking")) {
            return "View Bookings";
        } else if (title.contains("payment")) {
            return "View Payments";
        } else if (title.contains("session")) {
            return "View Sessions";
        } else if (title.contains("feedback")) {
            return "View History";
        }
        return "View Details";
    }
}

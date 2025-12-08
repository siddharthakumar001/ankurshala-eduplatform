package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.notification.MarkNotificationsReadRequest;
import com.ankurshala.backend.dto.notification.NotificationDto;
import com.ankurshala.backend.entity.Notification;
import com.ankurshala.backend.entity.NotificationType;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.repository.NotificationRepository;
import com.ankurshala.backend.util.TraceUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public Notification createNotification(Long userId, NotificationType type, String title, String message, Map<String, Object> meta) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating notification for user {} - TraceId: {}, Type: {}", userId, traceId, type);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        
        if (meta != null) {
            try {
                notification.setMeta(objectMapper.writeValueAsString(meta));
            } catch (Exception e) {
                log.warn("Failed to serialize notification meta - TraceId: {}, Error: {}", traceId, e.getMessage());
            }
        }

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created with ID {} for user {} - TraceId: {}", savedNotification.getId(), userId, traceId);

        // Send WebSocket notification
        sendWebSocketNotification(userId, convertToDto(savedNotification));
        
        return savedNotification;
    }

    public List<NotificationDto> getUserNotifications(Long userId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting notifications for user {} - TraceId: {}", userId, traceId);

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<NotificationDto> getUnreadNotifications(Long userId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting unread notifications for user {} - TraceId: {}", userId, traceId);

        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markNotificationsAsRead(Long userId, MarkNotificationsReadRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Marking notifications as read for user {} - TraceId: {}", userId, traceId);

        if (request.getNotificationIds() == null || request.getNotificationIds().isEmpty()) {
            // Mark all as read
            notificationRepository.markAllAsRead(userId);
            log.info("All notifications marked as read for user {} - TraceId: {}", userId, traceId);
        } else {
            // Mark specific notifications as read
            int updated = notificationRepository.markAsRead(userId, request.getNotificationIds());
            log.info("{} notifications marked as read for user {} - TraceId: {}", updated, userId, traceId);
        }
    }

    public void sendWebSocketNotification(Long userId, NotificationDto notification) {
        try {
            String destination = "/topic/user/" + userId;
            messagingTemplate.convertAndSend(destination, notification);
            log.debug("WebSocket notification sent to user {} - TraceId: {}", userId, TraceUtil.getTraceId());
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification to user {} - TraceId: {}, Error: {}", 
                    userId, TraceUtil.getTraceId(), e.getMessage());
        }
    }

    public void sendBookingNotification(Long studentId, Long teacherId, NotificationType type, String title, String message, Map<String, Object> meta) {
        // Send to student
        if (studentId != null) {
            createNotification(studentId, type, title, message, meta);
        }
        
        // Send to teacher
        if (teacherId != null) {
            createNotification(teacherId, type, title, message, meta);
        }
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.getRead());
        dto.setCreatedAt(notification.getCreatedAt());
        
        if (notification.getMeta() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> meta = objectMapper.readValue(notification.getMeta(), Map.class);
                dto.setMeta(meta);
            } catch (Exception e) {
                log.warn("Failed to deserialize notification meta for ID {} - Error: {}", notification.getId(), e.getMessage());
            }
        }
        
        return dto;
    }
}

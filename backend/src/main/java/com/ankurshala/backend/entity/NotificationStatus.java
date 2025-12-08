package com.ankurshala.backend.entity;

/**
 * Notification status enum
 */
public enum NotificationStatus {
    PENDING,        // Notification created but not sent
    SENT,           // Notification sent successfully
    DELIVERED,      // Notification delivered to recipient
    READ,           // Notification read by recipient
    FAILED,         // Notification failed to send
    CANCELLED       // Notification cancelled
}

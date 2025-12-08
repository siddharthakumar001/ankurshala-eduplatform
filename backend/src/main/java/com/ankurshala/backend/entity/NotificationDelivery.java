package com.ankurshala.backend.entity;

/**
 * Notification delivery method enum
 */
public enum NotificationDelivery {
    IN_APP,         // In-app notification only
    EMAIL,          // Email notification only
    SMS,            // SMS notification only
    PUSH,           // Push notification only
    IN_APP_EMAIL,   // Both in-app and email
    IN_APP_SMS,     // Both in-app and SMS
    IN_APP_PUSH,    // Both in-app and push
    ALL             // All delivery methods
}

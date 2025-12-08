package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentNotificationDto {
    private Long id;
    private String title;
    private String message;
    private String type; // BOOKING_CONFIRMED, SESSION_REMINDER, PAYMENT_DUE, etc.
    private boolean isRead;
    private LocalDateTime createdAt;
    private String actionUrl;
    private String actionText;
}

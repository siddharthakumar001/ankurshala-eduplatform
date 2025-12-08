package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentNotificationSettingsDto {
    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean pushNotifications;
    private boolean bookingConfirmations;
    private boolean sessionReminders;
    private boolean paymentNotifications;
    private boolean feedbackReminders;
    private boolean promotionalEmails;
    private int reminderMinutesBeforeSession;
    private String preferredNotificationTime;
}

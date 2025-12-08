package com.ankurshala.backend.dto.notification;

import lombok.Data;

import java.util.List;

@Data
public class MarkNotificationsReadRequest {
    private List<Long> notificationIds; // If null, mark all as read
}

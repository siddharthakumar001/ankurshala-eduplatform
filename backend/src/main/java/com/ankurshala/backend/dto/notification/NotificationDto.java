package com.ankurshala.backend.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationDto {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Boolean read;
    private Map<String, Object> meta;
    private LocalDateTime createdAt;
}

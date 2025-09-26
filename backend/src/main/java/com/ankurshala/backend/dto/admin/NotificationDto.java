package com.ankurshala.backend.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NotificationDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private String title;
    private String body;
    private String audience;
    private String delivery;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public NotificationDto(Long id, Long userId, String userEmail, String title, String body,
                           String audience, String delivery, String status,
                           LocalDateTime createdAt, LocalDateTime sentAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.title = title;
        this.body = body;
        this.audience = audience;
        this.delivery = delivery;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }
}

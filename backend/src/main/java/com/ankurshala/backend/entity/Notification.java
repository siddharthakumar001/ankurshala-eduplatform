package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false)
    private NotificationAudience audience;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery", nullable = false)
    private NotificationDelivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.QUEUED;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public enum NotificationAudience {
        STUDENT, TEACHER, BOTH
    }

    public enum NotificationDelivery {
        IN_APP, EMAIL, BOTH
    }

    public enum NotificationStatus {
        QUEUED, SENT, FAILED
    }

    public Notification(String title, String body, NotificationAudience audience, NotificationDelivery delivery) {
        this.title = title;
        this.body = body;
        this.audience = audience;
        this.delivery = delivery;
    }
}

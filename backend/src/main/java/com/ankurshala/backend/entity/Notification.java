package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false)
    private NotificationAudience audience;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery", nullable = false)
    private NotificationDelivery delivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "read", nullable = false)
    private Boolean read = false;

    @Column(name = "meta", columnDefinition = "JSONB")
    private String meta; // JSON metadata for additional data

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Additional methods for compatibility
    public void setBody(String body) {
        this.message = body;
    }

    public String getBody() {
        return message;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        // This method is needed for compatibility but updatedAt is managed by @UpdateTimestamp
        // So we don't actually set it here
    }
}
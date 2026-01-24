package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_declines",
        uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "teacher_id"}))
@Data
public class BookingDecline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "declined_at", nullable = false, updatable = false)
    private LocalDateTime declinedAt;
}

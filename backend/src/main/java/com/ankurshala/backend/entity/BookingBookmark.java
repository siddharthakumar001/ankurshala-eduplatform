package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_bookmarks")
@Data
public class BookingBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "ts_seconds", nullable = false)
    private Integer tsSeconds;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public BookingBookmark() {}

    public BookingBookmark(Booking booking, User student) {
        this.bookingId = booking.getId();
        this.studentId = student.getId();
        this.createdAt = LocalDateTime.now();
    }
}
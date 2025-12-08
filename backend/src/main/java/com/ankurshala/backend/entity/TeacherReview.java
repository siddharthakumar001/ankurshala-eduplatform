package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_reviews", indexes = {
    @Index(name = "idx_teacher_review_teacher", columnList = "teacherId"),
    @Index(name = "idx_teacher_review_student", columnList = "studentId"),
    @Index(name = "idx_teacher_review_booking", columnList = "bookingId"),
    @Index(name = "idx_teacher_review_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Double rating; // 1.0 to 5.0

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Builder.Default
    @Column(nullable = false)
    private Boolean approved = true; // For moderation

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

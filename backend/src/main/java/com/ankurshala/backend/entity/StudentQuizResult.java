package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_quiz_result")
@Data
public class StudentQuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "breakdown", columnDefinition = "JSONB")
    private String breakdown;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

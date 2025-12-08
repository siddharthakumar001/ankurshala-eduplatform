package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "student_study_list",
    indexes = {
        @Index(name = "idx_study_list_student", columnList = "student_id"),
        @Index(name = "idx_study_list_topic", columnList = "topic_id"),
        @Index(name = "idx_study_list_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_topic", columnNames = {"student_id", "topic_id"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentStudyList {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "topic_id", nullable = false)
    private Long topicId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyStatus status;
    
    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private ZonedDateTime addedAt;
    
    @Column(name = "done_at")
    private ZonedDateTime doneAt;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    public enum StudyStatus {
        ADDED,
        IN_PROGRESS,
        DONE
    }
}

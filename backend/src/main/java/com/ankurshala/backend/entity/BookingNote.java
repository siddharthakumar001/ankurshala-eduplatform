package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_notes")
@Data
public class BookingNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_role", nullable = false)
    private Role authorRole;

    @Column(name = "url")
    private String url;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "note_type")
    private String noteType;

    @Column(name = "author_id")
    private Long authorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Additional getters and setters for compatibility
    public String getContent() {
        return content != null ? content : text;
    }

    public void setContent(String content) {
        this.content = content;
        this.text = content; // Keep both in sync
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public Long getAuthor() {
        return authorId;
    }

    public void setAuthor(Long authorId) {
        this.authorId = authorId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
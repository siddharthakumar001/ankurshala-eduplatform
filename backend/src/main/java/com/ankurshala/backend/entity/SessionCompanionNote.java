package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Individual notes captured during live sessions.
 */
@Entity
@Table(name = "session_companion_notes")
public class SessionCompanionNote {

    public enum NoteType {
        NOTE,           // General note
        QUESTION,       // Question asked during session
        HIGHLIGHT,      // Key point highlighted
        ACTION_ITEM     // Follow-up action item
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "companion_id", nullable = false)
    private Long companionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companion_id", insertable = false, updatable = false)
    private SessionCompanion companion;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 50)
    private NoteType noteType = NoteType.NOTE;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp_in_session")
    private Integer timestampInSession;

    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SessionCompanionNote() {}

    public SessionCompanionNote(Long companionId, Long studentId, NoteType noteType, String content) {
        this.companionId = companionId;
        this.studentId = studentId;
        this.noteType = noteType;
        this.content = content;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanionId() { return companionId; }
    public void setCompanionId(Long companionId) { this.companionId = companionId; }

    public SessionCompanion getCompanion() { return companion; }
    public void setCompanion(SessionCompanion companion) { this.companion = companion; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public NoteType getNoteType() { return noteType; }
    public void setNoteType(NoteType noteType) { this.noteType = noteType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getTimestampInSession() { return timestampInSession; }
    public void setTimestampInSession(Integer timestampInSession) { this.timestampInSession = timestampInSession; }

    public String getAiResponse() { return aiResponse; }
    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public void resolve() {
        this.isResolved = true;
    }

    public void addAiResponse(String response) {
        this.aiResponse = response;
    }
}


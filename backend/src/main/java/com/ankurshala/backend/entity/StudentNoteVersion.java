package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Version history for student notes.
 * Each regeneration creates a new version for tracking changes.
 */
@Entity
@Table(name = "student_notes_versions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"note_id", "version"}))
public class StudentNoteVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private StudentNote note;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "content_md", nullable = false, columnDefinition = "TEXT")
    private String contentMd;

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    @Column(name = "chunks_used")
    private Integer chunksUsed = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public StudentNoteVersion() {}

    public StudentNoteVersion(StudentNote note, Integer version, String contentMd) {
        this.note = note;
        this.version = version;
        this.contentMd = contentMd;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentNote getNote() { return note; }
    public void setNote(StudentNote note) { this.note = note; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getContentMd() { return contentMd; }
    public void setContentMd(String contentMd) { this.contentMd = contentMd; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public Integer getChunksUsed() { return chunksUsed; }
    public void setChunksUsed(Integer chunksUsed) { this.chunksUsed = chunksUsed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}


package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI-generated notes artifacts for students.
 * Stores personalized study notes organized by topic and format.
 */
@Entity
@Table(name = "student_notes")
public class StudentNote {

    public enum NoteFormat {
        SHORT,           // Quick summary with key points
        LONG,            // Detailed notes with examples
        REVISION_SHEET   // Exam prep with bullets and formulas
    }

    public enum NoteStatus {
        ACTIVE,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "topic_id")
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    @Column(name = "subject_id")
    private Long subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;

    @Column(name = "board", nullable = false, length = 50)
    private String board = "CBSE";

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 50)
    private NoteFormat format = NoteFormat.SHORT;

    @Column(name = "language", nullable = false, length = 20)
    private String language = "en";

    @Column(name = "content_md", nullable = false, columnDefinition = "TEXT")
    private String contentMd;

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    @Column(name = "chunks_used")
    private Integer chunksUsed = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private NoteStatus status = NoteStatus.ACTIVE;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentNoteVersion> versions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentNote() {}

    public StudentNote(Long studentId, Long topicId, Long subjectId, String title, NoteFormat format) {
        this.studentId = studentId;
        this.topicId = topicId;
        this.subjectId = subjectId;
        this.title = title;
        this.format = format;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public String getBoard() { return board; }
    public void setBoard(String board) { this.board = board; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public NoteFormat getFormat() { return format; }
    public void setFormat(NoteFormat format) { this.format = format; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getContentMd() { return contentMd; }
    public void setContentMd(String contentMd) { this.contentMd = contentMd; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public Integer getChunksUsed() { return chunksUsed; }
    public void setChunksUsed(Integer chunksUsed) { this.chunksUsed = chunksUsed; }

    public NoteStatus getStatus() { return status; }
    public void setStatus(NoteStatus status) { this.status = status; }

    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }

    public List<StudentNoteVersion> getVersions() { return versions; }
    public void setVersions(List<StudentNoteVersion> versions) { this.versions = versions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Add a new version snapshot of the current content
     */
    public void addVersionSnapshot() {
        int nextVersion = versions.size() + 1;
        StudentNoteVersion version = new StudentNoteVersion(this, nextVersion, this.contentMd);
        version.setGeneratedBy(this.generatedBy);
        version.setChunksUsed(this.chunksUsed);
        versions.add(version);
    }

    /**
     * Get the latest version number
     */
    public int getLatestVersion() {
        return versions.isEmpty() ? 1 : versions.size() + 1;
    }
}


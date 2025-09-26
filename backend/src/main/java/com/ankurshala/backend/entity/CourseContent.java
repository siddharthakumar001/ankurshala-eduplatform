package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_content")
public class CourseContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_level", nullable = false)
    private ClassLevel classLevel;

    @NotBlank
    @Size(max = 100)
    @Column(name = "subject", nullable = false)
    private String subject;

    @Size(max = 200)
    @Column(name = "chapter")
    private String chapter;

    @Size(max = 300)
    @Column(name = "topic")
    private String topic;

    @Column(name = "brief_description", columnDefinition = "TEXT")
    private String briefDescription;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "suggested_topics", columnDefinition = "TEXT")
    private String suggestedTopics;

    @Size(max = 500)
    @Column(name = "resource_url")
    private String resourceUrl;

    @Column(name = "expected_time_minutes")
    private Integer expectedTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "educational_board")
    private EducationalBoard educationalBoard;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_job_id")
    private ImportJob importJob;

    public CourseContent() {}

    public CourseContent(ClassLevel classLevel, String subject, String chapter, String topic) {
        this.classLevel = classLevel;
        this.subject = subject;
        this.chapter = chapter;
        this.topic = topic;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ClassLevel getClassLevel() { return classLevel; }
    public void setClassLevel(ClassLevel classLevel) { this.classLevel = classLevel; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getBriefDescription() { return briefDescription; }
    public void setBriefDescription(String briefDescription) { this.briefDescription = briefDescription; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getSuggestedTopics() { return suggestedTopics; }
    public void setSuggestedTopics(String suggestedTopics) { this.suggestedTopics = suggestedTopics; }

    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }

    public Integer getExpectedTimeMinutes() { return expectedTimeMinutes; }
    public void setExpectedTimeMinutes(Integer expectedTimeMinutes) { this.expectedTimeMinutes = expectedTimeMinutes; }

    public EducationalBoard getEducationalBoard() { return educationalBoard; }
    public void setEducationalBoard(EducationalBoard educationalBoard) { this.educationalBoard = educationalBoard; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public ImportJob getImportJob() { return importJob; }
    public void setImportJob(ImportJob importJob) { this.importJob = importJob; }
}

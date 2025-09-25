package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_jobs")
public class ImportJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "file_type", nullable = false)
    private String fileType; // CSV, XLSX

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImportJobStatus status = ImportJobStatus.PENDING;

    @Column(name = "type")
    private String type = "CONTENT_CSV";

    @Column(name = "total_rows")
    private Integer totalRows = 0;

    @Column(name = "success_rows")
    private Integer successRows = 0;

    @Column(name = "error_rows")
    private Integer errorRows = 0;

    @Column(name = "stats", columnDefinition = "TEXT")
    private String stats;

    @Column(name = "errors", columnDefinition = "TEXT")
    private String errors;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "importJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CourseContent> courseContents = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    public ImportJob() {}

    public ImportJob(String fileName, String fileType, Long fileSize) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public ImportJobStatus getStatus() { return status; }
    public void setStatus(ImportJobStatus status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getSuccessRows() { return successRows; }
    public void setSuccessRows(Integer successRows) { this.successRows = successRows; }

    public Integer getErrorRows() { return errorRows; }
    public void setErrorRows(Integer errorRows) { this.errorRows = errorRows; }

    public String getStats() { return stats; }
    public void setStats(String stats) { this.stats = stats; }

    public String getErrors() { return errors; }
    public void setErrors(String errors) { this.errors = errors; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<CourseContent> getCourseContents() { return courseContents; }
    public void setCourseContents(List<CourseContent> courseContents) { this.courseContents = courseContents; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    // Helper methods
    public double getProgressPercentage() {
        if (totalRows == null || totalRows == 0) return 0.0;
        return (double) successRows / totalRows * 100.0;
    }

    public boolean isCompleted() {
        return status == ImportJobStatus.SUCCEEDED || status == ImportJobStatus.FAILED;
    }
}

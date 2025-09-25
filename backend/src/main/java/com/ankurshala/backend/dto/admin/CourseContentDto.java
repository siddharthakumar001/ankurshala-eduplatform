package com.ankurshala.backend.dto.admin;

import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CourseContentDto implements Serializable {
    private Long id;
    private ClassLevel classLevel;
    private String subject;
    private String chapter;
    private String topic;
    private String briefDescription;
    private String summary;
    private String suggestedTopics;
    private String resourceUrl;
    private Integer expectedTimeMinutes;
    private EducationalBoard educationalBoard;
    private Boolean isActive;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Constructors
    public CourseContentDto() {}

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
}

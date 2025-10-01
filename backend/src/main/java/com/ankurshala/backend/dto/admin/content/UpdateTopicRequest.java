package com.ankurshala.backend.dto.admin.content;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating Topic
 * All fields are optional since it's an update operation
 */
public class UpdateTopicRequest {
    
    @Size(max = 300, message = "Topic title cannot exceed 300 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @Size(max = 1000, message = "Summary cannot exceed 1000 characters")
    private String summary;
    
    private Integer expectedTimeMins;
    
    private Long boardId;
    
    private Long gradeId;
    
    private Long subjectId;
    
    private Long chapterId;
    
    private Boolean active;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Integer getExpectedTimeMins() { return expectedTimeMins; }
    public void setExpectedTimeMins(Integer expectedTimeMins) { this.expectedTimeMins = expectedTimeMins; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

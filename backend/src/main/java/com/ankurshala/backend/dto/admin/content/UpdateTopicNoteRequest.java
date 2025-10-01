package com.ankurshala.backend.dto.admin.content;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating TopicNote
 * All fields are optional since it's an update operation
 */
public class UpdateTopicNoteRequest {
    
    @Size(max = 300, message = "Note title cannot exceed 300 characters")
    private String title;
    
    @Size(max = 10000, message = "Note content cannot exceed 10000 characters")
    private String content;
    
    @Size(max = 2000, message = "Attachments cannot exceed 2000 characters")
    private String attachments;
    
    // Optional hierarchy fields for easier filtering/querying
    private Long topicId;
    private Long boardId;
    private Long gradeId;
    private Long subjectId;
    private Long chapterId;
    
    private Boolean active;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    // Hierarchy field getters and setters
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }
}

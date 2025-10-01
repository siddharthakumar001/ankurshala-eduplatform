package com.ankurshala.backend.dto.admin.content;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating Chapter
 * All fields are optional since it's an update operation
 */
public class UpdateChapterRequest {
    
    @Size(max = 200, message = "Chapter name cannot exceed 200 characters")
    private String name;
    
    private Long boardId;
    
    private Long gradeId;
    
    private Long subjectId;
    
    private Boolean active;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

package com.ankurshala.backend.dto.admin.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new Subject
 */
public class CreateSubjectRequest {
    
    @NotBlank(message = "Subject name is required")
    @Size(max = 100, message = "Subject name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Board ID is required")
    private Long boardId;
    
    @NotNull(message = "Grade ID is required")
    private Long gradeId;
    
    private Boolean active = true;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

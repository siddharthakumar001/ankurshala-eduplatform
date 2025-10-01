package com.ankurshala.backend.dto.admin.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new Grade
 */
public class CreateGradeRequest {
    
    @NotBlank(message = "Grade name is required")
    @Size(max = 50, message = "Grade name cannot exceed 50 characters")
    private String name;
    
    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;
    
    @NotNull(message = "Board ID is required")
    private Long boardId;
    
    private Boolean active = true;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

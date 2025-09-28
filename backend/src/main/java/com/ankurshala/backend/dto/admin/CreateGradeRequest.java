package com.ankurshala.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateGradeRequest {
    @NotBlank(message = "Grade name is required")
    private String name;
    
    @NotBlank(message = "Grade display name is required")
    private String displayName;
    
    @NotNull(message = "Board ID is required")
    private Long boardId;
    
    private Boolean active = true;

    public CreateGradeRequest() {}

    public CreateGradeRequest(String name, String displayName, Long boardId, Boolean active) {
        this.name = name;
        this.displayName = displayName;
        this.boardId = boardId;
        this.active = active;
    }

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

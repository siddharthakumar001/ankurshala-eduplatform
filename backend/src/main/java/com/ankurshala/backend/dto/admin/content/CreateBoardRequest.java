package com.ankurshala.backend.dto.admin.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new Board
 */
public class CreateBoardRequest {
    
    @NotBlank(message = "Board name is required")
    @Size(max = 100, message = "Board name cannot exceed 100 characters")
    private String name;
    
    private Boolean active = true;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

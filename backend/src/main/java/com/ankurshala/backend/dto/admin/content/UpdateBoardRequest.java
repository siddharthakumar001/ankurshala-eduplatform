package com.ankurshala.backend.dto.admin.content;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating Board
 * All fields are optional since it's an update operation
 */
public class UpdateBoardRequest {
    
    @Size(max = 100, message = "Board name cannot exceed 100 characters")
    private String name;
    
    private Boolean active;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

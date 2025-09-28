package com.ankurshala.backend.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class UpdateGradeRequest {
    @NotBlank(message = "Grade name is required")
    private String name;
    
    @NotBlank(message = "Grade display name is required")
    private String displayName;
    
    private Boolean active;

    public UpdateGradeRequest() {}

    public UpdateGradeRequest(String name, String displayName, Boolean active) {
        this.name = name;
        this.displayName = displayName;
        this.active = active;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateStudyListItemRequest {
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "ADDED|IN_PROGRESS|DONE", message = "Status must be ADDED, IN_PROGRESS, or DONE")
    private String status;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

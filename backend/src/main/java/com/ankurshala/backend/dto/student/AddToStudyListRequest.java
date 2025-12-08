package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AddToStudyListRequest {
    
    @NotNull(message = "Topic ID is required")
    private Long topicId;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Getters and Setters
    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CompleteOnboardingRequest {
    
    @NotNull(message = "Board ID is required")
    private Long boardId;
    
    @NotNull(message = "Grade ID is required")
    private Long gradeId;
    
    @NotNull(message = "Language is required")
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;
    
    @Size(max = 1000, message = "Goals must not exceed 1000 characters")
    private String goals;
    
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    // Getters and Setters
    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

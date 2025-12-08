package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public class TeacherSessionFeedbackDto {
    private Long id;
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @Min(value = 1, message = "Session rating must be between 1 and 5")
    @Max(value = 5, message = "Session rating must be between 1 and 5")
    private Integer sessionRating;
    
    @Size(max = 1000, message = "Student engagement notes cannot exceed 1000 characters")
    private String studentEngagement;
    
    @Size(max = 2000, message = "Session notes cannot exceed 2000 characters")
    private String sessionNotes;
    
    @Size(max = 1000, message = "Improvement suggestions cannot exceed 1000 characters")
    private String improvementSuggestions;
    
    private Boolean wouldRecommend;
    
    public TeacherSessionFeedbackDto() {}
    
    public TeacherSessionFeedbackDto(Long bookingId, Integer sessionRating, String studentEngagement, 
                                   String sessionNotes, String improvementSuggestions, Boolean wouldRecommend) {
        this.bookingId = bookingId;
        this.sessionRating = sessionRating;
        this.studentEngagement = studentEngagement;
        this.sessionNotes = sessionNotes;
        this.improvementSuggestions = improvementSuggestions;
        this.wouldRecommend = wouldRecommend;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Integer getSessionRating() { return sessionRating; }
    public void setSessionRating(Integer sessionRating) { this.sessionRating = sessionRating; }
    
    public String getStudentEngagement() { return studentEngagement; }
    public void setStudentEngagement(String studentEngagement) { this.studentEngagement = studentEngagement; }
    
    public String getSessionNotes() { return sessionNotes; }
    public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }
    
    public String getImprovementSuggestions() { return improvementSuggestions; }
    public void setImprovementSuggestions(String improvementSuggestions) { this.improvementSuggestions = improvementSuggestions; }
    
    public Boolean getWouldRecommend() { return wouldRecommend; }
    public void setWouldRecommend(Boolean wouldRecommend) { this.wouldRecommend = wouldRecommend; }
}

package com.ankurshala.backend.dto.teacher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TeacherUpcomingBookingDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long topicId;
    private String topicTitle;
    private String subjectName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String status;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String studentNotes;
    private LocalDateTime createdAt;
    
    public TeacherUpcomingBookingDto() {}
    
    public TeacherUpcomingBookingDto(Long id, Long studentId, String studentName, String studentEmail, 
                                   Long topicId, String topicTitle, String subjectName, LocalDateTime startTime, 
                                   LocalDateTime endTime, Integer durationMinutes, String status, 
                                   BigDecimal priceMin, BigDecimal priceMax, String studentNotes, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.topicId = topicId;
        this.topicTitle = topicTitle;
        this.subjectName = subjectName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
        this.studentNotes = studentNotes;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    
    public Long getTopicId() { return topicId; }
    public void setTopicId(Long topicId) { this.topicId = topicId; }
    
    public String getTopicTitle() { return topicTitle; }
    public void setTopicTitle(String topicTitle) { this.topicTitle = topicTitle; }
    
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public BigDecimal getPriceMin() { return priceMin; }
    public void setPriceMin(BigDecimal priceMin) { this.priceMin = priceMin; }
    
    public BigDecimal getPriceMax() { return priceMax; }
    public void setPriceMax(BigDecimal priceMax) { this.priceMax = priceMax; }
    
    public String getStudentNotes() { return studentNotes; }
    public void setStudentNotes(String studentNotes) { this.studentNotes = studentNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

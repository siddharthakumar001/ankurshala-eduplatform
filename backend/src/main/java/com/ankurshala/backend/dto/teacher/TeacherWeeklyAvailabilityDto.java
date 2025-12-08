package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalTime;

public class TeacherWeeklyAvailabilityDto {
    private Long id;
    
    @NotNull(message = "Day of week is required")
    @Min(value = 0, message = "Day of week must be between 0 and 6")
    @Max(value = 6, message = "Day of week must be between 0 and 6")
    private Integer dayOfWeek;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;
    
    public TeacherWeeklyAvailabilityDto() {}
    
    public TeacherWeeklyAvailabilityDto(Integer dayOfWeek, LocalTime startTime, LocalTime endTime, Boolean isAvailable) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}

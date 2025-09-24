package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public class TeacherAvailabilityDto {
    private Long id;
    
    private LocalTime availableFrom;
    
    private LocalTime availableTo;
    
    @Size(max = 1000)
    private String preferredStudentLevels;
    
    @Size(max = 1000)
    private String languagesSpoken;

    // Constructors
    public TeacherAvailabilityDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(LocalTime availableFrom) {
        this.availableFrom = availableFrom;
    }

    public LocalTime getAvailableTo() {
        return availableTo;
    }

    public void setAvailableTo(LocalTime availableTo) {
        this.availableTo = availableTo;
    }

    public String getPreferredStudentLevels() {
        return preferredStudentLevels;
    }

    public void setPreferredStudentLevels(String preferredStudentLevels) {
        this.preferredStudentLevels = preferredStudentLevels;
    }

    public String getLanguagesSpoken() {
        return languagesSpoken;
    }

    public void setLanguagesSpoken(String languagesSpoken) {
        this.languagesSpoken = languagesSpoken;
    }
}

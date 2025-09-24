package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TeacherExperienceDto {
    private Long id;
    
    @Size(max = 255)
    private String institution;
    
    @Size(max = 255)
    private String role;
    
    @Size(max = 1000)
    private String subjectsTaught;
    
    private LocalDate fromDate;
    
    private LocalDate toDate;
    
    private Boolean currentlyWorking = false;

    // Constructors
    public TeacherExperienceDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSubjectsTaught() {
        return subjectsTaught;
    }

    public void setSubjectsTaught(String subjectsTaught) {
        this.subjectsTaught = subjectsTaught;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public Boolean getCurrentlyWorking() {
        return currentlyWorking;
    }

    public void setCurrentlyWorking(Boolean currentlyWorking) {
        this.currentlyWorking = currentlyWorking;
    }
}

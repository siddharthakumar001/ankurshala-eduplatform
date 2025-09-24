package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TeacherQualificationDto {
    private Long id;
    
    @NotBlank
    @Size(max = 255)
    private String degree;
    
    @Size(max = 255)
    private String specialization;
    
    @Size(max = 255)
    private String university;
    
    @Min(value = 1900)
    private Integer year;

    // Constructors
    public TeacherQualificationDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}

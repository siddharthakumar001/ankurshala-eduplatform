package com.ankurshala.backend.dto.admin;

import com.ankurshala.backend.entity.TeacherStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TeacherListDto implements Serializable {
    private Long id;
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String specialization;
    private Integer yearsOfExperience;
    private BigDecimal hourlyRate;
    private BigDecimal rating;
    private Integer totalReviews;
    private TeacherStatus status;
    private Boolean verified;
    private Boolean enabled;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // Constructors
    public TeacherListDto() {}

    // Explicit getters for Jackson serialization
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getSpecialization() { return specialization; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public BigDecimal getRating() { return rating; }
    public Integer getTotalReviews() { return totalReviews; }
    public TeacherStatus getStatus() { return status; }
    public Boolean getVerified() { return verified; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
    public void setStatus(TeacherStatus status) { this.status = status; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}

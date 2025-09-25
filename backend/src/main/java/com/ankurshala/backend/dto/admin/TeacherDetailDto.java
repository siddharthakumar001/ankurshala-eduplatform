package com.ankurshala.backend.dto.admin;

import com.ankurshala.backend.entity.TeacherStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TeacherDetailDto implements Serializable {
    private Long id;
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String alternateMobileNumber;
    private String contactEmail;
    private String highestEducation;
    private String postalAddress;
    private String city;
    private String state;
    private String country;
    private String secondaryAddress;
    private String profilePhotoUrl;
    private String govtIdProofUrl;
    private String bio;
    private String qualifications;
    private BigDecimal hourlyRate;
    private Integer yearsOfExperience;
    private String specialization;
    private TeacherStatus status;
    private Boolean verified;
    private BigDecimal rating;
    private Integer totalReviews;
    private Boolean enabled;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLoginAt;

    // Constructors
    public TeacherDetailDto() {}

    // Explicit getters for Jackson serialization
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getAlternateMobileNumber() { return alternateMobileNumber; }
    public String getContactEmail() { return contactEmail; }
    public String getHighestEducation() { return highestEducation; }
    public String getPostalAddress() { return postalAddress; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getSecondaryAddress() { return secondaryAddress; }
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public String getGovtIdProofUrl() { return govtIdProofUrl; }
    public String getBio() { return bio; }
    public String getQualifications() { return qualifications; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public String getSpecialization() { return specialization; }
    public TeacherStatus getStatus() { return status; }
    public Boolean getVerified() { return verified; }
    public BigDecimal getRating() { return rating; }
    public Integer getTotalReviews() { return totalReviews; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public void setAlternateMobileNumber(String alternateMobileNumber) { this.alternateMobileNumber = alternateMobileNumber; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setHighestEducation(String highestEducation) { this.highestEducation = highestEducation; }
    public void setPostalAddress(String postalAddress) { this.postalAddress = postalAddress; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setCountry(String country) { this.country = country; }
    public void setSecondaryAddress(String secondaryAddress) { this.secondaryAddress = secondaryAddress; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    public void setGovtIdProofUrl(String govtIdProofUrl) { this.govtIdProofUrl = govtIdProofUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setStatus(TeacherStatus status) { this.status = status; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}

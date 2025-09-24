package com.ankurshala.backend.dto.teacher;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TeacherProfileDto {
    private Long id;
    
    @Size(max = 100)
    private String firstName;
    
    @Size(max = 100)
    private String middleName;
    
    @Size(max = 100)
    private String lastName;
    
    @Size(max = 20)
    private String mobileNumber;
    
    @Size(max = 20)
    private String alternateMobileNumber;
    
    @Email
    @Size(max = 150)
    private String contactEmail;
    
    @Size(max = 200)
    private String highestEducation;
    
    @Size(max = 500)
    private String postalAddress;
    
    @Size(max = 100)
    private String city;
    
    @Size(max = 100)
    private String state;
    
    @Size(max = 100)
    private String country = "India";
    
    @Size(max = 500)
    private String secondaryAddress;
    
    @Size(max = 500)
    private String profilePhotoUrl;
    
    @Size(max = 500)
    private String govtIdProofUrl;
    
    @Size(max = 1000)
    private String bio;
    
    @Size(max = 1000)
    private String qualifications;
    
    @DecimalMin(value = "0.0", message = "Hourly rate must be positive")
    private BigDecimal hourlyRate;
    
    @Min(value = 0, message = "Years of experience must be non-negative")
    private Integer yearsOfExperience;
    
    @Size(max = 255)
    private String specialization;
    
    private Boolean verified = false;
    
    @DecimalMin(value = "0.0", message = "Rating must be non-negative")
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Min(value = 0, message = "Total reviews must be non-negative")
    private Integer totalReviews = 0;

    // Constructors
    public TeacherProfileDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAlternateMobileNumber() {
        return alternateMobileNumber;
    }

    public void setAlternateMobileNumber(String alternateMobileNumber) {
        this.alternateMobileNumber = alternateMobileNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public void setHighestEducation(String highestEducation) {
        this.highestEducation = highestEducation;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSecondaryAddress() {
        return secondaryAddress;
    }

    public void setSecondaryAddress(String secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getGovtIdProofUrl() {
        return govtIdProofUrl;
    }

    public void setGovtIdProofUrl(String govtIdProofUrl) {
        this.govtIdProofUrl = govtIdProofUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }
}

package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 20)
    @Column(name = "phone_number")
    private String phoneNumber;

    @Size(max = 20)
    @Column(name = "alternate_phone_number")
    private String alternatePhoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Size(max = 500)
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Size(max = 255)
    @Column(name = "linkedin_profile")
    private String linkedinProfile;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TeacherStatus status = TeacherStatus.PENDING;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "languages", columnDefinition = "TEXT")
    private String languages; // JSON array of languages

    @Column(name = "categories", columnDefinition = "TEXT")
    private String categories; // JSON array of categories

    @Column(name = "hourly_rate", precision = 8, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeacherProfile teacherProfile;

    @OneToOne(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeacherProfessionalInfo professionalInfo;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherQualification> qualifications = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherCertification> certifications = new ArrayList<>();

    @OneToOne(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeacherAvailability availability;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherDocument> documents = new ArrayList<>();

    @OneToOne(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeacherBankDetails bankDetails;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeacherAddress> addresses = new ArrayList<>();

    public Teacher() {}

    public Teacher(User user, String name, String email) {
        this.user = user;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAlternatePhoneNumber() {
        return alternatePhoneNumber;
    }

    public void setAlternatePhoneNumber(String alternatePhoneNumber) {
        this.alternatePhoneNumber = alternatePhoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getLinkedinProfile() {
        return linkedinProfile;
    }

    public void setLinkedinProfile(String linkedinProfile) {
        this.linkedinProfile = linkedinProfile;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public TeacherStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TeacherProfile getTeacherProfile() {
        return teacherProfile;
    }

    public void setTeacherProfile(TeacherProfile teacherProfile) {
        this.teacherProfile = teacherProfile;
    }

    public TeacherProfessionalInfo getProfessionalInfo() {
        return professionalInfo;
    }

    public void setProfessionalInfo(TeacherProfessionalInfo professionalInfo) {
        this.professionalInfo = professionalInfo;
    }

    public List<TeacherQualification> getQualifications() {
        return qualifications;
    }

    public void setQualifications(List<TeacherQualification> qualifications) {
        this.qualifications = qualifications;
    }

    public List<TeacherExperience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<TeacherExperience> experiences) {
        this.experiences = experiences;
    }

    public List<TeacherCertification> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<TeacherCertification> certifications) {
        this.certifications = certifications;
    }

    public TeacherAvailability getAvailability() {
        return availability;
    }

    public void setAvailability(TeacherAvailability availability) {
        this.availability = availability;
    }

    public List<TeacherDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<TeacherDocument> documents) {
        this.documents = documents;
    }

    public TeacherBankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(TeacherBankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    public List<TeacherAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<TeacherAddress> addresses) {
        this.addresses = addresses;
    }

    // Additional getters and setters for new fields
    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(BigDecimal ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    // Helper methods for array fields
    public String[] getLanguagesArray() {
        if (languages == null || languages.isEmpty()) {
            return new String[0];
        }
        return languages.split(",");
    }

    public void setLanguagesArray(String[] languagesArray) {
        if (languagesArray == null || languagesArray.length == 0) {
            this.languages = null;
        } else {
            this.languages = String.join(",", languagesArray);
        }
    }

    public String[] getCategoriesArray() {
        if (categories == null || categories.isEmpty()) {
            return new String[0];
        }
        return categories.split(",");
    }

    public void setCategoriesArray(String[] categoriesArray) {
        if (categoriesArray == null || categoriesArray.length == 0) {
            this.categories = null;
        } else {
            this.categories = String.join(",", categoriesArray);
        }
    }
}

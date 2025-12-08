package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateStudentProfileRequest {
    
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    private String middleName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Size(max = 100, message = "Mother name must not exceed 100 characters")
    private String motherName;
    
    @Size(max = 100, message = "Father name must not exceed 100 characters")
    private String fatherName;
    
    @Size(max = 100, message = "Guardian name must not exceed 100 characters")
    private String guardianName;
    
    @Size(max = 100, message = "Parent name must not exceed 100 characters")
    private String parentName;
    
    @Size(max = 20, message = "Mobile number must not exceed 20 characters")
    private String mobileNumber;
    
    @Size(max = 20, message = "Alternate mobile number must not exceed 20 characters")
    private String alternateMobileNumber;
    
    private LocalDate dateOfBirth;
    
    @Size(max = 200, message = "School name must not exceed 200 characters")
    private String schoolName;
    
    @Size(max = 100, message = "Emergency contact must not exceed 100 characters")
    private String emergencyContact;
    
    @Size(max = 500, message = "Student photo URL must not exceed 500 characters")
    private String studentPhotoUrl;
    
    @Size(max = 500, message = "School ID card URL must not exceed 500 characters")
    private String schoolIdCardUrl;
    
    // Onboarding fields
    private Long boardId;
    
    private Long gradeId;
    
    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;
    
    @Size(max = 1000, message = "Goals must not exceed 1000 characters")
    private String goals;
    
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    // Getters and Setters
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

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getStudentPhotoUrl() {
        return studentPhotoUrl;
    }

    public void setStudentPhotoUrl(String studentPhotoUrl) {
        this.studentPhotoUrl = studentPhotoUrl;
    }

    public String getSchoolIdCardUrl() {
        return schoolIdCardUrl;
    }

    public void setSchoolIdCardUrl(String schoolIdCardUrl) {
        this.schoolIdCardUrl = schoolIdCardUrl;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

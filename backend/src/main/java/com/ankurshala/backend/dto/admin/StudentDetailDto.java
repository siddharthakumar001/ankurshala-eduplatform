package com.ankurshala.backend.dto.admin;

import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class StudentDetailDto implements Serializable {
    private Long id;
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String alternateMobileNumber;
    private LocalDate dateOfBirth;
    private String motherName;
    private String fatherName;
    private String guardianName;
    private String parentName;
    private EducationalBoard educationalBoard;
    private ClassLevel classLevel;
    private String gradeLevel;
    private String schoolName;
    private String emergencyContact;
    private String studentPhotoUrl;
    private String schoolIdCardUrl;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    public StudentDetailDto(Long id, Long userId, String firstName, String middleName, String lastName, 
                           String email, String mobileNumber, String alternateMobileNumber, 
                           LocalDate dateOfBirth, String motherName, String fatherName, 
                           String guardianName, String parentName, EducationalBoard educationalBoard, 
                           ClassLevel classLevel, String gradeLevel, String schoolName, 
                           String emergencyContact, String studentPhotoUrl, String schoolIdCardUrl, 
                           Boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt, 
                           LocalDateTime lastLoginAt) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.alternateMobileNumber = alternateMobileNumber;
        this.dateOfBirth = dateOfBirth;
        this.motherName = motherName;
        this.fatherName = fatherName;
        this.guardianName = guardianName;
        this.parentName = parentName;
        this.educationalBoard = educationalBoard;
        this.classLevel = classLevel;
        this.gradeLevel = gradeLevel;
        this.schoolName = schoolName;
        this.emergencyContact = emergencyContact;
        this.studentPhotoUrl = studentPhotoUrl;
        this.schoolIdCardUrl = schoolIdCardUrl;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Explicit getters for Jackson serialization
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getAlternateMobileNumber() { return alternateMobileNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getMotherName() { return motherName; }
    public String getFatherName() { return fatherName; }
    public String getGuardianName() { return guardianName; }
    public String getParentName() { return parentName; }
    public EducationalBoard getEducationalBoard() { return educationalBoard; }
    public ClassLevel getClassLevel() { return classLevel; }
    public String getGradeLevel() { return gradeLevel; }
    public String getSchoolName() { return schoolName; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getStudentPhotoUrl() { return studentPhotoUrl; }
    public String getSchoolIdCardUrl() { return schoolIdCardUrl; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
}

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
public class StudentListDto implements Serializable {
    private Long id;
    private Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private EducationalBoard educationalBoard;
    private ClassLevel classLevel;
    private String gradeLevel;
    private String schoolName;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public StudentListDto(Long id, Long userId, String firstName, String middleName, String lastName, 
                         String email, String mobileNumber, LocalDate dateOfBirth, 
                         EducationalBoard educationalBoard, ClassLevel classLevel, String gradeLevel, 
                         String schoolName, Boolean enabled, LocalDateTime createdAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
        this.educationalBoard = educationalBoard;
        this.classLevel = classLevel;
        this.gradeLevel = gradeLevel;
        this.schoolName = schoolName;
        this.enabled = enabled;
        this.createdAt = createdAt;
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
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public EducationalBoard getEducationalBoard() { return educationalBoard; }
    public ClassLevel getClassLevel() { return classLevel; }
    public String getGradeLevel() { return gradeLevel; }
    public String getSchoolName() { return schoolName; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
}

package com.ankurshala.backend.dto.admin;

import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class StudentUpdateDto implements Serializable {
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String middleName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @Size(max = 150)
    @Email
    private String email; // Allow updating email

    @Size(max = 20)
    private String mobileNumber;

    @Size(max = 20)
    private String alternateMobileNumber;

    private LocalDate dateOfBirth;
    private EducationalBoard educationalBoard;
    private ClassLevel classLevel;

    @Size(max = 20)
    private String gradeLevel;

    @Size(max = 200)
    private String schoolName;

    @Size(max = 100)
    private String emergencyContact;

    @Size(max = 500)
    private String studentPhotoUrl;

    @Size(max = 500)
    private String schoolIdCardUrl;

    private Boolean enabled; // Allow toggling status

    // Manual getters since Lombok is not working properly
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getAlternateMobileNumber() { return alternateMobileNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public EducationalBoard getEducationalBoard() { return educationalBoard; }
    public ClassLevel getClassLevel() { return classLevel; }
    public String getGradeLevel() { return gradeLevel; }
    public String getSchoolName() { return schoolName; }
    public String getEmergencyContact() { return emergencyContact; }
    public String getStudentPhotoUrl() { return studentPhotoUrl; }
    public String getSchoolIdCardUrl() { return schoolIdCardUrl; }
    public Boolean getEnabled() { return enabled; }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.StudentDetailDto;
import com.ankurshala.backend.dto.admin.StudentListDto;
import com.ankurshala.backend.dto.admin.StudentUpdateDto;
import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AdminStudentService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<StudentListDto> getStudentsWithFilters(String search, Boolean enabled, 
                                                      EducationalBoard educationalBoard, 
                                                      ClassLevel classLevel, Pageable pageable) {
        Page<StudentProfile> students = studentProfileRepository.findStudentsWithFilters(
                search, enabled, educationalBoard, classLevel, pageable);
        
        return students.map(this::convertToStudentListDto);
    }

    public Optional<StudentDetailDto> getStudentById(Long id) {
        return studentProfileRepository.findById(id)
                .map(this::convertToStudentDetailDto);
    }

    public StudentDetailDto updateStudent(Long id, StudentUpdateDto updateDto) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        User user = student.getUser();

        // Update User fields
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateDto.getEmail())) {
                throw new RuntimeException("Email already in use: " + updateDto.getEmail());
            }
            user.setEmail(updateDto.getEmail());
        }
        if (updateDto.getEnabled() != null) {
            user.setEnabled(updateDto.getEnabled());
        }
        userRepository.save(user); // Save user changes

        // Update StudentProfile fields
        student.setFirstName(updateDto.getFirstName());
        student.setMiddleName(updateDto.getMiddleName());
        student.setLastName(updateDto.getLastName());
        student.setMobileNumber(updateDto.getMobileNumber());
        student.setAlternateMobileNumber(updateDto.getAlternateMobileNumber());
        student.setDateOfBirth(updateDto.getDateOfBirth());
        student.setEducationalBoard(updateDto.getEducationalBoard());
        student.setClassLevel(updateDto.getClassLevel());
        student.setGradeLevel(updateDto.getGradeLevel());
        student.setSchoolName(updateDto.getSchoolName());
        student.setEmergencyContact(updateDto.getEmergencyContact());
        student.setStudentPhotoUrl(updateDto.getStudentPhotoUrl());
        student.setSchoolIdCardUrl(updateDto.getSchoolIdCardUrl());

        StudentProfile updatedStudentProfile = studentProfileRepository.save(student);
        return convertToStudentDetailDto(updatedStudentProfile);
    }

    public boolean toggleStudentStatus(Long id) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        
        User user = student.getUser();
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        
        return user.getEnabled();
    }

    public void deleteStudent(Long id) {
        StudentProfile student = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
        
        // Delete the user (which will cascade to student profile due to relationship)
        userRepository.delete(student.getUser());
    }

    private StudentListDto convertToStudentListDto(StudentProfile student) {
        User user = student.getUser();
        return new StudentListDto(
                student.getId(),
                user.getId(),
                student.getFirstName(),
                student.getMiddleName(),
                student.getLastName(),
                user.getEmail(),
                student.getMobileNumber(),
                student.getDateOfBirth(),
                student.getEducationalBoard(),
                student.getClassLevel(),
                student.getGradeLevel(),
                student.getSchoolName(),
                user.getEnabled(),
                student.getCreatedAt(),
                null // lastLoginAt - would need to be tracked separately
        );
    }

    private StudentDetailDto convertToStudentDetailDto(StudentProfile student) {
        User user = student.getUser();
        return new StudentDetailDto(
                student.getId(),
                user.getId(),
                student.getFirstName(),
                student.getMiddleName(),
                student.getLastName(),
                user.getEmail(),
                student.getMobileNumber(),
                student.getAlternateMobileNumber(),
                student.getDateOfBirth(),
                student.getMotherName(),
                student.getFatherName(),
                student.getGuardianName(),
                student.getParentName(),
                student.getEducationalBoard(),
                student.getClassLevel(),
                student.getGradeLevel(),
                student.getSchoolName(),
                student.getEmergencyContact(),
                student.getStudentPhotoUrl(),
                student.getSchoolIdCardUrl(),
                user.getEnabled(),
                student.getCreatedAt(),
                student.getUpdatedAt(),
                null // lastLoginAt - would need to be tracked separately
        );
    }


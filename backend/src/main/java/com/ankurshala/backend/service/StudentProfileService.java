package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.StudentDocumentDto;
import com.ankurshala.backend.dto.student.StudentProfileDto;
import com.ankurshala.backend.entity.StudentDocument;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentProfileService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    public StudentProfile createStudentProfile(User user, String name) {
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        
        // Split name into first and last name
        String[] nameParts = name.split(" ", 2);
        profile.setFirstName(nameParts[0]);
        profile.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        
        return studentProfileRepository.save(profile);
    }

    public StudentProfileDto getStudentProfile(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
        
        return convertToDto(profile);
    }

    public StudentProfileDto updateStudentProfile(Long userId, StudentProfileDto profileDto) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        // Update profile fields
        profile.setFirstName(profileDto.getFirstName());
        profile.setMiddleName(profileDto.getMiddleName());
        profile.setLastName(profileDto.getLastName());
        profile.setMotherName(profileDto.getMotherName());
        profile.setFatherName(profileDto.getFatherName());
        profile.setGuardianName(profileDto.getGuardianName());
        profile.setParentName(profileDto.getParentName());
        profile.setMobileNumber(profileDto.getMobileNumber());
        profile.setAlternateMobileNumber(profileDto.getAlternateMobileNumber());
        profile.setDateOfBirth(profileDto.getDateOfBirth());
        profile.setEducationalBoard(profileDto.getEducationalBoard());
        profile.setClassLevel(profileDto.getClassLevel());
        profile.setGradeLevel(profileDto.getGradeLevel());
        profile.setSchoolName(profileDto.getSchoolName());
        profile.setEmergencyContact(profileDto.getEmergencyContact());
        profile.setStudentPhotoUrl(profileDto.getStudentPhotoUrl());
        profile.setSchoolIdCardUrl(profileDto.getSchoolIdCardUrl());

        StudentProfile savedProfile = studentProfileRepository.save(profile);
        return convertToDto(savedProfile);
    }

    public List<StudentDocumentDto> getStudentDocuments(Long userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
        
        return profile.getDocuments().stream()
                .map(this::convertDocumentToDto)
                .collect(Collectors.toList());
    }

    public StudentDocumentDto addStudentDocument(Long userId, StudentDocumentDto documentDto) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        StudentDocument document = new StudentDocument();
        document.setStudentProfile(profile);
        document.setDocumentName(documentDto.getDocumentName());
        document.setDocumentUrl(documentDto.getDocumentUrl());
        document.setDocumentType(documentDto.getDocumentType());

        profile.getDocuments().add(document);
        studentProfileRepository.save(profile);

        return convertDocumentToDto(document);
    }

    public void deleteStudentDocument(Long userId, Long documentId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        profile.getDocuments().removeIf(doc -> doc.getId().equals(documentId));
        studentProfileRepository.save(profile);
    }

    private StudentProfileDto convertToDto(StudentProfile profile) {
        StudentProfileDto dto = new StudentProfileDto();
        dto.setId(profile.getId());
        dto.setFirstName(profile.getFirstName());
        dto.setMiddleName(profile.getMiddleName());
        dto.setLastName(profile.getLastName());
        dto.setMotherName(profile.getMotherName());
        dto.setFatherName(profile.getFatherName());
        dto.setGuardianName(profile.getGuardianName());
        dto.setParentName(profile.getParentName());
        dto.setMobileNumber(profile.getMobileNumber());
        dto.setAlternateMobileNumber(profile.getAlternateMobileNumber());
        dto.setDateOfBirth(profile.getDateOfBirth());
        dto.setEducationalBoard(profile.getEducationalBoard());
        dto.setClassLevel(profile.getClassLevel());
        dto.setGradeLevel(profile.getGradeLevel());
        dto.setSchoolName(profile.getSchoolName());
        dto.setEmergencyContact(profile.getEmergencyContact());
        dto.setStudentPhotoUrl(profile.getStudentPhotoUrl());
        dto.setSchoolIdCardUrl(profile.getSchoolIdCardUrl());
        
        dto.setDocuments(profile.getDocuments().stream()
                .map(this::convertDocumentToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    private StudentDocumentDto convertDocumentToDto(StudentDocument document) {
        StudentDocumentDto dto = new StudentDocumentDto();
        dto.setId(document.getId());
        dto.setDocumentName(document.getDocumentName());
        dto.setDocumentUrl(document.getDocumentUrl());
        dto.setDocumentType(document.getDocumentType());
        dto.setUploadDate(document.getUploadDate());
        return dto;
    }
}

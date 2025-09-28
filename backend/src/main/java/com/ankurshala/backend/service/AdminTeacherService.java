package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.TeacherDetailDto;
import com.ankurshala.backend.dto.admin.TeacherListDto;
import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.TeacherStatus;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AdminTeacherService {

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<TeacherListDto> getTeachersWithFilters(String search, Boolean enabled, 
                                                      TeacherStatus status, Boolean verified, 
                                                      Pageable pageable) {
        // For now, use the simple query to debug
        if (search == null && enabled == null && status == null && verified == null) {
            return teacherProfileRepository.findAllTeacherProfiles(pageable)
                    .map(this::convertToTeacherListDto);
        }
        
        Page<TeacherProfile> teacherProfiles = teacherProfileRepository.findTeachersWithFilters(
                search, enabled, status, verified, pageable);
        
        return teacherProfiles.map(this::convertToTeacherListDto);
    }

    public Optional<TeacherDetailDto> getTeacherById(Long id) {
        return teacherProfileRepository.findById(id)
                .map(this::convertToTeacherDetailDto);
    }

    public TeacherDetailDto updateTeacher(Long id, TeacherDetailDto teacherDetailDto) {
        TeacherProfile teacherProfile = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Update basic profile information
        teacherProfile.setFirstName(teacherDetailDto.getFirstName());
        teacherProfile.setMiddleName(teacherDetailDto.getMiddleName());
        teacherProfile.setLastName(teacherDetailDto.getLastName());
        teacherProfile.setMobileNumber(teacherDetailDto.getMobileNumber());
        teacherProfile.setAlternateMobileNumber(teacherDetailDto.getAlternateMobileNumber());
        teacherProfile.setContactEmail(teacherDetailDto.getContactEmail());
        teacherProfile.setHighestEducation(teacherDetailDto.getHighestEducation());
        teacherProfile.setPostalAddress(teacherDetailDto.getPostalAddress());
        teacherProfile.setCity(teacherDetailDto.getCity());
        teacherProfile.setState(teacherDetailDto.getState());
        teacherProfile.setCountry(teacherDetailDto.getCountry());
        teacherProfile.setSecondaryAddress(teacherDetailDto.getSecondaryAddress());
        teacherProfile.setProfilePhotoUrl(teacherDetailDto.getProfilePhotoUrl());
        teacherProfile.setGovtIdProofUrl(teacherDetailDto.getGovtIdProofUrl());
        teacherProfile.setBio(teacherDetailDto.getBio());
        teacherProfile.setQualifications(teacherDetailDto.getQualifications());
        teacherProfile.setHourlyRate(teacherDetailDto.getHourlyRate());
        teacherProfile.setYearsOfExperience(teacherDetailDto.getYearsOfExperience());
        teacherProfile.setSpecialization(teacherDetailDto.getSpecialization());
        teacherProfile.setVerified(teacherDetailDto.getVerified());
        teacherProfile.setRating(teacherDetailDto.getRating());
        teacherProfile.setTotalReviews(teacherDetailDto.getTotalReviews());

        // Update teacher status
        if (teacherDetailDto.getStatus() != null && teacherProfile.getTeacher() != null) {
            teacherProfile.getTeacher().setStatus(teacherDetailDto.getStatus());
        }

        TeacherProfile savedProfile = teacherProfileRepository.save(teacherProfile);
        return convertToTeacherDetailDto(savedProfile);
    }

    public boolean toggleTeacherStatus(Long id) {
        TeacherProfile teacherProfile = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        User user = teacherProfile.getUser();
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        
        return user.getEnabled();
    }

    private TeacherListDto convertToTeacherListDto(TeacherProfile teacherProfile) {
        TeacherListDto dto = new TeacherListDto();
        dto.setId(teacherProfile.getId());
        
        User user = teacherProfile.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setEnabled(user.getEnabled());
        }
        
        dto.setFirstName(teacherProfile.getFirstName());
        dto.setMiddleName(teacherProfile.getMiddleName());
        dto.setLastName(teacherProfile.getLastName());
        dto.setMobileNumber(teacherProfile.getMobileNumber());
        dto.setSpecialization(teacherProfile.getSpecialization());
        dto.setYearsOfExperience(teacherProfile.getYearsOfExperience());
        dto.setHourlyRate(teacherProfile.getHourlyRate());
        dto.setRating(teacherProfile.getRating());
        dto.setTotalReviews(teacherProfile.getTotalReviews());
        dto.setVerified(teacherProfile.getVerified());
        dto.setCreatedAt(teacherProfile.getCreatedAt());
        dto.setLastLoginAt(null); // lastLoginAt - would need to be tracked separately
        
        // Handle teacher status safely
        if (teacherProfile.getTeacher() != null) {
            dto.setStatus(teacherProfile.getTeacher().getStatus());
        } else {
            dto.setStatus(TeacherStatus.PENDING); // Default status
        }
        
        return dto;
    }

    private TeacherDetailDto convertToTeacherDetailDto(TeacherProfile teacherProfile) {
        TeacherDetailDto dto = new TeacherDetailDto();
        dto.setId(teacherProfile.getId());
        
        User user = teacherProfile.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setEnabled(user.getEnabled());
        }
        
        dto.setFirstName(teacherProfile.getFirstName());
        dto.setMiddleName(teacherProfile.getMiddleName());
        dto.setLastName(teacherProfile.getLastName());
        dto.setMobileNumber(teacherProfile.getMobileNumber());
        dto.setAlternateMobileNumber(teacherProfile.getAlternateMobileNumber());
        dto.setContactEmail(teacherProfile.getContactEmail());
        dto.setHighestEducation(teacherProfile.getHighestEducation());
        dto.setPostalAddress(teacherProfile.getPostalAddress());
        dto.setCity(teacherProfile.getCity());
        dto.setState(teacherProfile.getState());
        dto.setCountry(teacherProfile.getCountry());
        dto.setSecondaryAddress(teacherProfile.getSecondaryAddress());
        dto.setProfilePhotoUrl(teacherProfile.getProfilePhotoUrl());
        dto.setGovtIdProofUrl(teacherProfile.getGovtIdProofUrl());
        dto.setBio(teacherProfile.getBio());
        dto.setQualifications(teacherProfile.getQualifications());
        dto.setHourlyRate(teacherProfile.getHourlyRate());
        dto.setYearsOfExperience(teacherProfile.getYearsOfExperience());
        dto.setSpecialization(teacherProfile.getSpecialization());
        dto.setVerified(teacherProfile.getVerified());
        dto.setRating(teacherProfile.getRating());
        dto.setTotalReviews(teacherProfile.getTotalReviews());
        dto.setCreatedAt(teacherProfile.getCreatedAt());
        dto.setUpdatedAt(teacherProfile.getUpdatedAt());
        dto.setLastLoginAt(null); // lastLoginAt - would need to be tracked separately
        
        // Handle teacher status safely
        if (teacherProfile.getTeacher() != null) {
            dto.setStatus(teacherProfile.getTeacher().getStatus());
        } else {
            dto.setStatus(TeacherStatus.PENDING); // Default status
        }
        
        return dto;
    }
}

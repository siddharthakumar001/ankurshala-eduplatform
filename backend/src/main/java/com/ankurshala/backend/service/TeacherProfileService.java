package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.teacher.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherProfileService {

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Profile Management
    public TeacherProfileDto getTeacherProfile(Long userId) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for user ID: " + userId));
        return convertToDto(profile);
    }

    public TeacherProfileDto updateTeacherProfile(Long userId, TeacherProfileDto profileDto) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
        
        updateProfileFromDto(profile, profileDto);
        TeacherProfile updatedProfile = teacherProfileRepository.save(profile);
        return convertToDto(updatedProfile);
    }

    // Qualifications Management
    public List<TeacherQualificationDto> getTeacherQualifications(Long userId) {
        Teacher teacher = getTeacherByUserId(userId);
        return teacher.getQualifications().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TeacherQualificationDto addTeacherQualification(Long userId, TeacherQualificationDto qualificationDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherQualification qualification = new TeacherQualification();
        qualification.setTeacher(teacher);
        updateQualificationFromDto(qualification, qualificationDto);
        
        teacher.getQualifications().add(qualification);
        teacherRepository.save(teacher);
        
        return convertToDto(qualification);
    }

    public TeacherQualificationDto updateTeacherQualification(Long userId, Long qualificationId, TeacherQualificationDto qualificationDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherQualification qualification = teacher.getQualifications().stream()
                .filter(q -> q.getId().equals(qualificationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Qualification not found"));
        
        updateQualificationFromDto(qualification, qualificationDto);
        teacherRepository.save(teacher);
        
        return convertToDto(qualification);
    }

    public void deleteTeacherQualification(Long userId, Long qualificationId) {
        Teacher teacher = getTeacherByUserId(userId);
        teacher.getQualifications().removeIf(q -> q.getId().equals(qualificationId));
        teacherRepository.save(teacher);
    }

    // Experience Management
    public List<TeacherExperienceDto> getTeacherExperiences(Long userId) {
        Teacher teacher = getTeacherByUserId(userId);
        return teacher.getExperiences().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TeacherExperienceDto addTeacherExperience(Long userId, TeacherExperienceDto experienceDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherExperience experience = new TeacherExperience();
        experience.setTeacher(teacher);
        updateExperienceFromDto(experience, experienceDto);
        
        teacher.getExperiences().add(experience);
        teacherRepository.save(teacher);
        
        return convertToDto(experience);
    }

    public TeacherExperienceDto updateTeacherExperience(Long userId, Long experienceId, TeacherExperienceDto experienceDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherExperience experience = teacher.getExperiences().stream()
                .filter(e -> e.getId().equals(experienceId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Experience not found"));
        
        updateExperienceFromDto(experience, experienceDto);
        teacherRepository.save(teacher);
        
        return convertToDto(experience);
    }

    public void deleteTeacherExperience(Long userId, Long experienceId) {
        Teacher teacher = getTeacherByUserId(userId);
        teacher.getExperiences().removeIf(e -> e.getId().equals(experienceId));
        teacherRepository.save(teacher);
    }

    // Certifications Management
    public List<TeacherCertificationDto> getTeacherCertifications(Long userId) {
        Teacher teacher = getTeacherByUserId(userId);
        return teacher.getCertifications().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TeacherCertificationDto addTeacherCertification(Long userId, TeacherCertificationDto certificationDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherCertification certification = new TeacherCertification();
        certification.setTeacher(teacher);
        updateCertificationFromDto(certification, certificationDto);
        
        teacher.getCertifications().add(certification);
        teacherRepository.save(teacher);
        
        return convertToDto(certification);
    }

    public TeacherCertificationDto updateTeacherCertification(Long userId, Long certificationId, TeacherCertificationDto certificationDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherCertification certification = teacher.getCertifications().stream()
                .filter(c -> c.getId().equals(certificationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Certification not found"));
        
        updateCertificationFromDto(certification, certificationDto);
        teacherRepository.save(teacher);
        
        return convertToDto(certification);
    }

    public void deleteTeacherCertification(Long userId, Long certificationId) {
        Teacher teacher = getTeacherByUserId(userId);
        teacher.getCertifications().removeIf(c -> c.getId().equals(certificationId));
        teacherRepository.save(teacher);
    }

    // Documents Management
    public List<TeacherDocumentDto> getTeacherDocuments(Long userId) {
        Teacher teacher = getTeacherByUserId(userId);
        return teacher.getDocuments().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TeacherDocumentDto addTeacherDocument(Long userId, TeacherDocumentDto documentDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherDocument document = new TeacherDocument();
        document.setTeacher(teacher);
        updateDocumentFromDto(document, documentDto);
        
        teacher.getDocuments().add(document);
        teacherRepository.save(teacher);
        
        return convertToDto(document);
    }

    public TeacherDocumentDto updateTeacherDocument(Long userId, Long documentId, TeacherDocumentDto documentDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherDocument document = teacher.getDocuments().stream()
                .filter(d -> d.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        updateDocumentFromDto(document, documentDto);
        teacherRepository.save(teacher);
        
        return convertToDto(document);
    }

    public void deleteTeacherDocument(Long userId, Long documentId) {
        Teacher teacher = getTeacherByUserId(userId);
        teacher.getDocuments().removeIf(d -> d.getId().equals(documentId));
        teacherRepository.save(teacher);
    }

    // Availability Management
    public TeacherAvailabilityDto getTeacherAvailability(Long userId) {
        Teacher teacher = getTeacherByUserId(userId);
        if (teacher.getAvailability() == null) {
            return new TeacherAvailabilityDto();
        }
        return convertToDto(teacher.getAvailability());
    }

    public TeacherAvailabilityDto updateTeacherAvailability(Long userId, TeacherAvailabilityDto availabilityDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherAvailability availability = teacher.getAvailability();
        
        if (availability == null) {
            availability = new TeacherAvailability();
            availability.setTeacher(teacher);
            teacher.setAvailability(availability);
        }
        
        updateAvailabilityFromDto(availability, availabilityDto);
        teacherRepository.save(teacher);
        
        return convertToDto(availability);
    }

    // Addresses Management
    public List<TeacherAddressDto> getTeacherAddresses(Long userId) {
        Teacher teacher = getTeacherByUserId(userId);
        return teacher.getAddresses().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TeacherAddressDto addTeacherAddress(Long userId, TeacherAddressDto addressDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherAddress address = new TeacherAddress();
        address.setTeacher(teacher);
        updateAddressFromDto(address, addressDto);
        
        teacher.getAddresses().add(address);
        teacherRepository.save(teacher);
        
        return convertToDto(address);
    }

    public TeacherAddressDto updateTeacherAddress(Long userId, Long addressId, TeacherAddressDto addressDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherAddress address = teacher.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        updateAddressFromDto(address, addressDto);
        teacherRepository.save(teacher);
        
        return convertToDto(address);
    }

    public void deleteTeacherAddress(Long userId, Long addressId) {
        Teacher teacher = getTeacherByUserId(userId);
        teacher.getAddresses().removeIf(a -> a.getId().equals(addressId));
        teacherRepository.save(teacher);
    }

    // Bank Details Management
    public TeacherBankDetailsDto getTeacherBankDetails(Long userId) {
        Teacher teacher = getTeacherByUserId(userId);
        if (teacher.getBankDetails() == null) {
            return new TeacherBankDetailsDto();
        }
        return convertToDto(teacher.getBankDetails());
    }

    public TeacherBankDetailsDto updateTeacherBankDetails(Long userId, TeacherBankDetailsDto bankDetailsDto) {
        Teacher teacher = getTeacherByUserId(userId);
        TeacherBankDetails bankDetails = teacher.getBankDetails();
        
        if (bankDetails == null) {
            bankDetails = new TeacherBankDetails();
            bankDetails.setTeacher(teacher);
            teacher.setBankDetails(bankDetails);
        }
        
        updateBankDetailsFromDto(bankDetails, bankDetailsDto);
        teacherRepository.save(teacher);
        
        return convertToDto(bankDetails);
    }

    // Helper methods
    private Teacher getTeacherByUserId(Long userId) {
        return teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
    }

    // DTO Conversion methods
    private TeacherProfileDto convertToDto(TeacherProfile profile) {
        TeacherProfileDto dto = new TeacherProfileDto();
        dto.setId(profile.getId());
        dto.setFirstName(profile.getFirstName());
        dto.setMiddleName(profile.getMiddleName());
        dto.setLastName(profile.getLastName());
        dto.setMobileNumber(profile.getMobileNumber());
        dto.setAlternateMobileNumber(profile.getAlternateMobileNumber());
        dto.setContactEmail(profile.getContactEmail());
        dto.setHighestEducation(profile.getHighestEducation());
        dto.setPostalAddress(profile.getPostalAddress());
        dto.setCity(profile.getCity());
        dto.setState(profile.getState());
        dto.setCountry(profile.getCountry());
        dto.setSecondaryAddress(profile.getSecondaryAddress());
        dto.setProfilePhotoUrl(profile.getProfilePhotoUrl());
        dto.setGovtIdProofUrl(profile.getGovtIdProofUrl());
        dto.setBio(profile.getBio());
        dto.setQualifications(profile.getQualifications());
        dto.setHourlyRate(profile.getHourlyRate());
        dto.setYearsOfExperience(profile.getYearsOfExperience());
        dto.setSpecialization(profile.getSpecialization());
        dto.setVerified(profile.getVerified());
        dto.setRating(profile.getRating());
        dto.setTotalReviews(profile.getTotalReviews());
        return dto;
    }

    private void updateProfileFromDto(TeacherProfile profile, TeacherProfileDto dto) {
        profile.setFirstName(dto.getFirstName());
        profile.setMiddleName(dto.getMiddleName());
        profile.setLastName(dto.getLastName());
        profile.setMobileNumber(dto.getMobileNumber());
        profile.setAlternateMobileNumber(dto.getAlternateMobileNumber());
        profile.setContactEmail(dto.getContactEmail());
        profile.setHighestEducation(dto.getHighestEducation());
        profile.setPostalAddress(dto.getPostalAddress());
        profile.setCity(dto.getCity());
        profile.setState(dto.getState());
        profile.setCountry(dto.getCountry());
        profile.setSecondaryAddress(dto.getSecondaryAddress());
        profile.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        profile.setGovtIdProofUrl(dto.getGovtIdProofUrl());
        profile.setBio(dto.getBio());
        profile.setQualifications(dto.getQualifications());
        profile.setHourlyRate(dto.getHourlyRate());
        profile.setYearsOfExperience(dto.getYearsOfExperience());
        profile.setSpecialization(dto.getSpecialization());
        // Note: verified, rating, totalReviews are typically not updated by user
    }

    private TeacherQualificationDto convertToDto(TeacherQualification qualification) {
        TeacherQualificationDto dto = new TeacherQualificationDto();
        dto.setId(qualification.getId());
        dto.setDegree(qualification.getDegree());
        dto.setSpecialization(qualification.getSpecialization());
        dto.setUniversity(qualification.getUniversity());
        dto.setYear(qualification.getYear());
        return dto;
    }

    private void updateQualificationFromDto(TeacherQualification qualification, TeacherQualificationDto dto) {
        qualification.setDegree(dto.getDegree());
        qualification.setSpecialization(dto.getSpecialization());
        qualification.setUniversity(dto.getUniversity());
        qualification.setYear(dto.getYear());
    }

    private TeacherExperienceDto convertToDto(TeacherExperience experience) {
        TeacherExperienceDto dto = new TeacherExperienceDto();
        dto.setId(experience.getId());
        dto.setInstitution(experience.getInstitution());
        dto.setRole(experience.getRole());
        dto.setSubjectsTaught(experience.getSubjectsTaught());
        dto.setFromDate(experience.getFromDate());
        dto.setToDate(experience.getToDate());
        dto.setCurrentlyWorking(experience.getCurrentlyWorking());
        return dto;
    }

    private void updateExperienceFromDto(TeacherExperience experience, TeacherExperienceDto dto) {
        experience.setInstitution(dto.getInstitution());
        experience.setRole(dto.getRole());
        experience.setSubjectsTaught(dto.getSubjectsTaught());
        experience.setFromDate(dto.getFromDate());
        experience.setToDate(dto.getToDate());
        experience.setCurrentlyWorking(dto.getCurrentlyWorking());
    }

    private TeacherCertificationDto convertToDto(TeacherCertification certification) {
        TeacherCertificationDto dto = new TeacherCertificationDto();
        dto.setId(certification.getId());
        dto.setCertificationName(certification.getCertificationName());
        dto.setIssuingAuthority(certification.getIssuingAuthority());
        dto.setCertificationId(certification.getCertificationId());
        dto.setIssueYear(certification.getIssueYear());
        dto.setExpiryDate(certification.getExpiryDate());
        return dto;
    }

    private void updateCertificationFromDto(TeacherCertification certification, TeacherCertificationDto dto) {
        certification.setCertificationName(dto.getCertificationName());
        certification.setIssuingAuthority(dto.getIssuingAuthority());
        certification.setCertificationId(dto.getCertificationId());
        certification.setIssueYear(dto.getIssueYear());
        certification.setExpiryDate(dto.getExpiryDate());
    }

    private TeacherDocumentDto convertToDto(TeacherDocument document) {
        TeacherDocumentDto dto = new TeacherDocumentDto();
        dto.setId(document.getId());
        dto.setDocumentType(document.getDocumentType());
        dto.setDocumentUrl(document.getDocumentUrl());
        dto.setDocumentName(document.getDocumentName());
        return dto;
    }

    private void updateDocumentFromDto(TeacherDocument document, TeacherDocumentDto dto) {
        document.setDocumentType(dto.getDocumentType());
        document.setDocumentUrl(dto.getDocumentUrl());
        document.setDocumentName(dto.getDocumentName());
    }

    private TeacherAvailabilityDto convertToDto(TeacherAvailability availability) {
        TeacherAvailabilityDto dto = new TeacherAvailabilityDto();
        dto.setId(availability.getId());
        dto.setAvailableFrom(availability.getAvailableFrom());
        dto.setAvailableTo(availability.getAvailableTo());
        dto.setPreferredStudentLevels(availability.getPreferredStudentLevels());
        dto.setLanguagesSpoken(availability.getLanguagesSpoken());
        return dto;
    }

    private void updateAvailabilityFromDto(TeacherAvailability availability, TeacherAvailabilityDto dto) {
        availability.setAvailableFrom(dto.getAvailableFrom());
        availability.setAvailableTo(dto.getAvailableTo());
        availability.setPreferredStudentLevels(dto.getPreferredStudentLevels());
        availability.setLanguagesSpoken(dto.getLanguagesSpoken());
    }

    private TeacherAddressDto convertToDto(TeacherAddress address) {
        TeacherAddressDto dto = new TeacherAddressDto();
        dto.setId(address.getId());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setAddressType(address.getAddressType());
        return dto;
    }

    private void updateAddressFromDto(TeacherAddress address, TeacherAddressDto dto) {
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        address.setAddressType(dto.getAddressType());
    }

    private TeacherBankDetailsDto convertToDto(TeacherBankDetails bankDetails) {
        TeacherBankDetailsDto dto = new TeacherBankDetailsDto();
        dto.setId(bankDetails.getId());
        dto.setBankName(bankDetails.getBankName());
        dto.setBranchAddress(bankDetails.getBranchAddress());
        // Mask account number for security
        dto.setAccountNumber(maskAccountNumber(bankDetails.getAccountNumber()));
        dto.setIfscCode(bankDetails.getIfscCode());
        dto.setAccountHolderName(bankDetails.getAccountHolderName());
        dto.setAccountType(bankDetails.getAccountType());
        return dto;
    }

    private void updateBankDetailsFromDto(TeacherBankDetails bankDetails, TeacherBankDetailsDto dto) {
        bankDetails.setBankName(dto.getBankName());
        bankDetails.setBranchAddress(dto.getBranchAddress());
        // Only update account number if it's not masked
        if (dto.getAccountNumber() != null && !dto.getAccountNumber().contains("*")) {
            bankDetails.setAccountNumber(dto.getAccountNumber());
        }
        bankDetails.setIfscCode(dto.getIfscCode());
        bankDetails.setAccountHolderName(dto.getAccountHolderName());
        bankDetails.setAccountType(dto.getAccountType());
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }
        return "*".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }
}

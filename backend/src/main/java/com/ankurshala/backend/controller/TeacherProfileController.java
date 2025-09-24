package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.teacher.*;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.TeacherProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// NOTE: server.servlet.context-path=/api is set for the app.
// Therefore controller @RequestMapping must NOT start with "/api".
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherProfileController {

    @Autowired
    private TeacherProfileService teacherProfileService;

    // Profile Management
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherProfileDto> getProfile(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherProfileDto profile = teacherProfileService.getTeacherProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherProfileDto> updateProfile(
            @Valid @RequestBody TeacherProfileDto profileDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherProfileDto updatedProfile = teacherProfileService.updateTeacherProfile(userId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    // Qualifications Management
    @GetMapping("/profile/qualifications")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherQualificationDto>> getQualifications(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<TeacherQualificationDto> qualifications = teacherProfileService.getTeacherQualifications(userId);
        return ResponseEntity.ok(qualifications);
    }

    @PostMapping("/profile/qualifications")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherQualificationDto> addQualification(
            @Valid @RequestBody TeacherQualificationDto qualificationDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherQualificationDto addedQualification = teacherProfileService.addTeacherQualification(userId, qualificationDto);
        return ResponseEntity.ok(addedQualification);
    }

    @PutMapping("/profile/qualifications/{qualificationId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherQualificationDto> updateQualification(
            @PathVariable Long qualificationId,
            @Valid @RequestBody TeacherQualificationDto qualificationDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherQualificationDto updatedQualification = teacherProfileService.updateTeacherQualification(userId, qualificationId, qualificationDto);
        return ResponseEntity.ok(updatedQualification);
    }

    @DeleteMapping("/profile/qualifications/{qualificationId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteQualification(
            @PathVariable Long qualificationId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        teacherProfileService.deleteTeacherQualification(userId, qualificationId);
        return ResponseEntity.ok().build();
    }

    // Experience Management
    @GetMapping("/profile/experiences")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherExperienceDto>> getExperiences(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<TeacherExperienceDto> experiences = teacherProfileService.getTeacherExperiences(userId);
        return ResponseEntity.ok(experiences);
    }

    @PostMapping("/profile/experiences")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherExperienceDto> addExperience(
            @Valid @RequestBody TeacherExperienceDto experienceDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherExperienceDto addedExperience = teacherProfileService.addTeacherExperience(userId, experienceDto);
        return ResponseEntity.ok(addedExperience);
    }

    @PutMapping("/profile/experiences/{experienceId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherExperienceDto> updateExperience(
            @PathVariable Long experienceId,
            @Valid @RequestBody TeacherExperienceDto experienceDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherExperienceDto updatedExperience = teacherProfileService.updateTeacherExperience(userId, experienceId, experienceDto);
        return ResponseEntity.ok(updatedExperience);
    }

    @DeleteMapping("/profile/experiences/{experienceId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long experienceId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        teacherProfileService.deleteTeacherExperience(userId, experienceId);
        return ResponseEntity.ok().build();
    }

    // Certifications Management
    @GetMapping("/profile/certifications")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherCertificationDto>> getCertifications(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<TeacherCertificationDto> certifications = teacherProfileService.getTeacherCertifications(userId);
        return ResponseEntity.ok(certifications);
    }

    @PostMapping("/profile/certifications")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherCertificationDto> addCertification(
            @Valid @RequestBody TeacherCertificationDto certificationDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherCertificationDto addedCertification = teacherProfileService.addTeacherCertification(userId, certificationDto);
        return ResponseEntity.ok(addedCertification);
    }

    @PutMapping("/profile/certifications/{certificationId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherCertificationDto> updateCertification(
            @PathVariable Long certificationId,
            @Valid @RequestBody TeacherCertificationDto certificationDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherCertificationDto updatedCertification = teacherProfileService.updateTeacherCertification(userId, certificationId, certificationDto);
        return ResponseEntity.ok(updatedCertification);
    }

    @DeleteMapping("/profile/certifications/{certificationId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteCertification(
            @PathVariable Long certificationId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        teacherProfileService.deleteTeacherCertification(userId, certificationId);
        return ResponseEntity.ok().build();
    }

    // Documents Management
    @GetMapping("/profile/documents")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherDocumentDto>> getDocuments(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<TeacherDocumentDto> documents = teacherProfileService.getTeacherDocuments(userId);
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/profile/documents")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDocumentDto> addDocument(
            @Valid @RequestBody TeacherDocumentDto documentDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherDocumentDto addedDocument = teacherProfileService.addTeacherDocument(userId, documentDto);
        return ResponseEntity.ok(addedDocument);
    }

    @PutMapping("/profile/documents/{documentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherDocumentDto> updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody TeacherDocumentDto documentDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherDocumentDto updatedDocument = teacherProfileService.updateTeacherDocument(userId, documentId, documentDto);
        return ResponseEntity.ok(updatedDocument);
    }

    @DeleteMapping("/profile/documents/{documentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        teacherProfileService.deleteTeacherDocument(userId, documentId);
        return ResponseEntity.ok().build();
    }

    // Availability Management
    @GetMapping("/profile/availability")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherAvailabilityDto> getAvailability(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherAvailabilityDto availability = teacherProfileService.getTeacherAvailability(userId);
        return ResponseEntity.ok(availability);
    }

    @PutMapping("/profile/availability")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherAvailabilityDto> updateAvailability(
            @Valid @RequestBody TeacherAvailabilityDto availabilityDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherAvailabilityDto updatedAvailability = teacherProfileService.updateTeacherAvailability(userId, availabilityDto);
        return ResponseEntity.ok(updatedAvailability);
    }

    // Addresses Management
    @GetMapping("/profile/addresses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeacherAddressDto>> getAddresses(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<TeacherAddressDto> addresses = teacherProfileService.getTeacherAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/profile/addresses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherAddressDto> addAddress(
            @Valid @RequestBody TeacherAddressDto addressDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherAddressDto addedAddress = teacherProfileService.addTeacherAddress(userId, addressDto);
        return ResponseEntity.ok(addedAddress);
    }

    @PutMapping("/profile/addresses/{addressId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherAddressDto> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody TeacherAddressDto addressDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherAddressDto updatedAddress = teacherProfileService.updateTeacherAddress(userId, addressId, addressDto);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/profile/addresses/{addressId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        teacherProfileService.deleteTeacherAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    // Bank Details Management
    @GetMapping("/profile/bank-details")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherBankDetailsDto> getBankDetails(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherBankDetailsDto bankDetails = teacherProfileService.getTeacherBankDetails(userId);
        return ResponseEntity.ok(bankDetails);
    }

    @PutMapping("/profile/bank-details")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<TeacherBankDetailsDto> updateBankDetails(
            @Valid @RequestBody TeacherBankDetailsDto bankDetailsDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        TeacherBankDetailsDto updatedBankDetails = teacherProfileService.updateTeacherBankDetails(userId, bankDetailsDto);
        return ResponseEntity.ok(updatedBankDetails);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}

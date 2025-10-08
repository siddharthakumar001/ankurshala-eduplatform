package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.teacher.*;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.ResourceAuthorizationService;
import com.ankurshala.backend.service.TeacherProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherProfileControllerTest {

    @Mock
    private TeacherProfileService teacherProfileService;

    @Mock
    private ResourceAuthorizationService resourceAuthorizationService;

    @InjectMocks
    private TeacherProfileController teacherProfileController;

    private Authentication authentication;
    private UserPrincipal userPrincipal;
    private TeacherProfileDto mockProfile;
    private TeacherQualificationDto mockQualification;
    private TeacherExperienceDto mockExperience;
    private TeacherCertificationDto mockCertification;
    private TeacherDocumentDto mockDocument;
    private TeacherAvailabilityDto mockAvailability;
    private TeacherAddressDto mockAddress;
    private TeacherBankDetailsDto mockBankDetails;

    @BeforeEach
    void setUp() {
        // Setup user principal
        userPrincipal = new UserPrincipal(1L, "teacher@example.com", "Teacher User", "password", Role.TEACHER, true);

        // Setup authentication
        authentication = new TestingAuthenticationToken(userPrincipal, null);

        // Setup mock profile
        mockProfile = new TeacherProfileDto();
        mockProfile.setId(1L);
        mockProfile.setFirstName("John");
        mockProfile.setLastName("Doe");

        // Setup mock qualification
        mockQualification = new TeacherQualificationDto();
        mockQualification.setId(1L);
        mockQualification.setDegree("Bachelor of Science");

        // Setup mock experience
        mockExperience = new TeacherExperienceDto();
        mockExperience.setId(1L);

        // Setup mock certification
        mockCertification = new TeacherCertificationDto();
        mockCertification.setId(1L);
        mockCertification.setCertificationName("Teaching Certificate");

        // Setup mock document
        mockDocument = new TeacherDocumentDto();
        mockDocument.setId(1L);
        mockDocument.setDocumentType("DEGREE_CERTIFICATE");
        mockDocument.setDocumentUrl("https://example.com/document.pdf");

        // Setup mock availability
        mockAvailability = new TeacherAvailabilityDto();
        mockAvailability.setId(1L);

        // Setup mock address
        mockAddress = new TeacherAddressDto();
        mockAddress.setId(1L);
        mockAddress.setAddressLine1("123 Main Street");
        mockAddress.setCity("Mumbai");
        mockAddress.setState("Maharashtra");

        // Setup mock bank details
        mockBankDetails = new TeacherBankDetailsDto();
        mockBankDetails.setId(1L);
        mockBankDetails.setAccountNumber("1234567890");
        mockBankDetails.setBankName("State Bank of India");
    }

    // Profile Management Tests
    @Test
    void testGetProfile_Success() {
        // Given
        when(teacherProfileService.getTeacherProfile(1L)).thenReturn(mockProfile);

        // When
        ResponseEntity<TeacherProfileDto> response = teacherProfileController.getProfile(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John", response.getBody().getFirstName());

        verify(teacherProfileService, times(1)).getTeacherProfile(1L);
    }

    @Test
    void testUpdateProfile_Success() {
        // Given
        when(teacherProfileService.updateTeacherProfile(1L, mockProfile)).thenReturn(mockProfile);

        // When
        ResponseEntity<TeacherProfileDto> response = teacherProfileController.updateProfile(mockProfile, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).updateTeacherProfile(1L, mockProfile);
    }

    // Qualifications Management Tests
    @Test
    void testGetQualifications_Success() {
        // Given
        List<TeacherQualificationDto> mockQualifications = Arrays.asList(mockQualification);
        when(teacherProfileService.getTeacherQualifications(1L)).thenReturn(mockQualifications);

        // When
        ResponseEntity<List<TeacherQualificationDto>> response = teacherProfileController.getQualifications(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Bachelor of Science", response.getBody().get(0).getDegree());

        verify(teacherProfileService, times(1)).getTeacherQualifications(1L);
    }

    @Test
    void testAddQualification_Success() {
        // Given
        when(teacherProfileService.addTeacherQualification(1L, mockQualification)).thenReturn(mockQualification);

        // When
        ResponseEntity<TeacherQualificationDto> response = teacherProfileController.addQualification(mockQualification, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).addTeacherQualification(1L, mockQualification);
    }

    @Test
    void testUpdateQualification_Success() {
        // Given
        Long qualificationId = 1L;
        when(teacherProfileService.updateTeacherQualification(1L, qualificationId, mockQualification)).thenReturn(mockQualification);

        // When
        ResponseEntity<TeacherQualificationDto> response = teacherProfileController.updateQualification(qualificationId, mockQualification, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).updateTeacherQualification(1L, qualificationId, mockQualification);
    }

    @Test
    void testDeleteQualification_Success() {
        // Given
        Long qualificationId = 1L;
        when(resourceAuthorizationService.canAccessTeacherResource(1L, qualificationId)).thenReturn(true);
        doNothing().when(teacherProfileService).deleteTeacherQualification(1L, qualificationId);

        // When
        ResponseEntity<Void> response = teacherProfileController.deleteQualification(qualificationId, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(resourceAuthorizationService, times(1)).canAccessTeacherResource(1L, qualificationId);
        verify(teacherProfileService, times(1)).deleteTeacherQualification(1L, qualificationId);
    }

    @Test
    void testDeleteQualification_Unauthorized() {
        // Given
        Long qualificationId = 1L;
        when(resourceAuthorizationService.canAccessTeacherResource(1L, qualificationId)).thenReturn(false);

        // When
        ResponseEntity<Void> response = teacherProfileController.deleteQualification(qualificationId, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(resourceAuthorizationService, times(1)).canAccessTeacherResource(1L, qualificationId);
        verify(teacherProfileService, never()).deleteTeacherQualification(any(), any());
    }

    // Experience Management Tests
    @Test
    void testGetExperiences_Success() {
        // Given
        List<TeacherExperienceDto> mockExperiences = Arrays.asList(mockExperience);
        when(teacherProfileService.getTeacherExperiences(1L)).thenReturn(mockExperiences);

        // When
        ResponseEntity<List<TeacherExperienceDto>> response = teacherProfileController.getExperiences(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());

        verify(teacherProfileService, times(1)).getTeacherExperiences(1L);
    }

    @Test
    void testAddExperience_Success() {
        // Given
        when(teacherProfileService.addTeacherExperience(1L, mockExperience)).thenReturn(mockExperience);

        // When
        ResponseEntity<TeacherExperienceDto> response = teacherProfileController.addExperience(mockExperience, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).addTeacherExperience(1L, mockExperience);
    }

    @Test
    void testDeleteExperience_Success() {
        // Given
        Long experienceId = 1L;
        doNothing().when(teacherProfileService).deleteTeacherExperience(1L, experienceId);

        // When
        ResponseEntity<Void> response = teacherProfileController.deleteExperience(experienceId, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(teacherProfileService, times(1)).deleteTeacherExperience(1L, experienceId);
    }

    // Certifications Management Tests
    @Test
    void testGetCertifications_Success() {
        // Given
        List<TeacherCertificationDto> mockCertifications = Arrays.asList(mockCertification);
        when(teacherProfileService.getTeacherCertifications(1L)).thenReturn(mockCertifications);

        // When
        ResponseEntity<List<TeacherCertificationDto>> response = teacherProfileController.getCertifications(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Teaching Certificate", response.getBody().get(0).getCertificationName());

        verify(teacherProfileService, times(1)).getTeacherCertifications(1L);
    }

    @Test
    void testAddCertification_Success() {
        // Given
        when(teacherProfileService.addTeacherCertification(1L, mockCertification)).thenReturn(mockCertification);

        // When
        ResponseEntity<TeacherCertificationDto> response = teacherProfileController.addCertification(mockCertification, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).addTeacherCertification(1L, mockCertification);
    }

    // Documents Management Tests
    @Test
    void testGetDocuments_Success() {
        // Given
        List<TeacherDocumentDto> mockDocuments = Arrays.asList(mockDocument);
        when(teacherProfileService.getTeacherDocuments(1L)).thenReturn(mockDocuments);

        // When
        ResponseEntity<List<TeacherDocumentDto>> response = teacherProfileController.getDocuments(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("DEGREE_CERTIFICATE", response.getBody().get(0).getDocumentType());

        verify(teacherProfileService, times(1)).getTeacherDocuments(1L);
    }

    @Test
    void testAddDocument_Success() {
        // Given
        when(teacherProfileService.addTeacherDocument(1L, mockDocument)).thenReturn(mockDocument);

        // When
        ResponseEntity<TeacherDocumentDto> response = teacherProfileController.addDocument(mockDocument, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).addTeacherDocument(1L, mockDocument);
    }

    @Test
    void testDeleteDocument_Success() {
        // Given
        Long documentId = 1L;
        when(resourceAuthorizationService.canAccessTeacherResource(1L, documentId)).thenReturn(true);
        doNothing().when(teacherProfileService).deleteTeacherDocument(1L, documentId);

        // When
        ResponseEntity<Void> response = teacherProfileController.deleteDocument(documentId, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(resourceAuthorizationService, times(1)).canAccessTeacherResource(1L, documentId);
        verify(teacherProfileService, times(1)).deleteTeacherDocument(1L, documentId);
    }

    @Test
    void testDeleteDocument_Unauthorized() {
        // Given
        Long documentId = 1L;
        when(resourceAuthorizationService.canAccessTeacherResource(1L, documentId)).thenReturn(false);

        // When
        ResponseEntity<Void> response = teacherProfileController.deleteDocument(documentId, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(resourceAuthorizationService, times(1)).canAccessTeacherResource(1L, documentId);
        verify(teacherProfileService, never()).deleteTeacherDocument(any(), any());
    }

    // Availability Management Tests
    @Test
    void testGetAvailability_Success() {
        // Given
        when(teacherProfileService.getTeacherAvailability(1L)).thenReturn(mockAvailability);

        // When
        ResponseEntity<TeacherAvailabilityDto> response = teacherProfileController.getAvailability(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).getTeacherAvailability(1L);
    }

    @Test
    void testUpdateAvailability_Success() {
        // Given
        when(teacherProfileService.updateTeacherAvailability(1L, mockAvailability)).thenReturn(mockAvailability);

        // When
        ResponseEntity<TeacherAvailabilityDto> response = teacherProfileController.updateAvailability(mockAvailability, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).updateTeacherAvailability(1L, mockAvailability);
    }

    // Addresses Management Tests
    @Test
    void testGetAddresses_Success() {
        // Given
        List<TeacherAddressDto> mockAddresses = Arrays.asList(mockAddress);
        when(teacherProfileService.getTeacherAddresses(1L)).thenReturn(mockAddresses);

        // When
        ResponseEntity<List<TeacherAddressDto>> response = teacherProfileController.getAddresses(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("123 Main Street", response.getBody().get(0).getAddressLine1());

        verify(teacherProfileService, times(1)).getTeacherAddresses(1L);
    }

    @Test
    void testAddAddress_Success() {
        // Given
        when(teacherProfileService.addTeacherAddress(1L, mockAddress)).thenReturn(mockAddress);

        // When
        ResponseEntity<TeacherAddressDto> response = teacherProfileController.addAddress(mockAddress, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).addTeacherAddress(1L, mockAddress);
    }

    // Bank Details Management Tests
    @Test
    void testGetBankDetails_Success() {
        // Given
        when(teacherProfileService.getTeacherBankDetails(1L)).thenReturn(mockBankDetails);

        // When
        ResponseEntity<TeacherBankDetailsDto> response = teacherProfileController.getBankDetails(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("1234567890", response.getBody().getAccountNumber());

        verify(teacherProfileService, times(1)).getTeacherBankDetails(1L);
    }

    @Test
    void testUpdateBankDetails_Success() {
        // Given
        when(teacherProfileService.updateTeacherBankDetails(1L, mockBankDetails)).thenReturn(mockBankDetails);

        // When
        ResponseEntity<TeacherBankDetailsDto> response = teacherProfileController.updateBankDetails(mockBankDetails, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(teacherProfileService, times(1)).updateTeacherBankDetails(1L, mockBankDetails);
    }

    // Error Handling Tests
    @Test
    void testGetProfile_ServiceThrowsException() {
        // Given
        when(teacherProfileService.getTeacherProfile(1L)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            teacherProfileController.getProfile(authentication);
        });

        verify(teacherProfileService, times(1)).getTeacherProfile(1L);
    }

    @Test
    void testUpdateProfile_ServiceThrowsException() {
        // Given
        when(teacherProfileService.updateTeacherProfile(1L, mockProfile)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            teacherProfileController.updateProfile(mockProfile, authentication);
        });

        verify(teacherProfileService, times(1)).updateTeacherProfile(1L, mockProfile);
    }
}

package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.StudentDocumentDto;
import com.ankurshala.backend.dto.student.StudentProfileDto;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.ResourceAuthorizationService;
import com.ankurshala.backend.service.StudentProfileService;
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
class StudentProfileControllerTest {

    @Mock
    private StudentProfileService studentProfileService;

    @Mock
    private ResourceAuthorizationService resourceAuthorizationService;

    @InjectMocks
    private StudentProfileController studentProfileController;

    private Authentication authentication;
    private UserPrincipal userPrincipal;
    private StudentProfileDto mockProfile;
    private StudentDocumentDto mockDocument;

    @BeforeEach
    void setUp() {
        // Setup user principal
        userPrincipal = new UserPrincipal(1L, "test@example.com", "Test User", "password", Role.STUDENT, true);

        // Setup authentication
        authentication = new TestingAuthenticationToken(userPrincipal, null);

        // Setup mock profile
        mockProfile = new StudentProfileDto();
        mockProfile.setId(1L);
        mockProfile.setFirstName("John");
        mockProfile.setLastName("Doe");

        // Setup mock document
        mockDocument = new StudentDocumentDto();
        mockDocument.setId(1L);
        mockDocument.setDocumentType("ID_PROOF");
        mockDocument.setDocumentUrl("https://example.com/document.pdf");
    }

    @Test
    void testGetProfile_Success() {
        // Given
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(true);
        when(studentProfileService.getStudentProfile(1L)).thenReturn(mockProfile);

        // When
        ResponseEntity<StudentProfileDto> response = studentProfileController.getProfile(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, times(1)).getStudentProfile(1L);
    }

    @Test
    void testGetProfile_Unauthorized() {
        // Given
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(false);

        // When
        ResponseEntity<StudentProfileDto> response = studentProfileController.getProfile(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, never()).getStudentProfile(any());
    }

    @Test
    void testUpdateProfile_Success() {
        // Given
        com.ankurshala.backend.dto.student.UpdateStudentProfileRequest request = 
            new com.ankurshala.backend.dto.student.UpdateStudentProfileRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(true);
        when(studentProfileService.updateProfile(1L, request)).thenReturn(mockProfile);

        // When
        ResponseEntity<StudentProfileDto> response = studentProfileController.updateProfile(request, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, times(1)).updateProfile(1L, request);
    }

    @Test
    void testUpdateProfile_Unauthorized() {
        // Given
        com.ankurshala.backend.dto.student.UpdateStudentProfileRequest request = 
            new com.ankurshala.backend.dto.student.UpdateStudentProfileRequest();
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(false);

        // When
        ResponseEntity<StudentProfileDto> response = studentProfileController.updateProfile(request, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, never()).updateStudentProfile(any(), any());
    }

    @Test
    void testGetDocuments_Success() {
        // Given
        List<StudentDocumentDto> mockDocuments = Arrays.asList(mockDocument);
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(true);
        when(studentProfileService.getStudentDocuments(1L)).thenReturn(mockDocuments);

        // When
        ResponseEntity<List<StudentDocumentDto>> response = studentProfileController.getDocuments(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ID_PROOF", response.getBody().get(0).getDocumentType());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, times(1)).getStudentDocuments(1L);
    }

    @Test
    void testGetDocuments_Unauthorized() {
        // Given
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(false);

        // When
        ResponseEntity<List<StudentDocumentDto>> response = studentProfileController.getDocuments(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, never()).getStudentDocuments(any());
    }

    @Test
    void testAddDocument_Success() {
        // Given
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(true);
        when(studentProfileService.addStudentDocument(1L, mockDocument)).thenReturn(mockDocument);

        // When
        ResponseEntity<StudentDocumentDto> response = studentProfileController.addDocument(mockDocument, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, times(1)).addStudentDocument(1L, mockDocument);
    }

    @Test
    void testAddDocument_Unauthorized() {
        // Given
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(false);

        // When
        ResponseEntity<StudentDocumentDto> response = studentProfileController.addDocument(mockDocument, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, never()).addStudentDocument(any(), any());
    }

    @Test
    void testDeleteDocument_Success() {
        // Given
        Long documentId = 1L;
        when(resourceAuthorizationService.canAccessStudentDocument(1L, documentId)).thenReturn(true);
        doNothing().when(studentProfileService).deleteStudentDocument(1L, documentId);

        // When
        ResponseEntity<Void> response = studentProfileController.deleteDocument(documentId, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(resourceAuthorizationService, times(1)).canAccessStudentDocument(1L, documentId);
        verify(studentProfileService, times(1)).deleteStudentDocument(1L, documentId);
    }

    @Test
    void testDeleteDocument_Unauthorized() {
        // Given
        Long documentId = 1L;
        when(resourceAuthorizationService.canAccessStudentDocument(1L, documentId)).thenReturn(false);

        // When
        ResponseEntity<Void> response = studentProfileController.deleteDocument(documentId, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(resourceAuthorizationService, times(1)).canAccessStudentDocument(1L, documentId);
        verify(studentProfileService, never()).deleteStudentDocument(any(), any());
    }

    @Test
    void testGetProfile_ServiceThrowsException() {
        // Given
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(true);
        when(studentProfileService.getStudentProfile(1L)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            studentProfileController.getProfile(authentication);
        });

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, times(1)).getStudentProfile(1L);
    }

    @Test
    void testUpdateProfile_ServiceThrowsException() {
        // Given
        com.ankurshala.backend.dto.student.UpdateStudentProfileRequest request = 
            new com.ankurshala.backend.dto.student.UpdateStudentProfileRequest();
        when(resourceAuthorizationService.canAccessStudentProfile(1L)).thenReturn(true);
        when(studentProfileService.updateProfile(1L, request)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            studentProfileController.updateProfile(request, authentication);
        });

        verify(resourceAuthorizationService, times(1)).canAccessStudentProfile(1L);
        verify(studentProfileService, times(1)).updateProfile(1L, request);
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.StudentDocumentDto;
import com.ankurshala.backend.dto.student.StudentProfileDto;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentProfileService Tests")
class StudentProfileServiceTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private StudentProfileService studentProfileService;

    private User testUser;
    private StudentProfile testProfile;
    private StudentDocument testDocument;
    private StudentProfileDto testProfileDto;
    private StudentDocumentDto testDocumentDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");

        testProfile = new StudentProfile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
        testProfile.setFirstName("John");
        testProfile.setLastName("Doe");
        testProfile.setMobileNumber("1234567890");
        testProfile.setDateOfBirth(LocalDate.of(2000, 1, 1));
        testProfile.setEducationalBoard(EducationalBoard.CBSE);
        testProfile.setClassLevel(ClassLevel.GRADE_10);
        testProfile.setGradeLevel("10");
        testProfile.setSchoolName("Test School");
        testProfile.setDocuments(new ArrayList<>());

        testDocument = new StudentDocument();
        testDocument.setId(1L);
        testDocument.setStudentProfile(testProfile);
        testDocument.setDocumentName("Aadhar Card");
        testDocument.setDocumentUrl("http://example.com/aadhar.pdf");
        testDocument.setDocumentType("ID_PROOF");
        testDocument.setUploadDate(LocalDateTime.now());

        testProfileDto = new StudentProfileDto();
        testProfileDto.setId(1L);
        testProfileDto.setFirstName("John");
        testProfileDto.setLastName("Doe");
        testProfileDto.setMobileNumber("1234567890");
        testProfileDto.setDateOfBirth(LocalDate.of(2000, 1, 1));
        testProfileDto.setEducationalBoard(EducationalBoard.CBSE);
        testProfileDto.setClassLevel(ClassLevel.GRADE_10);
        testProfileDto.setGradeLevel("10");
        testProfileDto.setSchoolName("Test School");

        testDocumentDto = new StudentDocumentDto();
        testDocumentDto.setId(1L);
        testDocumentDto.setDocumentName("Aadhar Card");
        testDocumentDto.setDocumentUrl("http://example.com/aadhar.pdf");
        testDocumentDto.setDocumentType("ID_PROOF");
        testDocumentDto.setUploadDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create student profile successfully")
    void testCreateStudentProfile_Success() {
        // Given
        when(studentProfileRepository.save(any(StudentProfile.class))).thenReturn(testProfile);

        // When
        StudentProfile result = studentProfileService.createStudentProfile(testUser, "John Doe");

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should create student profile with single name")
    void testCreateStudentProfile_SingleName() {
        // Given
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> {
            StudentProfile profile = invocation.getArgument(0);
            profile.setId(1L);
            return profile;
        });

        // When
        StudentProfile result = studentProfileService.createStudentProfile(testUser, "John");

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals("John", result.getFirstName());
        assertEquals("", result.getLastName());

        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should create student profile with multiple name parts")
    void testCreateStudentProfile_MultipleNameParts() {
        // Given
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> {
            StudentProfile profile = invocation.getArgument(0);
            profile.setId(1L);
            return profile;
        });

        // When
        StudentProfile result = studentProfileService.createStudentProfile(testUser, "John Michael Doe");

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals("John", result.getFirstName());
        assertEquals("Michael Doe", result.getLastName());

        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should get student profile successfully")
    void testGetStudentProfile_Success() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));

        // When
        StudentProfileDto result = studentProfileService.getStudentProfile(testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(testProfile.getId(), result.getId());
        assertEquals(testProfile.getFirstName(), result.getFirstName());
        assertEquals(testProfile.getLastName(), result.getLastName());
        assertEquals(testProfile.getMobileNumber(), result.getMobileNumber());
        assertEquals(testProfile.getDateOfBirth(), result.getDateOfBirth());
        assertEquals(testProfile.getEducationalBoard(), result.getEducationalBoard());
        assertEquals(testProfile.getClassLevel(), result.getClassLevel());
        assertEquals(testProfile.getGradeLevel(), result.getGradeLevel());
        assertEquals(testProfile.getSchoolName(), result.getSchoolName());

        verify(studentProfileRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when student profile not found")
    void testGetStudentProfile_NotFound() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentProfileService.getStudentProfile(testUser.getId());
        });

        assertEquals("Student profile not found", exception.getMessage());
        verify(studentProfileRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should update student profile successfully")
    void testUpdateStudentProfile_Success() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenReturn(testProfile);

        // When
        StudentProfileDto result = studentProfileService.updateStudentProfile(testUser.getId(), testProfileDto);

        // Then
        assertNotNull(result);
        assertEquals(testProfile.getId(), result.getId());
        assertEquals(testProfileDto.getFirstName(), result.getFirstName());
        assertEquals(testProfileDto.getLastName(), result.getLastName());
        assertEquals(testProfileDto.getMobileNumber(), result.getMobileNumber());

        verify(studentProfileRepository).findByUserId(testUser.getId());
        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent student profile")
    void testUpdateStudentProfile_NotFound() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentProfileService.updateStudentProfile(testUser.getId(), testProfileDto);
        });

        assertEquals("Student profile not found", exception.getMessage());
        verify(studentProfileRepository).findByUserId(testUser.getId());
        verify(studentProfileRepository, never()).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should get student documents successfully")
    void testGetStudentDocuments_Success() {
        // Given
        testProfile.getDocuments().add(testDocument);
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));

        // When
        List<StudentDocumentDto> result = studentProfileService.getStudentDocuments(testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDocument.getId(), result.get(0).getId());
        assertEquals(testDocument.getDocumentName(), result.get(0).getDocumentName());
        assertEquals(testDocument.getDocumentUrl(), result.get(0).getDocumentUrl());
        assertEquals(testDocument.getDocumentType(), result.get(0).getDocumentType());

        verify(studentProfileRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when getting documents for non-existent profile")
    void testGetStudentDocuments_NotFound() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentProfileService.getStudentDocuments(testUser.getId());
        });

        assertEquals("Student profile not found", exception.getMessage());
        verify(studentProfileRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should add student document successfully")
    void testAddStudentDocument_Success() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenReturn(testProfile);

        // When
        StudentDocumentDto result = studentProfileService.addStudentDocument(testUser.getId(), testDocumentDto);

        // Then
        assertNotNull(result);
        assertEquals(testDocumentDto.getDocumentName(), result.getDocumentName());
        assertEquals(testDocumentDto.getDocumentUrl(), result.getDocumentUrl());
        assertEquals(testDocumentDto.getDocumentType(), result.getDocumentType());

        verify(studentProfileRepository).findByUserId(testUser.getId());
        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should throw exception when adding document to non-existent profile")
    void testAddStudentDocument_NotFound() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentProfileService.addStudentDocument(testUser.getId(), testDocumentDto);
        });

        assertEquals("Student profile not found", exception.getMessage());
        verify(studentProfileRepository).findByUserId(testUser.getId());
        verify(studentProfileRepository, never()).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should delete student document successfully")
    void testDeleteStudentDocument_Success() {
        // Given
        testProfile.getDocuments().add(testDocument);
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testProfile));
        when(studentProfileRepository.save(any(StudentProfile.class))).thenReturn(testProfile);

        // When
        assertDoesNotThrow(() -> {
            studentProfileService.deleteStudentDocument(testUser.getId(), testDocument.getId());
        });

        // Then
        verify(studentProfileRepository).findByUserId(testUser.getId());
        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting document from non-existent profile")
    void testDeleteStudentDocument_NotFound() {
        // Given
        when(studentProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studentProfileService.deleteStudentDocument(testUser.getId(), testDocument.getId());
        });

        assertEquals("Student profile not found", exception.getMessage());
        verify(studentProfileRepository).findByUserId(testUser.getId());
        verify(studentProfileRepository, never()).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void testCreateStudentProfile_NullUser() {
        // Given
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> {
            StudentProfile profile = invocation.getArgument(0);
            profile.setId(1L);
            return profile;
        });

        // When
        StudentProfile result = studentProfileService.createStudentProfile(null, "John Doe");

        // Then
        assertNotNull(result);
        assertNull(result.getUser());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(studentProfileRepository).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should handle null name gracefully")
    void testCreateStudentProfile_NullName() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            studentProfileService.createStudentProfile(testUser, null);
        });

        verify(studentProfileRepository, never()).save(any(StudentProfile.class));
    }

    @Test
    @DisplayName("Should handle repository save exceptions")
    void testCreateStudentProfile_RepositoryException() {
        // Given
        when(studentProfileRepository.save(any(StudentProfile.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            studentProfileService.createStudentProfile(testUser, "John Doe");
        });

        verify(studentProfileRepository).save(any(StudentProfile.class));
    }
}

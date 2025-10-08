package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.TeacherStatus;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import com.ankurshala.backend.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeacherService Tests")
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @InjectMocks
    private TeacherService teacherService;

    private User testUser;
    private Teacher testTeacher;
    private TeacherProfile testTeacherProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test Teacher");
        testUser.setEmail("teacher@example.com");

        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setUser(testUser);
        testTeacher.setName("Test Teacher");
        testTeacher.setEmail("teacher@example.com");
        testTeacher.setStatus(TeacherStatus.PENDING);

        testTeacherProfile = new TeacherProfile();
        testTeacherProfile.setId(1L);
        testTeacherProfile.setUser(testUser);
        testTeacherProfile.setTeacher(testTeacher);
    }

    @Test
    @DisplayName("Should create teacher successfully")
    void testCreateTeacher_Success() {
        // Given
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherProfileRepository.save(any(TeacherProfile.class))).thenReturn(testTeacherProfile);

        // When
        Teacher result = teacherService.createTeacher(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testTeacher.getId(), result.getId());
        assertEquals(testUser, result.getUser());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(TeacherStatus.PENDING, result.getStatus());

        verify(teacherRepository).save(any(Teacher.class));
        verify(teacherProfileRepository).save(any(TeacherProfile.class));
    }

    @Test
    @DisplayName("Should create teacher with correct properties")
    void testCreateTeacher_CorrectProperties() {
        // Given
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
            Teacher teacher = invocation.getArgument(0);
            teacher.setId(1L);
            return teacher;
        });
        when(teacherProfileRepository.save(any(TeacherProfile.class))).thenAnswer(invocation -> {
            TeacherProfile profile = invocation.getArgument(0);
            profile.setId(1L);
            return profile;
        });

        // When
        Teacher result = teacherService.createTeacher(testUser);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(TeacherStatus.PENDING, result.getStatus());

        verify(teacherRepository).save(argThat(teacher -> 
            teacher.getUser().equals(testUser) &&
            teacher.getName().equals(testUser.getName()) &&
            teacher.getEmail().equals(testUser.getEmail()) &&
            teacher.getStatus().equals(TeacherStatus.PENDING)
        ));
        verify(teacherProfileRepository).save(argThat(profile ->
            profile.getUser().equals(testUser) &&
            profile.getTeacher().equals(result)
        ));
    }

    @Test
    @DisplayName("Should get teacher by user ID successfully")
    void testGetTeacherByUserId_Success() {
        // Given
        when(teacherRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testTeacher));

        // When
        Teacher result = teacherService.getTeacherByUserId(testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(testTeacher.getId(), result.getId());
        assertEquals(testUser, result.getUser());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(TeacherStatus.PENDING, result.getStatus());

        verify(teacherRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when teacher not found by user ID")
    void testGetTeacherByUserId_NotFound() {
        // Given
        when(teacherRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teacherService.getTeacherByUserId(testUser.getId());
        });

        assertEquals("Teacher not found", exception.getMessage());
        verify(teacherRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should get teacher profile by user ID successfully")
    void testGetTeacherProfileByUserId_Success() {
        // Given
        when(teacherProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testTeacherProfile));

        // When
        TeacherProfile result = teacherService.getTeacherProfileByUserId(testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(testTeacherProfile.getId(), result.getId());
        assertEquals(testUser, result.getUser());
        assertEquals(testTeacher, result.getTeacher());

        verify(teacherProfileRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should throw exception when teacher profile not found by user ID")
    void testGetTeacherProfileByUserId_NotFound() {
        // Given
        when(teacherProfileRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teacherService.getTeacherProfileByUserId(testUser.getId());
        });

        assertEquals("Teacher profile not found", exception.getMessage());
        verify(teacherProfileRepository).findByUserId(testUser.getId());
    }

    @Test
    @DisplayName("Should handle repository save exceptions")
    void testCreateTeacher_RepositoryException() {
        // Given
        when(teacherRepository.save(any(Teacher.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            teacherService.createTeacher(testUser);
        });

        verify(teacherRepository).save(any(Teacher.class));
        verify(teacherProfileRepository, never()).save(any(TeacherProfile.class));
    }

    @Test
    @DisplayName("Should handle teacher profile save exceptions")
    void testCreateTeacher_ProfileSaveException() {
        // Given
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherProfileRepository.save(any(TeacherProfile.class))).thenThrow(new RuntimeException("Profile save error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            teacherService.createTeacher(testUser);
        });

        verify(teacherRepository).save(any(Teacher.class));
        verify(teacherProfileRepository).save(any(TeacherProfile.class));
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void testCreateTeacher_NullUser() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            teacherService.createTeacher(null);
        });

        verify(teacherRepository, never()).save(any(Teacher.class));
        verify(teacherProfileRepository, never()).save(any(TeacherProfile.class));
    }

    @Test
    @DisplayName("Should handle null user ID gracefully")
    void testGetTeacherByUserId_NullUserId() {
        // When & Then
        assertThrows(Exception.class, () -> {
            teacherService.getTeacherByUserId(null);
        });

        verify(teacherRepository).findByUserId(null);
    }

    @Test
    @DisplayName("Should handle null user ID for profile gracefully")
    void testGetTeacherProfileByUserId_NullUserId() {
        // When & Then
        assertThrows(Exception.class, () -> {
            teacherService.getTeacherProfileByUserId(null);
        });

        verify(teacherProfileRepository).findByUserId(null);
    }
}

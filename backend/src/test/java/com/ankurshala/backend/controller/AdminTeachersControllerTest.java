package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.TeacherDetailDto;
import com.ankurshala.backend.dto.admin.TeacherListDto;
import com.ankurshala.backend.entity.TeacherStatus;
import com.ankurshala.backend.service.AdminTeacherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminTeachersControllerTest {

    @Mock
    private AdminTeacherService adminTeacherService;

    @InjectMocks
    private AdminTeachersController controller;

    private TeacherListDto teacherListDto;
    private TeacherDetailDto teacherDetailDto;

    @BeforeEach
    void setUp() {
        teacherListDto = new TeacherListDto();
        teacherListDto.setId(1L);
        teacherListDto.setFirstName("John");
        teacherListDto.setLastName("Doe");
        teacherListDto.setEmail("john.doe@example.com");
        teacherListDto.setStatus(TeacherStatus.ACTIVE);
        teacherListDto.setVerified(true);
        teacherListDto.setRating(java.math.BigDecimal.valueOf(4.5));
        teacherListDto.setYearsOfExperience(5);
        teacherListDto.setHourlyRate(java.math.BigDecimal.valueOf(500.0));

        teacherDetailDto = new TeacherDetailDto();
        teacherDetailDto.setId(1L);
        teacherDetailDto.setFirstName("John");
        teacherDetailDto.setLastName("Doe");
        teacherDetailDto.setEmail("john.doe@example.com");
        teacherDetailDto.setMobileNumber("+91-9876543210");
        teacherDetailDto.setStatus(TeacherStatus.ACTIVE);
        teacherDetailDto.setVerified(true);
        teacherDetailDto.setRating(java.math.BigDecimal.valueOf(4.5));
        teacherDetailDto.setYearsOfExperience(5);
        teacherDetailDto.setHourlyRate(java.math.BigDecimal.valueOf(500.0));
        teacherDetailDto.setSpecialization("Mathematics");
        teacherDetailDto.setBio("Experienced mathematics teacher");
    }

    @Test
    void testGetTeachers_Success() {
        // Setup
        List<TeacherListDto> teachers = List.of(teacherListDto);
        Page<TeacherListDto> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 10), 1);

        // Mock service
        when(adminTeacherService.getTeachersWithFilters(eq("search"), eq(true), eq(TeacherStatus.ACTIVE), 
                eq(true), any(Pageable.class))).thenReturn(teacherPage);

        // Execute
        ResponseEntity<Page<TeacherListDto>> response = controller.getTeachers(0, 10, "createdAt", "desc", 
                "search", true, TeacherStatus.ACTIVE, true);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(teacherListDto, response.getBody().getContent().get(0));
        verify(adminTeacherService).getTeachersWithFilters(eq("search"), eq(true), eq(TeacherStatus.ACTIVE), eq(true), any(Pageable.class));
    }

    @Test
    void testGetTeachers_WithDefaultParameters() {
        // Setup
        List<TeacherListDto> teachers = List.of(teacherListDto);
        Page<TeacherListDto> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 10), 1);

        // Mock service
        when(adminTeacherService.getTeachersWithFilters(isNull(), isNull(), isNull(), 
                isNull(), any(Pageable.class))).thenReturn(teacherPage);

        // Execute with default parameters
        ResponseEntity<Page<TeacherListDto>> response = controller.getTeachers(0, 10, "createdAt", "desc", 
                null, null, null, null);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminTeacherService).getTeachersWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void testGetTeachers_WithInvalidSortField() {
        // Setup
        List<TeacherListDto> teachers = List.of(teacherListDto);
        Page<TeacherListDto> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 10), 1);

        // Mock service
        when(adminTeacherService.getTeachersWithFilters(eq("search"), eq(true), eq(TeacherStatus.ACTIVE), 
                eq(true), any(Pageable.class))).thenReturn(teacherPage);

        // Execute with invalid sort field
        ResponseEntity<Page<TeacherListDto>> response = controller.getTeachers(0, 10, "invalidField", "desc", 
                "search", true, TeacherStatus.ACTIVE, true);

        // Verify that the invalid sort field is replaced with "createdAt"
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminTeacherService).getTeachersWithFilters(eq("search"), eq(true), eq(TeacherStatus.ACTIVE), eq(true), any(Pageable.class));
    }

    @Test
    void testGetTeachers_WithEmailSortField() {
        // Setup
        List<TeacherListDto> teachers = List.of(teacherListDto);
        Page<TeacherListDto> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 10), 1);

        // Mock service
        when(adminTeacherService.getTeachersWithFilters(eq("search"), eq(true), eq(TeacherStatus.ACTIVE), 
                eq(true), any(Pageable.class))).thenReturn(teacherPage);

        // Execute with email sort field
        ResponseEntity<Page<TeacherListDto>> response = controller.getTeachers(0, 10, "email", "asc", 
                "search", true, TeacherStatus.ACTIVE, true);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminTeacherService).getTeachersWithFilters(eq("search"), eq(true), eq(TeacherStatus.ACTIVE), eq(true), any(Pageable.class));
    }

    @Test
    void testGetTeacher_Success() {
        // Mock service
        when(adminTeacherService.getTeacherById(1L)).thenReturn(Optional.of(teacherDetailDto));

        // Execute
        ResponseEntity<TeacherDetailDto> response = controller.getTeacher(1L);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(teacherDetailDto, response.getBody());
        verify(adminTeacherService).getTeacherById(1L);
    }

    @Test
    void testGetTeacher_NotFound() {
        // Mock service
        when(adminTeacherService.getTeacherById(1L)).thenReturn(Optional.empty());

        // Execute
        ResponseEntity<TeacherDetailDto> response = controller.getTeacher(1L);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(adminTeacherService).getTeacherById(1L);
    }

    @Test
    void testUpdateTeacher_Success() {
        // Setup
        TeacherDetailDto updatedTeacher = new TeacherDetailDto();
        updatedTeacher.setId(1L);
        updatedTeacher.setFirstName("Jane");
        updatedTeacher.setLastName("Smith");
        updatedTeacher.setEmail("jane.smith@example.com");

        // Mock service
        when(adminTeacherService.updateTeacher(1L, teacherDetailDto)).thenReturn(updatedTeacher);

        // Execute
        ResponseEntity<TeacherDetailDto> response = controller.updateTeacher(1L, teacherDetailDto);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedTeacher, response.getBody());
        verify(adminTeacherService).updateTeacher(1L, teacherDetailDto);
    }

    @Test
    void testUpdateTeacher_NotFound() {
        // Mock service to throw exception
        when(adminTeacherService.updateTeacher(1L, teacherDetailDto))
                .thenThrow(new RuntimeException("Teacher not found"));

        // Execute
        ResponseEntity<TeacherDetailDto> response = controller.updateTeacher(1L, teacherDetailDto);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(adminTeacherService).updateTeacher(1L, teacherDetailDto);
    }

    @Test
    void testToggleTeacherStatus_Success() {
        // Mock service
        when(adminTeacherService.toggleTeacherStatus(1L)).thenReturn(false);

        // Execute
        ResponseEntity<Map<String, Object>> response = controller.toggleTeacherStatus(1L);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().get("id"));
        assertEquals(false, response.getBody().get("enabled"));
        assertEquals("Teacher status updated successfully", response.getBody().get("message"));
        verify(adminTeacherService).toggleTeacherStatus(1L);
    }

    @Test
    void testToggleTeacherStatus_NotFound() {
        // Mock service to throw exception
        when(adminTeacherService.toggleTeacherStatus(1L))
                .thenThrow(new RuntimeException("Teacher not found"));

        // Execute
        ResponseEntity<Map<String, Object>> response = controller.toggleTeacherStatus(1L);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(adminTeacherService).toggleTeacherStatus(1L);
    }

    @Test
    void testValidateSortField_ValidField() {
        // This tests the private method indirectly through the public method
        List<TeacherListDto> teachers = List.of(teacherListDto);
        Page<TeacherListDto> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 10), 1);

        when(adminTeacherService.getTeachersWithFilters(isNull(), isNull(), isNull(), 
                isNull(), any(Pageable.class))).thenReturn(teacherPage);

        // Execute with valid sort field
        ResponseEntity<Page<TeacherListDto>> response = controller.getTeachers(0, 10, "firstName", "asc", 
                null, null, null, null);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminTeacherService).getTeachersWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void testValidateSortField_NullField() {
        // This tests the private method indirectly through the public method
        List<TeacherListDto> teachers = List.of(teacherListDto);
        Page<TeacherListDto> teacherPage = new PageImpl<>(teachers, PageRequest.of(0, 10), 1);

        when(adminTeacherService.getTeachersWithFilters(isNull(), isNull(), isNull(), 
                isNull(), any(Pageable.class))).thenReturn(teacherPage);

        // Execute with null sort field
        ResponseEntity<Page<TeacherListDto>> response = controller.getTeachers(0, 10, null, "asc", 
                null, null, null, null);

        // Verify that null sort field defaults to "createdAt"
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminTeacherService).getTeachersWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }
}

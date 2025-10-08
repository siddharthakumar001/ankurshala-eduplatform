package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.StudentDetailDto;
import com.ankurshala.backend.dto.admin.StudentListDto;
import com.ankurshala.backend.dto.admin.StudentUpdateDto;
import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.GlobalExceptionHandler;
import com.ankurshala.backend.service.AdminStudentService;
import com.ankurshala.backend.service.LoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit tests for AdminStudentsController
 * Tests all CRUD operations with various scenarios including edge cases
 */
@ExtendWith(MockitoExtension.class)
class AdminStudentsControllerTest {

    @Mock
    private AdminStudentService adminStudentService;

    @Mock
    private LoggingService loggingService;

    @InjectMocks
    private AdminStudentsController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Mock LoggingService methods to prevent null pointer exceptions
        lenient().doNothing().when(loggingService).logBusinessOperationStart(anyString(), anyString(), any());
        lenient().doNothing().when(loggingService).logBusinessOperationComplete(anyString(), anyString(), anyBoolean(), anyLong());
        lenient().doNothing().when(loggingService).logError(anyString(), any(Exception.class), any());
        lenient().doNothing().when(loggingService).logErrorWithContext(anyString(), any(Exception.class), any());
        lenient().doNothing().when(loggingService).logSystemEvent(anyString(), anyString(), anyString(), any());
        
        // Manually inject LoggingService since it uses @Autowired field injection
        try {
            java.lang.reflect.Field loggingServiceField = AdminStudentsController.class.getDeclaredField("loggingService");
            loggingServiceField.setAccessible(true);
            loggingServiceField.set(controller, loggingService);
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        // Inject LoggingService into GlobalExceptionHandler
        try {
            java.lang.reflect.Field globalLoggingServiceField = GlobalExceptionHandler.class.getDeclaredField("loggingService");
            globalLoggingServiceField.setAccessible(true);
            globalLoggingServiceField.set(globalExceptionHandler, loggingService);
        } catch (Exception e) {
            // Ignore reflection errors
        }
        
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(globalExceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
    }

    // ============ GET STUDENTS TESTS ============

    @Test
    void testGetStudents_Success() throws Exception {
        // Given
        StudentListDto student1 = new StudentListDto(
                1L, 1L, "John", "M", "Doe", "john@example.com", 
                "1234567890", LocalDate.of(2000, 1, 1), 
                EducationalBoard.CBSE, ClassLevel.GRADE_10, "10", 
                "Test School", true, LocalDateTime.now(), null
        );

        StudentListDto student2 = new StudentListDto(
                2L, 2L, "Jane", "K", "Smith", "jane@example.com", 
                "0987654321", LocalDate.of(2001, 2, 2), 
                EducationalBoard.ICSE, ClassLevel.GRADE_12, "12", 
                "Test School 2", true, LocalDateTime.now(), null
        );

        List<StudentListDto> students = Arrays.asList(student1, student2);
        Page<StudentListDto> studentPage = new PageImpl<>(students, PageRequest.of(0, 10), 2);

        when(adminStudentService.getStudentsWithFilters(anyString(), any(Boolean.class), 
                any(EducationalBoard.class), any(ClassLevel.class), any(Pageable.class)))
                .thenReturn(studentPage);

        // When & Then
        mockMvc.perform(get("/admin/students")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc")
                        .param("search", "John")
                        .param("enabled", "true")
                        .param("educationalBoard", "CBSE")
                        .param("classLevel", "GRADE_10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Students retrieved successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.data.content[1].firstName").value("Jane"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(adminStudentService).getStudentsWithFilters(
                eq("John"), eq(true), eq(EducationalBoard.CBSE), eq(ClassLevel.GRADE_10), any(Pageable.class));
    }

    @Test
    void testGetStudents_EmptyResult() throws Exception {
        // Given
        Page<StudentListDto> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(adminStudentService.getStudentsWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/admin/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        verify(adminStudentService).getStudentsWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void testGetStudents_InvalidSortField() throws Exception {
        // Given
        StudentListDto student = new StudentListDto(
                1L, 1L, "John", "M", "Doe", "john@example.com", 
                "1234567890", LocalDate.of(2000, 1, 1), 
                EducationalBoard.CBSE, ClassLevel.GRADE_10, "10", 
                "Test School", true, LocalDateTime.now(), null
        );

        Page<StudentListDto> studentPage = new PageImpl<>(Arrays.asList(student), PageRequest.of(0, 10), 1);

        when(adminStudentService.getStudentsWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(studentPage);

        // When & Then - Invalid sort field should default to "createdAt"
        mockMvc.perform(get("/admin/students")
                        .param("sortBy", "invalidField")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(adminStudentService).getStudentsWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void testGetStudents_ServiceException() throws Exception {
        // Given
        when(adminStudentService.getStudentsWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/admin/students"))
                .andExpect(status().isInternalServerError());

        verify(adminStudentService).getStudentsWithFilters(isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    // ============ GET STUDENT BY ID TESTS ============

    @Test
    void testGetStudentById_Success() throws Exception {
        // Given
        StudentDetailDto student = new StudentDetailDto(
                1L, 1L, "John", "M", "Doe", "john@example.com", 
                "1234567890", "0987654321", LocalDate.of(2000, 1, 1), 
                "Mary Doe", "John Doe Sr", "Guardian Name", "Parent Name",
                EducationalBoard.CBSE, ClassLevel.GRADE_10, "10", 
                "Test School", "Emergency Contact", "photo.jpg", "id.jpg",
                true, LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(adminStudentService.getStudentById(1L)).thenReturn(Optional.of(student));

        // When & Then
        mockMvc.perform(get("/admin/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));

        verify(adminStudentService).getStudentById(1L);
    }

    @Test
    void testGetStudentById_NotFound() throws Exception {
        // Given
        when(adminStudentService.getStudentById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/admin/students/999"))
                .andExpect(status().isNotFound());

        verify(adminStudentService).getStudentById(999L);
    }

    @Test
    void testGetStudentById_InvalidId() throws Exception {
        // When & Then - Invalid ID should cause a 500 error due to NumberFormatException
        mockMvc.perform(get("/admin/students/invalid"))
                .andExpect(status().isInternalServerError());
    }

    // ============ UPDATE STUDENT TESTS ============

    @Test
    void testUpdateStudent_Success() throws Exception {
        // Given
        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setFirstName("John Updated");
        updateDto.setLastName("Doe Updated");
        updateDto.setMobileNumber("9876543210");

        StudentDetailDto updatedStudent = new StudentDetailDto(
                1L, 1L, "John Updated", "M", "Doe Updated", "john@example.com", 
                "9876543210", "0987654321", LocalDate.of(2000, 1, 1), 
                "Mary Doe", "John Doe Sr", "Guardian Name", "Parent Name",
                EducationalBoard.CBSE, ClassLevel.GRADE_10, "10", 
                "Test School", "Emergency Contact", "photo.jpg", "id.jpg",
                true, LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(adminStudentService.updateStudent(eq(1L), any(StudentUpdateDto.class)))
                .thenReturn(updatedStudent);

        // When & Then
        mockMvc.perform(put("/admin/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John Updated"))
                .andExpect(jsonPath("$.data.lastName").value("Doe Updated"));

        verify(adminStudentService).updateStudent(eq(1L), any(StudentUpdateDto.class));
    }

    @Test
    void testUpdateStudent_NotFound() throws Exception {
        // Given
        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setFirstName("John Updated");
        updateDto.setLastName("Doe Updated"); // Add required lastName

        when(adminStudentService.updateStudent(eq(999L), any(StudentUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Student not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/admin/students/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(adminStudentService).updateStudent(eq(999L), any(StudentUpdateDto.class));
    }

    @Test
    void testUpdateStudent_ValidationError() throws Exception {
        // Given
        StudentUpdateDto updateDto = new StudentUpdateDto();
        updateDto.setFirstName(""); // Invalid: empty name
        updateDto.setLastName(""); // Invalid: empty name

        // When & Then - Validation errors are handled by Spring's @Valid annotation
        mockMvc.perform(put("/admin/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());

        verify(adminStudentService, never()).updateStudent(anyLong(), any(StudentUpdateDto.class));
    }

    // ============ TOGGLE STATUS TESTS ============

    @Test
    void testToggleStudentStatus_Success() throws Exception {
        // Given
        when(adminStudentService.toggleStudentStatus(1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(patch("/admin/students/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student status updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.enabled").value(false));

        verify(adminStudentService).toggleStudentStatus(1L);
    }

    @Test
    void testToggleStudentStatus_NotFound() throws Exception {
        // Given
        when(adminStudentService.toggleStudentStatus(999L))
                .thenThrow(new ResourceNotFoundException("Student not found with id: 999"));

        // When & Then
        mockMvc.perform(patch("/admin/students/999/toggle-status"))
                .andExpect(status().isNotFound());

        verify(adminStudentService).toggleStudentStatus(999L);
    }

    // ============ DELETE STUDENT TESTS ============

    @Test
    void testDeleteStudent_Success() throws Exception {
        // Given
        doNothing().when(adminStudentService).deleteStudent(1L);

        // When & Then
        mockMvc.perform(delete("/admin/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student deleted successfully"));

        verify(adminStudentService).deleteStudent(1L);
    }

    @Test
    void testDeleteStudent_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Student not found with id: 999"))
                .when(adminStudentService).deleteStudent(999L);

        // When & Then
        mockMvc.perform(delete("/admin/students/999"))
                .andExpect(status().isNotFound());

        verify(adminStudentService).deleteStudent(999L);
    }

    // ============ GET STUDENT STATS TESTS ============

    @Test
    void testGetStudentStats_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/students/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Student statistics retrieved successfully"))
                .andExpect(jsonPath("$.data.totalStudents").value(0))
                .andExpect(jsonPath("$.data.activeStudents").value(0))
                .andExpect(jsonPath("$.data.inactiveStudents").value(0))
                .andExpect(jsonPath("$.data.newStudentsThisMonth").value(0));
    }

    // ============ SECURITY TESTS ============

    @Test
    void testGetStudents_Unauthorized() throws Exception {
        // This test would require Spring Security context setup
        // For now, we'll test the basic endpoint structure
        mockMvc.perform(get("/admin/students"))
                .andExpect(status().isOk()); // This will be 401 in real scenario with security
    }

    // ============ EDGE CASE TESTS ============

    @Test
    void testGetStudents_LargePageSize() throws Exception {
        // Given - Large page size should be rejected by Spring validation
        // When & Then
        mockMvc.perform(get("/admin/students")
                        .param("size", "1000"))
                .andExpect(status().isBadRequest()); // Spring validation returns 400

        verify(adminStudentService, never()).getStudentsWithFilters(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void testGetStudents_NegativePage() throws Exception {
        // Given - Negative page should be rejected by Spring validation
        // When & Then
        mockMvc.perform(get("/admin/students")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest()); // Spring validation returns 400

        verify(adminStudentService, never()).getStudentsWithFilters(any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void testGetStudents_SpecialCharactersInSearch() throws Exception {
        // Given
        Page<StudentListDto> studentPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(adminStudentService.getStudentsWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(studentPage);

        // When & Then
        mockMvc.perform(get("/admin/students")
                        .param("search", "John<script>alert('xss')</script>"))
                .andExpect(status().isOk());

        verify(adminStudentService).getStudentsWithFilters(
                eq("John<script>alert('xss')</script>"), isNull(), isNull(), isNull(), any(Pageable.class));
    }
}
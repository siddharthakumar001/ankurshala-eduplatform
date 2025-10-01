package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.StudentDetailDto;
import com.ankurshala.backend.dto.admin.StudentListDto;
import com.ankurshala.backend.dto.admin.StudentUpdateDto;
import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.service.AdminStudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminStudentsController.class)
public class AdminStudentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminStudentService adminStudentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetStudents() throws Exception {
        // Mock data
        StudentListDto student1 = new StudentListDto(
                1L, 1L, "John", "M", "Doe", "john@example.com", "1234567890",
                LocalDate.of(2000, 1, 1), EducationalBoard.CBSE, ClassLevel.GRADE_10,
                "10A", "Test School", true, LocalDateTime.now(), null
        );

        StudentListDto student2 = new StudentListDto(
                2L, 2L, "Jane", null, "Smith", "jane@example.com", "0987654321",
                LocalDate.of(2001, 2, 2), EducationalBoard.ICSE, ClassLevel.GRADE_9,
                "9B", "Another School", false, LocalDateTime.now(), null
        );

        Page<StudentListDto> studentsPage = new PageImpl<>(
                Arrays.asList(student1, student2),
                PageRequest.of(0, 10, Sort.by("createdAt").descending()),
                2
        );

        when(adminStudentService.getStudentsWithFilters(
                any(), any(), any(), any(), any()
        )).thenReturn(studentsPage);

        // Test
        mockMvc.perform(get("/admin/students")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"))
                .andExpect(jsonPath("$.content[1].firstName").value("Jane"))
                .andExpect(jsonPath("$.content[1].lastName").value("Smith"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetStudentsWithFilters() throws Exception {
        Page<StudentListDto> emptyPage = new PageImpl<>(
                Arrays.asList(),
                PageRequest.of(0, 10, Sort.by("createdAt").descending()),
                0
        );

        when(adminStudentService.getStudentsWithFilters(
                eq("john"), eq(true), eq(EducationalBoard.CBSE), eq(ClassLevel.GRADE_10), any()
        )).thenReturn(emptyPage);

        mockMvc.perform(get("/admin/students")
                        .param("search", "john")
                        .param("enabled", "true")
                        .param("educationalBoard", "CBSE")
                        .param("classLevel", "GRADE_10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetStudentById() throws Exception {
        StudentDetailDto student = new StudentDetailDto(
                1L, 1L, "John", "M", "Doe", "john@example.com", "1234567890", "0987654321",
                LocalDate.of(2000, 1, 1), "Mother Name", "Father Name", "Guardian Name",
                "Parent Name", EducationalBoard.CBSE, ClassLevel.GRADE_10, "10A", "Test School",
                "Emergency Contact", null, null, true, LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(adminStudentService.getStudentById(1L)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/admin/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.educationalBoard").value("CBSE"))
                .andExpect(jsonPath("$.classLevel").value("GRADE_10"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetStudentByIdNotFound() throws Exception {
        when(adminStudentService.getStudentById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/students/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testUpdateStudent() throws Exception {
        StudentUpdateDto updateDto = new StudentUpdateDto(
                "Updated John", "M", "Updated Doe", "1111111111", "2222222222",
                LocalDate.of(2000, 1, 1), "Updated Mother", "Updated Father",
                "Updated Guardian", "Updated Parent", EducationalBoard.ICSE,
                ClassLevel.GRADE_11, "11A", "Updated School", "Updated Emergency",
                null, null, true
        );

        StudentDetailDto updatedStudent = new StudentDetailDto(
                1L, 1L, "Updated John", "M", "Updated Doe", "john@example.com", "1111111111", "2222222222",
                LocalDate.of(2000, 1, 1), "Updated Mother", "Updated Father", "Updated Guardian",
                "Updated Parent", EducationalBoard.ICSE, ClassLevel.GRADE_11, "11A", "Updated School",
                "Updated Emergency", null, null, true, LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(adminStudentService.updateStudent(eq(1L), any(StudentUpdateDto.class)))
                .thenReturn(updatedStudent);

        mockMvc.perform(put("/admin/students/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated John"))
                .andExpect(jsonPath("$.lastName").value("Updated Doe"))
                .andExpect(jsonPath("$.educationalBoard").value("ICSE"))
                .andExpect(jsonPath("$.classLevel").value("GRADE_11"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testToggleStudentStatus() throws Exception {
        when(adminStudentService.toggleStudentStatus(1L)).thenReturn(false);

        mockMvc.perform(patch("/admin/students/1/toggle-status")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.message").value("Student status updated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeleteStudent() throws Exception {
        mockMvc.perform(delete("/admin/students/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Student deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetStudentStats() throws Exception {
        mockMvc.perform(get("/admin/students/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").exists())
                .andExpect(jsonPath("$.activeStudents").exists())
                .andExpect(jsonPath("$.inactiveStudents").exists())
                .andExpect(jsonPath("$.newStudentsThisMonth").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/admin/students"))
                .andExpect(status().isUnauthorized());
    }
}

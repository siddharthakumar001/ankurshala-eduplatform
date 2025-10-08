package com.ankurshala.backend.dto.teacher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TeacherExperienceDto Tests")
class TeacherExperienceDtoTest {

    @Test
    @DisplayName("Should create TeacherExperienceDto with no-args constructor")
    void testNoArgsConstructor() {
        TeacherExperienceDto dto = new TeacherExperienceDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getInstitution());
        assertNull(dto.getRole());
        assertNull(dto.getSubjectsTaught());
        assertNull(dto.getFromDate());
        assertNull(dto.getToDate());
        assertFalse(dto.getCurrentlyWorking());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        LocalDate fromDate = LocalDate.of(2020, 1, 1);
        LocalDate toDate = LocalDate.of(2023, 12, 31);
        
        TeacherExperienceDto dto = new TeacherExperienceDto();
        
        dto.setId(1L);
        dto.setInstitution("ABC School");
        dto.setRole("Mathematics Teacher");
        dto.setSubjectsTaught("Algebra, Geometry, Calculus");
        dto.setFromDate(fromDate);
        dto.setToDate(toDate);
        dto.setCurrentlyWorking(false);
        
        assertEquals(1L, dto.getId());
        assertEquals("ABC School", dto.getInstitution());
        assertEquals("Mathematics Teacher", dto.getRole());
        assertEquals("Algebra, Geometry, Calculus", dto.getSubjectsTaught());
        assertEquals(fromDate, dto.getFromDate());
        assertEquals(toDate, dto.getToDate());
        assertFalse(dto.getCurrentlyWorking());
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void testNullValues() {
        TeacherExperienceDto dto = new TeacherExperienceDto();
        
        dto.setInstitution(null);
        dto.setRole(null);
        dto.setSubjectsTaught(null);
        dto.setFromDate(null);
        dto.setToDate(null);
        dto.setCurrentlyWorking(null);
        
        assertNull(dto.getInstitution());
        assertNull(dto.getRole());
        assertNull(dto.getSubjectsTaught());
        assertNull(dto.getFromDate());
        assertNull(dto.getToDate());
        assertNull(dto.getCurrentlyWorking());
    }
}

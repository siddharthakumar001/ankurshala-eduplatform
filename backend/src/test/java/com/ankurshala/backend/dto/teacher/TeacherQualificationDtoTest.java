package com.ankurshala.backend.dto.teacher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TeacherQualificationDto Tests")
class TeacherQualificationDtoTest {

    @Test
    @DisplayName("Should create TeacherQualificationDto with no-args constructor")
    void testNoArgsConstructor() {
        TeacherQualificationDto dto = new TeacherQualificationDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getDegree());
        assertNull(dto.getSpecialization());
        assertNull(dto.getUniversity());
        assertNull(dto.getYear());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        TeacherQualificationDto dto = new TeacherQualificationDto();
        Long id = 1L;
        String degree = "M.Sc Mathematics";
        String specialization = "Applied Mathematics";
        String university = "University of Delhi";
        Integer year = 2020;

        dto.setId(id);
        dto.setDegree(degree);
        dto.setSpecialization(specialization);
        dto.setUniversity(university);
        dto.setYear(year);

        assertEquals(id, dto.getId());
        assertEquals(degree, dto.getDegree());
        assertEquals(specialization, dto.getSpecialization());
        assertEquals(university, dto.getUniversity());
        assertEquals(year, dto.getYear());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        TeacherQualificationDto dto = new TeacherQualificationDto();
        dto.setId(null);
        dto.setDegree(null);
        dto.setSpecialization(null);
        dto.setUniversity(null);
        dto.setYear(null);

        assertNull(dto.getId());
        assertNull(dto.getDegree());
        assertNull(dto.getSpecialization());
        assertNull(dto.getUniversity());
        assertNull(dto.getYear());
    }
}

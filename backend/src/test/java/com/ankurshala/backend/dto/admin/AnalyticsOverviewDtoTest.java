package com.ankurshala.backend.dto.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnalyticsOverviewDto Tests")
class AnalyticsOverviewDtoTest {

    @Test
    @DisplayName("Should create AnalyticsOverviewDto with no-args constructor")
    void testNoArgsConstructor() {
        AnalyticsOverviewDto dto = new AnalyticsOverviewDto();
        assertNotNull(dto);
        assertEquals(0, dto.getTotalStudents());
        assertEquals(0, dto.getTotalTeachers());
        assertEquals(0, dto.getActiveStudents());
        assertEquals(0, dto.getActiveTeachers());
        assertEquals(0, dto.getTotalBoards());
        assertEquals(0, dto.getTotalSubjects());
        assertEquals(0, dto.getTotalChapters());
        assertEquals(0, dto.getTotalTopics());
        assertEquals(0, dto.getTotalImports());
        assertEquals(0, dto.getSuccessfulImports());
        assertEquals(0, dto.getFailedImports());
        assertEquals(0, dto.getNewStudents());
        assertEquals(0, dto.getNewTeachers());
    }

    @Test
    @DisplayName("Should create AnalyticsOverviewDto with all-args constructor")
    void testAllArgsConstructor() {
        AnalyticsOverviewDto dto = new AnalyticsOverviewDto(
            100L, 50L, 80L, 40L,
            5L, 20L, 100L, 500L,
            10L, 8L, 2L,
            15L, 5L
        );
        
        assertEquals(100L, dto.getTotalStudents());
        assertEquals(50L, dto.getTotalTeachers());
        assertEquals(80L, dto.getActiveStudents());
        assertEquals(40L, dto.getActiveTeachers());
        assertEquals(5L, dto.getTotalBoards());
        assertEquals(20L, dto.getTotalSubjects());
        assertEquals(100L, dto.getTotalChapters());
        assertEquals(500L, dto.getTotalTopics());
        assertEquals(10L, dto.getTotalImports());
        assertEquals(8L, dto.getSuccessfulImports());
        assertEquals(2L, dto.getFailedImports());
        assertEquals(15L, dto.getNewStudents());
        assertEquals(5L, dto.getNewTeachers());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        AnalyticsOverviewDto dto = new AnalyticsOverviewDto();
        
        dto.setTotalStudents(200L);
        dto.setTotalTeachers(100L);
        dto.setActiveStudents(150L);
        dto.setActiveTeachers(80L);
        dto.setTotalBoards(10L);
        dto.setTotalSubjects(40L);
        dto.setTotalChapters(200L);
        dto.setTotalTopics(1000L);
        dto.setTotalImports(20L);
        dto.setSuccessfulImports(18L);
        dto.setFailedImports(2L);
        dto.setNewStudents(30L);
        dto.setNewTeachers(10L);
        
        assertEquals(200L, dto.getTotalStudents());
        assertEquals(100L, dto.getTotalTeachers());
        assertEquals(150L, dto.getActiveStudents());
        assertEquals(80L, dto.getActiveTeachers());
        assertEquals(10L, dto.getTotalBoards());
        assertEquals(40L, dto.getTotalSubjects());
        assertEquals(200L, dto.getTotalChapters());
        assertEquals(1000L, dto.getTotalTopics());
        assertEquals(20L, dto.getTotalImports());
        assertEquals(18L, dto.getSuccessfulImports());
        assertEquals(2L, dto.getFailedImports());
        assertEquals(30L, dto.getNewStudents());
        assertEquals(10L, dto.getNewTeachers());
    }
}

package com.ankurshala.backend.dto.teacher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TeacherAvailabilityDto Tests")
class TeacherAvailabilityDtoTest {

    @Test
    @DisplayName("Should create TeacherAvailabilityDto with no-args constructor")
    void testNoArgsConstructor() {
        TeacherAvailabilityDto dto = new TeacherAvailabilityDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getAvailableFrom());
        assertNull(dto.getAvailableTo());
        assertNull(dto.getPreferredStudentLevels());
        assertNull(dto.getLanguagesSpoken());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        LocalTime fromTime = LocalTime.of(9, 0);
        LocalTime toTime = LocalTime.of(17, 0);
        
        TeacherAvailabilityDto dto = new TeacherAvailabilityDto();
        
        dto.setId(1L);
        dto.setAvailableFrom(fromTime);
        dto.setAvailableTo(toTime);
        dto.setPreferredStudentLevels("Grade 9-12");
        dto.setLanguagesSpoken("English, Hindi");
        
        assertEquals(1L, dto.getId());
        assertEquals(fromTime, dto.getAvailableFrom());
        assertEquals(toTime, dto.getAvailableTo());
        assertEquals("Grade 9-12", dto.getPreferredStudentLevels());
        assertEquals("English, Hindi", dto.getLanguagesSpoken());
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void testNullValues() {
        TeacherAvailabilityDto dto = new TeacherAvailabilityDto();
        
        dto.setAvailableFrom(null);
        dto.setAvailableTo(null);
        dto.setPreferredStudentLevels(null);
        dto.setLanguagesSpoken(null);
        
        assertNull(dto.getAvailableFrom());
        assertNull(dto.getAvailableTo());
        assertNull(dto.getPreferredStudentLevels());
        assertNull(dto.getLanguagesSpoken());
    }
}

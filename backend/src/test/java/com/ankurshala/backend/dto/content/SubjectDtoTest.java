package com.ankurshala.backend.dto.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SubjectDto Tests")
class SubjectDtoTest {

    @Test
    @DisplayName("Should create SubjectDto with no-args constructor")
    void testNoArgsConstructor() {
        SubjectDto dto = new SubjectDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getActive());
        assertNull(dto.getBoardId());
        assertNull(dto.getGradeId());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create SubjectDto with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        SubjectDto dto = new SubjectDto(1L, "Mathematics", true, 1L, 1L, now, now);
        
        assertEquals(1L, dto.getId());
        assertEquals("Mathematics", dto.getName());
        assertTrue(dto.getActive());
        assertEquals(1L, dto.getBoardId());
        assertEquals(1L, dto.getGradeId());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        SubjectDto dto = new SubjectDto();
        
        dto.setId(2L);
        dto.setName("Physics");
        dto.setActive(false);
        dto.setBoardId(2L);
        dto.setGradeId(2L);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        
        assertEquals(2L, dto.getId());
        assertEquals("Physics", dto.getName());
        assertFalse(dto.getActive());
        assertEquals(2L, dto.getBoardId());
        assertEquals(2L, dto.getGradeId());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should generate toString")
    void testToString() {
        SubjectDto dto = new SubjectDto(1L, "Mathematics", true, 1L, 1L, LocalDateTime.now(), LocalDateTime.now());
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Mathematics"));
    }

    @Test
    @DisplayName("Should generate equals and hashCode")
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        SubjectDto dto1 = new SubjectDto(1L, "Mathematics", true, 1L, 1L, now, now);
        SubjectDto dto2 = new SubjectDto(1L, "Mathematics", true, 1L, 1L, now, now);
        SubjectDto dto3 = new SubjectDto(2L, "Physics", false, 2L, 2L, now, now);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
}

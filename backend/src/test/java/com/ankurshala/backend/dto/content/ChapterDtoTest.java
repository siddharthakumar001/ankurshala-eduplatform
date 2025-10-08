package com.ankurshala.backend.dto.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChapterDto Tests")
class ChapterDtoTest {

    @Test
    @DisplayName("Should create ChapterDto with no-args constructor")
    void testNoArgsConstructor() {
        ChapterDto dto = new ChapterDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getSubjectId());
        assertNull(dto.getSubjectName());
        assertNull(dto.getBoardId());
        assertNull(dto.getActive());
        assertNull(dto.getDeletedAt());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create ChapterDto with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ChapterDto dto = new ChapterDto(1L, "Algebra", 1L, "Mathematics", 1L, true, now, now, now);
        
        assertEquals(1L, dto.getId());
        assertEquals("Algebra", dto.getName());
        assertEquals(1L, dto.getSubjectId());
        assertEquals("Mathematics", dto.getSubjectName());
        assertEquals(1L, dto.getBoardId());
        assertTrue(dto.getActive());
        assertEquals(now, dto.getDeletedAt());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        ChapterDto dto = new ChapterDto();
        
        dto.setId(2L);
        dto.setName("Geometry");
        dto.setSubjectId(2L);
        dto.setSubjectName("Physics");
        dto.setBoardId(2L);
        dto.setActive(false);
        dto.setDeletedAt(now);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        
        assertEquals(2L, dto.getId());
        assertEquals("Geometry", dto.getName());
        assertEquals(2L, dto.getSubjectId());
        assertEquals("Physics", dto.getSubjectName());
        assertEquals(2L, dto.getBoardId());
        assertFalse(dto.getActive());
        assertEquals(now, dto.getDeletedAt());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should generate toString")
    void testToString() {
        ChapterDto dto = new ChapterDto(1L, "Algebra", 1L, "Mathematics", 1L, true, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Algebra"));
    }

    @Test
    @DisplayName("Should generate equals and hashCode")
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        ChapterDto dto1 = new ChapterDto(1L, "Algebra", 1L, "Mathematics", 1L, true, now, now, now);
        ChapterDto dto2 = new ChapterDto(1L, "Algebra", 1L, "Mathematics", 1L, true, now, now, now);
        ChapterDto dto3 = new ChapterDto(2L, "Geometry", 2L, "Physics", 2L, false, now, now, now);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
}

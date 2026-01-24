package com.ankurshala.backend.dto.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TopicDto Tests")
class TopicDtoTest {

    @Test
    @DisplayName("Should create TopicDto with no-args constructor")
    void testNoArgsConstructor() {
        TopicDto dto = new TopicDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getTitle());
        assertNull(dto.getCode());
        assertNull(dto.getDescription());
        assertNull(dto.getSummary());
        assertNull(dto.getExpectedTimeMins());
        assertNull(dto.getChapterId());
        assertNull(dto.getChapterName());
        assertNull(dto.getSubjectName());
        assertNull(dto.getBoardId());
        assertNull(dto.getSubjectId());
        assertNull(dto.getActive());
        assertNull(dto.getDeletedAt());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create TopicDto with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        TopicDto dto = new TopicDto(1L, "Linear Equations", "LE001", "Introduction to linear equations",
            "Basic concepts", "Suggested topics", 60, 1L, "Algebra", "Mathematics", 1L, 1L, true, now, now, now);
        
        assertEquals(1L, dto.getId());
        assertEquals("Linear Equations", dto.getTitle());
        assertEquals("LE001", dto.getCode());
        assertEquals("Introduction to linear equations", dto.getDescription());
        assertEquals("Basic concepts", dto.getSummary());
        assertEquals(60, dto.getExpectedTimeMins());
        assertEquals(1L, dto.getChapterId());
        assertEquals("Algebra", dto.getChapterName());
        assertEquals("Mathematics", dto.getSubjectName());
        assertEquals(1L, dto.getBoardId());
        assertEquals(1L, dto.getSubjectId());
        assertTrue(dto.getActive());
        assertEquals(now, dto.getDeletedAt());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        TopicDto dto = new TopicDto();
        
        dto.setId(2L);
        dto.setTitle("Quadratic Equations");
        dto.setCode("QE001");
        dto.setDescription("Introduction to quadratic equations");
        dto.setSummary("Advanced concepts");
        dto.setExpectedTimeMins(90);
        dto.setChapterId(2L);
        dto.setChapterName("Geometry");
        dto.setSubjectName("Physics");
        dto.setBoardId(2L);
        dto.setSubjectId(2L);
        dto.setActive(false);
        dto.setDeletedAt(now);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        
        assertEquals(2L, dto.getId());
        assertEquals("Quadratic Equations", dto.getTitle());
        assertEquals("QE001", dto.getCode());
        assertEquals("Introduction to quadratic equations", dto.getDescription());
        assertEquals("Advanced concepts", dto.getSummary());
        assertEquals(90, dto.getExpectedTimeMins());
        assertEquals(2L, dto.getChapterId());
        assertEquals("Geometry", dto.getChapterName());
        assertEquals("Physics", dto.getSubjectName());
        assertEquals(2L, dto.getBoardId());
        assertEquals(2L, dto.getSubjectId());
        assertFalse(dto.getActive());
        assertEquals(now, dto.getDeletedAt());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should generate toString")
    void testToString() {
        TopicDto dto = new TopicDto(1L, "Linear Equations", "LE001", "Introduction to linear equations",
            "Basic concepts", "Suggested topics", 60, 1L, "Algebra", "Mathematics", 1L, 1L, true,
            LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Linear Equations"));
    }

    @Test
    @DisplayName("Should generate equals and hashCode")
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        TopicDto dto1 = new TopicDto(1L, "Linear Equations", "LE001", "Introduction to linear equations",
            "Basic concepts", "Suggested topics", 60, 1L, "Algebra", "Mathematics", 1L, 1L, true, now, now, now);
        TopicDto dto2 = new TopicDto(1L, "Linear Equations", "LE001", "Introduction to linear equations",
            "Basic concepts", "Suggested topics", 60, 1L, "Algebra", "Mathematics", 1L, 1L, true, now, now, now);
        TopicDto dto3 = new TopicDto(2L, "Quadratic Equations", "QE001", "Introduction to quadratic equations",
            "Advanced concepts", "Suggested topics", 90, 2L, "Geometry", "Physics", 2L, 2L, false, now, now, now);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
}

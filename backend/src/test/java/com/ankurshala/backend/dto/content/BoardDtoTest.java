package com.ankurshala.backend.dto.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BoardDto Tests")
class BoardDtoTest {

    @Test
    @DisplayName("Should create BoardDto with no-args constructor")
    void testNoArgsConstructor() {
        BoardDto dto = new BoardDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getActive());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create BoardDto with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        BoardDto dto = new BoardDto(1L, "CBSE", true, now, now);
        
        assertEquals(1L, dto.getId());
        assertEquals("CBSE", dto.getName());
        assertTrue(dto.getActive());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        BoardDto dto = new BoardDto();
        
        dto.setId(2L);
        dto.setName("ICSE");
        dto.setActive(false);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        
        assertEquals(2L, dto.getId());
        assertEquals("ICSE", dto.getName());
        assertFalse(dto.getActive());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should generate toString")
    void testToString() {
        BoardDto dto = new BoardDto(1L, "CBSE", true, LocalDateTime.now(), LocalDateTime.now());
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("CBSE"));
    }

    @Test
    @DisplayName("Should generate equals and hashCode")
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        BoardDto dto1 = new BoardDto(1L, "CBSE", true, now, now);
        BoardDto dto2 = new BoardDto(1L, "CBSE", true, now, now);
        BoardDto dto3 = new BoardDto(2L, "ICSE", false, now, now);
        
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
}

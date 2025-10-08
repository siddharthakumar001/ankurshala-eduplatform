package com.ankurshala.backend.dto.admin;

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
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        SubjectDto dto = new SubjectDto();
        Long id = 1L;
        String name = "Mathematics";
        Boolean active = true;
        Long boardId = 2L;
        Long gradeId = 10L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        dto.setId(id);
        dto.setName(name);
        dto.setActive(active);
        dto.setBoardId(boardId);
        dto.setGradeId(gradeId);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(active, dto.getActive());
        assertEquals(boardId, dto.getBoardId());
        assertEquals(gradeId, dto.getGradeId());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        SubjectDto dto = new SubjectDto();
        dto.setId(null);
        dto.setName(null);
        dto.setActive(null);
        dto.setBoardId(null);
        dto.setGradeId(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getActive());
        assertNull(dto.getBoardId());
        assertNull(dto.getGradeId());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }
}

package com.ankurshala.backend.dto.admin;

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
        assertNull(dto.getChapterId());
        assertNull(dto.getChapterName());
        assertNull(dto.getSubjectName());
        assertNull(dto.getTitle());
        assertNull(dto.getCode());
        assertNull(dto.getDescription());
        assertNull(dto.getSummary());
        assertNull(dto.getExpectedTimeMins());
        assertNull(dto.getActive());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        TopicDto dto = new TopicDto();
        Long id = 1L;
        Long chapterId = 2L;
        String chapterName = "Algebra";
        String subjectName = "Mathematics";
        String title = "Linear Equations";
        String code = "LE001";
        String description = "Introduction to linear equations";
        String summary = "Basic concepts";
        Integer expectedTimeMins = 60;
        Boolean active = true;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        dto.setId(id);
        dto.setChapterId(chapterId);
        dto.setChapterName(chapterName);
        dto.setSubjectName(subjectName);
        dto.setTitle(title);
        dto.setCode(code);
        dto.setDescription(description);
        dto.setSummary(summary);
        dto.setExpectedTimeMins(expectedTimeMins);
        dto.setActive(active);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        assertEquals(id, dto.getId());
        assertEquals(chapterId, dto.getChapterId());
        assertEquals(chapterName, dto.getChapterName());
        assertEquals(subjectName, dto.getSubjectName());
        assertEquals(title, dto.getTitle());
        assertEquals(code, dto.getCode());
        assertEquals(description, dto.getDescription());
        assertEquals(summary, dto.getSummary());
        assertEquals(expectedTimeMins, dto.getExpectedTimeMins());
        assertEquals(active, dto.getActive());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        TopicDto dto = new TopicDto();
        dto.setId(null);
        dto.setChapterId(null);
        dto.setChapterName(null);
        dto.setSubjectName(null);
        dto.setTitle(null);
        dto.setCode(null);
        dto.setDescription(null);
        dto.setSummary(null);
        dto.setExpectedTimeMins(null);
        dto.setActive(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);

        assertNull(dto.getId());
        assertNull(dto.getChapterId());
        assertNull(dto.getChapterName());
        assertNull(dto.getSubjectName());
        assertNull(dto.getTitle());
        assertNull(dto.getCode());
        assertNull(dto.getDescription());
        assertNull(dto.getSummary());
        assertNull(dto.getExpectedTimeMins());
        assertNull(dto.getActive());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }
}

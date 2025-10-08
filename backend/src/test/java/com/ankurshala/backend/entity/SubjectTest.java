package com.ankurshala.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subject Entity Tests")
class SubjectTest {

    @Test
    @DisplayName("Should create Subject with no-args constructor")
    void testNoArgsConstructor() {
        Subject subject = new Subject();
        assertNotNull(subject);
        assertNull(subject.getId());
        assertNull(subject.getName());
        assertTrue(subject.getActive()); // Default value is true
        assertFalse(subject.getSoftDeleted()); // Default value is false
        assertNull(subject.getBoardId());
        assertNull(subject.getGradeId());
        assertNull(subject.getCreatedAt());
        assertNull(subject.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create Subject with name constructor")
    void testNameConstructor() {
        String name = "Mathematics";
        Subject subject = new Subject(name);
        
        assertNull(subject.getId());
        assertEquals(name, subject.getName());
        assertTrue(subject.getActive());
        assertFalse(subject.getSoftDeleted());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        Subject subject = new Subject();
        Long id = 1L;
        String name = "Physics";
        Boolean active = false;
        Boolean softDeleted = true;
        Long boardId = 1L;
        Long gradeId = 10L;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        subject.setId(id);
        subject.setName(name);
        subject.setActive(active);
        subject.setSoftDeleted(softDeleted);
        subject.setBoardId(boardId);
        subject.setGradeId(gradeId);
        subject.setCreatedAt(createdAt);
        subject.setUpdatedAt(updatedAt);

        assertEquals(id, subject.getId());
        assertEquals(name, subject.getName());
        assertEquals(active, subject.getActive());
        assertEquals(softDeleted, subject.getSoftDeleted());
        assertEquals(boardId, subject.getBoardId());
        assertEquals(gradeId, subject.getGradeId());
        assertEquals(createdAt, subject.getCreatedAt());
        assertEquals(updatedAt, subject.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        Subject subject = new Subject();
        subject.setId(null);
        subject.setName(null);
        subject.setActive(null);
        subject.setSoftDeleted(null);
        subject.setBoardId(null);
        subject.setGradeId(null);
        subject.setCreatedAt(null);
        subject.setUpdatedAt(null);

        assertNull(subject.getId());
        assertNull(subject.getName());
        assertNull(subject.getActive());
        assertNull(subject.getSoftDeleted());
        assertNull(subject.getBoardId());
        assertNull(subject.getGradeId());
        assertNull(subject.getCreatedAt());
        assertNull(subject.getUpdatedAt());
    }
}

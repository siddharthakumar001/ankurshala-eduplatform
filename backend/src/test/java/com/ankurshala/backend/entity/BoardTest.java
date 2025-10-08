package com.ankurshala.backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board Entity Tests")
class BoardTest {

    @Test
    @DisplayName("Should create Board with no-args constructor")
    void testNoArgsConstructor() {
        Board board = new Board();
        assertNotNull(board);
        assertNull(board.getId());
        assertNull(board.getName());
        assertTrue(board.getActive()); // Default value is true
        assertFalse(board.getSoftDeleted()); // Default value is false
        assertNull(board.getDeletedAt());
        assertNull(board.getCreatedAt());
        assertNull(board.getUpdatedAt());
    }

    @Test
    @DisplayName("Should create Board with name constructor")
    void testNameConstructor() {
        String name = "CBSE";
        Board board = new Board(name);
        
        assertNull(board.getId());
        assertEquals(name, board.getName());
        assertTrue(board.getActive());
        assertFalse(board.getSoftDeleted());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        Board board = new Board();
        Long id = 1L;
        String name = "ICSE";
        Boolean active = false;
        Boolean softDeleted = true;
        LocalDateTime deletedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime updatedAt = LocalDateTime.now();

        board.setId(id);
        board.setName(name);
        board.setActive(active);
        board.setSoftDeleted(softDeleted);
        board.setDeletedAt(deletedAt);
        board.setCreatedAt(createdAt);
        board.setUpdatedAt(updatedAt);

        assertEquals(id, board.getId());
        assertEquals(name, board.getName());
        assertEquals(active, board.getActive());
        assertEquals(softDeleted, board.getSoftDeleted());
        assertEquals(deletedAt, board.getDeletedAt());
        assertEquals(createdAt, board.getCreatedAt());
        assertEquals(updatedAt, board.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        Board board = new Board();
        board.setId(null);
        board.setName(null);
        board.setActive(null);
        board.setSoftDeleted(null);
        board.setDeletedAt(null);
        board.setCreatedAt(null);
        board.setUpdatedAt(null);

        assertNull(board.getId());
        assertNull(board.getName());
        assertNull(board.getActive());
        assertNull(board.getSoftDeleted());
        assertNull(board.getDeletedAt());
        assertNull(board.getCreatedAt());
        assertNull(board.getUpdatedAt());
    }
}

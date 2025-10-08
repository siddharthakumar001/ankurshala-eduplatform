package com.ankurshala.backend.dto.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StudentDocumentDto Tests")
class StudentDocumentDtoTest {

    @Test
    @DisplayName("Should create StudentDocumentDto with no-args constructor")
    void testNoArgsConstructor() {
        StudentDocumentDto dto = new StudentDocumentDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getDocumentName());
        assertNull(dto.getDocumentUrl());
        assertNull(dto.getDocumentType());
        assertNull(dto.getUploadDate());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        StudentDocumentDto dto = new StudentDocumentDto();
        Long id = 1L;
        String documentName = "Birth Certificate";
        String documentUrl = "https://example.com/birth-cert.pdf";
        String documentType = "BIRTH_CERTIFICATE";
        LocalDateTime uploadDate = LocalDateTime.now();

        dto.setId(id);
        dto.setDocumentName(documentName);
        dto.setDocumentUrl(documentUrl);
        dto.setDocumentType(documentType);
        dto.setUploadDate(uploadDate);

        assertEquals(id, dto.getId());
        assertEquals(documentName, dto.getDocumentName());
        assertEquals(documentUrl, dto.getDocumentUrl());
        assertEquals(documentType, dto.getDocumentType());
        assertEquals(uploadDate, dto.getUploadDate());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        StudentDocumentDto dto = new StudentDocumentDto();
        dto.setId(null);
        dto.setDocumentName(null);
        dto.setDocumentUrl(null);
        dto.setDocumentType(null);
        dto.setUploadDate(null);

        assertNull(dto.getId());
        assertNull(dto.getDocumentName());
        assertNull(dto.getDocumentUrl());
        assertNull(dto.getDocumentType());
        assertNull(dto.getUploadDate());
    }
}

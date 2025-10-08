package com.ankurshala.backend.dto.teacher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TeacherDocumentDto Tests")
class TeacherDocumentDtoTest {

    @Test
    @DisplayName("Should create TeacherDocumentDto with no-args constructor")
    void testNoArgsConstructor() {
        TeacherDocumentDto dto = new TeacherDocumentDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getDocumentType());
        assertNull(dto.getDocumentUrl());
        assertNull(dto.getDocumentName());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        TeacherDocumentDto dto = new TeacherDocumentDto();
        Long id = 1L;
        String documentType = "DEGREE_CERTIFICATE";
        String documentUrl = "https://example.com/certificate.pdf";
        String documentName = "M.Sc Mathematics Certificate";

        dto.setId(id);
        dto.setDocumentType(documentType);
        dto.setDocumentUrl(documentUrl);
        dto.setDocumentName(documentName);

        assertEquals(id, dto.getId());
        assertEquals(documentType, dto.getDocumentType());
        assertEquals(documentUrl, dto.getDocumentUrl());
        assertEquals(documentName, dto.getDocumentName());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        TeacherDocumentDto dto = new TeacherDocumentDto();
        dto.setId(null);
        dto.setDocumentType(null);
        dto.setDocumentUrl(null);
        dto.setDocumentName(null);

        assertNull(dto.getId());
        assertNull(dto.getDocumentType());
        assertNull(dto.getDocumentUrl());
        assertNull(dto.getDocumentName());
    }
}

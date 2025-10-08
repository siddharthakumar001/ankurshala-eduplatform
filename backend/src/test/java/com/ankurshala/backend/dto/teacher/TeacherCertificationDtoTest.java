package com.ankurshala.backend.dto.teacher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TeacherCertificationDto Tests")
class TeacherCertificationDtoTest {

    @Test
    @DisplayName("Should create TeacherCertificationDto with no-args constructor")
    void testNoArgsConstructor() {
        TeacherCertificationDto dto = new TeacherCertificationDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getCertificationName());
        assertNull(dto.getIssuingAuthority());
        assertNull(dto.getCertificationId());
        assertNull(dto.getIssueYear());
        assertNull(dto.getExpiryDate());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        TeacherCertificationDto dto = new TeacherCertificationDto();
        Long id = 1L;
        String certificationName = "Teaching License";
        String issuingAuthority = "State Education Board";
        String certificationId = "TL123456";
        Integer issueYear = 2020;
        LocalDate expiryDate = LocalDate.of(2025, 12, 31);

        dto.setId(id);
        dto.setCertificationName(certificationName);
        dto.setIssuingAuthority(issuingAuthority);
        dto.setCertificationId(certificationId);
        dto.setIssueYear(issueYear);
        dto.setExpiryDate(expiryDate);

        assertEquals(id, dto.getId());
        assertEquals(certificationName, dto.getCertificationName());
        assertEquals(issuingAuthority, dto.getIssuingAuthority());
        assertEquals(certificationId, dto.getCertificationId());
        assertEquals(issueYear, dto.getIssueYear());
        assertEquals(expiryDate, dto.getExpiryDate());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        TeacherCertificationDto dto = new TeacherCertificationDto();
        dto.setId(null);
        dto.setCertificationName(null);
        dto.setIssuingAuthority(null);
        dto.setCertificationId(null);
        dto.setIssueYear(null);
        dto.setExpiryDate(null);

        assertNull(dto.getId());
        assertNull(dto.getCertificationName());
        assertNull(dto.getIssuingAuthority());
        assertNull(dto.getCertificationId());
        assertNull(dto.getIssueYear());
        assertNull(dto.getExpiryDate());
    }
}

package com.ankurshala.backend.dto.teacher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TeacherProfileDto Tests")
class TeacherProfileDtoTest {

    @Test
    @DisplayName("Should create TeacherProfileDto with no-args constructor")
    void testNoArgsConstructor() {
        TeacherProfileDto dto = new TeacherProfileDto();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getFirstName());
        assertNull(dto.getMiddleName());
        assertNull(dto.getLastName());
        assertNull(dto.getMobileNumber());
        assertNull(dto.getAlternateMobileNumber());
        assertNull(dto.getContactEmail());
        assertNull(dto.getHighestEducation());
        assertNull(dto.getPostalAddress());
        assertNull(dto.getCity());
        assertNull(dto.getState());
        assertEquals("India", dto.getCountry());
        assertNull(dto.getSecondaryAddress());
        assertNull(dto.getProfilePhotoUrl());
        assertNull(dto.getGovtIdProofUrl());
        assertNull(dto.getBio());
        assertNull(dto.getQualifications());
        assertNull(dto.getHourlyRate());
        assertNull(dto.getYearsOfExperience());
        assertNull(dto.getSpecialization());
        assertFalse(dto.getVerified());
        assertEquals(BigDecimal.ZERO, dto.getRating());
        assertEquals(0, dto.getTotalReviews());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        TeacherProfileDto dto = new TeacherProfileDto();
        
        dto.setId(1L);
        dto.setFirstName("John");
        dto.setMiddleName("Michael");
        dto.setLastName("Doe");
        dto.setMobileNumber("9876543210");
        dto.setAlternateMobileNumber("9876543211");
        dto.setContactEmail("john.doe@example.com");
        dto.setHighestEducation("Masters");
        dto.setPostalAddress("123 Main St");
        dto.setCity("Mumbai");
        dto.setState("Maharashtra");
        dto.setCountry("India");
        dto.setSecondaryAddress("456 Secondary St");
        dto.setProfilePhotoUrl("http://example.com/photo.jpg");
        dto.setGovtIdProofUrl("http://example.com/id.jpg");
        dto.setBio("Experienced teacher");
        dto.setQualifications("M.Sc Mathematics");
        dto.setHourlyRate(new BigDecimal("500.00"));
        dto.setYearsOfExperience(5);
        dto.setSpecialization("Mathematics");
        dto.setVerified(true);
        dto.setRating(new BigDecimal("4.5"));
        dto.setTotalReviews(100);
        
        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Michael", dto.getMiddleName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("9876543210", dto.getMobileNumber());
        assertEquals("9876543211", dto.getAlternateMobileNumber());
        assertEquals("john.doe@example.com", dto.getContactEmail());
        assertEquals("Masters", dto.getHighestEducation());
        assertEquals("123 Main St", dto.getPostalAddress());
        assertEquals("Mumbai", dto.getCity());
        assertEquals("Maharashtra", dto.getState());
        assertEquals("India", dto.getCountry());
        assertEquals("456 Secondary St", dto.getSecondaryAddress());
        assertEquals("http://example.com/photo.jpg", dto.getProfilePhotoUrl());
        assertEquals("http://example.com/id.jpg", dto.getGovtIdProofUrl());
        assertEquals("Experienced teacher", dto.getBio());
        assertEquals("M.Sc Mathematics", dto.getQualifications());
        assertEquals(new BigDecimal("500.00"), dto.getHourlyRate());
        assertEquals(5, dto.getYearsOfExperience());
        assertEquals("Mathematics", dto.getSpecialization());
        assertTrue(dto.getVerified());
        assertEquals(new BigDecimal("4.5"), dto.getRating());
        assertEquals(100, dto.getTotalReviews());
    }

    @Test
    @DisplayName("Should handle null values correctly")
    void testNullValues() {
        TeacherProfileDto dto = new TeacherProfileDto();
        
        dto.setFirstName(null);
        dto.setMiddleName(null);
        dto.setLastName(null);
        dto.setMobileNumber(null);
        dto.setAlternateMobileNumber(null);
        dto.setContactEmail(null);
        dto.setHighestEducation(null);
        dto.setPostalAddress(null);
        dto.setCity(null);
        dto.setState(null);
        dto.setCountry(null);
        dto.setSecondaryAddress(null);
        dto.setProfilePhotoUrl(null);
        dto.setGovtIdProofUrl(null);
        dto.setBio(null);
        dto.setQualifications(null);
        dto.setHourlyRate(null);
        dto.setYearsOfExperience(null);
        dto.setSpecialization(null);
        dto.setVerified(null);
        dto.setRating(null);
        dto.setTotalReviews(null);
        
        assertNull(dto.getFirstName());
        assertNull(dto.getMiddleName());
        assertNull(dto.getLastName());
        assertNull(dto.getMobileNumber());
        assertNull(dto.getAlternateMobileNumber());
        assertNull(dto.getContactEmail());
        assertNull(dto.getHighestEducation());
        assertNull(dto.getPostalAddress());
        assertNull(dto.getCity());
        assertNull(dto.getState());
        assertNull(dto.getCountry());
        assertNull(dto.getSecondaryAddress());
        assertNull(dto.getProfilePhotoUrl());
        assertNull(dto.getGovtIdProofUrl());
        assertNull(dto.getBio());
        assertNull(dto.getQualifications());
        assertNull(dto.getHourlyRate());
        assertNull(dto.getYearsOfExperience());
        assertNull(dto.getSpecialization());
        assertNull(dto.getVerified());
        assertNull(dto.getRating());
        assertNull(dto.getTotalReviews());
    }
}
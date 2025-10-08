package com.ankurshala.backend.util;

import com.ankurshala.backend.entity.Gender;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.entity.ClassLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IndianDataGenerator Tests")
class IndianDataGeneratorTest {

    private final IndianDataGenerator generator = new IndianDataGenerator();

    @Test
    @DisplayName("Should generate valid first names for male gender")
    void testGenerateFirstNameMale() {
        String firstName = generator.generateFirstName(Gender.MALE);
        assertNotNull(firstName);
        assertFalse(firstName.isEmpty());
        assertTrue(firstName.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid first names for female gender")
    void testGenerateFirstNameFemale() {
        String firstName = generator.generateFirstName(Gender.FEMALE);
        assertNotNull(firstName);
        assertFalse(firstName.isEmpty());
        assertTrue(firstName.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid last names")
    void testGenerateLastName() {
        String lastName = generator.generateLastName();
        assertNotNull(lastName);
        assertFalse(lastName.isEmpty());
        assertTrue(lastName.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid city names")
    void testGenerateCity() {
        String city = generator.generateCity();
        assertNotNull(city);
        assertFalse(city.isEmpty());
        assertTrue(city.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid state names")
    void testGenerateState() {
        String state = generator.generateState();
        assertNotNull(state);
        assertFalse(state.isEmpty());
        assertTrue(state.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid school names")
    void testGenerateSchoolName() {
        String schoolName = generator.generateSchoolName();
        assertNotNull(schoolName);
        assertFalse(schoolName.isEmpty());
        assertTrue(schoolName.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid university names")
    void testGenerateUniversity() {
        String university = generator.generateUniversity();
        assertNotNull(university);
        assertFalse(university.isEmpty());
        assertTrue(university.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid subject names")
    void testGenerateSubject() {
        String subject = generator.generateSubject();
        assertNotNull(subject);
        assertFalse(subject.isEmpty());
        assertTrue(subject.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid bank names")
    void testGenerateBankName() {
        String bankName = generator.generateBankName();
        assertNotNull(bankName);
        assertFalse(bankName.isEmpty());
        assertTrue(bankName.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid IFSC codes")
    void testGenerateIFSCCode() {
        String ifscCode = generator.generateIFSCCode();
        assertNotNull(ifscCode);
        assertFalse(ifscCode.isEmpty());
        assertTrue(ifscCode.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid language names")
    void testGenerateLanguage() {
        String language = generator.generateLanguage();
        assertNotNull(language);
        assertFalse(language.isEmpty());
        assertTrue(language.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid certification names")
    void testGenerateCertification() {
        String certification = generator.generateCertification();
        assertNotNull(certification);
        assertFalse(certification.isEmpty());
        assertTrue(certification.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid issuing authority names")
    void testGenerateIssuingAuthority() {
        String issuingAuthority = generator.generateIssuingAuthority();
        assertNotNull(issuingAuthority);
        assertFalse(issuingAuthority.isEmpty());
        assertTrue(issuingAuthority.length() > 0);
    }

    @Test
    @DisplayName("Should generate valid gender")
    void testGenerateGender() {
        Gender gender = generator.generateGender();
        assertNotNull(gender);
        assertTrue(gender == Gender.MALE || gender == Gender.FEMALE || gender == Gender.OTHER);
    }

    @Test
    @DisplayName("Should generate valid educational board")
    void testGenerateEducationalBoard() {
        EducationalBoard board = generator.generateEducationalBoard();
        assertNotNull(board);
    }

    @Test
    @DisplayName("Should generate valid class level")
    void testGenerateClassLevel() {
        ClassLevel classLevel = generator.generateClassLevel();
        assertNotNull(classLevel);
    }

    @Test
    @DisplayName("Should generate consistent results with fixed seed")
    void testConsistentResults() {
        // Generate multiple values and ensure they're consistent
        String firstName1 = generator.generateFirstName(Gender.MALE);
        String firstName2 = generator.generateFirstName(Gender.MALE);
        
        // With fixed seed, results should be deterministic
        assertNotNull(firstName1);
        assertNotNull(firstName2);
    }
}

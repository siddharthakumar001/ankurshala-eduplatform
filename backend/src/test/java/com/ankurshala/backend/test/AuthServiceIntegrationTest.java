package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.auth.StudentSignupRequest;
import com.ankurshala.backend.dto.auth.TeacherSignupRequest;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.service.EnhancedAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceIntegrationTest {

    @Autowired
    private EnhancedAuthService authService;
    
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testStudentSignupSuccess() {
        // Given
        StudentSignupRequest request = createValidStudentSignupRequest();

        // When
        var response = authService.signupStudent(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(Role.STUDENT, response.getRole());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getName(), response.getName());
        
        // Verify user is saved
        assertTrue(userRepository.existsByEmail(request.getEmail()));
    }

    @Test
    void testStudentSignupDuplicateEmail() {
        // Given
        StudentSignupRequest request = createValidStudentSignupRequest();
        authService.signupStudent(request);

        // When & Then
        assertThrows(Exception.class, () -> authService.signupStudent(request));
    }

    @Test
    void testTeacherSignupSuccess() {
        // Given
        TeacherSignupRequest request = createValidTeacherSignupRequest();

        // When
        var response = authService.signupTeacher(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(Role.TEACHER, response.getRole());
        assertEquals(request.getEmail(), response.getEmail());
        assertEquals(request.getName(), response.getName());
        
        // Verify user is saved
        assertTrue(userRepository.existsByEmail(request.getEmail()));
    }

    @Test
    void testSigninSuccess() {
        // Given
        StudentSignupRequest signupRequest = createValidStudentSignupRequest();
        authService.signupStudent(signupRequest);

        // When
        var response = authService.signin(createSigninRequest(signupRequest.getEmail(), signupRequest.getPassword()));

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(Role.STUDENT, response.getRole());
    }

    @Test
    void testSigninInvalidCredentials() {
        // When & Then
        assertThrows(Exception.class, () -> 
            authService.signin(createSigninRequest("invalid@email.com", "wrongpassword")));
    }

    private StudentSignupRequest createValidStudentSignupRequest() {
        StudentSignupRequest request = new StudentSignupRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePassword123!");
        request.setBoard("CBSE");
        request.setGrade("10");
        request.setLanguage("English");
        request.setGoals(Arrays.asList("Improve Math", "Better Science"));
        request.setSchool("Delhi Public School");
        request.setDob(LocalDate.of(2005, 5, 15));
        request.setPincode("110001");
        request.setGuardianName("Jane Doe");
        request.setGuardianContact("9876543210");
        return request;
    }

    private TeacherSignupRequest createValidTeacherSignupRequest() {
        TeacherSignupRequest request = new TeacherSignupRequest();
        request.setName("Dr. Smith");
        request.setEmail("dr.smith@example.com");
        request.setPassword("SecurePassword123!");
        request.setBio("Experienced mathematics teacher with 10 years of experience");
        request.setYearsExperience(10);
        request.setLanguages(Arrays.asList("English", "Hindi"));
        request.setCategories(Arrays.asList("STANDARD", "PREMIUM"));
        request.setHourlyRate(new BigDecimal("500.00"));
        request.setSubjectExpertise(Arrays.asList());
        request.setAvailability(Arrays.asList());
        return request;
    }

    private com.ankurshala.backend.dto.auth.SigninRequest createSigninRequest(String email, String password) {
        com.ankurshala.backend.dto.auth.SigninRequest request = new com.ankurshala.backend.dto.auth.SigninRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}

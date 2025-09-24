package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.dto.teacher.TeacherQualificationDto;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for cascade delete operations.
 * Tests that deleting a User properly cascades to all related entities.
 */
@AutoConfigureWebMvc
@Transactional
public class CascadeDeleteTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Test
    public void testDeleteStudentCascadesToProfileAndDocuments() throws Exception {
        // 1. Create a student
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test Student");
        signupRequest.setEmail("test.student@example.com");
        signupRequest.setPassword("SecurePass123!");
        signupRequest.setRole(Role.STUDENT);

        MvcResult result = mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. Verify user and profile were created
        assertTrue(userRepository.existsByEmail("test.student@example.com"));
        assertTrue(studentProfileRepository.existsByUserEmail("test.student@example.com"));

        // 3. Add a document to the student
        String authResponseJson = result.getResponse().getContentAsString();
        String accessToken = extractAccessToken(authResponseJson);

        mockMvc.perform(post("/student/profile/documents")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"documentType\":\"ID_PROOF\",\"documentUrl\":\"https://example.com/id.pdf\",\"uploadedAt\":\"2024-01-01T00:00:00\"}"))
                .andExpect(status().isOk());

        // 4. Verify document was created
        assertTrue(studentProfileRepository.existsByUserEmail("test.student@example.com"));

        // 5. Delete the user
        Long userId = userRepository.findByEmail("test.student@example.com").get().getId();
        userRepository.deleteById(userId);

        // 6. Verify cascade delete worked
        assertFalse(userRepository.existsByEmail("test.student@example.com"));
        assertFalse(studentProfileRepository.existsByUserEmail("test.student@example.com"));
    }

    @Test
    public void testDeleteTeacherCascadesToAllRelatedEntities() throws Exception {
        // 1. Create a teacher
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test Teacher");
        signupRequest.setEmail("test.teacher@example.com");
        signupRequest.setPassword("SecurePass123!");
        signupRequest.setRole(Role.TEACHER);

        MvcResult result = mockMvc.perform(post("/auth/signup/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. Verify user and teacher were created
        assertTrue(userRepository.existsByEmail("test.teacher@example.com"));
        assertTrue(teacherRepository.existsByUserEmail("test.teacher@example.com"));

        // 3. Add a qualification to the teacher
        String authResponseJson = result.getResponse().getContentAsString();
        String accessToken = extractAccessToken(authResponseJson);

        TeacherQualificationDto qualificationDto = new TeacherQualificationDto();
        qualificationDto.setDegree("Bachelor of Science");
        qualificationDto.setFieldOfStudy("Mathematics");
        qualificationDto.setInstitution("Test University");
        qualificationDto.setGraduationYear(2020);
        qualificationDto.setGrade("A+");

        mockMvc.perform(post("/teacher/profile/qualifications")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualificationDto)))
                .andExpect(status().isOk());

        // 4. Verify qualification was created
        assertTrue(teacherRepository.existsByUserEmail("test.teacher@example.com"));

        // 5. Delete the user
        Long userId = userRepository.findByEmail("test.teacher@example.com").get().getId();
        userRepository.deleteById(userId);

        // 6. Verify cascade delete worked
        assertFalse(userRepository.existsByEmail("test.teacher@example.com"));
        assertFalse(teacherRepository.existsByUserEmail("test.teacher@example.com"));
        assertFalse(teacherProfileRepository.existsByUserEmail("test.teacher@example.com"));
    }

    @Test
    public void testDeleteUserCascadesToRefreshTokens() throws Exception {
        // 1. Create a student and sign in to generate refresh token
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test Student");
        signupRequest.setEmail("test.student@example.com");
        signupRequest.setPassword("SecurePass123!");
        signupRequest.setRole(Role.STUDENT);

        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 2. Sign in to create refresh token
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test.student@example.com\",\"password\":\"SecurePass123!\"}"))
                .andExpect(status().isOk());

        // 3. Verify refresh token was created
        Long userId = userRepository.findByEmail("test.student@example.com").get().getId();
        assertTrue(refreshTokenRepository.existsByUserId(userId));

        // 4. Delete the user
        userRepository.deleteById(userId);

        // 5. Verify refresh tokens were cascade deleted
        assertFalse(refreshTokenRepository.existsByUserId(userId));
    }

    private String extractAccessToken(String authResponseJson) throws Exception {
        // Simple JSON parsing to extract access token
        // In a real scenario, you might want to use a more robust approach
        int startIndex = authResponseJson.indexOf("\"accessToken\":\"") + 15;
        int endIndex = authResponseJson.indexOf("\"", startIndex);
        return authResponseJson.substring(startIndex, endIndex);
    }
}

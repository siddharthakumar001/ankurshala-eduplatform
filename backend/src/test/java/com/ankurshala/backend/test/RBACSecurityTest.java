package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.SigninRequest;
import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Role-Based Access Control (RBAC) security.
 * Tests that users can only access endpoints appropriate to their role.
 */
@AutoConfigureWebMvc
public class RBACSecurityTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testStudentCannotAccessTeacherEndpoints() throws Exception {
        // 1. Create a student
        String studentToken = createStudentAndGetToken();

        // 2. Try to access teacher profile endpoint
        mockMvc.perform(get("/teacher/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Access denied"));

        // 3. Try to access teacher qualifications endpoint
        mockMvc.perform(get("/teacher/profile/qualifications")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        // 4. Try to access admin profile endpoint
        mockMvc.perform(get("/admin/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testTeacherCannotAccessStudentEndpoints() throws Exception {
        // 1. Create a teacher
        String teacherToken = createTeacherAndGetToken();

        // 2. Try to access student profile endpoint
        mockMvc.perform(get("/student/profile")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Access denied"));

        // 3. Try to access student documents endpoint
        mockMvc.perform(get("/student/profile/documents")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());

        // 4. Try to access admin profile endpoint
        mockMvc.perform(get("/admin/profile")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testStudentCanAccessStudentEndpoints() throws Exception {
        // 1. Create a student
        String studentToken = createStudentAndGetToken();

        // 2. Access student profile endpoint
        mockMvc.perform(get("/student/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists());

        // 3. Access student documents endpoint
        mockMvc.perform(get("/student/profile/documents")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testTeacherCanAccessTeacherEndpoints() throws Exception {
        // 1. Create a teacher
        String teacherToken = createTeacherAndGetToken();

        // 2. Access teacher profile endpoint
        mockMvc.perform(get("/teacher/profile")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists());

        // 3. Access teacher qualifications endpoint
        mockMvc.perform(get("/teacher/profile/qualifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 4. Access teacher experiences endpoint
        mockMvc.perform(get("/teacher/profile/experiences")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 5. Access teacher certifications endpoint
        mockMvc.perform(get("/teacher/profile/certifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 6. Access teacher documents endpoint
        mockMvc.perform(get("/teacher/profile/documents")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 7. Access teacher availability endpoint
        mockMvc.perform(get("/teacher/profile/availability")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // 8. Access teacher addresses endpoint
        mockMvc.perform(get("/teacher/profile/addresses")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 9. Access teacher bank details endpoint
        mockMvc.perform(get("/teacher/profile/bank-details")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testUnauthenticatedAccessIsDenied() throws Exception {
        // Try to access protected endpoints without authentication
        mockMvc.perform(get("/student/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/teacher/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidTokenIsRejected() throws Exception {
        // Try to access protected endpoints with invalid token
        mockMvc.perform(get("/student/profile")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/teacher/profile")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/profile")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    private String createStudentAndGetToken() throws Exception {
        // Create student
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

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return response.getAccessToken();
    }

    private String createTeacherAndGetToken() throws Exception {
        // Create teacher
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

        AuthResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponse.class);
        return response.getAccessToken();
    }
}

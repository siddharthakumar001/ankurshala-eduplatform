package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.auth.AuthResponse;
import com.ankurshala.backend.dto.auth.RefreshTokenRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication flow including signup, signin, refresh, and logout.
 */
@AutoConfigureWebMvc
public class AuthFlowTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testStudentSignupSigninRefreshLogoutFlow() throws Exception {
        // 1. Student Signup
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Student");
        signupRequest.setEmail("john.student@example.com");
        signupRequest.setPassword("SecurePass123!");
        signupRequest.setRole(Role.STUDENT);

        MvcResult signupResult = mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("John Student"))
                .andExpect(jsonPath("$.user.email").value("john.student@example.com"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andReturn();

        AuthResponse signupResponse = objectMapper.readValue(
                signupResult.getResponse().getContentAsString(), AuthResponse.class);

        // 2. Student Signin
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setEmail("john.student@example.com");
        signinRequest.setPassword("SecurePass123!");

        MvcResult signinResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("John Student"))
                .andExpect(jsonPath("$.user.email").value("john.student@example.com"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andReturn();

        AuthResponse signinResponse = objectMapper.readValue(
                signinResult.getResponse().getContentAsString(), AuthResponse.class);

        // 3. Token Refresh
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(signinResponse.getRefreshToken());

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("John Student"))
                .andExpect(jsonPath("$.user.email").value("john.student@example.com"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andReturn();

        AuthResponse refreshResponse = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(), AuthResponse.class);

        // 4. Logout (invalidate refresh token)
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken(refreshResponse.getRefreshToken());

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // 5. Verify refresh token is invalidated
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testTeacherSignupSigninRefreshLogoutFlow() throws Exception {
        // 1. Teacher Signup
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Jane Teacher");
        signupRequest.setEmail("jane.teacher@example.com");
        signupRequest.setPassword("SecurePass123!");
        signupRequest.setRole(Role.TEACHER);

        MvcResult signupResult = mockMvc.perform(post("/auth/signup/teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("Jane Teacher"))
                .andExpect(jsonPath("$.user.email").value("jane.teacher@example.com"))
                .andExpect(jsonPath("$.user.role").value("TEACHER"))
                .andReturn();

        AuthResponse signupResponse = objectMapper.readValue(
                signupResult.getResponse().getContentAsString(), AuthResponse.class);

        // 2. Teacher Signin
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setEmail("jane.teacher@example.com");
        signinRequest.setPassword("SecurePass123!");

        MvcResult signinResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("Jane Teacher"))
                .andExpect(jsonPath("$.user.email").value("jane.teacher@example.com"))
                .andExpect(jsonPath("$.user.role").value("TEACHER"))
                .andReturn();

        AuthResponse signinResponse = objectMapper.readValue(
                signinResult.getResponse().getContentAsString(), AuthResponse.class);

        // 3. Token Refresh
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(signinResponse.getRefreshToken());

        MvcResult refreshResult = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.name").value("Jane Teacher"))
                .andExpect(jsonPath("$.user.email").value("jane.teacher@example.com"))
                .andExpect(jsonPath("$.user.role").value("TEACHER"))
                .andReturn();

        AuthResponse refreshResponse = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(), AuthResponse.class);

        // 4. Logout (invalidate refresh token)
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest();
        logoutRequest.setRefreshToken(refreshResponse.getRefreshToken());

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // 5. Verify refresh token is invalidated
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSignupWithDuplicateEmail() throws Exception {
        // First signup
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("John Student");
        signupRequest.setEmail("duplicate@example.com");
        signupRequest.setPassword("SecurePass123!");
        signupRequest.setRole(Role.STUDENT);

        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Second signup with same email should fail
        mockMvc.perform(post("/auth/signup/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    public void testSigninWithInvalidCredentials() throws Exception {
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setEmail("nonexistent@example.com");
        signinRequest.setPassword("WrongPassword123!");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    public void testRefreshWithInvalidToken() throws Exception {
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
}

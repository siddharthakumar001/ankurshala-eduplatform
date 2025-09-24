package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.auth.SignupRequest;
import com.ankurshala.backend.dto.teacher.*;
import com.ankurshala.backend.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for teacher nested resource CRUD operations.
 * Tests creating, reading, updating, and deleting teacher nested resources.
 */
@AutoConfigureWebMvc
public class TeacherNestedCrudTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testTeacherQualificationCrud() throws Exception {
        String teacherToken = createTeacherAndGetToken();

        // Create qualification
        TeacherQualificationDto qualificationDto = new TeacherQualificationDto();
        qualificationDto.setDegree("Master of Science");
        qualificationDto.setFieldOfStudy("Computer Science");
        qualificationDto.setInstitution("Test University");
        qualificationDto.setGraduationYear(2020);
        qualificationDto.setGrade("A+");

        MvcResult createResult = mockMvc.perform(post("/teacher/profile/qualifications")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualificationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.degree").value("Master of Science"))
                .andExpect(jsonPath("$.fieldOfStudy").value("Computer Science"))
                .andExpect(jsonPath("$.institution").value("Test University"))
                .andExpect(jsonPath("$.graduationYear").value(2020))
                .andExpect(jsonPath("$.grade").value("A+"))
                .andReturn();

        // Extract qualification ID
        String responseJson = createResult.getResponse().getContentAsString();
        Long qualificationId = extractIdFromResponse(responseJson);

        // Read qualifications
        mockMvc.perform(get("/teacher/profile/qualifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].degree").value("Master of Science"));

        // Update qualification
        qualificationDto.setDegree("Master of Engineering");
        qualificationDto.setGrade("A++");

        mockMvc.perform(put("/teacher/profile/qualifications/" + qualificationId)
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(qualificationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.degree").value("Master of Engineering"))
                .andExpect(jsonPath("$.grade").value("A++"));

        // Delete qualification
        mockMvc.perform(delete("/teacher/profile/qualifications/" + qualificationId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get("/teacher/profile/qualifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testTeacherExperienceCrud() throws Exception {
        String teacherToken = createTeacherAndGetToken();

        // Create experience
        TeacherExperienceDto experienceDto = new TeacherExperienceDto();
        experienceDto.setCompanyName("Test Company");
        experienceDto.setPosition("Senior Developer");
        experienceDto.setStartDate("2020-01-01");
        experienceDto.setEndDate("2023-12-31");
        experienceDto.setDescription("Worked on various projects");

        MvcResult createResult = mockMvc.perform(post("/teacher/profile/experiences")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(experienceDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Test Company"))
                .andExpect(jsonPath("$.position").value("Senior Developer"))
                .andExpect(jsonPath("$.description").value("Worked on various projects"))
                .andReturn();

        // Extract experience ID
        String responseJson = createResult.getResponse().getContentAsString();
        Long experienceId = extractIdFromResponse(responseJson);

        // Read experiences
        mockMvc.perform(get("/teacher/profile/experiences")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].companyName").value("Test Company"));

        // Update experience
        experienceDto.setPosition("Lead Developer");
        experienceDto.setDescription("Led multiple development teams");

        mockMvc.perform(put("/teacher/profile/experiences/" + experienceId)
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(experienceDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("Lead Developer"))
                .andExpect(jsonPath("$.description").value("Led multiple development teams"));

        // Delete experience
        mockMvc.perform(delete("/teacher/profile/experiences/" + experienceId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get("/teacher/profile/experiences")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testTeacherCertificationCrud() throws Exception {
        String teacherToken = createTeacherAndGetToken();

        // Create certification
        TeacherCertificationDto certificationDto = new TeacherCertificationDto();
        certificationDto.setCertificationName("AWS Certified Solutions Architect");
        certificationDto.setIssuingOrganization("Amazon Web Services");
        certificationDto.setIssueDate("2023-01-01");
        certificationDto.setExpiryDate("2026-01-01");
        certificationDto.setCredentialId("AWS-CSA-123456");

        MvcResult createResult = mockMvc.perform(post("/teacher/profile/certifications")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(certificationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificationName").value("AWS Certified Solutions Architect"))
                .andExpect(jsonPath("$.issuingOrganization").value("Amazon Web Services"))
                .andExpect(jsonPath("$.credentialId").value("AWS-CSA-123456"))
                .andReturn();

        // Extract certification ID
        String responseJson = createResult.getResponse().getContentAsString();
        Long certificationId = extractIdFromResponse(responseJson);

        // Read certifications
        mockMvc.perform(get("/teacher/profile/certifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].certificationName").value("AWS Certified Solutions Architect"));

        // Update certification
        certificationDto.setCertificationName("AWS Certified Solutions Architect Professional");

        mockMvc.perform(put("/teacher/profile/certifications/" + certificationId)
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(certificationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificationName").value("AWS Certified Solutions Architect Professional"));

        // Delete certification
        mockMvc.perform(delete("/teacher/profile/certifications/" + certificationId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get("/teacher/profile/certifications")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testTeacherBankDetailsWithMasking() throws Exception {
        String teacherToken = createTeacherAndGetToken();

        // Create bank details
        TeacherBankDetailsDto bankDetailsDto = new TeacherBankDetailsDto();
        bankDetailsDto.setPanNumber("ABCDE1234F");
        bankDetailsDto.setAccountHolderName("Test Teacher");
        bankDetailsDto.setBankName("Test Bank");
        bankDetailsDto.setAccountNumber("1234567890123456");
        bankDetailsDto.setIfscCode("TEST0001234");
        bankDetailsDto.setAccountType("SAVINGS");
        bankDetailsDto.setBranchAddress("Test Branch, Test City");
        bankDetailsDto.setTermsAccepted(true);

        mockMvc.perform(put("/teacher/profile/bank-details")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bankDetailsDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.panNumber").value("ABCDE1234F"))
                .andExpect(jsonPath("$.accountHolderName").value("Test Teacher"))
                .andExpect(jsonPath("$.bankName").value("Test Bank"))
                .andExpect(jsonPath("$.accountNumber").value("****3456")) // Should be masked
                .andExpect(jsonPath("$.ifscCode").value("TEST0001234"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.branchAddress").value("Test Branch, Test City"))
                .andExpect(jsonPath("$.termsAccepted").value(true));

        // Read bank details to verify masking
        mockMvc.perform(get("/teacher/profile/bank-details")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("****3456")); // Should be masked
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

        String authResponseJson = result.getResponse().getContentAsString();
        return extractAccessToken(authResponseJson);
    }

    private String extractAccessToken(String authResponseJson) throws Exception {
        int startIndex = authResponseJson.indexOf("\"accessToken\":\"") + 15;
        int endIndex = authResponseJson.indexOf("\"", startIndex);
        return authResponseJson.substring(startIndex, endIndex);
    }

    private Long extractIdFromResponse(String responseJson) throws Exception {
        int startIndex = responseJson.indexOf("\"id\":") + 5;
        int endIndex = responseJson.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = responseJson.indexOf("}", startIndex);
        }
        return Long.parseLong(responseJson.substring(startIndex, endIndex).trim());
    }
}

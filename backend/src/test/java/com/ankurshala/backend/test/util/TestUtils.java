package com.ankurshala.backend.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultActions performPost(MockMvc mockMvc, String url, Object requestBody) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)));
    }

    public static ResultActions performGet(MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public static ResultActions performPut(MockMvc mockMvc, String url, Object requestBody) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)));
    }

    public static ResultActions performDelete(MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public static ResultActions performPostWithAuth(MockMvc mockMvc, String url, Object requestBody, String token) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(asJsonString(requestBody)));
    }

    public static ResultActions performGetWithAuth(MockMvc mockMvc, String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token));
    }

    public static ResultActions performPutWithAuth(MockMvc mockMvc, String url, Object requestBody, String token) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(asJsonString(requestBody)));
    }

    public static ResultActions performDeleteWithAuth(MockMvc mockMvc, String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token));
    }

    public static void expectSuccessResponse(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    public static void expectCreatedResponse(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    public static void expectBadRequestResponse(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    public static void expectUnauthorizedResponse(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    public static void expectForbiddenResponse(ResultActions result) throws Exception {
        result.andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    public static void expectNotFoundResponse(ResultActions result) throws Exception {
        result.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    public static void expectValidationError(ResultActions result, String field) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors[?(@.field == '" + field + "')]").exists());
    }
}

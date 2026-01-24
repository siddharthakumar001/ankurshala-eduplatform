package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.AddToStudyListRequest;
import com.ankurshala.backend.dto.student.StudyListItemDto;
import com.ankurshala.backend.dto.student.UpdateStudyListItemRequest;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.ResourceAuthorizationService;
import com.ankurshala.backend.service.StudyListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class StudentStudyListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyListService studyListService;

    @MockBean
    private ResourceAuthorizationService resourceAuthorizationService;

    @BeforeEach
    void setUp() {
        when(resourceAuthorizationService.canAccessStudentProfile(anyLong())).thenReturn(true);
    }

    @Test
    @DisplayName("GET /api/student/study-list - Success")
    public void testGetStudyList_Success() throws Exception {
        // Arrange
        Long studentId = 1L;
        StudyListItemDto item1 = new StudyListItemDto();
        item1.setId(1L);
        item1.setTopicId(101L);
        item1.setTopicName("Atomic Structure");
        item1.setStatus("IN_PROGRESS");
        item1.setNotes("Started reading");
        item1.setAddedAt(ZonedDateTime.now());

        StudyListItemDto item2 = new StudyListItemDto();
        item2.setId(2L);
        item2.setTopicId(102L);
        item2.setTopicName("Chemical Bonding");
        item2.setStatus("ADDED");
        item2.setAddedAt(ZonedDateTime.now());

        List<StudyListItemDto> studyList = Arrays.asList(item1, item2);

        when(studyListService.getStudyList(eq(studentId))).thenReturn(studyList);

        // Act & Assert
        mockMvc.perform(get("/student/study-list").with(studentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].topicName").value("Atomic Structure"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].topicName").value("Chemical Bonding"))
                .andExpect(jsonPath("$[1].status").value("ADDED"));
    }

    @Test
    @DisplayName("GET /api/student/study-list - Unauthorized without authentication")
    public void testGetStudyList_Unauthorized() throws Exception {
        mockMvc.perform(get("/student/study-list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/student/study-list/status/{status} - Success")
    public void testGetStudyListByStatus_Success() throws Exception {
        // Arrange
        Long studentId = 1L;
        String status = "IN_PROGRESS";
        
        StudyListItemDto item = new StudyListItemDto();
        item.setId(1L);
        item.setTopicId(101L);
        item.setTopicName("Atomic Structure");
        item.setStatus("IN_PROGRESS");
        item.setAddedAt(ZonedDateTime.now());

        List<StudyListItemDto> studyList = Arrays.asList(item);

        when(studyListService.getStudyListByStatus(eq(studentId), eq(status))).thenReturn(studyList);

        // Act & Assert
        mockMvc.perform(get("/student/study-list/status/{status}", "IN_PROGRESS").with(studentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("POST /api/student/study-list - Success")
    public void testAddToStudyList_Success() throws Exception {
        // Arrange
        Long studentId = 1L;
        AddToStudyListRequest request = new AddToStudyListRequest();
        request.setTopicId(101L);
        request.setNotes("Need to study this");

        StudyListItemDto createdItem = new StudyListItemDto();
        createdItem.setId(1L);
        createdItem.setTopicId(101L);
        createdItem.setTopicName("Atomic Structure");
        createdItem.setStatus("ADDED");
        createdItem.setNotes("Need to study this");
        createdItem.setAddedAt(ZonedDateTime.now());

        when(studyListService.addTopic(eq(studentId), any(AddToStudyListRequest.class)))
                .thenReturn(createdItem);

        // Act & Assert
        mockMvc.perform(post("/student/study-list")
                .with(studentUser())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.topicId").value(101))
                .andExpect(jsonPath("$.status").value("ADDED"))
                .andExpect(jsonPath("$.notes").value("Need to study this"));
    }

    @Test
    @DisplayName("POST /api/student/study-list - Bad Request with invalid data")
    public void testAddToStudyList_BadRequest() throws Exception {
        // Arrange - missing topicId
        AddToStudyListRequest request = new AddToStudyListRequest();
        request.setNotes("Some notes");

        // Act & Assert
        mockMvc.perform(post("/student/study-list")
                .with(studentUser())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/student/study-list/{itemId} - Success")
    public void testUpdateStudyListItem_Success() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long itemId = 1L;
        UpdateStudyListItemRequest request = new UpdateStudyListItemRequest();
        request.setStatus("IN_PROGRESS");
        request.setNotes("Making good progress");

        StudyListItemDto updatedItem = new StudyListItemDto();
        updatedItem.setId(itemId);
        updatedItem.setTopicId(101L);
        updatedItem.setTopicName("Atomic Structure");
        updatedItem.setStatus("IN_PROGRESS");
        updatedItem.setNotes("Making good progress");
        updatedItem.setAddedAt(ZonedDateTime.now());

        when(studyListService.updateItem(eq(studentId), eq(itemId), any(UpdateStudyListItemRequest.class)))
                .thenReturn(updatedItem);

        // Act & Assert
        mockMvc.perform(patch("/student/study-list/{itemId}", itemId)
                .with(studentUser())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.notes").value("Making good progress"));
    }

    @Test
    @DisplayName("POST /api/student/study-list/{itemId}/mark-done - Success")
    public void testMarkAsDone_Success() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long itemId = 1L;

        StudyListItemDto updatedItem = new StudyListItemDto();
        updatedItem.setId(itemId);
        updatedItem.setTopicId(101L);
        updatedItem.setTopicName("Atomic Structure");
        updatedItem.setStatus("DONE");
        updatedItem.setDoneAt(ZonedDateTime.now());
        updatedItem.setAddedAt(ZonedDateTime.now().minusDays(5));

        when(studyListService.markAsDone(eq(studentId), eq(itemId)))
                .thenReturn(updatedItem);

        // Act & Assert
        mockMvc.perform(post("/student/study-list/{itemId}/mark-done", itemId)
                .with(studentUser())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.doneAt").exists());
    }

    @Test
    @DisplayName("POST /api/student/study-list/{itemId}/mark-done - Updates status correctly")
    public void testMarkAsDone_UpdatesStatus() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long itemId = 1L;

        StudyListItemDto updatedItem = new StudyListItemDto();
        updatedItem.setId(itemId);
        updatedItem.setTopicId(101L);
        updatedItem.setTopicName("Atomic Structure");
        updatedItem.setStatus("DONE");
        updatedItem.setDoneAt(ZonedDateTime.now());
        updatedItem.setAddedAt(ZonedDateTime.now().minusDays(5));

        when(studyListService.markAsDone(eq(studentId), eq(itemId)))
                .thenReturn(updatedItem);

        // Act & Assert
        mockMvc.perform(post("/student/study-list/{itemId}/mark-done", itemId)
                .with(studentUser())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @DisplayName("DELETE /api/student/study-list/{itemId} - Success")
    public void testRemoveFromStudyList_Success() throws Exception {
        // Arrange
        Long studentId = 1L;
        Long itemId = 1L;

        doNothing().when(studyListService).removeItem(eq(studentId), eq(itemId));

        // Act & Assert
        mockMvc.perform(delete("/student/study-list/{itemId}", itemId)
                .with(studentUser())
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/student/study-list/count/{status} - Success")
    public void testGetStudyListCount_Success() throws Exception {
        // Arrange
        Long studentId = 1L;
        String status = "IN_PROGRESS";
        long count = 3L;

        when(studyListService.getCountByStatus(eq(studentId), eq(status))).thenReturn(count);

        // Act & Assert
        mockMvc.perform(get("/student/study-list/count/{status}", status).with(studentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }

    @Test
    @DisplayName("Test authorization - TEACHER role cannot access student endpoints")
    public void testGetStudyList_TeacherUnauthorized() throws Exception {
        mockMvc.perform(get("/student/study-list").with(teacherUser()))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor studentUser() {
        UserPrincipal principal = new UserPrincipal(1L, "student@test.com", "Student", "password", Role.STUDENT, true);
        return user(principal);
    }

    private RequestPostProcessor teacherUser() {
        UserPrincipal principal = new UserPrincipal(2L, "teacher@test.com", "Teacher", "password", Role.TEACHER, true);
        return user(principal);
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.AddToStudyListRequest;
import com.ankurshala.backend.dto.student.StudyListItemDto;
import com.ankurshala.backend.dto.student.UpdateStudyListItemRequest;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.StudentStudyListRepository;
import com.ankurshala.backend.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyListService Tests")
class StudyListServiceTest {

    @Mock
    private StudentStudyListRepository studyListRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private StudyListService studyListService;

    private User testUser;
    private StudentProfile testProfile;
    private Topic testTopic;
    private Chapter testChapter;
    private Subject testSubject;
    private StudentStudyList testStudyListItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("student@test.com");

        testProfile = new StudentProfile();
        testProfile.setId(100L);
        testProfile.setUser(testUser);
        testProfile.setFirstName("John");
        testProfile.setLastName("Doe");

        testSubject = new Subject();
        testSubject.setId(10L);
        testSubject.setName("Mathematics");

        testChapter = new Chapter();
        testChapter.setId(20L);
        testChapter.setName("Algebra");
        testChapter.setSubject(testSubject);

        testTopic = new Topic(testChapter, "Linear Equations");
        testTopic.setId(30L);

        testStudyListItem = new StudentStudyList();
        testStudyListItem.setId(1L);
        testStudyListItem.setStudentId(100L);
        testStudyListItem.setTopicId(30L);
        testStudyListItem.setStatus(StudentStudyList.StudyStatus.ADDED);
        testStudyListItem.setAddedAt(ZonedDateTime.now());
        testStudyListItem.setNotes("Need to study this");
    }

    @Test
    @DisplayName("Should get all study list items for a student")
    void testGetStudyList_Success() {
        // Given
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findByStudentIdOrderByAddedAtDesc(100L))
                .thenReturn(Arrays.asList(testStudyListItem));
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));

        // When
        List<StudyListItemDto> result = studyListService.getStudyList(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(30L, result.get(0).getTopicId());
        assertEquals("ADDED", result.get(0).getStatus());
        verify(studyListRepository).findByStudentIdOrderByAddedAtDesc(100L);
    }

    @Test
    @DisplayName("Should throw exception when student profile not found")
    void testGetStudyList_StudentNotFound() {
        // Given
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studyListService.getStudyList(1L);
        });
        assertEquals("Student profile not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should get study list items filtered by status")
    void testGetStudyListByStatus_Success() {
        // Given
        testStudyListItem.setStatus(StudentStudyList.StudyStatus.IN_PROGRESS);
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findByStudentIdAndStatusOrderByAddedAtDesc(100L, StudentStudyList.StudyStatus.IN_PROGRESS))
                .thenReturn(Arrays.asList(testStudyListItem));
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));

        // When
        List<StudyListItemDto> result = studyListService.getStudyListByStatus(1L, "IN_PROGRESS");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("IN_PROGRESS", result.get(0).getStatus());
        verify(studyListRepository).findByStudentIdAndStatusOrderByAddedAtDesc(100L, StudentStudyList.StudyStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Should add topic to study list successfully")
    void testAddTopic_Success() {
        // Given
        AddToStudyListRequest request = new AddToStudyListRequest();
        request.setTopicId(30L);
        request.setNotes("Important topic");

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));
        when(studyListRepository.existsByStudentIdAndTopicId(100L, 30L)).thenReturn(false);
        when(studyListRepository.save(any(StudentStudyList.class))).thenReturn(testStudyListItem);

        // When
        StudyListItemDto result = studyListService.addTopic(1L, request);

        // Then
        assertNotNull(result);
        assertEquals(30L, result.getTopicId());
        verify(studyListRepository).save(any(StudentStudyList.class));
    }

    @Test
    @DisplayName("Should throw exception when topic already in study list")
    void testAddTopic_AlreadyExists() {
        // Given
        AddToStudyListRequest request = new AddToStudyListRequest();
        request.setTopicId(30L);

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));
        when(studyListRepository.existsByStudentIdAndTopicId(100L, 30L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studyListService.addTopic(1L, request);
        });
        assertEquals("Topic already in study list", exception.getMessage());
        verify(studyListRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when topic not found")
    void testAddTopic_TopicNotFound() {
        // Given
        AddToStudyListRequest request = new AddToStudyListRequest();
        request.setTopicId(999L);

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(topicRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studyListService.addTopic(1L, request);
        });
        assertEquals("Topic not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should update study list item successfully")
    void testUpdateItem_Success() {
        // Given
        UpdateStudyListItemRequest request = new UpdateStudyListItemRequest();
        request.setStatus("IN_PROGRESS");
        request.setNotes("Making progress");

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findById(1L)).thenReturn(Optional.of(testStudyListItem));
        when(studyListRepository.save(any(StudentStudyList.class))).thenReturn(testStudyListItem);
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));

        // When
        StudyListItemDto result = studyListService.updateItem(1L, 1L, request);

        // Then
        assertNotNull(result);
        verify(studyListRepository).save(any(StudentStudyList.class));
    }

    @Test
    @DisplayName("Should throw exception when updating unauthorized item")
    void testUpdateItem_Unauthorized() {
        // Given
        UpdateStudyListItemRequest request = new UpdateStudyListItemRequest();
        request.setStatus("IN_PROGRESS");

        StudentStudyList otherStudentItem = new StudentStudyList();
        otherStudentItem.setId(1L);
        otherStudentItem.setStudentId(999L); // Different student
        otherStudentItem.setTopicId(30L);

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findById(1L)).thenReturn(Optional.of(otherStudentItem));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studyListService.updateItem(1L, 1L, request);
        });
        assertEquals("Unauthorized access to study list item", exception.getMessage());
    }

    @Test
    @DisplayName("Should mark item as done successfully")
    void testMarkAsDone_Success() {
        // Given
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findById(1L)).thenReturn(Optional.of(testStudyListItem));
        when(studyListRepository.save(any(StudentStudyList.class))).thenReturn(testStudyListItem);
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));

        // When
        StudyListItemDto result = studyListService.markAsDone(1L, 1L);

        // Then
        assertNotNull(result);
        verify(studyListRepository).save(argThat(item -> 
            item.getStatus() == StudentStudyList.StudyStatus.DONE && item.getDoneAt() != null
        ));
    }

    @Test
    @DisplayName("Should remove item from study list successfully")
    void testRemoveItem_Success() {
        // Given
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findById(1L)).thenReturn(Optional.of(testStudyListItem));

        // When
        studyListService.removeItem(1L, 1L);

        // Then
        verify(studyListRepository).delete(testStudyListItem);
    }

    @Test
    @DisplayName("Should throw exception when removing unauthorized item")
    void testRemoveItem_Unauthorized() {
        // Given
        StudentStudyList otherStudentItem = new StudentStudyList();
        otherStudentItem.setId(1L);
        otherStudentItem.setStudentId(999L); // Different student

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findById(1L)).thenReturn(Optional.of(otherStudentItem));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            studyListService.removeItem(1L, 1L);
        });
        assertEquals("Unauthorized access to study list item", exception.getMessage());
        verify(studyListRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get count by status successfully")
    void testGetCountByStatus_Success() {
        // Given
        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.countByStudentIdAndStatus(100L, StudentStudyList.StudyStatus.DONE))
                .thenReturn(5L);

        // When
        long result = studyListService.getCountByStatus(1L, "DONE");

        // Then
        assertEquals(5L, result);
        verify(studyListRepository).countByStudentIdAndStatus(100L, StudentStudyList.StudyStatus.DONE);
    }

    @Test
    @DisplayName("Should set doneAt when marking status as DONE")
    void testUpdateItem_SetDoneAtWhenDone() {
        // Given
        UpdateStudyListItemRequest request = new UpdateStudyListItemRequest();
        request.setStatus("DONE");

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findById(1L)).thenReturn(Optional.of(testStudyListItem));
        when(studyListRepository.save(any(StudentStudyList.class))).thenReturn(testStudyListItem);
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));

        // When
        studyListService.updateItem(1L, 1L, request);

        // Then
        verify(studyListRepository).save(argThat(item -> 
            item.getStatus() == StudentStudyList.StudyStatus.DONE && item.getDoneAt() != null
        ));
    }

    @Test
    @DisplayName("Should clear doneAt when changing status from DONE")
    void testUpdateItem_ClearDoneAtWhenNotDone() {
        // Given
        testStudyListItem.setStatus(StudentStudyList.StudyStatus.DONE);
        testStudyListItem.setDoneAt(ZonedDateTime.now());

        UpdateStudyListItemRequest request = new UpdateStudyListItemRequest();
        request.setStatus("IN_PROGRESS");

        when(studentProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));
        when(studyListRepository.findById(1L)).thenReturn(Optional.of(testStudyListItem));
        when(studyListRepository.save(any(StudentStudyList.class))).thenReturn(testStudyListItem);
        when(topicRepository.findById(30L)).thenReturn(Optional.of(testTopic));

        // When
        studyListService.updateItem(1L, 1L, request);

        // Then
        verify(studyListRepository).save(argThat(item -> 
            item.getStatus() == StudentStudyList.StudyStatus.IN_PROGRESS && item.getDoneAt() == null
        ));
    }
}

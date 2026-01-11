package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.MasteryDTO;
import com.ankurshala.backend.entity.StudentTopicMastery;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.repository.StudentTopicMasteryRepository;
import com.ankurshala.backend.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasteryServiceTest {

    @Mock
    private StudentTopicMasteryRepository masteryRepository;

    @Mock
    private TopicRepository topicRepository;

    @InjectMocks
    private MasteryService masteryService;

    private Long studentId;
    private Long topicId;
    private Topic testTopic;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        topicId = 100L;

        testTopic = new Topic();
        testTopic.setId(topicId);
        testTopic.setTitle("Test Topic");
        testTopic.setSubjectId(10L);
        testTopic.setChapterId(50L);
    }

    @Test
    void getOrCreateMastery_ExistingMastery_ReturnsMastery() {
        // Given
        StudentTopicMastery existingMastery = new StudentTopicMastery(studentId, topicId);
        existingMastery.setMasteryScore(new BigDecimal("0.750"));

        when(masteryRepository.findByStudentIdAndTopicId(studentId, topicId))
                .thenReturn(Optional.of(existingMastery));

        // When
        StudentTopicMastery result = masteryService.getOrCreateMastery(studentId, topicId);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("0.750"), result.getMasteryScore());
        verify(masteryRepository, never()).save(any());
    }

    @Test
    void getOrCreateMastery_NoExistingMastery_CreatesNew() {
        // Given
        when(masteryRepository.findByStudentIdAndTopicId(studentId, topicId))
                .thenReturn(Optional.empty());

        StudentTopicMastery newMastery = new StudentTopicMastery(studentId, topicId);
        when(masteryRepository.save(any(StudentTopicMastery.class)))
                .thenReturn(newMastery);

        // When
        StudentTopicMastery result = masteryService.getOrCreateMastery(studentId, topicId);

        // Then
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(topicId, result.getTopicId());
        verify(masteryRepository).save(any(StudentTopicMastery.class));
    }

    @Test
    void getTopicMastery_ValidTopic_ReturnsMastery() {
        // Given
        StudentTopicMastery existingMastery = new StudentTopicMastery(studentId, topicId);
        existingMastery.setMasteryScore(new BigDecimal("0.650"));
        existingMastery.setTotalAttempts(5);
        existingMastery.setCorrectAttempts(4);

        when(masteryRepository.findByStudentIdAndTopicId(studentId, topicId))
                .thenReturn(Optional.of(existingMastery));
        when(topicRepository.findById(topicId))
                .thenReturn(Optional.of(testTopic));

        // When
        MasteryDTO.TopicMastery result = masteryService.getTopicMastery(studentId, topicId);

        // Then
        assertNotNull(result);
        assertEquals(topicId, result.getTopicId());
        assertEquals("Test Topic", result.getTopicTitle());
        assertEquals(new BigDecimal("0.650"), result.getMasteryScore());
        assertEquals(5, result.getTotalAttempts());
    }

    @Test
    void updateMastery_ValidRequest_UpdatesMastery() {
        // Given
        StudentTopicMastery existingMastery = new StudentTopicMastery(studentId, topicId);
        existingMastery.setMasteryScore(new BigDecimal("0.500"));
        existingMastery.setTotalAttempts(3);
        existingMastery.setCorrectAttempts(2);

        when(masteryRepository.findByStudentIdAndTopicId(studentId, topicId))
                .thenReturn(Optional.of(existingMastery));
        when(masteryRepository.save(any(StudentTopicMastery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MasteryDTO.MasteryUpdateRequest request = MasteryDTO.MasteryUpdateRequest.builder()
                .topicId(topicId)
                .scoreAchieved(new BigDecimal("0.800"))
                .questionsAnswered(5)
                .correctAnswers(4)
                .source("QUIZ")
                .build();

        // When
        MasteryDTO.MasteryUpdateResponse result = masteryService.updateMastery(studentId, request);

        // Then
        assertNotNull(result);
        assertEquals(topicId, result.getTopicId());
        assertEquals(new BigDecimal("0.500"), result.getPreviousScore());
        assertNotNull(result.getNewScore());
        assertNotNull(result.getDelta());
        verify(masteryRepository).save(any(StudentTopicMastery.class));
    }

    @Test
    void getWeakTopics_HasWeakTopics_ReturnsWeakTopics() {
        // Given
        StudentTopicMastery weakMastery1 = new StudentTopicMastery(studentId, topicId);
        weakMastery1.setMasteryScore(new BigDecimal("0.300"));

        StudentTopicMastery weakMastery2 = new StudentTopicMastery(studentId, 101L);
        weakMastery2.setMasteryScore(new BigDecimal("0.400"));

        Topic topic2 = new Topic();
        topic2.setId(101L);
        topic2.setTitle("Topic 2");
        topic2.setSubjectId(10L);
        topic2.setChapterId(50L);

        when(masteryRepository.findWeakTopics(eq(studentId), any(BigDecimal.class)))
                .thenReturn(Arrays.asList(weakMastery1, weakMastery2));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(topicRepository.findById(101L)).thenReturn(Optional.of(topic2));

        // When
        List<MasteryDTO.TopicMastery> result = masteryService.getWeakTopics(studentId, new BigDecimal("0.650"), 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getMasteryScore().compareTo(new BigDecimal("0.650")) < 0);
    }

    @Test
    void getMasteryScoresForTopics_MixedMasteryAndNew_ReturnsAllScores() {
        // Given
        List<Long> topicIds = Arrays.asList(100L, 101L, 102L);

        StudentTopicMastery existingMastery = new StudentTopicMastery(studentId, 100L);
        existingMastery.setMasteryScore(new BigDecimal("0.700"));

        when(masteryRepository.findByStudentIdAndTopicIds(studentId, topicIds))
                .thenReturn(Collections.singletonList(existingMastery));

        // When
        Map<Long, BigDecimal> result = masteryService.getMasteryScoresForTopics(studentId, topicIds);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(new BigDecimal("0.700"), result.get(100L));
        // Topics 101 and 102 should have default score (0.300)
        assertEquals(new BigDecimal("0.300"), result.get(101L));
        assertEquals(new BigDecimal("0.300"), result.get(102L));
    }

    @Test
    void isReadyForTopic_AllPrereqsMastered_ReturnsTrue() {
        // Given
        List<Long> prereqIds = Arrays.asList(200L, 201L);

        StudentTopicMastery mastery1 = new StudentTopicMastery(studentId, 200L);
        mastery1.setMasteryScore(new BigDecimal("0.800"));

        StudentTopicMastery mastery2 = new StudentTopicMastery(studentId, 201L);
        mastery2.setMasteryScore(new BigDecimal("0.700"));

        when(masteryRepository.findByStudentIdAndTopicIds(studentId, prereqIds))
                .thenReturn(Arrays.asList(mastery1, mastery2));

        // When
        boolean result = masteryService.isReadyForTopic(studentId, topicId, prereqIds);

        // Then
        assertTrue(result);
    }

    @Test
    void isReadyForTopic_SomePrereqsWeak_ReturnsFalse() {
        // Given
        List<Long> prereqIds = Arrays.asList(200L, 201L);

        StudentTopicMastery mastery1 = new StudentTopicMastery(studentId, 200L);
        mastery1.setMasteryScore(new BigDecimal("0.800"));

        StudentTopicMastery mastery2 = new StudentTopicMastery(studentId, 201L);
        mastery2.setMasteryScore(new BigDecimal("0.400"));  // Below threshold

        when(masteryRepository.findByStudentIdAndTopicIds(studentId, prereqIds))
                .thenReturn(Arrays.asList(mastery1, mastery2));

        // When
        boolean result = masteryService.isReadyForTopic(studentId, topicId, prereqIds);

        // Then
        assertFalse(result);
    }

    @Test
    void isReadyForTopic_NoPrereqs_ReturnsTrue() {
        // When
        boolean result = masteryService.isReadyForTopic(studentId, topicId, Collections.emptyList());

        // Then
        assertTrue(result);
        verify(masteryRepository, never()).findByStudentIdAndTopicIds(any(), any());
    }
}


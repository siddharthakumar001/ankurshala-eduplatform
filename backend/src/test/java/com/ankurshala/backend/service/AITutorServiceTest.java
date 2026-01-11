package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.dto.ai.ChatDTO;
import com.ankurshala.backend.entity.ContentChunk;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.repository.AIInteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AITutorServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ContentChunkService contentChunkService;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private SafetyModerationService safetyService;

    @Mock
    private AIConfig.AIProperties aiProperties;

    @Mock
    private AIInteractionRepository aiInteractionRepository;

    @InjectMocks
    private AITutorService aiTutorService;

    private Long studentId;
    private ChatDTO.ChatRequest validRequest;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        validRequest = ChatDTO.ChatRequest.builder()
                .message("What is photosynthesis?")
                .sessionId("test-session-123")
                .topicId(101L)
                .language("en")
                .build();
    }

    @Test
    void chat_safetyCheckFails_returnsSafetyResponse() {
        ChatDTO.SafetyCheckResult unsafeResult = ChatDTO.SafetyCheckResult.builder()
                .isSafe(false)
                .category("HARMFUL")
                .reason("Contains harmful content")
                .suggestedResponse("I can't help with that.")
                .build();

        when(safetyService.checkUserInput(any())).thenReturn(unsafeResult);

        ChatDTO.ChatResponse response = aiTutorService.chat(validRequest, studentId);

        assertNotNull(response);
        assertEquals("I can't help with that.", response.getMessage());
        assertTrue(response.getSafetyFiltered());
        assertFalse(response.getWasGrounded());
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void chat_aiUnavailable_returnsUnavailableResponse() {
        ChatDTO.SafetyCheckResult safeResult = ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();

        when(safetyService.checkUserInput(any())).thenReturn(safeResult);
        when(aiProperties.isAvailable()).thenReturn(false);

        ChatDTO.ChatResponse response = aiTutorService.chat(validRequest, studentId);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("temporarily unavailable"));
        assertFalse(response.getWasGrounded());
        verify(chatModel, never()).call(any(Prompt.class));
    }

    @Test
    void chat_withContext_returnsGroundedResponse() {
        // Arrange
        ChatDTO.SafetyCheckResult safeResult = ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();

        ContentChunk mockChunk = createMockChunk();
        ContentChunkService.ChunkWithScore chunkWithScore = 
                new ContentChunkService.ChunkWithScore(mockChunk, 0.9);
        ContentChunkService.RetrievalResult retrievalResult = 
                new ContentChunkService.RetrievalResult(
                        List.of(chunkWithScore),
                        "Context about photosynthesis...",
                        1,
                        BigDecimal.valueOf(0.9)
                );

        AssistantMessage assistantMessage = new AssistantMessage(
                "Photosynthesis is the process by which plants convert light into energy.");
        Generation generation = new Generation(assistantMessage);
        ChatResponse aiResponse = mock(ChatResponse.class);
        when(aiResponse.getResult()).thenReturn(generation);

        when(safetyService.checkUserInput(any())).thenReturn(safeResult);
        when(safetyService.checkAIResponse(any())).thenReturn(safeResult);
        when(safetyService.sanitizeRetrievedContent(any())).thenAnswer(i -> i.getArgument(0));
        when(aiProperties.isAvailable()).thenReturn(true);
        when(aiProperties.getProvider()).thenReturn("openai");
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(contentChunkService.retrieveForRAG(any(), any(), any(), any(), anyInt()))
                .thenReturn(retrievalResult);
        when(chatModel.call(any(Prompt.class))).thenReturn(aiResponse);

        // Act
        ChatDTO.ChatResponse response = aiTutorService.chat(validRequest, studentId);

        // Assert
        assertNotNull(response);
        assertTrue(response.getWasGrounded());
        assertFalse(response.getReferences().isEmpty());
        assertFalse(response.getSafetyFiltered());
        verify(chatModel).call(any(Prompt.class));
        verify(aiInteractionRepository).save(any());
    }

    @Test
    void chat_noContextAvailable_returnsUngroundedResponse() {
        ChatDTO.SafetyCheckResult safeResult = ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();

        ContentChunkService.RetrievalResult emptyResult = 
                new ContentChunkService.RetrievalResult(
                        Collections.emptyList(),
                        "",
                        0,
                        BigDecimal.ZERO
                );

        AssistantMessage assistantMessage = new AssistantMessage("Based on general knowledge...");
        Generation generation = new Generation(assistantMessage);
        ChatResponse aiResponse = mock(ChatResponse.class);
        when(aiResponse.getResult()).thenReturn(generation);

        when(safetyService.checkUserInput(any())).thenReturn(safeResult);
        when(safetyService.checkAIResponse(any())).thenReturn(safeResult);
        when(aiProperties.isAvailable()).thenReturn(true);
        when(aiProperties.getProvider()).thenReturn("openai");
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(contentChunkService.retrieveForRAG(any(), any(), any(), any(), anyInt()))
                .thenReturn(emptyResult);
        when(chatModel.call(any(Prompt.class))).thenReturn(aiResponse);

        ChatDTO.ChatResponse response = aiTutorService.chat(validRequest, studentId);

        assertNotNull(response);
        assertFalse(response.getWasGrounded());
        assertTrue(response.getReferences().isEmpty());
    }

    @Test
    void chat_aiResponseUnsafe_filtersSafetyResponse() {
        ChatDTO.SafetyCheckResult inputSafe = ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();
        ChatDTO.SafetyCheckResult outputUnsafe = ChatDTO.SafetyCheckResult.builder()
                .isSafe(false)
                .category("HARMFUL")
                .reason("AI generated unsafe content")
                .suggestedResponse("I apologize, let me try a different approach.")
                .build();

        ContentChunkService.RetrievalResult emptyResult = 
                new ContentChunkService.RetrievalResult(
                        Collections.emptyList(), "", 0, BigDecimal.ZERO);

        AssistantMessage assistantMessage = new AssistantMessage("Some unsafe content...");
        Generation generation = new Generation(assistantMessage);
        ChatResponse aiResponse = mock(ChatResponse.class);
        when(aiResponse.getResult()).thenReturn(generation);

        when(safetyService.checkUserInput(any())).thenReturn(inputSafe);
        when(safetyService.checkAIResponse(any())).thenReturn(outputUnsafe);
        when(aiProperties.isAvailable()).thenReturn(true);
        when(aiProperties.getProvider()).thenReturn("openai");
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(contentChunkService.retrieveForRAG(any(), any(), any(), any(), anyInt()))
                .thenReturn(emptyResult);
        when(chatModel.call(any(Prompt.class))).thenReturn(aiResponse);

        ChatDTO.ChatResponse response = aiTutorService.chat(validRequest, studentId);

        assertNotNull(response);
        assertEquals("I apologize, let me try a different approach.", response.getMessage());
        assertTrue(response.getSafetyFiltered());
    }

    @Test
    void chat_aiModelThrowsException_returnsErrorResponse() {
        ChatDTO.SafetyCheckResult safeResult = ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();

        ContentChunkService.RetrievalResult emptyResult = 
                new ContentChunkService.RetrievalResult(
                        Collections.emptyList(), "", 0, BigDecimal.ZERO);

        when(safetyService.checkUserInput(any())).thenReturn(safeResult);
        when(aiProperties.isAvailable()).thenReturn(true);
        when(aiProperties.getProvider()).thenReturn("openai");
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(contentChunkService.retrieveForRAG(any(), any(), any(), any(), anyInt()))
                .thenReturn(emptyResult);
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("API error"));

        ChatDTO.ChatResponse response = aiTutorService.chat(validRequest, studentId);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("error"));
    }

    @Test
    void chat_generatesSessionId_whenNotProvided() {
        ChatDTO.ChatRequest requestWithoutSession = ChatDTO.ChatRequest.builder()
                .message("Test message")
                .build();

        ChatDTO.SafetyCheckResult unsafeResult = ChatDTO.SafetyCheckResult.builder()
                .isSafe(false)
                .category("OFF_TOPIC")
                .suggestedResponse("Please ask educational questions.")
                .build();

        when(safetyService.checkUserInput(any())).thenReturn(unsafeResult);

        ChatDTO.ChatResponse response = aiTutorService.chat(requestWithoutSession, studentId);

        assertNotNull(response.getSessionId());
        assertFalse(response.getSessionId().isEmpty());
    }

    @Test
    void chat_suggestedActionsIncludeTakeQuiz_whenContextAvailable() {
        ChatDTO.SafetyCheckResult safeResult = ChatDTO.SafetyCheckResult.builder()
                .isSafe(true)
                .category("SAFE")
                .build();

        ContentChunk mockChunk = createMockChunk();
        ContentChunkService.ChunkWithScore chunkWithScore = 
                new ContentChunkService.ChunkWithScore(mockChunk, 0.9);
        ContentChunkService.RetrievalResult retrievalResult = 
                new ContentChunkService.RetrievalResult(
                        List.of(chunkWithScore),
                        "Context...",
                        1,
                        BigDecimal.valueOf(0.9)
                );

        AssistantMessage assistantMessage = new AssistantMessage("Answer...");
        Generation generation = new Generation(assistantMessage);
        ChatResponse aiResponse = mock(ChatResponse.class);
        when(aiResponse.getResult()).thenReturn(generation);

        when(safetyService.checkUserInput(any())).thenReturn(safeResult);
        when(safetyService.checkAIResponse(any())).thenReturn(safeResult);
        when(safetyService.sanitizeRetrievedContent(any())).thenAnswer(i -> i.getArgument(0));
        when(aiProperties.isAvailable()).thenReturn(true);
        when(aiProperties.getProvider()).thenReturn("openai");
        when(aiProperties.getModel()).thenReturn("gpt-4o-mini");
        when(contentChunkService.retrieveForRAG(any(), any(), any(), any(), anyInt()))
                .thenReturn(retrievalResult);
        when(chatModel.call(any(Prompt.class))).thenReturn(aiResponse);

        ChatDTO.ChatResponse response = aiTutorService.chat(validRequest, studentId);

        assertNotNull(response.getSuggestedActions());
        assertTrue(response.getSuggestedActions().stream()
                .anyMatch(a -> "TAKE_QUIZ".equals(a.getActionType())));
    }

    private ContentChunk createMockChunk() {
        ContentChunk chunk = new ContentChunk();
        chunk.setId(1L);
        chunk.setTopicId(101L);
        chunk.setChunkText("Photosynthesis is the process...");
        chunk.setSourceType(ContentChunk.SourceType.TEXTBOOK);
        chunk.setSourceRef("Chapter 5, Page 32");
        chunk.setLanguage("en");

        Topic topic = new Topic();
        topic.setId(101L);
        topic.setTitle("Photosynthesis");
        chunk.setTopic(topic);

        return chunk;
    }
}


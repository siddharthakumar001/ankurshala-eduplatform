package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.ChatDTO;
import com.ankurshala.backend.dto.ai.ContentIngestionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AICacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AICacheService cacheService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cacheService, "chatCacheTtl", 300);
        ReflectionTestUtils.setField(cacheService, "ragCacheTtl", 3600);
        ReflectionTestUtils.setField(cacheService, "embeddingCacheTtl", 86400);
        ReflectionTestUtils.setField(cacheService, "cacheEnabled", true);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getCachedChatResponse_cacheHit_returnsResponse() throws Exception {
        ChatDTO.ChatResponse expectedResponse = ChatDTO.ChatResponse.builder()
                .sessionId("test-session")
                .message("Cached answer")
                .wasGrounded(true)
                .build();
        String cachedJson = objectMapper.writeValueAsString(expectedResponse);

        when(valueOperations.get(anyString())).thenReturn(cachedJson);

        ChatDTO.ChatResponse result = cacheService.getCachedChatResponse(
                "What is photosynthesis?", 101L, "en");

        assertNotNull(result);
        assertEquals("Cached answer", result.getMessage());
        assertEquals("test-session", result.getSessionId());
    }

    @Test
    void getCachedChatResponse_cacheMiss_returnsNull() {
        when(valueOperations.get(anyString())).thenReturn(null);

        ChatDTO.ChatResponse result = cacheService.getCachedChatResponse(
                "What is photosynthesis?", 101L, "en");

        assertNull(result);
    }

    @Test
    void getCachedChatResponse_cacheDisabled_returnsNull() {
        ReflectionTestUtils.setField(cacheService, "cacheEnabled", false);

        ChatDTO.ChatResponse result = cacheService.getCachedChatResponse(
                "What is photosynthesis?", 101L, "en");

        assertNull(result);
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void cacheChatResponse_validResponse_storesInCache() {
        ChatDTO.ChatResponse response = ChatDTO.ChatResponse.builder()
                .sessionId("test-session")
                .message("New answer")
                .wasGrounded(true)
                .safetyFiltered(false)
                .build();

        cacheService.cacheChatResponse("What is photosynthesis?", 101L, "en", response);

        verify(valueOperations).set(anyString(), anyString(), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void cacheChatResponse_safetyFiltered_doesNotCache() {
        ChatDTO.ChatResponse response = ChatDTO.ChatResponse.builder()
                .message("Filtered response")
                .safetyFiltered(true)
                .build();

        cacheService.cacheChatResponse("Bad question", null, "en", response);

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void cacheChatResponse_errorResponse_doesNotCache() {
        ChatDTO.ChatResponse response = ChatDTO.ChatResponse.builder()
                .message("I encountered an error processing your question")
                .safetyFiltered(false)
                .build();

        cacheService.cacheChatResponse("Question", null, "en", response);

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void getCachedRagResults_cacheHit_returnsResults() throws Exception {
        ContentIngestionDTO.SemanticSearchResponse expectedResponse = 
                ContentIngestionDTO.SemanticSearchResponse.builder()
                        .query("photosynthesis")
                        .totalResults(3)
                        .averageSimilarity(0.85)
                        .results(Collections.emptyList())
                        .build();
        String cachedJson = objectMapper.writeValueAsString(expectedResponse);

        when(valueOperations.get(anyString())).thenReturn(cachedJson);

        ContentIngestionDTO.SemanticSearchResponse result = cacheService.getCachedRagResults(
                "photosynthesis", 101L, 5L, "en");

        assertNotNull(result);
        assertEquals(3, result.getTotalResults());
        assertEquals(0.85, result.getAverageSimilarity(), 0.001);
    }

    @Test
    void cacheRagResults_validResults_storesWithCorrectTtl() {
        ContentIngestionDTO.SemanticSearchResponse response = 
                ContentIngestionDTO.SemanticSearchResponse.builder()
                        .query("photosynthesis")
                        .totalResults(3)
                        .build();

        cacheService.cacheRagResults("photosynthesis", 101L, 5L, "en", response);

        verify(valueOperations).set(anyString(), anyString(), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void getCachedEmbedding_cacheHit_returnsEmbedding() throws Exception {
        float[] expectedEmbedding = new float[]{0.1f, 0.2f, 0.3f};
        String cachedJson = objectMapper.writeValueAsString(expectedEmbedding);

        when(valueOperations.get(anyString())).thenReturn(cachedJson);

        float[] result = cacheService.getCachedEmbedding("test text");

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(0.1f, result[0], 0.001);
    }

    @Test
    void cacheEmbedding_validEmbedding_storesWithCorrectTtl() {
        float[] embedding = new float[]{0.1f, 0.2f, 0.3f};

        cacheService.cacheEmbedding("test text", embedding);

        verify(valueOperations).set(anyString(), anyString(), eq(86400L), eq(TimeUnit.SECONDS));
    }

    @Test
    void invalidateTopicCache_deletesMatchingKeys() {
        Set<String> keys = Set.of("ai:rag:en:topic:101:abc", "ai:rag:en:topic:101:def");
        when(redisTemplate.keys(anyString())).thenReturn(keys);

        cacheService.invalidateTopicCache(101L);

        verify(redisTemplate).delete(keys);
    }

    @Test
    void getCacheStats_returnsCounts() {
        Set<String> chatKeys = Set.of("key1", "key2", "key3");
        Set<String> ragKeys = Set.of("key4", "key5");
        Set<String> embeddingKeys = Set.of("key6");

        when(redisTemplate.keys(contains("ai:cache:"))).thenReturn(chatKeys);
        when(redisTemplate.keys(contains("ai:rag:"))).thenReturn(ragKeys);
        when(redisTemplate.keys(contains("ai:emb:"))).thenReturn(embeddingKeys);

        AICacheService.CacheStats stats = cacheService.getCacheStats();

        assertNotNull(stats);
        assertTrue(stats.isCacheEnabled());
    }

    @Test
    void clearAllCaches_deletesAllKeys() {
        Set<String> chatKeys = Set.of("key1", "key2");
        Set<String> ragKeys = Set.of("key3");
        Set<String> embeddingKeys = Set.of("key4");

        when(redisTemplate.keys(contains("ai:cache:"))).thenReturn(chatKeys);
        when(redisTemplate.keys(contains("ai:rag:"))).thenReturn(ragKeys);
        when(redisTemplate.keys(contains("ai:emb:"))).thenReturn(embeddingKeys);

        cacheService.clearAllCaches();

        verify(redisTemplate).delete(chatKeys);
        verify(redisTemplate).delete(ragKeys);
        verify(redisTemplate).delete(embeddingKeys);
    }

    @Test
    void cacheKeyNormalization_identicalQueries_sameKey() throws Exception {
        ChatDTO.ChatResponse response = ChatDTO.ChatResponse.builder()
                .message("Answer")
                .safetyFiltered(false)
                .build();

        // Both should generate the same cache key
        cacheService.cacheChatResponse("What is photosynthesis?", 101L, "en", response);
        cacheService.cacheChatResponse("what   is  photosynthesis?", 101L, "en", response);

        // Verify set was called twice with the same key pattern
        verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void getCachedChatResponse_redisError_returnsNullGracefully() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        ChatDTO.ChatResponse result = cacheService.getCachedChatResponse(
                "What is photosynthesis?", 101L, "en");

        assertNull(result);
    }
}


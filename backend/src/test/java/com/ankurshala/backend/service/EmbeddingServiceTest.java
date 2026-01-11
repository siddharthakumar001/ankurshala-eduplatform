package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.repository.ContentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private AIConfig.AIProperties aiProperties;

    @Mock
    private ContentChunkRepository chunkRepository;

    @InjectMocks
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        // Default mock behavior
    }

    @Test
    void isAvailable_whenModelAndPropertiesSet_returnsTrue() {
        when(aiProperties.isAvailable()).thenReturn(true);

        boolean result = embeddingService.isAvailable();

        assertTrue(result);
    }

    @Test
    void isAvailable_whenModelNull_returnsFalse() {
        // Create a new instance without embedding model
        EmbeddingService serviceWithoutModel = new EmbeddingService();
        
        // Use reflection or setter to inject dependencies
        // For this test, we'll verify the behavior when embeddingModel is null
        assertFalse(serviceWithoutModel.isAvailable());
    }

    @Test
    void generateEmbedding_whenServiceUnavailable_returnsMockEmbedding() {
        when(aiProperties.isAvailable()).thenReturn(false);

        float[] result = embeddingService.generateEmbedding("Test text");

        assertNotNull(result);
        assertEquals(1536, result.length);  // Default dimensions
    }

    @Test
    void cosineSimilarity_identicalVectors_returnsOne() {
        float[] vector = new float[]{0.5f, 0.5f, 0.5f, 0.5f};

        double similarity = embeddingService.cosineSimilarity(vector, vector);

        assertEquals(1.0, similarity, 0.001);
    }

    @Test
    void cosineSimilarity_orthogonalVectors_returnsZero() {
        float[] a = new float[]{1, 0, 0, 0};
        float[] b = new float[]{0, 1, 0, 0};

        double similarity = embeddingService.cosineSimilarity(a, b);

        assertEquals(0.0, similarity, 0.001);
    }

    @Test
    void cosineSimilarity_oppositeVectors_returnsNegativeOne() {
        float[] a = new float[]{1, 0, 0, 0};
        float[] b = new float[]{-1, 0, 0, 0};

        double similarity = embeddingService.cosineSimilarity(a, b);

        assertEquals(-1.0, similarity, 0.001);
    }

    @Test
    void cosineSimilarity_differentLengths_throwsException() {
        float[] a = new float[]{1, 2, 3};
        float[] b = new float[]{1, 2};

        assertThrows(IllegalArgumentException.class, 
                () -> embeddingService.cosineSimilarity(a, b));
    }

    @Test
    void floatArrayToVectorString_validArray_returnsCorrectFormat() {
        float[] embedding = new float[]{0.1f, 0.2f, 0.3f};

        String result = embeddingService.floatArrayToVectorString(embedding);

        assertEquals("[0.1,0.2,0.3]", result);
    }

    @Test
    void vectorStringToFloatArray_validString_returnsCorrectArray() {
        String vectorString = "[0.1,0.2,0.3]";

        float[] result = embeddingService.vectorStringToFloatArray(vectorString);

        assertEquals(3, result.length);
        assertEquals(0.1f, result[0], 0.001);
        assertEquals(0.2f, result[1], 0.001);
        assertEquals(0.3f, result[2], 0.001);
    }

    @Test
    void vectorStringToFloatArray_emptyString_returnsEmptyArray() {
        float[] result = embeddingService.vectorStringToFloatArray("");

        assertEquals(0, result.length);
    }

    @Test
    void vectorStringToFloatArray_null_returnsEmptyArray() {
        float[] result = embeddingService.vectorStringToFloatArray(null);

        assertEquals(0, result.length);
    }

    @Test
    void getEmbeddingDimensions_returnsCorrectValue() {
        assertEquals(1536, embeddingService.getEmbeddingDimensions());
    }

    @Test
    void generateAndStoreEmbedding_callsRepositoryUpdate() {
        when(aiProperties.isAvailable()).thenReturn(false);

        embeddingService.generateAndStoreEmbedding(1L, "Test text");

        verify(chunkRepository).updateEmbedding(eq(1L), anyString());
    }
}


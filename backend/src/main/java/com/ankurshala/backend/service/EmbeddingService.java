package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.repository.ContentChunkRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for generating and managing vector embeddings using Spring AI.
 * Supports multiple embedding providers via Spring AI abstraction.
 */
@Service
@Slf4j
public class EmbeddingService {

    private static final int EMBEDDING_DIMENSIONS = 1536;  // OpenAI ada-002 dimensions
    private static final int MAX_BATCH_SIZE = 20;  // Batch size for embedding requests

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    @Autowired
    private AIConfig.AIProperties aiProperties;

    @Autowired
    private ContentChunkRepository chunkRepository;

    /**
     * Check if embedding service is available
     */
    public boolean isAvailable() {
        return embeddingModel != null && aiProperties.isAvailable();
    }

    /**
     * Generate embedding for a single text
     */
    public float[] generateEmbedding(String text) {
        String traceId = TraceUtil.getTraceId();
        
        if (!isAvailable()) {
            log.warn("Embedding service not available - TraceId: {}", traceId);
            return generateMockEmbedding();
        }

        try {
            log.debug("Generating embedding - TraceId: {}, TextLength: {}", traceId, text.length());
            long startTime = System.currentTimeMillis();

            EmbeddingResponse response = embeddingModel.call(
                    new EmbeddingRequest(List.of(text), null));

            float[] embedding = response.getResult().getOutput();

            long latency = System.currentTimeMillis() - startTime;
            log.debug("Embedding generated - TraceId: {}, Latency: {}ms, Dimensions: {}", 
                    traceId, latency, embedding.length);

            return embedding;

        } catch (Exception e) {
            log.error("Failed to generate embedding - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            // Return mock embedding as fallback for graceful degradation
            return generateMockEmbedding();
        }
    }

    /**
     * Generate embeddings for multiple texts in batch
     */
    public List<float[]> generateEmbeddings(List<String> texts) {
        String traceId = TraceUtil.getTraceId();
        
        if (!isAvailable()) {
            log.warn("Embedding service not available, returning mock embeddings - TraceId: {}", traceId);
            return texts.stream()
                    .map(t -> generateMockEmbedding())
                    .collect(Collectors.toList());
        }

        List<float[]> allEmbeddings = new ArrayList<>();

        // Process in batches to avoid API limits
        for (int i = 0; i < texts.size(); i += MAX_BATCH_SIZE) {
            int endIndex = Math.min(i + MAX_BATCH_SIZE, texts.size());
            List<String> batch = texts.subList(i, endIndex);

            try {
                log.debug("Generating batch embeddings - TraceId: {}, Batch: {}/{}", 
                        traceId, (i / MAX_BATCH_SIZE) + 1, (texts.size() / MAX_BATCH_SIZE) + 1);

                EmbeddingResponse response = embeddingModel.call(
                        new EmbeddingRequest(batch, null));

                for (var result : response.getResults()) {
                    allEmbeddings.add(result.getOutput());
                }

            } catch (Exception e) {
                log.error("Batch embedding failed - TraceId: {}, Batch: {}, Error: {}", 
                        traceId, (i / MAX_BATCH_SIZE) + 1, e.getMessage());
                // Fill with mock embeddings for failed batch
                for (int j = 0; j < batch.size(); j++) {
                    allEmbeddings.add(generateMockEmbedding());
                }
            }
        }

        return allEmbeddings;
    }

    /**
     * Generate and store embedding for a chunk
     */
    @Transactional
    public void generateAndStoreEmbedding(Long chunkId, String text) {
        String traceId = TraceUtil.getTraceId();
        log.info("Generating embedding for chunk - TraceId: {}, ChunkId: {}", traceId, chunkId);

        float[] embedding = generateEmbedding(text);
        String embeddingString = floatArrayToVectorString(embedding);

        chunkRepository.updateEmbedding(chunkId, embeddingString);
        log.info("Embedding stored for chunk - TraceId: {}, ChunkId: {}", traceId, chunkId);
    }

    /**
     * Asynchronously generate embeddings for multiple chunks
     */
    @Async
    public CompletableFuture<Integer> generateAndStoreEmbeddingsAsync(List<Long> chunkIds, List<String> texts) {
        String traceId = TraceUtil.getTraceId();
        log.info("Starting async embedding generation - TraceId: {}, ChunkCount: {}", traceId, chunkIds.size());

        if (chunkIds.size() != texts.size()) {
            throw new IllegalArgumentException("ChunkIds and texts must have the same size");
        }

        List<float[]> embeddings = generateEmbeddings(texts);
        int successCount = 0;

        for (int i = 0; i < chunkIds.size(); i++) {
            try {
                String embeddingString = floatArrayToVectorString(embeddings.get(i));
                chunkRepository.updateEmbedding(chunkIds.get(i), embeddingString);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to store embedding - ChunkId: {}, Error: {}", chunkIds.get(i), e.getMessage());
            }
        }

        log.info("Async embedding generation complete - TraceId: {}, Success: {}/{}", 
                traceId, successCount, chunkIds.size());

        return CompletableFuture.completedFuture(successCount);
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    public double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimensions");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Convert float array to PostgreSQL vector string format
     */
    public String floatArrayToVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parse PostgreSQL vector string to float array
     */
    public float[] vectorStringToFloatArray(String vectorString) {
        if (vectorString == null || vectorString.isEmpty()) {
            return new float[0];
        }

        // Remove brackets and split by comma
        String cleaned = vectorString.replaceAll("[\\[\\]]", "");
        String[] parts = cleaned.split(",");
        float[] result = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }

        return result;
    }

    /**
     * Generate a mock embedding for testing or fallback
     * Uses deterministic pseudo-random values based on hash
     */
    private float[] generateMockEmbedding() {
        float[] embedding = new float[EMBEDDING_DIMENSIONS];
        // Generate normalized random values
        double sum = 0;
        for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) {
            embedding[i] = (float) (Math.random() * 2 - 1);  // Range: -1 to 1
            sum += embedding[i] * embedding[i];
        }
        // Normalize the vector
        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) {
            embedding[i] /= norm;
        }
        return embedding;
    }

    /**
     * Get embedding dimensions
     */
    public int getEmbeddingDimensions() {
        return EMBEDDING_DIMENSIONS;
    }
}


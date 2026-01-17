package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.ContentIngestionDTO;
import com.ankurshala.backend.entity.ContentChunk;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.ContentChunkRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing content chunks for RAG (Retrieval-Augmented Generation).
 * Handles ingestion, embedding, and semantic retrieval of curriculum content.
 */
@Service
@Transactional
@Slf4j
public class ContentChunkService {

    @Autowired
    private ContentChunkRepository chunkRepository;

    @Autowired(required = false)
    private EmbeddingService embeddingService;

    /**
     * Create a new content chunk
     */
    public ContentChunk createChunk(ContentChunk chunk) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating content chunk - TraceId: {}, TopicId: {}, SourceType: {}", 
                traceId, chunk.getTopicId(), chunk.getSourceType());

        // Validate content
        if (chunk.getChunkText() == null || chunk.getChunkText().trim().isEmpty()) {
            throw new IllegalArgumentException("Chunk text cannot be empty");
        }

        chunk.setStatus(ContentChunk.ChunkStatus.ACTIVE);
        ContentChunk saved = chunkRepository.save(chunk);

        // TODO: Generate embedding asynchronously
        // generateAndSaveEmbedding(saved);

        return saved;
    }

    /**
     * Bulk create content chunks (for content ingestion)
     */
    public List<ContentChunk> createChunks(List<ContentChunk> chunks) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating {} content chunks - TraceId: {}", chunks.size(), traceId);

        List<ContentChunk> savedChunks = new ArrayList<>();
        for (ContentChunk chunk : chunks) {
            chunk.setStatus(ContentChunk.ChunkStatus.ACTIVE);
            savedChunks.add(chunkRepository.save(chunk));
        }

        // TODO: Generate embeddings for all chunks asynchronously

        return savedChunks;
    }

    /**
     * Get chunk by ID
     */
    @Transactional(readOnly = true)
    public ContentChunk getChunk(Long chunkId) {
        return chunkRepository.findById(chunkId)
                .orElseThrow(() -> new ResourceNotFoundException("Content chunk not found: " + chunkId));
    }

    /**
     * Get chunks for a topic
     */
    @Transactional(readOnly = true)
    public List<ContentChunk> getChunksForTopic(Long topicId, String language) {
        if (language != null) {
            return chunkRepository.findByTopicIdAndLanguageAndStatus(
                    topicId, language, ContentChunk.ChunkStatus.ACTIVE);
        }
        return chunkRepository.findByTopicIdAndStatus(topicId, ContentChunk.ChunkStatus.ACTIVE);
    }

    /**
     * Semantic search for relevant content chunks using vector embeddings
     */
    @Transactional(readOnly = true)
    public List<ChunkWithScore> searchSimilarChunks(
            String query,
            Long topicId,
            Long subjectId,
            Long gradeId,
            Long boardId,
            String language,
            int limit) {
        
        String traceId = TraceUtil.getTraceId();
        log.info("Searching similar chunks - TraceId: {}, Query length: {}", traceId, query.length());

        // Check if embedding service is available for semantic search
        if (embeddingService != null && embeddingService.isAvailable()) {
            try {
                return performSemanticSearch(query, topicId, subjectId, gradeId, boardId, language, limit);
            } catch (Exception e) {
                log.warn("Semantic search failed, falling back to filter-based search - TraceId: {}, Error: {}", 
                        traceId, e.getMessage());
            }
        }

        // Fallback to filter-based search
        return performFilterSearch(topicId, subjectId, gradeId, boardId, language, limit);
    }

    /**
     * Perform semantic search using vector embeddings
     */
    private List<ChunkWithScore> performSemanticSearch(
            String query,
            Long topicId,
            Long subjectId,
            Long gradeId,
            Long boardId,
            String language,
            int limit) {
        
        String traceId = TraceUtil.getTraceId();
        log.debug("Performing semantic search - TraceId: {}", traceId);

        // Generate query embedding
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        String embeddingString = embeddingService.floatArrayToVectorString(queryEmbedding);

        // Perform vector search with similarity scores
        List<Object[]> resultsWithScore = chunkRepository.findSimilarChunksWithScore(
                embeddingString, topicId, limit);

        List<ChunkWithScore> chunksWithScores = new ArrayList<>();
        for (Object[] row : resultsWithScore) {
            // The query returns the chunk entity and similarity score
            ContentChunk chunk = (ContentChunk) row[0];
            Double similarity = ((Number) row[1]).doubleValue();
            chunksWithScores.add(new ChunkWithScore(chunk, similarity));
        }

        log.debug("Semantic search returned {} results - TraceId: {}", chunksWithScores.size(), traceId);
        return chunksWithScores;
    }

    /**
     * Fallback filter-based search when semantic search is unavailable
     */
    private List<ChunkWithScore> performFilterSearch(
            Long topicId,
            Long subjectId,
            Long gradeId,
            Long boardId,
            String language,
            int limit) {
        
        String traceId = TraceUtil.getTraceId();
        log.debug("Performing filter-based search - TraceId: {}", traceId);

        List<ContentChunk> chunks = chunkRepository.findByFilters(
                topicId, subjectId, gradeId, boardId, language);

        // Return with default similarity score of 0.8 for filter matches
        return chunks.stream()
                .limit(limit)
                .map(chunk -> new ChunkWithScore(chunk, 0.8))
                .collect(Collectors.toList());
    }

    /**
     * Semantic search with request DTO
     */
    @Transactional(readOnly = true)
    public ContentIngestionDTO.SemanticSearchResponse semanticSearch(ContentIngestionDTO.SemanticSearchRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Semantic search request - TraceId: {}, Query: {}", traceId, 
                request.getQuery().substring(0, Math.min(50, request.getQuery().length())) + "...");

        List<ChunkWithScore> results = searchSimilarChunks(
                request.getQuery(),
                request.getTopicId(),
                request.getSubjectId(),
                request.getGradeId(),
                request.getBoardId(),
                request.getLanguage(),
                request.getLimit() != null ? request.getLimit() : 5
        );

        // Filter by minimum similarity if specified
        if (request.getMinSimilarity() != null) {
            results = results.stream()
                    .filter(r -> r.getScore() >= request.getMinSimilarity())
                    .collect(Collectors.toList());
        }

        // Calculate average similarity
        double avgSimilarity = results.stream()
                .mapToDouble(ChunkWithScore::getScore)
                .average()
                .orElse(0.0);

        // Map to response
        List<ContentIngestionDTO.ChunkWithSimilarity> responseResults = results.stream()
                .map(this::mapToChunkWithSimilarity)
                .collect(Collectors.toList());

        return ContentIngestionDTO.SemanticSearchResponse.builder()
                .query(request.getQuery())
                .totalResults(results.size())
                .averageSimilarity(avgSimilarity)
                .results(responseResults)
                .build();
    }

    private ContentIngestionDTO.ChunkWithSimilarity mapToChunkWithSimilarity(ChunkWithScore chunkWithScore) {
        ContentChunk chunk = chunkWithScore.getChunk();
        return ContentIngestionDTO.ChunkWithSimilarity.builder()
                .chunkId(chunk.getId())
                .chunkText(chunk.getChunkText())
                .similarity(chunkWithScore.getScore())
                .topicTitle(chunk.getTopic() != null ? chunk.getTopic().getTitle() : null)
                .chapterName(chunk.getChapter() != null ? chunk.getChapter().getName() : null)
                .subjectName(chunk.getSubject() != null ? chunk.getSubject().getName() : null)
                .sourceType(chunk.getSourceType())
                .sourceRef(chunk.getSourceRef())
                .language(chunk.getLanguage())
                .build();
    }

    /**
     * Retrieve content for RAG context with student profile personalization
     * Returns formatted content suitable for LLM context, filtered by grade and board
     */
    @Transactional(readOnly = true)
    public RetrievalResult retrieveForRAGWithProfile(
            String query,
            Long topicId,
            Long subjectId,
            Long gradeId,
            Long boardId,
            String language,
            int maxChunks) {
        
        String traceId = TraceUtil.getTraceId();
        log.info("Retrieving for RAG with profile - TraceId: {}, TopicId: {}, GradeId: {}, BoardId: {}", 
                traceId, topicId, gradeId, boardId);

        List<ChunkWithScore> results = searchSimilarChunks(
                query, topicId, subjectId, gradeId, boardId, language, maxChunks);

        if (results.isEmpty()) {
            return new RetrievalResult(
                    Collections.emptyList(),
                    "",
                    0,
                    BigDecimal.ZERO
            );
        }

        // Build context string
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            ChunkWithScore result = results.get(i);
            ContentChunk chunk = result.getChunk();
            
            contextBuilder.append("--- Source ").append(i + 1);
            if (chunk.getSourceRef() != null) {
                contextBuilder.append(" (").append(chunk.getSourceRef()).append(")");
            }
            contextBuilder.append(" ---\n");
            contextBuilder.append(chunk.getChunkText());
            contextBuilder.append("\n\n");
        }

        // Calculate average score
        double avgScore = results.stream()
                .mapToDouble(ChunkWithScore::getScore)
                .average()
                .orElse(0.0);

        return new RetrievalResult(
                results,
                contextBuilder.toString(),
                results.size(),
                BigDecimal.valueOf(avgScore)
        );
    }

    /**
     * Retrieve content for RAG context
     * Returns formatted content suitable for LLM context
     */
    @Transactional(readOnly = true)
    public RetrievalResult retrieveForRAG(
            String query,
            Long topicId,
            Long subjectId,
            String language,
            int maxChunks) {
        
        String traceId = TraceUtil.getTraceId();
        log.info("Retrieving for RAG - TraceId: {}, TopicId: {}", traceId, topicId);

        List<ChunkWithScore> results = searchSimilarChunks(
                query, topicId, subjectId, null, null, language, maxChunks);

        if (results.isEmpty()) {
            return new RetrievalResult(
                    Collections.emptyList(),
                    "",
                    0,
                    BigDecimal.ZERO
            );
        }

        // Build context string
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            ChunkWithScore result = results.get(i);
            ContentChunk chunk = result.getChunk();
            
            contextBuilder.append("--- Source ").append(i + 1);
            if (chunk.getSourceRef() != null) {
                contextBuilder.append(" (").append(chunk.getSourceRef()).append(")");
            }
            contextBuilder.append(" ---\n");
            contextBuilder.append(chunk.getChunkText());
            contextBuilder.append("\n\n");
        }

        // Calculate average score
        double avgScore = results.stream()
                .mapToDouble(ChunkWithScore::getScore)
                .average()
                .orElse(0.0);

        return new RetrievalResult(
                results,
                contextBuilder.toString(),
                results.size(),
                BigDecimal.valueOf(avgScore)
        );
    }

    /**
     * Update chunk status
     */
    public ContentChunk updateStatus(Long chunkId, ContentChunk.ChunkStatus status) {
        ContentChunk chunk = getChunk(chunkId);
        chunk.setStatus(status);
        return chunkRepository.save(chunk);
    }

    /**
     * Verify a chunk (admin action)
     */
    public ContentChunk verifyChunk(Long chunkId, Long adminId) {
        ContentChunk chunk = getChunk(chunkId);
        chunk.verify(adminId);
        return chunkRepository.save(chunk);
    }

    /**
     * Get unverified chunks for review
     */
    @Transactional(readOnly = true)
    public Page<ContentChunk> getUnverifiedChunks(Pageable pageable) {
        return chunkRepository.findUnverifiedChunks(pageable);
    }

    /**
     * Get chunks by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<ContentChunk> getChunksByStatus(ContentChunk.ChunkStatus status, Pageable pageable) {
        return chunkRepository.findByStatus(status, pageable);
    }

    /**
     * Get chunks by topic with pagination
     */
    @Transactional(readOnly = true)
    public Page<ContentChunk> getChunksByTopic(Long topicId, Pageable pageable) {
        return chunkRepository.findByTopicId(topicId, pageable);
    }

    /**
     * Get all chunks with pagination
     */
    @Transactional(readOnly = true)
    public Page<ContentChunk> getAllChunks(Pageable pageable) {
        return chunkRepository.findAll(pageable);
    }

    /**
     * Delete a chunk (soft delete by setting status to ARCHIVED)
     */
    public void archiveChunk(Long chunkId) {
        ContentChunk chunk = getChunk(chunkId);
        chunk.setStatus(ContentChunk.ChunkStatus.ARCHIVED);
        chunkRepository.save(chunk);
    }

    /**
     * Count chunks without embeddings (for monitoring)
     */
    @Transactional(readOnly = true)
    public long countChunksNeedingEmbeddings() {
        return chunkRepository.countChunksWithoutEmbeddings();
    }

    /**
     * Split text into chunks for ingestion
     * Uses simple sentence-based splitting with overlap
     */
    public List<String> splitTextIntoChunks(String text, int maxChunkSize, int overlapSize) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        StringBuilder currentChunk = new StringBuilder();
        StringBuilder overlapBuffer = new StringBuilder();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > maxChunkSize && currentChunk.length() > 0) {
                // Save current chunk
                chunks.add(currentChunk.toString().trim());
                
                // Start new chunk with overlap
                currentChunk = new StringBuilder();
                if (overlapBuffer.length() > 0) {
                    currentChunk.append(overlapBuffer);
                }
                overlapBuffer = new StringBuilder();
            }
            
            currentChunk.append(sentence).append(" ");
            
            // Update overlap buffer (keep last few sentences)
            overlapBuffer.append(sentence).append(" ");
            if (overlapBuffer.length() > overlapSize) {
                String overlap = overlapBuffer.toString();
                int cutPoint = overlap.length() - overlapSize;
                overlapBuffer = new StringBuilder(overlap.substring(cutPoint));
            }
        }

        // Add final chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    // Inner classes for results

    public static class ChunkWithScore {
        private final ContentChunk chunk;
        private final double score;

        public ChunkWithScore(ContentChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }

        public ContentChunk getChunk() { return chunk; }
        public double getScore() { return score; }
    }

    public static class RetrievalResult {
        private final List<ChunkWithScore> chunks;
        private final String contextString;
        private final int retrievalCount;
        private final BigDecimal averageScore;

        public RetrievalResult(List<ChunkWithScore> chunks, String contextString, 
                               int retrievalCount, BigDecimal averageScore) {
            this.chunks = chunks;
            this.contextString = contextString;
            this.retrievalCount = retrievalCount;
            this.averageScore = averageScore;
        }

        public List<ChunkWithScore> getChunks() { return chunks; }
        public String getContextString() { return contextString; }
        public int getRetrievalCount() { return retrievalCount; }
        public BigDecimal getAverageScore() { return averageScore; }
    }
}


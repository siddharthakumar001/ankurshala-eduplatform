package com.ankurshala.backend.dto.ai;

import com.ankurshala.backend.entity.ContentChunk;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for content chunk ingestion and management
 */
public class ContentIngestionDTO {

    /**
     * Request to ingest a single content chunk
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingleChunkRequest {
        @NotNull(message = "Topic ID is required")
        private Long topicId;

        private Long chapterId;
        private Long subjectId;
        private Long gradeId;
        private Long boardId;

        @NotBlank(message = "Chunk text is required")
        @Size(min = 10, max = 10000, message = "Chunk text must be between 10 and 10000 characters")
        private String chunkText;

        @NotNull(message = "Source type is required")
        private ContentChunk.SourceType sourceType;

        private String sourceRef;

        @Builder.Default
        private String language = "en";

        private Map<String, Object> metadata;
    }

    /**
     * Request to ingest content from raw text (will be chunked automatically)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkIngestRequest {
        @NotNull(message = "Topic ID is required")
        private Long topicId;

        private Long chapterId;
        private Long subjectId;
        private Long gradeId;
        private Long boardId;

        @NotBlank(message = "Content is required")
        private String rawContent;

        @NotNull(message = "Source type is required")
        private ContentChunk.SourceType sourceType;

        private String sourceRef;

        @Builder.Default
        private String language = "en";

        @Builder.Default
        private Integer maxChunkSize = 1000;

        @Builder.Default
        private Integer overlapSize = 100;

        private Map<String, Object> metadata;
    }

    /**
     * Response for chunk creation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkResponse {
        private Long id;
        private Long topicId;
        private String topicTitle;
        private Long chapterId;
        private String chapterName;
        private Long subjectId;
        private String subjectName;
        private Long gradeId;
        private Long boardId;
        private ContentChunk.SourceType sourceType;
        private String sourceRef;
        private String chunkText;
        private String language;
        private Boolean isVerified;
        private ContentChunk.ChunkStatus status;
        private Boolean hasEmbedding;
        private Map<String, Object> metadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * Response for bulk ingestion
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkIngestResponse {
        private Integer chunksCreated;
        private Integer embeddingsQueued;
        private List<ChunkResponse> chunks;
        private String message;
    }

    /**
     * Request for semantic search
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemanticSearchRequest {
        @NotBlank(message = "Query is required")
        @Size(max = 1000, message = "Query cannot exceed 1000 characters")
        private String query;

        private Long topicId;
        private Long subjectId;
        private Long gradeId;
        private Long boardId;
        private String language;

        @Builder.Default
        private Integer limit = 5;

        @Builder.Default
        private Double minSimilarity = 0.7;
    }

    /**
     * Response for semantic search
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemanticSearchResponse {
        private String query;
        private Integer totalResults;
        private Double averageSimilarity;
        private List<ChunkWithSimilarity> results;
    }

    /**
     * Chunk with similarity score
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkWithSimilarity {
        private Long chunkId;
        private String chunkText;
        private Double similarity;
        private String topicTitle;
        private String chapterName;
        private String subjectName;
        private ContentChunk.SourceType sourceType;
        private String sourceRef;
        private String language;
    }

    /**
     * Request to verify a chunk
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyChunkRequest {
        @NotNull(message = "Chunk ID is required")
        private Long chunkId;

        private Boolean approved;  // If false, mark as ARCHIVED instead
        private String reviewNote;
    }

    /**
     * Request to update chunk status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull(message = "Status is required")
        private ContentChunk.ChunkStatus status;
    }

    /**
     * Request to regenerate embeddings
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegenerateEmbeddingsRequest {
        private Long topicId;        // Regenerate for a specific topic
        private Long subjectId;      // Regenerate for a specific subject
        private Boolean forceAll;    // Regenerate even if embeddings exist

        @Builder.Default
        private Integer batchSize = 50;
    }

    /**
     * Response for embedding regeneration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegenerateEmbeddingsResponse {
        private Integer totalChunks;
        private Integer chunksProcessed;
        private Integer chunksQueued;
        private String message;
        private String jobId;  // For async tracking
    }

    /**
     * Content statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentStats {
        private Long totalChunks;
        private Long activeChunks;
        private Long pendingReviewChunks;
        private Long archivedChunks;
        private Long chunksWithEmbeddings;
        private Long chunksWithoutEmbeddings;
        private Map<String, Long> chunksBySourceType;
        private Map<String, Long> chunksByLanguage;
        private Map<Long, Long> chunksBySubject;
    }
}


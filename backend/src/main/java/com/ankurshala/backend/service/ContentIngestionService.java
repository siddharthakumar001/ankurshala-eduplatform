package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.ContentIngestionDTO;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for ingesting curriculum content into the RAG system.
 * Handles chunking, embedding generation, and storage.
 */
@Service
@Slf4j
public class ContentIngestionService {

    @Autowired
    private ContentChunkRepository chunkRepository;

    @Autowired
    private TopicRepository topicRepository;

    // Reserved for future content hierarchy enrichment
    @SuppressWarnings("unused")
    @Autowired
    private ChapterRepository chapterRepository;

    // Reserved for future content hierarchy enrichment
    @SuppressWarnings("unused")
    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ContentChunkService contentChunkService;

    @Autowired
    private EmbeddingService embeddingService;

    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP = 100;
    private static final int EMBEDDING_BATCH_SIZE = 20;

    /**
     * Ingest a single content chunk
     */
    @Transactional
    public ContentIngestionDTO.ChunkResponse ingestSingleChunk(ContentIngestionDTO.SingleChunkRequest request, Long adminId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Ingesting single chunk - TraceId: {}, TopicId: {}, SourceType: {}", 
                traceId, request.getTopicId(), request.getSourceType());

        // Validate topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.getTopicId()));

        // Enrich with hierarchy info if not provided
        Long chapterId = request.getChapterId() != null ? request.getChapterId() : topic.getChapterId();
        Long subjectId = request.getSubjectId() != null ? request.getSubjectId() : 
                (topic.getChapter() != null ? topic.getChapter().getSubjectId() : null);

        ContentChunk chunk = new ContentChunk();
        chunk.setTopicId(request.getTopicId());
        chunk.setChapterId(chapterId);
        chunk.setSubjectId(subjectId);
        chunk.setGradeId(request.getGradeId());
        chunk.setBoardId(request.getBoardId());
        chunk.setChunkText(request.getChunkText());
        chunk.setSourceType(request.getSourceType());
        chunk.setSourceRef(request.getSourceRef());
        chunk.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        chunk.setMetadata(request.getMetadata());
        chunk.setStatus(ContentChunk.ChunkStatus.ACTIVE);

        ContentChunk savedChunk = chunkRepository.save(chunk);

        // Generate embedding asynchronously
        generateEmbeddingAsync(savedChunk.getId(), savedChunk.getChunkText());

        log.info("Chunk ingested - TraceId: {}, ChunkId: {}", traceId, savedChunk.getId());
        return mapToResponse(savedChunk);
    }

    /**
     * Ingest bulk content by splitting into chunks
     */
    @Transactional
    public ContentIngestionDTO.BulkIngestResponse ingestBulkContent(ContentIngestionDTO.BulkIngestRequest request, Long adminId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Starting bulk ingestion - TraceId: {}, TopicId: {}, ContentLength: {}", 
                traceId, request.getTopicId(), request.getRawContent().length());

        // Validate topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.getTopicId()));

        // Split content into chunks
        int maxChunkSize = request.getMaxChunkSize() != null ? request.getMaxChunkSize() : DEFAULT_CHUNK_SIZE;
        int overlapSize = request.getOverlapSize() != null ? request.getOverlapSize() : DEFAULT_OVERLAP;
        
        List<String> textChunks = contentChunkService.splitTextIntoChunks(
                request.getRawContent(), maxChunkSize, overlapSize);

        log.info("Content split into {} chunks - TraceId: {}", textChunks.size(), traceId);

        // Enrich with hierarchy info
        Long chapterId = request.getChapterId() != null ? request.getChapterId() : topic.getChapterId();
        Long subjectId = request.getSubjectId() != null ? request.getSubjectId() : 
                (topic.getChapter() != null ? topic.getChapter().getSubjectId() : null);

        List<ContentChunk> savedChunks = new ArrayList<>();
        int chunkIndex = 0;

        for (String text : textChunks) {
            ContentChunk chunk = new ContentChunk();
            chunk.setTopicId(request.getTopicId());
            chunk.setChapterId(chapterId);
            chunk.setSubjectId(subjectId);
            chunk.setGradeId(request.getGradeId());
            chunk.setBoardId(request.getBoardId());
            chunk.setChunkText(text);
            chunk.setSourceType(request.getSourceType());
            chunk.setSourceRef(request.getSourceRef() + " (chunk " + (chunkIndex + 1) + ")");
            chunk.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
            chunk.setStatus(ContentChunk.ChunkStatus.ACTIVE);

            // Add metadata with chunk info
            Map<String, Object> metadata = new HashMap<>();
            if (request.getMetadata() != null) {
                metadata.putAll(request.getMetadata());
            }
            metadata.put("chunkIndex", chunkIndex);
            metadata.put("totalChunks", textChunks.size());
            chunk.setMetadata(metadata);

            savedChunks.add(chunkRepository.save(chunk));
            chunkIndex++;
        }

        // Generate embeddings asynchronously in batches
        queueEmbeddingGeneration(savedChunks);

        log.info("Bulk ingestion complete - TraceId: {}, ChunksCreated: {}", traceId, savedChunks.size());

        return ContentIngestionDTO.BulkIngestResponse.builder()
                .chunksCreated(savedChunks.size())
                .embeddingsQueued(savedChunks.size())
                .chunks(savedChunks.stream().map(this::mapToResponse).collect(Collectors.toList()))
                .message("Successfully ingested " + savedChunks.size() + " chunks. Embeddings are being generated.")
                .build();
    }

    /**
     * Verify a chunk (admin approval)
     */
    @Transactional
    public ContentIngestionDTO.ChunkResponse verifyChunk(ContentIngestionDTO.VerifyChunkRequest request, Long adminId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Verifying chunk - TraceId: {}, ChunkId: {}, Approved: {}", 
                traceId, request.getChunkId(), request.getApproved());

        ContentChunk chunk = chunkRepository.findById(request.getChunkId())
                .orElseThrow(() -> new ResourceNotFoundException("Chunk not found: " + request.getChunkId()));

        if (Boolean.TRUE.equals(request.getApproved())) {
            chunk.verify(adminId);
        } else {
            chunk.setStatus(ContentChunk.ChunkStatus.ARCHIVED);
        }

        ContentChunk savedChunk = chunkRepository.save(chunk);
        log.info("Chunk verified - TraceId: {}, ChunkId: {}, Status: {}", traceId, chunk.getId(), chunk.getStatus());
        return mapToResponse(savedChunk);
    }

    /**
     * Update chunk status
     */
    @Transactional
    public ContentIngestionDTO.ChunkResponse updateChunkStatus(Long chunkId, ContentChunk.ChunkStatus status) {
        String traceId = TraceUtil.getTraceId();
        log.info("Updating chunk status - TraceId: {}, ChunkId: {}, NewStatus: {}", traceId, chunkId, status);

        ContentChunk chunk = chunkRepository.findById(chunkId)
                .orElseThrow(() -> new ResourceNotFoundException("Chunk not found: " + chunkId));

        chunk.setStatus(status);
        ContentChunk savedChunk = chunkRepository.save(chunk);
        return mapToResponse(savedChunk);
    }

    /**
     * Regenerate embeddings for chunks
     */
    @Transactional
    public ContentIngestionDTO.RegenerateEmbeddingsResponse regenerateEmbeddings(
            ContentIngestionDTO.RegenerateEmbeddingsRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        log.info("Regenerating embeddings - TraceId: {}, TopicId: {}, SubjectId: {}, ForceAll: {}", 
                traceId, request.getTopicId(), request.getSubjectId(), request.getForceAll());

        List<ContentChunk> chunks;

        if (request.getTopicId() != null) {
            chunks = chunkRepository.findByTopicIdAndStatus(request.getTopicId(), ContentChunk.ChunkStatus.ACTIVE);
        } else if (request.getSubjectId() != null) {
            chunks = chunkRepository.findBySubjectIdAndStatus(request.getSubjectId(), ContentChunk.ChunkStatus.ACTIVE);
        } else {
            // Get all active chunks without embeddings
            if (!Boolean.TRUE.equals(request.getForceAll())) {
                chunks = chunkRepository.findAll().stream()
                        .filter(c -> c.getStatus() == ContentChunk.ChunkStatus.ACTIVE && c.getEmbedding() == null)
                        .collect(Collectors.toList());
            } else {
                chunks = chunkRepository.findByStatus(ContentChunk.ChunkStatus.ACTIVE, null).getContent();
            }
        }

        // Filter out chunks that already have embeddings (unless forceAll)
        if (!Boolean.TRUE.equals(request.getForceAll())) {
            chunks = chunks.stream()
                    .filter(c -> c.getEmbedding() == null)
                    .collect(Collectors.toList());
        }

        int totalChunks = chunks.size();
        log.info("Found {} chunks needing embeddings - TraceId: {}", totalChunks, traceId);

        // Queue embedding generation
        queueEmbeddingGeneration(chunks);

        String jobId = UUID.randomUUID().toString();

        return ContentIngestionDTO.RegenerateEmbeddingsResponse.builder()
                .totalChunks(totalChunks)
                .chunksProcessed(0)
                .chunksQueued(totalChunks)
                .message("Embedding generation queued for " + totalChunks + " chunks")
                .jobId(jobId)
                .build();
    }

    /**
     * Get content statistics
     */
    @Transactional(readOnly = true)
    public ContentIngestionDTO.ContentStats getContentStats() {
        String traceId = TraceUtil.getTraceId();
        log.debug("Getting content stats - TraceId: {}", traceId);

        long total = chunkRepository.count();
        long active = chunkRepository.findByStatus(ContentChunk.ChunkStatus.ACTIVE, null).getTotalElements();
        long pending = chunkRepository.findByStatus(ContentChunk.ChunkStatus.PENDING_REVIEW, null).getTotalElements();
        long archived = chunkRepository.findByStatus(ContentChunk.ChunkStatus.ARCHIVED, null).getTotalElements();
        long withoutEmbeddings = chunkRepository.countChunksWithoutEmbeddings();
        long withEmbeddings = active - withoutEmbeddings;

        // Group by source type
        Map<String, Long> bySourceType = new HashMap<>();
        for (ContentChunk.SourceType type : ContentChunk.SourceType.values()) {
            long count = chunkRepository.findBySourceTypeAndStatus(type, ContentChunk.ChunkStatus.ACTIVE).size();
            if (count > 0) {
                bySourceType.put(type.name(), count);
            }
        }

        return ContentIngestionDTO.ContentStats.builder()
                .totalChunks(total)
                .activeChunks(active)
                .pendingReviewChunks(pending)
                .archivedChunks(archived)
                .chunksWithEmbeddings(withEmbeddings)
                .chunksWithoutEmbeddings(withoutEmbeddings)
                .chunksBySourceType(bySourceType)
                .build();
    }

    /**
     * Asynchronously generate embedding for a single chunk
     */
    @Async
    protected void generateEmbeddingAsync(Long chunkId, String text) {
        try {
            embeddingService.generateAndStoreEmbedding(chunkId, text);
        } catch (Exception e) {
            log.error("Failed to generate embedding for chunk {}", chunkId, e);
        }
    }

    /**
     * Queue embedding generation for multiple chunks
     */
    @Async
    protected void queueEmbeddingGeneration(List<ContentChunk> chunks) {
        String traceId = TraceUtil.getTraceId();
        log.info("Queueing embedding generation for {} chunks - TraceId: {}", chunks.size(), traceId);

        // Process in batches
        for (int i = 0; i < chunks.size(); i += EMBEDDING_BATCH_SIZE) {
            int endIndex = Math.min(i + EMBEDDING_BATCH_SIZE, chunks.size());
            List<ContentChunk> batch = chunks.subList(i, endIndex);

            List<Long> chunkIds = batch.stream().map(ContentChunk::getId).collect(Collectors.toList());
            List<String> texts = batch.stream().map(ContentChunk::getChunkText).collect(Collectors.toList());

            try {
                embeddingService.generateAndStoreEmbeddingsAsync(chunkIds, texts);
                log.debug("Batch {}/{} embeddings queued - TraceId: {}", 
                        (i / EMBEDDING_BATCH_SIZE) + 1, (chunks.size() / EMBEDDING_BATCH_SIZE) + 1, traceId);
            } catch (Exception e) {
                log.error("Failed to queue embeddings for batch {}", (i / EMBEDDING_BATCH_SIZE) + 1, e);
            }
        }
    }

    /**
     * Map entity to response DTO
     */
    private ContentIngestionDTO.ChunkResponse mapToResponse(ContentChunk chunk) {
        ContentIngestionDTO.ChunkResponse.ChunkResponseBuilder builder = ContentIngestionDTO.ChunkResponse.builder()
                .id(chunk.getId())
                .topicId(chunk.getTopicId())
                .chapterId(chunk.getChapterId())
                .subjectId(chunk.getSubjectId())
                .gradeId(chunk.getGradeId())
                .boardId(chunk.getBoardId())
                .sourceType(chunk.getSourceType())
                .sourceRef(chunk.getSourceRef())
                .chunkText(chunk.getChunkText())
                .language(chunk.getLanguage())
                .isVerified(chunk.getIsVerified())
                .status(chunk.getStatus())
                .hasEmbedding(chunk.getEmbedding() != null)
                .metadata(chunk.getMetadata())
                .createdAt(chunk.getCreatedAt())
                .updatedAt(chunk.getUpdatedAt());

        // Add related entity names if available
        if (chunk.getTopic() != null) {
            builder.topicTitle(chunk.getTopic().getTitle());
        }
        if (chunk.getChapter() != null) {
            builder.chapterName(chunk.getChapter().getName());
        }
        if (chunk.getSubject() != null) {
            builder.subjectName(chunk.getSubject().getName());
        }

        return builder.build();
    }
}


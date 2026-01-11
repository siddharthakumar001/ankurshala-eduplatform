package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.ai.ContentIngestionDTO;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.entity.ContentChunk;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.ContentChunkService;
import com.ankurshala.backend.service.ContentIngestionService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin endpoints for managing RAG content chunks.
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", 
        "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/admin/content")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Content Management", description = "APIs for managing RAG content chunks")
public class AdminContentController {

    @Autowired
    private ContentIngestionService contentIngestionService;

    @Autowired
    private ContentChunkService contentChunkService;

    @Autowired
    private LoggingService loggingService;

    @PostMapping("/chunks")
    @Operation(summary = "Ingest a single content chunk")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.ChunkResponse>> ingestSingleChunk(
            @Valid @RequestBody ContentIngestionDTO.SingleChunkRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long adminId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("adminId", adminId);
        context.put("topicId", request.getTopicId());
        context.put("sourceType", request.getSourceType());

        loggingService.logBusinessOperationStart("INGEST_SINGLE_CHUNK", adminId.toString(), context);

        try {
            ContentIngestionDTO.ChunkResponse response = contentIngestionService.ingestSingleChunk(request, adminId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("INGEST_SINGLE_CHUNK", adminId.toString(), true, executionTime);

            ApiResponse<ContentIngestionDTO.ChunkResponse> apiResponse = ApiResponse.success(response, "Content chunk ingested successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("INGEST_SINGLE_CHUNK", adminId.toString(), false, executionTime);
            loggingService.logError("INGEST_SINGLE_CHUNK", e, context);
            throw e;
        }
    }

    @PostMapping("/chunks/bulk")
    @Operation(summary = "Ingest content by splitting into chunks automatically")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.BulkIngestResponse>> ingestBulkContent(
            @Valid @RequestBody ContentIngestionDTO.BulkIngestRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long adminId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("adminId", adminId);
        context.put("topicId", request.getTopicId());
        context.put("contentLength", request.getRawContent().length());

        loggingService.logBusinessOperationStart("INGEST_BULK_CONTENT", adminId.toString(), context);

        try {
            ContentIngestionDTO.BulkIngestResponse response = contentIngestionService.ingestBulkContent(request, adminId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("INGEST_BULK_CONTENT", adminId.toString(), true, executionTime);

            ApiResponse<ContentIngestionDTO.BulkIngestResponse> apiResponse = ApiResponse.success(response, "Bulk content ingested successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("INGEST_BULK_CONTENT", adminId.toString(), false, executionTime);
            loggingService.logError("INGEST_BULK_CONTENT", e, context);
            throw e;
        }
    }

    @GetMapping("/chunks")
    @Operation(summary = "List content chunks with filters")
    public ResponseEntity<ApiResponse<Page<ContentIngestionDTO.ChunkResponse>>> listChunks(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) ContentChunk.ChunkStatus status,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        // Authentication verified by @PreAuthorize
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ContentChunk> chunks;
        if (status != null) {
            chunks = contentChunkService.getChunksByStatus(status, pageable);
        } else if (topicId != null) {
            chunks = contentChunkService.getChunksByTopic(topicId, pageable);
        } else {
            chunks = contentChunkService.getAllChunks(pageable);
        }

        Page<ContentIngestionDTO.ChunkResponse> response = chunks.map(this::mapToChunkResponse);

        ApiResponse<Page<ContentIngestionDTO.ChunkResponse>> apiResponse = ApiResponse.success(response, "Chunks retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/chunks/{chunkId}")
    @Operation(summary = "Get a specific content chunk")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.ChunkResponse>> getChunk(@PathVariable Long chunkId) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        ContentChunk chunk = contentChunkService.getChunk(chunkId);
        ContentIngestionDTO.ChunkResponse response = mapToChunkResponse(chunk);

        ApiResponse<ContentIngestionDTO.ChunkResponse> apiResponse = ApiResponse.success(response, "Chunk retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/chunks/{chunkId}/status")
    @Operation(summary = "Update chunk status")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.ChunkResponse>> updateChunkStatus(
            @PathVariable Long chunkId,
            @Valid @RequestBody ContentIngestionDTO.UpdateStatusRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        ContentIngestionDTO.ChunkResponse response = contentIngestionService.updateChunkStatus(chunkId, request.getStatus());

        ApiResponse<ContentIngestionDTO.ChunkResponse> apiResponse = ApiResponse.success(response, "Chunk status updated");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/chunks/{chunkId}/verify")
    @Operation(summary = "Verify or reject a content chunk")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.ChunkResponse>> verifyChunk(
            @PathVariable Long chunkId,
            @Valid @RequestBody ContentIngestionDTO.VerifyChunkRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long adminId = userPrincipal.getId();

        request.setChunkId(chunkId);
        ContentIngestionDTO.ChunkResponse response = contentIngestionService.verifyChunk(request, adminId);

        ApiResponse<ContentIngestionDTO.ChunkResponse> apiResponse = ApiResponse.success(response, "Chunk verified successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/chunks/pending-review")
    @Operation(summary = "Get chunks pending review")
    public ResponseEntity<ApiResponse<Page<ContentIngestionDTO.ChunkResponse>>> getPendingReviewChunks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<ContentChunk> chunks = contentChunkService.getUnverifiedChunks(pageable);
        Page<ContentIngestionDTO.ChunkResponse> response = chunks.map(this::mapToChunkResponse);

        ApiResponse<Page<ContentIngestionDTO.ChunkResponse>> apiResponse = ApiResponse.success(response, "Pending review chunks retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/chunks/search")
    @Operation(summary = "Semantic search across content chunks")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.SemanticSearchResponse>> semanticSearch(
            @Valid @RequestBody ContentIngestionDTO.SemanticSearchRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        ContentIngestionDTO.SemanticSearchResponse response = contentChunkService.semanticSearch(request);

        ApiResponse<ContentIngestionDTO.SemanticSearchResponse> apiResponse = ApiResponse.success(response, "Search completed");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/embeddings/regenerate")
    @Operation(summary = "Regenerate embeddings for chunks")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.RegenerateEmbeddingsResponse>> regenerateEmbeddings(
            @Valid @RequestBody ContentIngestionDTO.RegenerateEmbeddingsRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        // Authentication verified by @PreAuthorize
        ContentIngestionDTO.RegenerateEmbeddingsResponse response = contentIngestionService.regenerateEmbeddings(request);

        ApiResponse<ContentIngestionDTO.RegenerateEmbeddingsResponse> apiResponse = ApiResponse.success(response, "Embedding regeneration queued");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get content statistics")
    public ResponseEntity<ApiResponse<ContentIngestionDTO.ContentStats>> getContentStats() {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        ContentIngestionDTO.ContentStats stats = contentIngestionService.getContentStats();

        ApiResponse<ContentIngestionDTO.ContentStats> apiResponse = ApiResponse.success(stats, "Content stats retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/chunks/{chunkId}")
    @Operation(summary = "Archive a content chunk")
    public ResponseEntity<ApiResponse<Void>> archiveChunk(@PathVariable Long chunkId) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        contentChunkService.archiveChunk(chunkId);

        ApiResponse<Void> apiResponse = ApiResponse.success(null, "Chunk archived successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Map ContentChunk entity to response DTO
     */
    private ContentIngestionDTO.ChunkResponse mapToChunkResponse(ContentChunk chunk) {
        return ContentIngestionDTO.ChunkResponse.builder()
                .id(chunk.getId())
                .topicId(chunk.getTopicId())
                .topicTitle(chunk.getTopic() != null ? chunk.getTopic().getTitle() : null)
                .chapterId(chunk.getChapterId())
                .chapterName(chunk.getChapter() != null ? chunk.getChapter().getName() : null)
                .subjectId(chunk.getSubjectId())
                .subjectName(chunk.getSubject() != null ? chunk.getSubject().getName() : null)
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
                .updatedAt(chunk.getUpdatedAt())
                .build();
    }
}


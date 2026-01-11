package com.ankurshala.backend.controller;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.dto.ai.ChatDTO;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.*;
import com.ankurshala.backend.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Student-facing AI chat endpoints.
 * Provides access to the AI Tutor for personalized learning assistance.
 * Includes rate limiting, caching, and usage tracking.
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", 
        "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/student/ai")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student AI Chat", description = "AI Tutor chat APIs for students")
public class StudentChatController {

    @Autowired
    private AITutorService aiTutorService;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private AIRateLimitService rateLimitService;

    @Autowired
    private AICacheService cacheService;

    @Autowired
    private AIConfig.AIProperties aiProperties;

    @PostMapping("/chat")
    @Operation(summary = "Send a message to the AI Tutor")
    public ResponseEntity<ApiResponse<ChatDTO.ChatResponse>> chat(
            @Valid @RequestBody ChatDTO.ChatRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("sessionId", request.getSessionId());
        context.put("topicId", request.getTopicId());
        context.put("messageLength", request.getMessage().length());

        loggingService.logBusinessOperationStart("AI_CHAT", studentId.toString(), context);

        // Check rate limit
        AIRateLimitService.RateLimitResult rateLimitResult = rateLimitService.checkChatLimit(studentId);
        if (!rateLimitResult.isAllowed()) {
            log.warn("Rate limit exceeded for student {} - Type: {}", studentId, rateLimitResult.getLimitType());
            ApiResponse<ChatDTO.ChatResponse> apiResponse = ApiResponse.error(rateLimitResult.getMessage());
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(apiResponse);
        }

        // Check cache for similar queries
        ChatDTO.ChatResponse cachedResponse = cacheService.getCachedChatResponse(
                request.getMessage(), request.getTopicId(), request.getLanguage());
        if (cachedResponse != null) {
            log.debug("Returning cached response for student {}", studentId);
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("AI_CHAT", studentId.toString(), true, executionTime);

            ApiResponse<ChatDTO.ChatResponse> apiResponse = ApiResponse.success(cachedResponse, "Chat response (cached)");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            return ResponseEntity.ok(apiResponse);
        }

        try {
            ChatDTO.ChatResponse response = aiTutorService.chat(request, studentId);

            // Cache the response
            cacheService.cacheChatResponse(request.getMessage(), request.getTopicId(), 
                    request.getLanguage(), response);

            // Record token usage for rate limiting
            if (response.getTokensUsed() != null && response.getTokensUsed() > 0) {
                rateLimitService.recordTokenUsage(studentId, 0, response.getTokensUsed());
            }

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("AI_CHAT", studentId.toString(), true, executionTime);

            ApiResponse<ChatDTO.ChatResponse> apiResponse = ApiResponse.success(response, "Chat response generated");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            // Add rate limit headers
            if (rateLimitResult.getRemainingRequests() >= 0) {
                return ResponseEntity.ok()
                        .header("X-RateLimit-Remaining", String.valueOf(rateLimitResult.getRemainingRequests()))
                        .header("X-RateLimit-Daily-Remaining", String.valueOf(rateLimitResult.getRemainingDaily()))
                        .body(apiResponse);
            }

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("AI_CHAT", studentId.toString(), false, executionTime);
            loggingService.logError("AI_CHAT", e, context);
            throw e;
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream chat response from AI Tutor (Server-Sent Events)")
    public Flux<ChatDTO.StreamChunk> streamChat(
            @Valid @RequestBody ChatDTO.ChatRequest request,
            Authentication authentication) {

        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        // Check rate limit for streaming
        AIRateLimitService.RateLimitResult rateLimitResult = rateLimitService.checkStreamChatLimit(studentId);
        if (!rateLimitResult.isAllowed()) {
            return Flux.just(ChatDTO.StreamChunk.builder()
                    .sessionId(request.getSessionId())
                    .content(rateLimitResult.getMessage())
                    .isComplete(true)
                    .chunkIndex(0)
                    .build());
        }

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("sessionId", request.getSessionId());
        context.put("topicId", request.getTopicId());

        loggingService.logBusinessOperationStart("AI_CHAT_STREAM", studentId.toString(), context);

        return aiTutorService.streamChat(request, studentId)
                .doOnComplete(() -> {
                    long executionTime = System.currentTimeMillis() - startTime;
                    loggingService.logBusinessOperationComplete("AI_CHAT_STREAM", studentId.toString(), true, executionTime);
                })
                .doOnError(e -> {
                    long executionTime = System.currentTimeMillis() - startTime;
                    loggingService.logBusinessOperationComplete("AI_CHAT_STREAM", studentId.toString(), false, executionTime);
                    loggingService.logError("AI_CHAT_STREAM", (Exception) e, context);
                });
    }

    @GetMapping("/health")
    @Operation(summary = "Check AI service health status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck(Authentication authentication) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> status = new HashMap<>();
        status.put("available", aiProperties.isAvailable());
        status.put("model", aiProperties.getModel());
        status.put("provider", aiProperties.getProvider());
        status.put("safetyEnabled", aiProperties.isSafetyEnabled());

        // Include rate limit status
        AIRateLimitService.UsageStats usageStats = rateLimitService.getUsageStats(studentId);
        status.put("usageStats", usageStats);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(status, "AI service health check");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/usage")
    @Operation(summary = "Get student's AI usage statistics")
    public ResponseEntity<ApiResponse<AIRateLimitService.UsageStats>> getUsageStats(Authentication authentication) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        AIRateLimitService.UsageStats stats = rateLimitService.getUsageStats(studentId);

        ApiResponse<AIRateLimitService.UsageStats> apiResponse = ApiResponse.success(stats, "Usage stats retrieved");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);

        return ResponseEntity.ok(apiResponse);
    }
}


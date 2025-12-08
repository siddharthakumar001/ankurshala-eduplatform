package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.ai.*;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.AIService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;
    @Autowired
    private LoggingService loggingService;

    @PostMapping("/quiz/generate")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<GeneratedQuiz>> generateQuiz(
            @Valid @RequestBody GenerateQuizRequest request,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        context.put("subjectId", request.getSubjectId());
        context.put("topicId", request.getTopicId());
        context.put("numQuestions", request.getNumQuestions());

        loggingService.logBusinessOperationStart("GENERATE_AI_QUIZ", null, context);

        try {
            GeneratedQuiz response = aiService.generateQuiz(request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_AI_QUIZ", null, true, executionTime);

            ApiResponse<GeneratedQuiz> apiResponse = ApiResponse.success(response, "AI quiz generated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_AI_QUIZ", null, false, executionTime);
            loggingService.logError("GENERATE_AI_QUIZ", e, context);
            throw e;
        }
    }

    @PostMapping("/quiz/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizResult>> submitQuiz(
            @Valid @RequestBody SubmitQuizRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("quizId", request.getQuizId());

        loggingService.logBusinessOperationStart("SUBMIT_AI_QUIZ", studentId.toString(), context);

        try {
            QuizResult response = aiService.submitQuiz(studentId, request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("SUBMIT_AI_QUIZ", studentId.toString(), true, executionTime);

            ApiResponse<QuizResult> apiResponse = ApiResponse.success(response, "AI quiz submitted successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("SUBMIT_AI_QUIZ", studentId.toString(), false, executionTime);
            loggingService.logError("SUBMIT_AI_QUIZ", e, context);
            throw e;
        }
    }

    @GetMapping("/recommendations/topics")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RecommendationsResponse>> getTopicRecommendations(
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);

        loggingService.logBusinessOperationStart("GET_AI_RECOMMENDATIONS", studentId.toString(), context);

        try {
            RecommendationsResponse response = aiService.getTopicRecommendations(studentId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_AI_RECOMMENDATIONS", studentId.toString(), true, executionTime);

            ApiResponse<RecommendationsResponse> apiResponse = ApiResponse.success(response, "AI recommendations retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_AI_RECOMMENDATIONS", studentId.toString(), false, executionTime);
            loggingService.logError("GET_AI_RECOMMENDATIONS", e, context);
            throw e;
        }
    }

    @GetMapping("/summary/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<ClassSummary>> generateClassSummary(
            @PathVariable Long bookingId,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", bookingId);

        loggingService.logBusinessOperationStart("GENERATE_AI_SUMMARY", userId.toString(), context);

        try {
            ClassSummary response = aiService.generateClassSummary(bookingId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_AI_SUMMARY", userId.toString(), true, executionTime);

            ApiResponse<ClassSummary> apiResponse = ApiResponse.success(response, "AI class summary generated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_AI_SUMMARY", userId.toString(), false, executionTime);
            loggingService.logError("GENERATE_AI_SUMMARY", e, context);
            throw e;
        }
    }
}

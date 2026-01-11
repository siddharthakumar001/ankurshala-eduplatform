package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.ai.QuizDTO;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.PersonalizedQuizService;
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
import java.util.List;
import java.util.Map;

/**
 * Controller for personalized quiz endpoints.
 * All endpoints are secured and require STUDENT role.
 */
@Slf4j
@RestController
@RequestMapping("/student/quizzes")
@PreAuthorize("hasRole('STUDENT')")
public class StudentQuizController {

    @Autowired
    private PersonalizedQuizService quizService;

    @Autowired
    private LoggingService loggingService;

    /**
     * Generate a new personalized quiz
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuizDTO.QuizResponse>> generateQuiz(
            @Valid @RequestBody QuizDTO.GenerateRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("topicId", request.getTopicId());
        context.put("numQuestions", request.getNumQuestions());
        context.put("difficulty", request.getDifficulty());

        loggingService.logBusinessOperationStart("GENERATE_PERSONALIZED_QUIZ", studentId.toString(), context);

        try {
            QuizDTO.QuizResponse quiz = quizService.generateQuiz(studentId, request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_PERSONALIZED_QUIZ", studentId.toString(), true, executionTime);

            ApiResponse<QuizDTO.QuizResponse> apiResponse = ApiResponse.success(quiz, "Quiz generated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_PERSONALIZED_QUIZ", studentId.toString(), false, executionTime);
            loggingService.logError("GENERATE_PERSONALIZED_QUIZ", e, context);
            throw e;
        }
    }

    /**
     * Get a quiz by ID
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizDTO.QuizResponse>> getQuiz(
            @PathVariable Long quizId,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("quizId", quizId);

        loggingService.logBusinessOperationStart("GET_QUIZ", studentId.toString(), context);

        try {
            QuizDTO.QuizResponse quiz = quizService.getQuiz(quizId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_QUIZ", studentId.toString(), true, executionTime);

            ApiResponse<QuizDTO.QuizResponse> apiResponse = ApiResponse.success(quiz, "Quiz retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_QUIZ", studentId.toString(), false, executionTime);
            loggingService.logError("GET_QUIZ", e, context);
            throw e;
        }
    }

    /**
     * Start a quiz attempt
     */
    @PostMapping("/{quizId}/attempts")
    public ResponseEntity<ApiResponse<QuizDTO.AttemptResponse>> startAttempt(
            @PathVariable Long quizId,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("quizId", quizId);

        loggingService.logBusinessOperationStart("START_QUIZ_ATTEMPT", studentId.toString(), context);

        try {
            QuizDTO.AttemptResponse attempt = quizService.startAttempt(studentId, quizId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("START_QUIZ_ATTEMPT", studentId.toString(), true, executionTime);

            ApiResponse<QuizDTO.AttemptResponse> apiResponse = ApiResponse.success(attempt, "Quiz attempt started");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("START_QUIZ_ATTEMPT", studentId.toString(), false, executionTime);
            loggingService.logError("START_QUIZ_ATTEMPT", e, context);
            throw e;
        }
    }

    /**
     * Submit a quiz attempt with answers
     */
    @PostMapping("/{quizId}/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<QuizDTO.GradeAttemptResponse>> submitAttempt(
            @PathVariable Long quizId,
            @PathVariable Long attemptId,
            @Valid @RequestBody QuizDTO.SubmitAttemptRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        // Ensure attemptId in path matches request body
        request.setAttemptId(attemptId);

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("quizId", quizId);
        context.put("attemptId", attemptId);
        context.put("answerCount", request.getAnswers() != null ? request.getAnswers().size() : 0);

        loggingService.logBusinessOperationStart("SUBMIT_QUIZ_ATTEMPT", studentId.toString(), context);

        try {
            QuizDTO.GradeAttemptResponse result = quizService.submitAttempt(studentId, request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("SUBMIT_QUIZ_ATTEMPT", studentId.toString(), true, executionTime);

            ApiResponse<QuizDTO.GradeAttemptResponse> apiResponse = ApiResponse.success(result, "Quiz submitted and graded successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("SUBMIT_QUIZ_ATTEMPT", studentId.toString(), false, executionTime);
            loggingService.logError("SUBMIT_QUIZ_ATTEMPT", e, context);
            throw e;
        }
    }

    /**
     * Get quiz history for the student
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<QuizDTO.QuizHistoryItem>>> getQuizHistory(
            @RequestParam(required = false, defaultValue = "20") int limit,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("limit", limit);

        loggingService.logBusinessOperationStart("GET_QUIZ_HISTORY", studentId.toString(), context);

        try {
            List<QuizDTO.QuizHistoryItem> history = quizService.getQuizHistory(studentId, limit);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_QUIZ_HISTORY", studentId.toString(), true, executionTime);

            ApiResponse<List<QuizDTO.QuizHistoryItem>> apiResponse = ApiResponse.success(history, "Quiz history retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_QUIZ_HISTORY", studentId.toString(), false, executionTime);
            loggingService.logError("GET_QUIZ_HISTORY", e, context);
            throw e;
        }
    }
}


package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.ai.MasteryDTO;
import com.ankurshala.backend.dto.ai.RecommendationDTO;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.MasteryService;
import com.ankurshala.backend.service.RecommendationService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for student mastery and recommendation endpoints.
 * All endpoints are secured and require STUDENT role.
 */
@Slf4j
@RestController
@RequestMapping("/student/mastery")
@PreAuthorize("hasRole('STUDENT')")
public class StudentMasteryController {

    @Autowired
    private MasteryService masteryService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private LoggingService loggingService;

    /**
     * Get mastery overview for the authenticated student
     * 
     * @param subjectId Optional filter by subject
     * @param gradeId Optional filter by grade
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MasteryDTO.MasteryOverview>> getMasteryOverview(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("subjectId", subjectId);
        context.put("gradeId", gradeId);

        loggingService.logBusinessOperationStart("GET_MASTERY_OVERVIEW", studentId.toString(), context);

        try {
            MasteryDTO.MasteryOverview overview = masteryService.getMasteryOverview(studentId, subjectId, gradeId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_MASTERY_OVERVIEW", studentId.toString(), true, executionTime);

            ApiResponse<MasteryDTO.MasteryOverview> apiResponse = ApiResponse.success(overview, "Mastery overview retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_MASTERY_OVERVIEW", studentId.toString(), false, executionTime);
            loggingService.logError("GET_MASTERY_OVERVIEW", e, context);
            throw e;
        }
    }

    /**
     * Get mastery for a specific topic
     */
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<ApiResponse<MasteryDTO.TopicMastery>> getTopicMastery(
            @PathVariable Long topicId,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("topicId", topicId);

        loggingService.logBusinessOperationStart("GET_TOPIC_MASTERY", studentId.toString(), context);

        try {
            MasteryDTO.TopicMastery mastery = masteryService.getTopicMastery(studentId, topicId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_MASTERY", studentId.toString(), true, executionTime);

            ApiResponse<MasteryDTO.TopicMastery> apiResponse = ApiResponse.success(mastery, "Topic mastery retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_MASTERY", studentId.toString(), false, executionTime);
            loggingService.logError("GET_TOPIC_MASTERY", e, context);
            throw e;
        }
    }

    /**
     * Get weak topics for the student
     */
    @GetMapping("/weak-topics")
    public ResponseEntity<ApiResponse<RecommendationDTO.WeakTopicsOverview>> getWeakTopics(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false, defaultValue = "0.65") BigDecimal threshold,
            @RequestParam(required = false, defaultValue = "10") int limit,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("threshold", threshold);
        context.put("limit", limit);

        loggingService.logBusinessOperationStart("GET_WEAK_TOPICS", studentId.toString(), context);

        try {
            RecommendationDTO.WeakTopicsOverview weakTopics = recommendationService.getWeakTopics(
                    studentId, subjectId, gradeId, threshold, limit);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_WEAK_TOPICS", studentId.toString(), true, executionTime);

            ApiResponse<RecommendationDTO.WeakTopicsOverview> apiResponse = ApiResponse.success(weakTopics, "Weak topics retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_WEAK_TOPICS", studentId.toString(), false, executionTime);
            loggingService.logError("GET_WEAK_TOPICS", e, context);
            throw e;
        }
    }

    /**
     * Get prerequisite recommendations for a target topic
     */
    @GetMapping("/recommendations/topic/{topicId}")
    public ResponseEntity<ApiResponse<RecommendationDTO.TopicRecommendationResponse>> getPrerequisiteRecommendations(
            @PathVariable Long topicId,
            @RequestParam(required = false, defaultValue = "0.65") BigDecimal threshold,
            @RequestParam(required = false, defaultValue = "false") boolean includeOptional,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("topicId", topicId);
        context.put("threshold", threshold);

        loggingService.logBusinessOperationStart("GET_PREREQUISITE_RECOMMENDATIONS", studentId.toString(), context);

        try {
            RecommendationDTO.TopicRecommendationResponse recommendations = 
                    recommendationService.getPrerequisiteRecommendations(studentId, topicId, threshold, includeOptional);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PREREQUISITE_RECOMMENDATIONS", studentId.toString(), true, executionTime);

            ApiResponse<RecommendationDTO.TopicRecommendationResponse> apiResponse = 
                    ApiResponse.success(recommendations, "Prerequisite recommendations retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PREREQUISITE_RECOMMENDATIONS", studentId.toString(), false, executionTime);
            loggingService.logError("GET_PREREQUISITE_RECOMMENDATIONS", e, context);
            throw e;
        }
    }

    /**
     * Generate a personalized study plan for a target topic
     */
    @PostMapping("/study-plan")
    public ResponseEntity<ApiResponse<RecommendationDTO.PersonalizedStudyPlan>> generateStudyPlan(
            @RequestBody RecommendationDTO.GenerateStudyPlanRequest planRequest,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("targetTopicId", planRequest.getTargetTopicId());

        loggingService.logBusinessOperationStart("GENERATE_STUDY_PLAN", studentId.toString(), context);

        try {
            RecommendationDTO.PersonalizedStudyPlan studyPlan = 
                    recommendationService.generateStudyPlan(studentId, planRequest);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_STUDY_PLAN", studentId.toString(), true, executionTime);

            ApiResponse<RecommendationDTO.PersonalizedStudyPlan> apiResponse = 
                    ApiResponse.success(studyPlan, "Study plan generated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GENERATE_STUDY_PLAN", studentId.toString(), false, executionTime);
            loggingService.logError("GENERATE_STUDY_PLAN", e, context);
            throw e;
        }
    }
}


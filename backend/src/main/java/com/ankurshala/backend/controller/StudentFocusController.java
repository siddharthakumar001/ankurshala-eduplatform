package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.student.FocusDTO;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentFocusService;
import com.ankurshala.backend.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Student Focus Mode Controller - AI Coach Loop with Study Sprints.
 * All endpoints are secured with STUDENT role and ownership validation.
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002",
        "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/student/focus")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student Focus Mode", description = "Focus Mode + Study Sprints APIs")
public class StudentFocusController {

    @Autowired
    private StudentFocusService focusService;

    // ===================== Settings Endpoints =====================

    @GetMapping("/settings")
    @Operation(summary = "Get focus mode settings")
    public ResponseEntity<ApiResponse<FocusDTO.FocusSettingsResponse>> getSettings(
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        FocusDTO.FocusSettingsResponse response = focusService.getSettings(studentId);

        ApiResponse<FocusDTO.FocusSettingsResponse> apiResponse =
                ApiResponse.success(response, "Focus settings retrieved");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/settings")
    @Operation(summary = "Update focus mode settings")
    public ResponseEntity<ApiResponse<FocusDTO.FocusSettingsResponse>> updateSettings(
            @Valid @RequestBody FocusDTO.UpdateFocusSettingsRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Update focus settings - TraceId: {}, StudentId: {}", traceId, studentId);

        FocusDTO.FocusSettingsResponse response = focusService.updateSettings(studentId, request);

        ApiResponse<FocusDTO.FocusSettingsResponse> apiResponse =
                ApiResponse.success(response, "Focus settings updated");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    // ===================== Session Endpoints =====================

    @PostMapping("/sessions")
    @Operation(summary = "Start a new focus session",
            description = "Starts a focus session with a specific goal and topic")
    public ResponseEntity<ApiResponse<FocusDTO.SessionResponse>> startSession(
            @Valid @RequestBody FocusDTO.StartSessionRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Start focus session - TraceId: {}, StudentId: {}, TopicId: {}",
                traceId, studentId, request.getTopicId());

        FocusDTO.SessionResponse response = focusService.startSession(studentId, request);

        ApiResponse<FocusDTO.SessionResponse> apiResponse =
                ApiResponse.success(response, "Focus session started");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/sessions/active")
    @Operation(summary = "Get current active session",
            description = "Returns the currently active focus session if any")
    public ResponseEntity<ApiResponse<FocusDTO.SessionResponse>> getActiveSession(
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        FocusDTO.SessionResponse response = focusService.getActiveSession(studentId);

        if (response == null) {
            ApiResponse<FocusDTO.SessionResponse> apiResponse =
                    ApiResponse.success(null, "No active session");
            apiResponse.setTraceId(traceId);
            return ResponseEntity.ok(apiResponse);
        }

        ApiResponse<FocusDTO.SessionResponse> apiResponse =
                ApiResponse.success(response, "Active session found");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Get session details")
    public ResponseEntity<ApiResponse<FocusDTO.SessionResponse>> getSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        FocusDTO.SessionResponse response = focusService.getSession(studentId, sessionId);

        ApiResponse<FocusDTO.SessionResponse> apiResponse =
                ApiResponse.success(response, "Session retrieved");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/sessions/{sessionId}/checkin")
    @Operation(summary = "Generate next check-in for session",
            description = "Generates the next AI-powered check-in step")
    public ResponseEntity<ApiResponse<FocusDTO.GenerateCheckinResponse>> generateCheckin(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Generate check-in - TraceId: {}, SessionId: {}", traceId, sessionId);

        FocusDTO.GenerateCheckinResponse response = focusService.generateNextCheckin(studentId, sessionId);

        ApiResponse<FocusDTO.GenerateCheckinResponse> apiResponse =
                ApiResponse.success(response, "Check-in generated");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/sessions/{sessionId}/checkin/{stepNumber}/respond")
    @Operation(summary = "Submit response to check-in",
            description = "Submit student's response to a check-in question")
    public ResponseEntity<ApiResponse<FocusDTO.SubmitCheckinResponse>> submitCheckinResponse(
            @PathVariable Long sessionId,
            @PathVariable Integer stepNumber,
            @Valid @RequestBody FocusDTO.CheckinRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Submit check-in response - TraceId: {}, SessionId: {}, Step: {}",
                traceId, sessionId, stepNumber);

        FocusDTO.SubmitCheckinResponse response = focusService.submitCheckinResponse(
                studentId, sessionId, stepNumber, request);

        ApiResponse<FocusDTO.SubmitCheckinResponse> apiResponse =
                ApiResponse.success(response, "Response submitted");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/sessions/{sessionId}/end")
    @Operation(summary = "End focus session",
            description = "Ends the current focus session and records stats")
    public ResponseEntity<ApiResponse<FocusDTO.SessionResponse>> endSession(
            @PathVariable Long sessionId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("End focus session - TraceId: {}, SessionId: {}", traceId, sessionId);

        FocusDTO.SessionResponse response = focusService.endSession(studentId, sessionId);

        ApiResponse<FocusDTO.SessionResponse> apiResponse =
                ApiResponse.success(response, "Session ended");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get session history",
            description = "Returns paginated history of focus sessions")
    public ResponseEntity<ApiResponse<FocusDTO.SessionsListResponse>> getSessionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        FocusDTO.SessionsListResponse response = focusService.getSessionHistory(studentId, page, size);

        ApiResponse<FocusDTO.SessionsListResponse> apiResponse =
                ApiResponse.success(response, "Session history retrieved");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get focus mode statistics",
            description = "Returns aggregate statistics for focus sessions")
    public ResponseEntity<ApiResponse<FocusDTO.FocusStatsResponse>> getStats(
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        FocusDTO.FocusStatsResponse response = focusService.getStats(studentId);

        ApiResponse<FocusDTO.FocusStatsResponse> apiResponse =
                ApiResponse.success(response, "Focus stats retrieved");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }
}


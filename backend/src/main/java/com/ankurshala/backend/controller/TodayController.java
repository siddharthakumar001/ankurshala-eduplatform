package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.CompleteStepRequest;
import com.ankurshala.backend.dto.student.CompleteStepResponse;
import com.ankurshala.backend.dto.student.DailyPlanDto;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.TodayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Today Controller - Endpoints for student's personalized daily plan
 * Feature A: Today Home - Single screen that drives daily habit loop
 */
@RestController
@RequestMapping("/student/today")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class TodayController {
    
    private final TodayService todayService;
    
    /**
     * GET /api/student/today
     * Get personalized daily plan for the student
     * 
     * Returns:
     * - Next booked class summary + companion link
     * - One weak topic recommendation with reason
     * - Today's practice items (3-5)
     * - One "revise note" suggestion
     * - Focus sprint quick-start
     * - Overall progress summary
     * 
     * Cached for 5 minutes per student
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<DailyPlanDto> getDailyPlan(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("GET /student/today - User: {}", userPrincipal.getId());
        
        DailyPlanDto dailyPlan = todayService.getDailyPlan(userPrincipal);
        
        log.info("Daily plan retrieved successfully for user: {}", userPrincipal.getId());
        return ResponseEntity.ok(dailyPlan);
    }
    
    /**
     * POST /api/student/today/complete-step
     * Mark a step as completed in the daily plan
     * 
     * Request body:
     * {
     *   "stepType": "PRACTICE" | "REVISE_NOTE" | "FOCUS_SPRINT" | "BOOKING_COMPANION",
     *   "stepIdentifier": "topicId | noteId | bookingId",
     *   "metadata": { "duration": 300, "score": 85, ... }
     * }
     * 
     * Invalidates cache to force daily plan refresh
     */
    @PostMapping("/complete-step")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CompleteStepResponse> completeStep(
            @Valid @RequestBody CompleteStepRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("POST /student/today/complete-step - User: {}, Step: {}", 
                 userPrincipal.getId(), request.getStepType());
        
        CompleteStepResponse response = todayService.completeStep(request, userPrincipal);
        
        log.info("Step completion response for user {}: success={}", 
                 userPrincipal.getId(), response.getSuccess());
        
        return ResponseEntity.ok(response);
    }
}

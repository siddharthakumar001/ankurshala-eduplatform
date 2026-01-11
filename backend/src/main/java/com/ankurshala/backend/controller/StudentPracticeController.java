package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.PracticeDTO.*;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.DailyPracticeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Daily Practice feature.
 * All endpoints require STUDENT role and validate ownership.
 */
@RestController
@RequestMapping("/api/student/practice")
@PreAuthorize("hasRole('STUDENT')")
@Slf4j
public class StudentPracticeController {

    @Autowired
    private DailyPracticeService practiceService;

    // ==================== PREFERENCES ====================

    /**
     * Get practice preferences for the authenticated student
     */
    @GetMapping("/preferences")
    public ResponseEntity<PracticePreferencesResponse> getPreferences(
            @AuthenticationPrincipal UserPrincipal userDetails) {
        Long studentId = userDetails.getId();
        PracticePreferencesResponse response = practiceService.getOrCreatePreferences(studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update practice preferences
     */
    @PutMapping("/preferences")
    public ResponseEntity<PracticePreferencesResponse> updatePreferences(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @Valid @RequestBody UpdatePracticePreferencesRequest request) {
        Long studentId = userDetails.getId();
        PracticePreferencesResponse response = practiceService.updatePreferences(studentId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== TODAY'S PRACTICE ====================

    /**
     * Get today's practice items
     * Generates practice if not already generated
     */
    @GetMapping("/today")
    public ResponseEntity<TodayPracticeResponse> getTodayPractice(
            @AuthenticationPrincipal UserPrincipal userDetails) {
        Long studentId = userDetails.getId();
        TodayPracticeResponse response = practiceService.generateTodayPracticeIfNeeded(studentId);
        return ResponseEntity.ok(response);
    }

    // ==================== START & SUBMIT ====================

    /**
     * Start a practice item (generates quiz questions)
     */
    @PostMapping("/{practiceId}/start")
    public ResponseEntity<StartPracticeResponse> startPractice(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long practiceId) {
        Long studentId = userDetails.getId();
        StartPracticeResponse response = practiceService.startPractice(studentId, practiceId);
        return ResponseEntity.ok(response);
    }

    /**
     * Submit practice answers
     */
    @PostMapping("/{practiceId}/submit")
    public ResponseEntity<SubmitPracticeResponse> submitPractice(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long practiceId,
            @Valid @RequestBody SubmitPracticeRequest request) {
        Long studentId = userDetails.getId();
        SubmitPracticeResponse response = practiceService.submitPractice(studentId, practiceId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Skip a practice item
     */
    @PostMapping("/{practiceId}/skip")
    public ResponseEntity<Void> skipPractice(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long practiceId) {
        Long studentId = userDetails.getId();
        practiceService.skipPractice(studentId, practiceId);
        return ResponseEntity.noContent().build();
    }

    // ==================== HISTORY ====================

    /**
     * Get practice history (last 30 days by default)
     */
    @GetMapping("/history")
    public ResponseEntity<PracticeHistoryResponse> getPracticeHistory(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @RequestParam(defaultValue = "30") int days) {
        Long studentId = userDetails.getId();
        PracticeHistoryResponse response = practiceService.getPracticeHistory(studentId, days);
        return ResponseEntity.ok(response);
    }
}


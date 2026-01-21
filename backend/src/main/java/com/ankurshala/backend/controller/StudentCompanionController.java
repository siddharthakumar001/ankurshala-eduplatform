package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.CompanionDTO.*;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.SessionCompanionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Session Companion (Live Class Companion) feature.
 * All endpoints require STUDENT role and validate ownership.
 */
@RestController
@RequestMapping("/student/bookings/{bookingId}/companion")
@PreAuthorize("hasRole('STUDENT')")
@Slf4j
public class StudentCompanionController {

    @Autowired
    private SessionCompanionService companionService;

    // ==================== GET COMPANION ====================

    /**
     * Get or create companion for a booking
     */
    @GetMapping
    public ResponseEntity<CompanionResponse> getCompanion(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long bookingId) {
        Long studentId = userDetails.getId();
        CompanionResponse response = companionService.getOrCreateCompanion(studentId, bookingId);
        return ResponseEntity.ok(response);
    }

    // ==================== PREP (BEFORE SESSION) ====================

    /**
     * Generate pre-session plan and warmup quiz
     */
    @PostMapping("/prep")
    public ResponseEntity<CompanionResponse> generatePrep(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long bookingId,
            @RequestBody(required = false) GeneratePrepRequest request) {
        Long studentId = userDetails.getId();
        if (request == null) {
            request = new GeneratePrepRequest();
        }
        CompanionResponse response = companionService.generatePrep(studentId, bookingId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get warmup quiz for the booking
     */
    @GetMapping("/warmup")
    public ResponseEntity<WarmupQuizResponse> getWarmupQuiz(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long bookingId) {
        Long studentId = userDetails.getId();
        WarmupQuizResponse response = companionService.getWarmupQuiz(studentId, bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark warmup quiz as completed
     */
    @PostMapping("/warmup/complete")
    public ResponseEntity<Void> markWarmupCompleted(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long bookingId) {
        Long studentId = userDetails.getId();
        companionService.markWarmupCompleted(studentId, bookingId);
        return ResponseEntity.noContent().build();
    }

    // ==================== LIVE NOTES (DURING SESSION) ====================

    /**
     * Add a note during live session
     */
    @PostMapping("/live-notes")
    public ResponseEntity<CompanionNoteResponse> addLiveNote(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long bookingId,
            @Valid @RequestBody AddLiveNoteRequest request) {
        Long studentId = userDetails.getId();
        CompanionNoteResponse response = companionService.addLiveNote(studentId, bookingId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all session notes
     */
    @GetMapping("/notes")
    public ResponseEntity<List<CompanionNoteResponse>> getSessionNotes(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long bookingId) {
        Long studentId = userDetails.getId();
        List<CompanionNoteResponse> response = companionService.getSessionNotes(studentId, bookingId);
        return ResponseEntity.ok(response);
    }

    // ==================== POST-SESSION ====================

    /**
     * Generate post-session summary and homework
     */
    @PostMapping("/post")
    public ResponseEntity<CompanionResponse> generatePost(
            @AuthenticationPrincipal UserPrincipal userDetails,
            @PathVariable Long bookingId,
            @RequestBody(required = false) GeneratePostRequest request) {
        Long studentId = userDetails.getId();
        if (request == null) {
            request = new GeneratePostRequest();
        }
        CompanionResponse response = companionService.generatePost(studentId, bookingId, request);
        return ResponseEntity.ok(response);
    }
}


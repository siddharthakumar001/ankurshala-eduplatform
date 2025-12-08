package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentSessionManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/sessions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class StudentSessionManagementController {

    private final StudentSessionManagementService sessionManagementService;

    @PostMapping("/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<JoinSessionResponse> joinSession(
            @Valid @RequestBody JoinSessionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Student {} joining session for booking {}", userPrincipal.getId(), request.getBookingId());
        
        JoinSessionResponse response = sessionManagementService.joinSession(request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SessionFeedbackResponse> submitSessionFeedback(
            @Valid @RequestBody SessionFeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Student {} submitting feedback for booking {}", userPrincipal.getId(), request.getBookingId());
        
        SessionFeedbackResponse response = sessionManagementService.submitSessionFeedback(request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingId}/status")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<JoinSessionResponse> getSessionStatus(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Student {} checking status for booking {}", userPrincipal.getId(), bookingId);
        
        // Create a join request to check status
        JoinSessionRequest request = new JoinSessionRequest();
        request.setBookingId(bookingId);
        
        JoinSessionResponse response = sessionManagementService.joinSession(request, userPrincipal);
        return ResponseEntity.ok(response);
    }
}

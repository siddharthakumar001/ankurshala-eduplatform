package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.StudentDashboardDto;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDashboardDto> getDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting student dashboard for user ID: {}", userPrincipal.getId());
        
        StudentDashboardDto dashboard = studentDashboardService.getStudentDashboard(userPrincipal);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDashboardDto> getDashboardStats(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting student dashboard stats for user ID: {}", userPrincipal.getId());
        
        StudentDashboardDto dashboard = studentDashboardService.getStudentDashboard(userPrincipal);
        return ResponseEntity.ok(dashboard);
    }
}

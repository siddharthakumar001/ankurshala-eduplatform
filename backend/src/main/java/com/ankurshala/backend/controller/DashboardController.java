package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.dashboard.StudentDashboardStats;
import com.ankurshala.backend.dto.dashboard.TeacherDashboardStats;
import com.ankurshala.backend.dto.dashboard.TeacherEarningsResponse;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.DashboardService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private LoggingService loggingService;

    @GetMapping("/student/stats")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardStats>> getStudentDashboardStats(
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);

        loggingService.logBusinessOperationStart("GET_STUDENT_DASHBOARD", studentId.toString(), context);

        try {
            StudentDashboardStats response = dashboardService.getStudentDashboardStats(studentId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_DASHBOARD", studentId.toString(), true, executionTime);

            ApiResponse<StudentDashboardStats> apiResponse = ApiResponse.success(response, "Student dashboard stats retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_DASHBOARD", studentId.toString(), false, executionTime);
            loggingService.logError("GET_STUDENT_DASHBOARD", e, context);
            throw e;
        }
    }

    @GetMapping("/teacher/stats")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherDashboardStats>> getTeacherDashboardStats(
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("teacherId", teacherId);

        loggingService.logBusinessOperationStart("GET_TEACHER_DASHBOARD", teacherId.toString(), context);

        try {
            TeacherDashboardStats response = dashboardService.getTeacherDashboardStats(teacherId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_DASHBOARD", teacherId.toString(), true, executionTime);

            ApiResponse<TeacherDashboardStats> apiResponse = ApiResponse.success(response, "Teacher dashboard stats retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_DASHBOARD", teacherId.toString(), false, executionTime);
            loggingService.logError("GET_TEACHER_DASHBOARD", e, context);
            throw e;
        }
    }

    @GetMapping("/teacher/earnings")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<TeacherEarningsResponse>> getTeacherEarnings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();

        // Default to last 6 months if not specified
        LocalDateTime fromDate = from != null ? from : LocalDateTime.now().minusMonths(6);
        LocalDateTime toDate = to != null ? to : LocalDateTime.now();

        Map<String, Object> context = new HashMap<>();
        context.put("teacherId", teacherId);
        context.put("fromDate", fromDate);
        context.put("toDate", toDate);

        loggingService.logBusinessOperationStart("GET_TEACHER_EARNINGS", teacherId.toString(), context);

        try {
            TeacherEarningsResponse response = dashboardService.getTeacherEarnings(teacherId, fromDate, toDate);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_EARNINGS", teacherId.toString(), true, executionTime);

            ApiResponse<TeacherEarningsResponse> apiResponse = ApiResponse.success(response, "Teacher earnings retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_EARNINGS", teacherId.toString(), false, executionTime);
            loggingService.logError("GET_TEACHER_EARNINGS", e, context);
            throw e;
        }
    }
}

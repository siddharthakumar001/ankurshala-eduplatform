package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.waiver.CreateWaiverRequest;
import com.ankurshala.backend.dto.waiver.FeeWaiverDto;
import com.ankurshala.backend.dto.waiver.RequestWaiverRequest;
import com.ankurshala.backend.entity.FeeWaiver;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.FeeWaiverService;
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

@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/waivers")
public class FeeWaiverController {

    @Autowired
    private FeeWaiverService feeWaiverService;
    @Autowired
    private LoggingService loggingService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<FeeWaiver>> requestWaiver(
            @Valid @RequestBody RequestWaiverRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);
        context.put("bookingId", request.getBookingId());

        loggingService.logBusinessOperationStart("REQUEST_WAIVER", studentId.toString(), context);

        try {
            FeeWaiver response = feeWaiverService.requestWaiver(studentId, request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REQUEST_WAIVER", studentId.toString(), true, executionTime);

            ApiResponse<FeeWaiver> apiResponse = ApiResponse.success(response, "Fee waiver request submitted successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.status(201).body(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REQUEST_WAIVER", studentId.toString(), false, executionTime);
            loggingService.logError("REQUEST_WAIVER", e, context);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeWaiver>> createWaiver(
            @Valid @RequestBody CreateWaiverRequest request,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", request.getStudentId());
        context.put("bookingId", request.getBookingId());

        loggingService.logBusinessOperationStart("CREATE_WAIVER", request.getStudentId().toString(), context);

        try {
            FeeWaiver response = feeWaiverService.createWaiver(request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_WAIVER", request.getStudentId().toString(), true, executionTime);

            ApiResponse<FeeWaiver> apiResponse = ApiResponse.success(response, "Fee waiver created successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.status(201).body(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_WAIVER", request.getStudentId().toString(), false, executionTime);
            loggingService.logError("CREATE_WAIVER", e, context);
            throw e;
        }
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<FeeWaiverDto>>> getStudentWaivers(
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("studentId", studentId);

        loggingService.logBusinessOperationStart("GET_STUDENT_WAIVERS", studentId.toString(), context);

        try {
            List<FeeWaiverDto> response = feeWaiverService.getStudentWaivers(studentId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_WAIVERS", studentId.toString(), true, executionTime);

            ApiResponse<List<FeeWaiverDto>> apiResponse = ApiResponse.success(response, "Student waivers retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_WAIVERS", studentId.toString(), false, executionTime);
            loggingService.logError("GET_STUDENT_WAIVERS", e, context);
            throw e;
        }
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FeeWaiverDto>> getBookingWaiver(
            @PathVariable Long bookingId,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        context.put("bookingId", bookingId);

        loggingService.logBusinessOperationStart("GET_BOOKING_WAIVER", bookingId.toString(), context);

        try {
            FeeWaiverDto response = feeWaiverService.getBookingWaiver(bookingId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOOKING_WAIVER", bookingId.toString(), true, executionTime);

            ApiResponse<FeeWaiverDto> apiResponse = ApiResponse.success(response, "Booking waiver retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOOKING_WAIVER", bookingId.toString(), false, executionTime);
            loggingService.logError("GET_BOOKING_WAIVER", e, context);
            throw e;
        }
    }

    @DeleteMapping("/{waiverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateWaiver(
            @PathVariable Long waiverId,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        context.put("waiverId", waiverId);

        loggingService.logBusinessOperationStart("DEACTIVATE_WAIVER", waiverId.toString(), context);

        try {
            feeWaiverService.deactivateWaiver(waiverId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DEACTIVATE_WAIVER", waiverId.toString(), true, executionTime);

            ApiResponse<Void> apiResponse = ApiResponse.success(null, "Fee waiver deactivated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DEACTIVATE_WAIVER", waiverId.toString(), false, executionTime);
            loggingService.logError("DEACTIVATE_WAIVER", e, context);
            throw e;
        }
    }
}

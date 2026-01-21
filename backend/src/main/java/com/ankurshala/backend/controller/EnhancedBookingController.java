package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.booking.*;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.EnhancedBookingService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Booking Controller
 * Implements Uber-style booking system with real-time teacher matching
 * Handles booking lifecycle, pricing, and availability validation
 *
 * Deprecated: use StudentBookingController at /student/bookings.
 */
@Slf4j
@Deprecated(forRemoval = true)
public class EnhancedBookingController {

    @Autowired
    private EnhancedBookingService enhancedBookingService;
    
    @Autowired
    private LoggingService loggingService;

    // ============ STUDENT BOOKING APIs ============

    @PostMapping("/quote")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<BookingQuoteResponse>> getBookingQuote(
            @Valid @RequestBody BookingQuoteRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("topicId", request.getTopicId());
        context.put("startTime", request.getStartTimeISO());
        context.put("category", request.getTeacherCategory());
        
        loggingService.logBusinessOperationStart("BOOKING_QUOTE", null, context);
        
        try {
            BookingQuoteResponse response = enhancedBookingService.getBookingQuote(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("BOOKING_QUOTE", null, true, executionTime);
            
            ApiResponse<BookingQuoteResponse> apiResponse = ApiResponse.success(response, "Booking quote generated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("BOOKING_QUOTE", null, false, executionTime);
            loggingService.logError("BOOKING_QUOTE", e, context);
            throw e;
        }
    }

    @PostMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
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
        context.put("startTime", request.getStartTimeISO());
        context.put("category", request.getTeacherCategory());
        
        loggingService.logBusinessOperationStart("CREATE_BOOKING", studentId.toString(), context);
        
        try {
            BookingResponse response = enhancedBookingService.createBooking(studentId, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_BOOKING", studentId.toString(), true, executionTime);
            
            ApiResponse<BookingResponse> apiResponse = ApiResponse.success(response, "Booking created successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_BOOKING", studentId.toString(), false, executionTime);
            loggingService.logError("CREATE_BOOKING", e, context);
            throw e;
        }
    }

    @GetMapping("/student/upcoming")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUpcomingBookings(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();
        
        // TODO: Implement getUpcomingBookings service method
        List<BookingResponse> bookings = List.of(); // Placeholder
        
        ApiResponse<List<BookingResponse>> apiResponse = ApiResponse.success(bookings, "Upcoming bookings retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/student/history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBookingHistory(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();
        
        // TODO: Implement getBookingHistory service method
        List<BookingResponse> bookings = List.of(); // Placeholder
        
        ApiResponse<List<BookingResponse>> apiResponse = ApiResponse.success(bookings, "Booking history retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/student/calendar")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCalendarBookings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();
        
        // TODO: Implement getCalendarBookings service method
        List<BookingResponse> bookings = List.of(); // Placeholder
        
        ApiResponse<List<BookingResponse>> apiResponse = ApiResponse.success(bookings, "Calendar bookings retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);
        
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/student/{bookingId}/fee-preview")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<FeePreviewResponse>> getFeePreview(
            @PathVariable Long bookingId,
            @RequestParam String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime newStart,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();
        
        FeePreviewRequest request = new FeePreviewRequest();
        request.setAction(action);
        request.setNewStart(newStart);
        
        try {
            FeePreviewResponse response = enhancedBookingService.getFeePreview(bookingId, request);
            
            ApiResponse<FeePreviewResponse> apiResponse = ApiResponse.success(response, "Fee preview generated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            loggingService.logError("FEE_PREVIEW", e, Map.of("bookingId", bookingId, "action", action));
            throw e;
        }
    }

    // ============ TEACHER BOOKING APIs ============

    @GetMapping("/teacher/pending")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPendingBookings(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();
        
        // TODO: Implement getPendingBookings service method
        List<BookingResponse> bookings = List.of(); // Placeholder
        
        ApiResponse<List<BookingResponse>> apiResponse = ApiResponse.success(bookings, "Pending bookings retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);
        
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/teacher/{bookingId}/accept")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<BookingAcceptResponse>> acceptBooking(
            @PathVariable Long bookingId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("teacherId", teacherId);
        context.put("bookingId", bookingId);
        
        loggingService.logBusinessOperationStart("ACCEPT_BOOKING", teacherId.toString(), context);
        
        try {
            BookingAcceptResponse response = enhancedBookingService.acceptBooking(teacherId, bookingId);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ACCEPT_BOOKING", teacherId.toString(), true, executionTime);
            
            ApiResponse<BookingAcceptResponse> apiResponse = ApiResponse.success(response, "Booking accepted successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ACCEPT_BOOKING", teacherId.toString(), false, executionTime);
            loggingService.logError("ACCEPT_BOOKING", e, context);
            throw e;
        }
    }

    @PutMapping("/teacher/{bookingId}/decline")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> declineBooking(
            @PathVariable Long bookingId,
            @RequestParam(required = false) String reason,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();
        
        Map<String, Object> context = new HashMap<>();
        context.put("teacherId", teacherId);
        context.put("bookingId", bookingId);
        context.put("reason", reason);
        
        loggingService.logBusinessOperationStart("DECLINE_BOOKING", teacherId.toString(), context);
        
        try {
            enhancedBookingService.declineBooking(teacherId, bookingId, reason);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DECLINE_BOOKING", teacherId.toString(), true, executionTime);
            
            ApiResponse<Void> apiResponse = ApiResponse.success(null, "Booking declined successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DECLINE_BOOKING", teacherId.toString(), false, executionTime);
            loggingService.logError("DECLINE_BOOKING", e, context);
            throw e;
        }
    }

    @GetMapping("/teacher/calendar")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTeacherCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();
        
        // TODO: Implement getTeacherCalendar service method
        List<BookingResponse> bookings = List.of(); // Placeholder
        
        ApiResponse<List<BookingResponse>> apiResponse = ApiResponse.success(bookings, "Teacher calendar retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/teacher/statistics")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeacherStatistics(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();
        
        // TODO: Implement getTeacherStatistics service method
        Map<String, Object> statistics = Map.of(
            "totalBookings", 0,
            "completedBookings", 0,
            "averageRating", 0.0,
            "totalEarnings", 0
        );
        
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(statistics, "Teacher statistics retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);
        
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/teacher/earnings")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTeacherEarnings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long teacherId = userPrincipal.getId();
        
        // TODO: Implement getTeacherEarnings service method
        Map<String, Object> earnings = Map.of(
            "totalEarnings", 0,
            "monthlyEarnings", 0,
            "completedSessions", 0
        );
        
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(earnings, "Teacher earnings retrieved successfully");
        apiResponse.setTraceId(traceId);
        apiResponse.setRequestId(requestId);
        
        return ResponseEntity.ok(apiResponse);
    }
}

package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.session.AddBookmarkRequest;
import com.ankurshala.backend.dto.session.AddNoteRequest;
import com.ankurshala.backend.dto.session.JoinSessionResponse;
import com.ankurshala.backend.entity.BookingBookmark;
import com.ankurshala.backend.entity.BookingNote;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.ClassSessionService;
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
@RequestMapping("/session")
public class ClassSessionController {

    @Autowired
    private ClassSessionService classSessionService;
    @Autowired
    private LoggingService loggingService;

    @GetMapping("/join/{bookingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<JoinSessionResponse>> joinSession(
            @PathVariable Long bookingId,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", bookingId);

        loggingService.logBusinessOperationStart("JOIN_SESSION", userId.toString(), context);

        try {
            JoinSessionResponse response = classSessionService.joinSession(userId, bookingId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("JOIN_SESSION", userId.toString(), true, executionTime);

            ApiResponse<JoinSessionResponse> apiResponse = ApiResponse.success(response, "Session join successful");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("JOIN_SESSION", userId.toString(), false, executionTime);
            loggingService.logError("JOIN_SESSION", e, context);
            throw e;
        }
    }

    @PostMapping("/notes")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<BookingNote>> addNote(
            @Valid @RequestBody AddNoteRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", request.getBookingId());

        loggingService.logBusinessOperationStart("ADD_NOTE", userId.toString(), context);

        try {
            BookingNote response = classSessionService.addNote(userId, request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ADD_NOTE", userId.toString(), true, executionTime);

            ApiResponse<BookingNote> apiResponse = ApiResponse.success(response, "Note added successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.status(201).body(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ADD_NOTE", userId.toString(), false, executionTime);
            loggingService.logError("ADD_NOTE", e, context);
            throw e;
        }
    }

    @PostMapping("/bookmarks")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<BookingBookmark>> addBookmark(
            @Valid @RequestBody AddBookmarkRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", request.getBookingId());

        loggingService.logBusinessOperationStart("ADD_BOOKMARK", userId.toString(), context);

        try {
            BookingBookmark response = classSessionService.addBookmark(userId, request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ADD_BOOKMARK", userId.toString(), true, executionTime);

            ApiResponse<BookingBookmark> apiResponse = ApiResponse.success(response, "Bookmark added successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.status(201).body(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ADD_BOOKMARK", userId.toString(), false, executionTime);
            loggingService.logError("ADD_BOOKMARK", e, context);
            throw e;
        }
    }

    @GetMapping("/notes/{bookingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<BookingNote>>> getBookingNotes(
            @PathVariable Long bookingId,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", bookingId);

        loggingService.logBusinessOperationStart("GET_BOOKING_NOTES", userId.toString(), context);

        try {
            List<BookingNote> response = classSessionService.getBookingNotes(userId, bookingId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOOKING_NOTES", userId.toString(), true, executionTime);

            ApiResponse<List<BookingNote>> apiResponse = ApiResponse.success(response, "Notes retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOOKING_NOTES", userId.toString(), false, executionTime);
            loggingService.logError("GET_BOOKING_NOTES", e, context);
            throw e;
        }
    }

    @GetMapping("/bookmarks/{bookingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<BookingBookmark>>> getBookingBookmarks(
            @PathVariable Long bookingId,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", bookingId);

        loggingService.logBusinessOperationStart("GET_BOOKING_BOOKMARKS", userId.toString(), context);

        try {
            List<BookingBookmark> response = classSessionService.getBookingBookmarks(userId, bookingId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOOKING_BOOKMARKS", userId.toString(), true, executionTime);

            ApiResponse<List<BookingBookmark>> apiResponse = ApiResponse.success(response, "Bookmarks retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOOKING_BOOKMARKS", userId.toString(), false, executionTime);
            loggingService.logError("GET_BOOKING_BOOKMARKS", e, context);
            throw e;
        }
    }
}

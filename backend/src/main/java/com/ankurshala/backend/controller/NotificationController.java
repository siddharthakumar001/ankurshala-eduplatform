package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.notification.MarkNotificationsReadRequest;
import com.ankurshala.backend.dto.notification.NotificationDto;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.service.NotificationService;
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
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private LoggingService loggingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @RequestParam(required = false) Boolean unreadOnly,
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("unreadOnly", unreadOnly);

        loggingService.logBusinessOperationStart("GET_NOTIFICATIONS", userId.toString(), context);

        try {
            List<NotificationDto> response = Boolean.TRUE.equals(unreadOnly) 
                    ? notificationService.getUnreadNotifications(userId)
                    : notificationService.getUserNotifications(userId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_NOTIFICATIONS", userId.toString(), true, executionTime);

            ApiResponse<List<NotificationDto>> apiResponse = ApiResponse.success(response, "Notifications retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_NOTIFICATIONS", userId.toString(), false, executionTime);
            loggingService.logError("GET_NOTIFICATIONS", e, context);
            throw e;
        }
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            Authentication authentication,
            HttpServletRequest request) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);

        loggingService.logBusinessOperationStart("GET_UNREAD_COUNT", userId.toString(), context);

        try {
            Long response = notificationService.getUnreadCount(userId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_UNREAD_COUNT", userId.toString(), true, executionTime);

            ApiResponse<Long> apiResponse = ApiResponse.success(response, "Unread count retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_UNREAD_COUNT", userId.toString(), false, executionTime);
            loggingService.logError("GET_UNREAD_COUNT", e, context);
            throw e;
        }
    }

    @PostMapping("/mark-read")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> markNotificationsAsRead(
            @Valid @RequestBody MarkNotificationsReadRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("notificationIds", request.getNotificationIds());

        loggingService.logBusinessOperationStart("MARK_NOTIFICATIONS_READ", userId.toString(), context);

        try {
            notificationService.markNotificationsAsRead(userId, request);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("MARK_NOTIFICATIONS_READ", userId.toString(), true, executionTime);

            ApiResponse<Void> apiResponse = ApiResponse.success(null, "Notifications marked as read successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("MARK_NOTIFICATIONS_READ", userId.toString(), false, executionTime);
            loggingService.logError("MARK_NOTIFICATIONS_READ", e, context);
            throw e;
        }
    }
}

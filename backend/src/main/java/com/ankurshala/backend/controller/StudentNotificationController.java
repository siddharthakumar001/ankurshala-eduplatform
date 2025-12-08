package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.StudentNotificationDto;
import com.ankurshala.backend.dto.student.StudentNotificationSettingsDto;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class StudentNotificationController {

    private final StudentNotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<StudentNotificationDto>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        
        log.info("Getting notifications for student {}", userPrincipal.getId());
        
        Page<StudentNotificationDto> notifications = notificationService.getNotifications(userPrincipal, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Integer> getUnreadNotificationCount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting unread notification count for student {}", userPrincipal.getId());
        
        int count = notificationService.getUnreadNotificationCount(userPrincipal);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentNotificationDto> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Marking notification {} as read for student {}", notificationId, userPrincipal.getId());
        
        StudentNotificationDto notification = notificationService.markAsRead(notificationId, userPrincipal);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Marking all notifications as read for student {}", userPrincipal.getId());
        
        notificationService.markAllAsRead(userPrincipal);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentNotificationSettingsDto> getNotificationSettings(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting notification settings for student {}", userPrincipal.getId());
        
        StudentNotificationSettingsDto settings = notificationService.getNotificationSettings(userPrincipal);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentNotificationSettingsDto> updateNotificationSettings(
            @RequestBody StudentNotificationSettingsDto settings,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Updating notification settings for student {}", userPrincipal.getId());
        
        StudentNotificationSettingsDto updatedSettings = notificationService.updateNotificationSettings(settings, userPrincipal);
        return ResponseEntity.ok(updatedSettings);
    }
}

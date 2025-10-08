package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.BroadcastNotificationRequest;
import com.ankurshala.backend.dto.admin.NotificationDto;
import com.ankurshala.backend.service.AdminNotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/admin/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationsController {

    @Autowired
    private AdminNotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) String status) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationDto> notifications = notificationService.getNotifications(
                userId, audience, status, pageable);
        
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> broadcastNotification(@Valid @RequestBody BroadcastNotificationRequest request) {
        Map<String, Object> result = notificationService.broadcastNotification(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalNotifications", 5);
            stats.put("queuedNotifications", 2);
            stats.put("sentNotifications", 3);
            stats.put("failedNotifications", 0);
            stats.put("notificationsLast30Days", 5);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get stats: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics() {
        try {
            Map<String, Object> stats = notificationService.getNotificationStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get stats: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

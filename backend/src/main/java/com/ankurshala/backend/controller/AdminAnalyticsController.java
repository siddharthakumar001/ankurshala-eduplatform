package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.AnalyticsOverviewDto;
import com.ankurshala.backend.service.AdminAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/analytics")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    @Autowired
    private AdminAnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalyticsOverviewDto> getOverview(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        AnalyticsOverviewDto overview = analyticsService.getAnalyticsOverview(from, to);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Map<String, Object> analytics = analyticsService.getUserAnalytics(from, to);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/content")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getContentAnalytics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Map<String, Object> analytics = analyticsService.getContentAnalytics(from, to);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/imports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getImportAnalytics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Map<String, Object> analytics = analyticsService.getImportAnalytics(from, to);
        return ResponseEntity.ok(analytics);
    }
}

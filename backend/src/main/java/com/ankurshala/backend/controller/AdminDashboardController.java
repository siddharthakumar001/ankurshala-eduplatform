package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.DashboardMetricsDto;
import com.ankurshala.backend.dto.admin.DashboardSeriesDto;
import com.ankurshala.backend.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardMetricsDto> getMetrics() {
        DashboardMetricsDto metrics = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/series")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DashboardSeriesDto>> getSeries() {
        List<DashboardSeriesDto> series = dashboardService.getDashboardSeries();
        return ResponseEntity.ok(series);
    }
}

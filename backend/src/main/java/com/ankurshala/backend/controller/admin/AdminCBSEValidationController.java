package com.ankurshala.backend.controller.admin;

import com.ankurshala.backend.health.CBSEReadinessIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin-only endpoint for CBSE readiness validation.
 * Returns detailed validation status for CBSE board configuration.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/cbse-validation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCBSEValidationController {

    private final CBSEReadinessIndicator cbseReadinessIndicator;

    /**
     * GET /api/admin/cbse-validation
     * Validates CBSE board readiness for student experience.
     * 
     * @return Validation status with detailed breakdown
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> validateCBSE() {
        log.info("CBSE validation check requested by admin");
        
        Health health = cbseReadinessIndicator.health();
        Map<String, Object> details = health.getDetails();
        
        boolean ready = health.getStatus().getCode().equals("UP");
        
        Map<String, Object> response = Map.of(
                "ready", ready,
                "status", health.getStatus().getCode(),
                "details", details
        );
        
        if (ready) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response); // Service Unavailable
        }
    }
}


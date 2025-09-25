package com.ankurshala.backend.controller;

import com.ankurshala.backend.bootstrap.BulkDemoSeeder;
import com.ankurshala.backend.bootstrap.BulkDemoSeeder.SeedingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin controller for development seeding operations.
 * Provides REST endpoint for bulk seeding with proper security and environment guards.
 */
@RestController
@RequestMapping("/api/admin/dev-seed")
public class AdminDevSeedController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDevSeedController.class);

    @Value("${DEMO_ENV:local}")
    private String demoEnv;

    @Value("${DEMO_FORCE:false}")
    private boolean demoForce;

    @Autowired(required = false)
    private BulkDemoSeeder bulkDemoSeeder;

    /**
     * Bulk seed endpoint that creates 15 students, 15 teachers, and 3 admins.
     * Protected by environment flags and admin role.
     * 
     * @return JSON response with seeding summary
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkSeed() {
        logger.info("Bulk seed endpoint called by admin");

        // Check if BulkDemoSeeder is available
        if (bulkDemoSeeder == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "BulkDemoSeeder is not available. Check environment variables DEMO_BULK_SEED and DEMO_ENV.");
            response.put("demoEnv", demoEnv);
            response.put("demoForce", demoForce);
            return ResponseEntity.badRequest().body(response);
        }

        // Check environment guards
        if (!canRunInEnvironment()) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Bulk seeding not allowed in " + demoEnv + " environment without DEMO_FORCE=true");
            response.put("demoEnv", demoEnv);
            response.put("demoForce", demoForce);
            return ResponseEntity.badRequest().body(response);
        }

        try {
            SeedingResult result = bulkDemoSeeder.seedAllUsers();
            
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> summary = new HashMap<>();
            
            Map<String, Object> students = new HashMap<>();
            students.put("created", result.getStudentsCreated());
            students.put("updated", result.getStudentsUpdated());
            
            Map<String, Object> teachers = new HashMap<>();
            teachers.put("created", result.getTeachersCreated());
            teachers.put("updated", result.getTeachersUpdated());
            
            Map<String, Object> admins = new HashMap<>();
            admins.put("created", result.getAdminsCreated());
            admins.put("updated", result.getAdminsUpdated());
            
            summary.put("students", students);
            summary.put("teachers", teachers);
            summary.put("admins", admins);
            
            response.put("summary", summary);
            response.put("errors", result.getErrors());
            
            logger.info("Bulk seeding completed successfully: {}", result);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Bulk seeding failed", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Bulk seeding failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Check if bulk seeding can run in the current environment.
     * 
     * @return true if seeding is allowed, false otherwise
     */
    private boolean canRunInEnvironment() {
        if ("prod".equalsIgnoreCase(demoEnv) && !demoForce) {
            return false;
        }
        return true;
    }

    /**
     * Health check endpoint to verify the seeding service is available.
     * 
     * @return simple status response
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "available");
        response.put("demoEnv", demoEnv);
        response.put("demoForce", demoForce);
        response.put("canRun", canRunInEnvironment());
        return ResponseEntity.ok(response);
    }
}

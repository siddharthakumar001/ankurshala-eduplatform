package com.ankurshala.backend.controller;

import com.ankurshala.backend.bootstrap.DemoDataSeeder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Stage-1 FE complete: Enhanced dev seeder endpoint for comprehensive demo data
@RestController
@RequestMapping("/admin")
@ConditionalOnProperty(name = "app.dev.seeder.enabled", havingValue = "true", matchIfMissing = false)
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
public class DevController {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @PostMapping("/dev-seed")
    public ResponseEntity<?> seedDemoData() {
        try {
            DemoDataSeeder.SeedResult result = demoDataSeeder.seedDemoData();
            
            return ResponseEntity.ok(Map.of(
                "message", "Demo data seeded successfully!",
                "result", result,
                "credentials", Map.of(
                    "admin", Map.of("email", "siddhartha@ankurshala.com", "password", "Maza@123"),
                    "students", Map.of(
                        "student1", Map.of("email", "student1@ankurshala.com", "password", "Maza@123"),
                        "student2", Map.of("email", "student2@ankurshala.com", "password", "Maza@123"),
                        "student3", Map.of("email", "student3@ankurshala.com", "password", "Maza@123"),
                        "student4", Map.of("email", "student4@ankurshala.com", "password", "Maza@123"),
                        "student5", Map.of("email", "student5@ankurshala.com", "password", "Maza@123")
                    ),
                    "teachers", Map.of(
                        "teacher1", Map.of("email", "teacher1@ankurshala.com", "password", "Maza@123"),
                        "teacher2", Map.of("email", "teacher2@ankurshala.com", "password", "Maza@123"),
                        "teacher3", Map.of("email", "teacher3@ankurshala.com", "password", "Maza@123"),
                        "teacher4", Map.of("email", "teacher4@ankurshala.com", "password", "Maza@123"),
                        "teacher5", Map.of("email", "teacher5@ankurshala.com", "password", "Maza@123")
                    )
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to seed demo data: " + e.getMessage()));
        }
    }
}

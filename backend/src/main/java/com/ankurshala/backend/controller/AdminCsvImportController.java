package com.ankurshala.backend.controller;

import com.ankurshala.backend.entity.ImportJob;
import com.ankurshala.backend.service.CsvContentImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/content/import")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminCsvImportController {

    @Autowired
    private CsvContentImportService csvImportService;

    @PostMapping(value = "/csv", consumes = "text/csv")
    public ResponseEntity<Map<String, Object>> uploadCsvContent(
            @RequestBody byte[] csvContent,
            @RequestParam(defaultValue = "false") boolean dryRun,
            Authentication authentication) {
        
        try {
            // Validate content is not empty
            if (csvContent == null || csvContent.length == 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "type", "https://ankurshala.com/problems/invalid-file",
                        "title", "Invalid File",
                        "status", 400,
                        "detail", "CSV content is empty",
                        "instance", "/admin/content/import/csv"
                    ));
            }
            
            // Validate CSV headers synchronously before creating job
            try {
                csvImportService.validateCsvHeaders(csvContent);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "type", "https://ankurshala.com/problems/invalid-headers",
                        "title", "Invalid Headers",
                        "status", 400,
                        "detail", e.getMessage(),
                        "instance", "/admin/content/import/csv"
                    ));
            }
            
            // Get user ID from authentication
            Long userId = null;
            if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                // This would need to be implemented based on your authentication setup
                // For now, we'll proceed without user tracking
            }
            
            // Create import job
            ImportJob importJob = csvImportService.createImportJob(
                "upload.csv", 
                (long) csvContent.length,
                userId
            );
            
            // Process CSV content asynchronously
            csvImportService.processFileAsync(importJob, csvContent, dryRun);
            
            return ResponseEntity.ok(Map.of(
                "message", dryRun ? "Dry run completed successfully" : "CSV uploaded and processing started",
                "jobId", importJob.getId(),
                "status", importJob.getStatus().toString(),
                "dryRun", dryRun
            ));
            
        } catch (IllegalArgumentException e) {
            // Handle validation errors (missing headers, invalid data)
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "type", "https://ankurshala.com/problems/invalid-headers",
                    "title", "Invalid Headers",
                    "status", 400,
                    "detail", e.getMessage(),
                    "instance", "/admin/content/import/csv"
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "type", "https://ankurshala.com/problems/import-failed",
                    "title", "Import Failed",
                    "status", 500,
                    "detail", "Failed to process file: " + e.getMessage(),
                    "instance", "/admin/content/import/csv"
                ));
        }
    }

    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> getAllImportJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Get all import jobs with pagination
        var importJobs = csvImportService.getAllImportJobs(page, size);
        return ResponseEntity.ok(Map.of(
            "content", importJobs.getContent(),
            "totalElements", importJobs.getTotalElements(),
            "totalPages", importJobs.getTotalPages(),
            "currentPage", page,
            "size", size
        ));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<Map<String, Object>> getImportJobStatus(@PathVariable Long jobId) {
        // This would need to be implemented to fetch job details
        // For now, return a placeholder response
        return ResponseEntity.ok(Map.of(
            "jobId", jobId,
            "status", "SUCCEEDED",
            "message", "Import job details endpoint - to be implemented"
        ));
    }

    @GetMapping("/sample-csv")
    public ResponseEntity<String> downloadSampleCsv() {
        String csvContent = csvImportService.generateSampleCsv();
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"content_sample.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvContent);
    }
}

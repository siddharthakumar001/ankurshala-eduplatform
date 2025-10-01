package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.StudentDetailDto;
import com.ankurshala.backend.dto.admin.StudentListDto;
import com.ankurshala.backend.dto.admin.StudentUpdateDto;
import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.service.AdminStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

// NOTE: server.servlet.context-path=/api is set for the app.
// Therefore controller @RequestMapping must NOT start with "/api".
@RestController
@RequestMapping("/admin/students")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentsController {

    // Whitelist of allowed sort fields to prevent SQL injection
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "firstName", "lastName", "createdAt", "schoolName", "user.email",
        "educationalBoard", "classLevel", "enabled"
    );

    @Autowired
    private AdminStudentService adminStudentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StudentListDto>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) EducationalBoard educationalBoard,
            @RequestParam(required = false) ClassLevel classLevel) {
        
        // Validate and sanitize sort field
        String validatedSortBy = validateSortField(sortBy);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(validatedSortBy).descending() : 
                   Sort.by(validatedSortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StudentListDto> students = adminStudentService.getStudentsWithFilters(
                search, enabled, educationalBoard, classLevel, pageable);
        
        return ResponseEntity.ok(students);
    }
    
    /**
     * Validates the sort field against the whitelist and maps frontend fields to backend fields
     */
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt"; // default
        }
        
        // Map frontend field names to backend field names
        String mappedField = switch (sortBy.toLowerCase()) {
            case "email" -> "user.email";
            case "firstname" -> "firstName";
            case "lastname" -> "lastName";
            case "createdat" -> "createdAt";
            case "schoolname" -> "schoolName";
            case "educationalboard" -> "educationalBoard";
            case "classlevel" -> "classLevel";
            case "enabled" -> "enabled";
            default -> sortBy;
        };
        
        // Check if the mapped field is in the whitelist
        if (ALLOWED_SORT_FIELDS.contains(mappedField)) {
            return mappedField;
        }
        
        // Return default if not in whitelist
        return "createdAt";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDetailDto> getStudent(@PathVariable Long id) {
        return adminStudentService.getStudentById(id)
                .map(student -> ResponseEntity.ok(student))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDetailDto> updateStudent(
            @PathVariable Long id, 
            @RequestBody StudentUpdateDto updateDto) {
        try {
            StudentDetailDto updatedStudent = adminStudentService.updateStudent(id, updateDto);
            return ResponseEntity.ok(updatedStudent);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleStudentStatus(@PathVariable Long id) {
        try {
            boolean newStatus = adminStudentService.toggleStudentStatus(id);
            return ResponseEntity.ok(Map.of(
                "id", id,
                "enabled", newStatus,
                "message", "Student status updated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteStudent(@PathVariable Long id) {
        try {
            adminStudentService.deleteStudent(id);
            return ResponseEntity.ok(Map.of(
                "message", "Student deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStudentStats() {
        // This would typically come from a service method
        // For now, returning placeholder data
        return ResponseEntity.ok(Map.of(
            "totalStudents", 0,
            "activeStudents", 0,
            "inactiveStudents", 0,
            "newStudentsThisMonth", 0
        ));
    }
}

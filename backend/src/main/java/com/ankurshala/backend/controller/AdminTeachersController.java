package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.TeacherDetailDto;
import com.ankurshala.backend.dto.admin.TeacherListDto;
import com.ankurshala.backend.entity.TeacherStatus;
import com.ankurshala.backend.service.AdminTeacherService;
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
import java.util.Set;

@RestController
@RequestMapping("/admin/teachers")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeachersController {

    // Whitelist of allowed sort fields to prevent SQL injection
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "firstName", "lastName", "createdAt", "specialization", "user.email",
        "status", "verified", "rating", "yearsOfExperience", "hourlyRate"
    );

    @Autowired
    private AdminTeacherService adminTeacherService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TeacherListDto>> getTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) TeacherStatus status,
            @RequestParam(required = false) Boolean verified) {

        // Validate and map sortBy field
        String validatedSortBy = validateSortField(sortBy);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                   Sort.by(validatedSortBy).descending() :
                   Sort.by(validatedSortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TeacherListDto> teachers = adminTeacherService.getTeachersWithFilters(
                search, enabled, status, verified, pageable);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherDetailDto> getTeacher(@PathVariable Long id) {
        return adminTeacherService.getTeacherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherDetailDto> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherDetailDto teacherDetailDto) {
        try {
            TeacherDetailDto updatedTeacher = adminTeacherService.updateTeacher(id, teacherDetailDto);
            return ResponseEntity.ok(updatedTeacher);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleTeacherStatus(@PathVariable Long id) {
        try {
            boolean newStatus = adminTeacherService.toggleTeacherStatus(id);
            return ResponseEntity.ok(Map.of(
                "id", id,
                "enabled", newStatus,
                "message", "Teacher status updated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String validateSortField(String sortBy) {
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            // Default to 'createdAt' if the requested sort field is not allowed
            return "createdAt";
        }
        // Map 'email' to 'user.email' for proper JPA sorting
        if ("email".equalsIgnoreCase(sortBy)) {
            return "user.email";
        }
        return sortBy;
    }
}

package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.student.TeacherSearchRequest;
import com.ankurshala.backend.dto.student.TeacherSearchResponse;
import com.ankurshala.backend.dto.student.TeacherAvailabilityRequest;
import com.ankurshala.backend.dto.student.TeacherAvailabilityResponse;
import com.ankurshala.backend.service.PublicTeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Teacher Controller
 * Provides public access to teacher search and availability for booking purposes
 */
@RestController
@RequestMapping("/public/teachers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class PublicTeacherController {

    private final PublicTeacherService publicTeacherService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<TeacherSearchResponse>>> searchTeachers(
            @Valid @RequestBody TeacherSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Searching teachers with criteria: {}", request);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TeacherSearchResponse> teachers = publicTeacherService.searchTeachers(request, pageable);
        
        ApiResponse<Page<TeacherSearchResponse>> response = ApiResponse.success(
            teachers, 
            "Teachers retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<TeacherAvailabilityResponse>> checkTeacherAvailability(
            @Valid @RequestBody TeacherAvailabilityRequest request) {
        
        log.info("Checking availability for teacher {} on {}", request.getTeacherId(), request.getDate());
        
        TeacherAvailabilityResponse availability = publicTeacherService.getTeacherAvailability(request);
        
        ApiResponse<TeacherAvailabilityResponse> response = ApiResponse.success(
            availability, 
            "Teacher availability retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teacherId}/profile")
    public ResponseEntity<ApiResponse<TeacherSearchResponse>> getTeacherProfile(
            @PathVariable Long teacherId) {
        
        log.info("Getting teacher profile for teacher {}", teacherId);
        
        TeacherSearchResponse teacher = publicTeacherService.getTeacherProfile(teacherId);
        
        ApiResponse<TeacherSearchResponse> response = ApiResponse.success(
            teacher, 
            "Teacher profile retrieved successfully"
        );
        
        return ResponseEntity.ok(response);
    }
}

package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.StudentDetailDto;
import com.ankurshala.backend.dto.admin.StudentListDto;
import com.ankurshala.backend.dto.admin.StudentUpdateDto;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.service.AdminStudentService;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Enhanced Admin Students Controller
 * Handles CRUD operations for student management with comprehensive logging and error handling
 */
@Slf4j
@RestController
@RequestMapping("/admin/students")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentsController {

    // Whitelist of allowed sort fields to prevent SQL injection
    private static final Set<String> ALLOWED_SORT_FIELDS = new HashSet<>();
    
    static {
        ALLOWED_SORT_FIELDS.add("firstName");
        ALLOWED_SORT_FIELDS.add("lastName");
        ALLOWED_SORT_FIELDS.add("createdAt");
        ALLOWED_SORT_FIELDS.add("schoolName");
        ALLOWED_SORT_FIELDS.add("user.email");
        ALLOWED_SORT_FIELDS.add("educationalBoard");
        ALLOWED_SORT_FIELDS.add("classLevel");
        ALLOWED_SORT_FIELDS.add("enabled");
    }

    @Autowired
    private AdminStudentService adminStudentService;
    
    @Autowired
    private LoggingService loggingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<StudentListDto>>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) EducationalBoard educationalBoard,
            @RequestParam(required = false) ClassLevel classLevel,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("page", page);
        context.put("size", size);
        context.put("sortBy", sortBy);
        context.put("sortDir", sortDir);
        context.put("search", search);
        context.put("enabled", enabled);
        context.put("educationalBoard", educationalBoard);
        context.put("classLevel", classLevel);
        
        loggingService.logBusinessOperationStart("GET_STUDENTS", null, context);
        
        try {
            // Validate and sanitize input parameters
            validatePaginationParams(page, size);
            String validatedSortBy = validateSortField(sortBy);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(validatedSortBy).descending() : 
                       Sort.by(validatedSortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<StudentListDto> students = adminStudentService.getStudentsWithFilters(
                    search, enabled, educationalBoard, classLevel, pageable);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENTS", null, true, executionTime);
            
            ApiResponse<Page<StudentListDto>> apiResponse = ApiResponse.success(students, "Students retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENTS", null, false, executionTime);
            loggingService.logError("GET_STUDENTS", e, context);
            throw e;
        }
    }
    
    /**
     * Validates pagination parameters to prevent abuse
     */
    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Page number cannot be negative");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException("Page size must be between 1 and 100");
        }
    }
    
    /**
     * Validates the sort field against the whitelist and maps frontend fields to backend fields
     */
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createdAt"; // default
        }
        
        // Map frontend field names to backend field names
        String mappedField;
        switch (sortBy.toLowerCase()) {
            case "email":
                mappedField = "user.email";
                break;
            case "firstname":
                mappedField = "firstName";
                break;
            case "lastname":
                mappedField = "lastName";
                break;
            case "createdat":
                mappedField = "createdAt";
                break;
            case "schoolname":
                mappedField = "schoolName";
                break;
            case "educationalboard":
                mappedField = "educationalBoard";
                break;
            case "classlevel":
                mappedField = "classLevel";
                break;
            case "enabled":
                mappedField = "enabled";
                break;
            default:
                mappedField = sortBy;
                break;
        }
        
        // Check if the mapped field is in the whitelist
        if (ALLOWED_SORT_FIELDS.contains(mappedField)) {
            return mappedField;
        }
        
        // Return default if not in whitelist
        return "createdAt";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDetailDto>> getStudent(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("studentId", id);
        
        loggingService.logBusinessOperationStart("GET_STUDENT_BY_ID", id.toString(), context);
        
        try {
            Optional<StudentDetailDto> student = adminStudentService.getStudentById(id);
            
            if (student.isPresent()) {
                long executionTime = System.currentTimeMillis() - startTime;
                loggingService.logBusinessOperationComplete("GET_STUDENT_BY_ID", id.toString(), true, executionTime);
                
                ApiResponse<StudentDetailDto> apiResponse = ApiResponse.success(student.get(), "Student retrieved successfully");
                apiResponse.setTraceId(traceId);
                apiResponse.setRequestId(requestId);
                
                return ResponseEntity.ok(apiResponse);
            } else {
                long executionTime = System.currentTimeMillis() - startTime;
                loggingService.logBusinessOperationComplete("GET_STUDENT_BY_ID", id.toString(), false, executionTime);
                
                throw new ResourceNotFoundException("Student not found with id: " + id);
            }
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_BY_ID", id.toString(), false, executionTime);
            loggingService.logError("GET_STUDENT_BY_ID", e, context);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StudentDetailDto>> updateStudent(
            @PathVariable Long id, 
            @Valid @RequestBody StudentUpdateDto updateDto,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("studentId", id);
        context.put("updateFields", updateDto);
        
        loggingService.logBusinessOperationStart("UPDATE_STUDENT", id.toString(), context);
        
        try {
            StudentDetailDto updatedStudent = adminStudentService.updateStudent(id, updateDto);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_STUDENT", id.toString(), true, executionTime);
            
            ApiResponse<StudentDetailDto> apiResponse = ApiResponse.success(updatedStudent, "Student updated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_STUDENT", id.toString(), false, executionTime);
            loggingService.logError("UPDATE_STUDENT", e, context);
            throw e;
        }
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleStudentStatus(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("studentId", id);
        
        loggingService.logBusinessOperationStart("TOGGLE_STUDENT_STATUS", id.toString(), context);
        
        try {
            boolean newStatus = adminStudentService.toggleStudentStatus(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TOGGLE_STUDENT_STATUS", id.toString(), true, executionTime);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", id);
            responseData.put("enabled", newStatus);
            responseData.put("message", "Student status updated successfully");
            
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(responseData, "Student status updated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TOGGLE_STUDENT_STATUS", id.toString(), false, executionTime);
            loggingService.logError("TOGGLE_STUDENT_STATUS", e, context);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteStudent(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("studentId", id);
        
        loggingService.logBusinessOperationStart("DELETE_STUDENT", id.toString(), context);
        
        try {
            adminStudentService.deleteStudent(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_STUDENT", id.toString(), true, executionTime);
            
            Map<String, String> responseData = new HashMap<>();
            responseData.put("message", "Student deleted successfully");
            
            ApiResponse<Map<String, String>> apiResponse = ApiResponse.success(responseData, "Student deleted successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_STUDENT", id.toString(), false, executionTime);
            loggingService.logError("DELETE_STUDENT", e, context);
            throw e;
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentStats(HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        loggingService.logBusinessOperationStart("GET_STUDENT_STATS", null, new HashMap<>());
        
        try {
            // This would typically come from a service method
            // For now, returning placeholder data
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalStudents", 0);
            stats.put("activeStudents", 0);
            stats.put("inactiveStudents", 0);
            stats.put("newStudentsThisMonth", 0);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_STATS", null, true, executionTime);
            
            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(stats, "Student statistics retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_STATS", null, false, executionTime);
            loggingService.logError("GET_STUDENT_STATS", e, new HashMap<>());
            throw e;
        }
    }
}

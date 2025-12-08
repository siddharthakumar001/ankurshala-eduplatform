package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.content.*;
import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.AdminContentManagementService;
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
import java.util.List;
import java.util.Map;

/**
 * Enhanced Admin Content Management Controller
 * Handles CRUD operations for educational content management
 * Implements proper design patterns with comprehensive logging and error handling
 */
@Slf4j
@RestController
@RequestMapping("/admin/content")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
public class AdminContentManagementController {

    private final AdminContentManagementService contentManagementService;
    
    @Autowired
    private LoggingService loggingService;
    
    public AdminContentManagementController(AdminContentManagementService contentManagementService) {
        this.contentManagementService = contentManagementService;
    }

    // ============ BOARDS CRUD ============
    
    @GetMapping("/boards")
    public ResponseEntity<ApiResponse<Page<BoardDto>>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("page", page);
        context.put("size", size);
        context.put("search", search);
        context.put("active", active);
        context.put("sortBy", sortBy);
        context.put("sortDir", sortDir);
        
        loggingService.logBusinessOperationStart("GET_BOARDS", null, context);
        
        try {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
            Page<BoardDto> boards = contentManagementService.getBoards(search, active, pageable);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOARDS", null, true, executionTime);
            
            ApiResponse<Page<BoardDto>> response = ApiResponse.success(boards, "Boards retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOARDS", null, false, executionTime);
            loggingService.logError("GET_BOARDS", e, context);
            throw e;
        }
    }

    @GetMapping("/boards/{id}")
    public ResponseEntity<ApiResponse<BoardDto>> getBoardById(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("boardId", id);
        
        loggingService.logBusinessOperationStart("GET_BOARD_BY_ID", null, context);
        
        try {
            BoardDto board = contentManagementService.getBoardById(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOARD_BY_ID", id.toString(), true, executionTime);
            
            ApiResponse<BoardDto> response = ApiResponse.success(board, "Board retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOARD_BY_ID", id.toString(), false, executionTime);
            loggingService.logError("GET_BOARD_BY_ID", e, context);
            throw e;
        }
    }

    @PostMapping("/boards")
    public ResponseEntity<ApiResponse<BoardDto>> createBoard(
            @Valid @RequestBody CreateBoardRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("boardName", request.getName());
        context.put("boardActive", request.getActive());
        
        loggingService.logBusinessOperationStart("CREATE_BOARD", null, context);
        
        try {
        BoardDto board = contentManagementService.createBoard(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_BOARD", board.getId().toString(), true, executionTime);
            
            ApiResponse<BoardDto> response = ApiResponse.success(board, "Board created successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_BOARD", null, false, executionTime);
            loggingService.logError("CREATE_BOARD", e, context);
            throw e;
        }
    }

    @PutMapping("/boards/{id}")
    public ResponseEntity<ApiResponse<BoardDto>> updateBoard(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateBoardRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("boardId", id);
        context.put("boardName", request.getName());
        context.put("boardActive", request.getActive());
        
        loggingService.logBusinessOperationStart("UPDATE_BOARD", id.toString(), context);
        
        try {
        BoardDto board = contentManagementService.updateBoard(id, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_BOARD", id.toString(), true, executionTime);
            
            ApiResponse<BoardDto> response = ApiResponse.success(board, "Board updated successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_BOARD", id.toString(), false, executionTime);
            loggingService.logError("UPDATE_BOARD", e, context);
            throw e;
        }
    }

    @DeleteMapping("/boards/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("boardId", id);
        
        loggingService.logBusinessOperationStart("DELETE_BOARD", id.toString(), context);
        
        try {
            contentManagementService.deleteBoard(id, false); // false = soft delete
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_BOARD", id.toString(), true, executionTime);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Board deleted successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_BOARD", id.toString(), false, executionTime);
            loggingService.logError("DELETE_BOARD", e, context);
            throw e;
        }
    }

    // ============ GRADES CRUD ============
    
    @GetMapping("/grades")
    public ResponseEntity<ApiResponse<Page<GradeDto>>> getGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("page", page);
        context.put("size", size);
        context.put("search", search);
        context.put("boardId", boardId);
        context.put("active", active);
        context.put("sortBy", sortBy);
        context.put("sortDir", sortDir);
        
        loggingService.logBusinessOperationStart("GET_GRADES", null, context);
        
        try {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
            Page<GradeDto> grades = contentManagementService.getGrades(search, boardId, active, pageable);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADES", null, true, executionTime);
            
            ApiResponse<Page<GradeDto>> response = ApiResponse.success(grades, "Grades retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADES", null, false, executionTime);
            loggingService.logError("GET_GRADES", e, context);
            throw e;
        }
    }

    @GetMapping("/grades/by-board")
    public ResponseEntity<ApiResponse<List<GradeDto>>> getGradesByBoard(
            @RequestParam String board,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("board", board);
        
        loggingService.logBusinessOperationStart("GET_GRADES_BY_BOARD", null, context);
        
        try {
            List<GradeDto> grades = contentManagementService.getGradesByBoardName(board);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADES_BY_BOARD", null, true, executionTime);
            
            ApiResponse<List<GradeDto>> response = ApiResponse.success(grades, "Grades by board retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADES_BY_BOARD", null, false, executionTime);
            loggingService.logError("GET_GRADES_BY_BOARD", e, context);
            throw e;
        }
    }

    @GetMapping("/grades/{id}")
    public ResponseEntity<ApiResponse<GradeDto>> getGradeById(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("gradeId", id);
        
        loggingService.logBusinessOperationStart("GET_GRADE_BY_ID", null, context);
        
        try {
            GradeDto grade = contentManagementService.getGradeById(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADE_BY_ID", id.toString(), true, executionTime);
            
            ApiResponse<GradeDto> response = ApiResponse.success(grade, "Grade retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADE_BY_ID", id.toString(), false, executionTime);
            loggingService.logError("GET_GRADE_BY_ID", e, context);
            throw e;
        }
    }

    @PostMapping("/grades")
    public ResponseEntity<ApiResponse<GradeDto>> createGrade(
            @Valid @RequestBody CreateGradeRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("gradeName", request.getName());
        context.put("gradeDisplayName", request.getDisplayName());
        context.put("boardId", request.getBoardId());
        
        loggingService.logBusinessOperationStart("CREATE_GRADE", null, context);
        
        try {
            GradeDto grade = contentManagementService.createGrade(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_GRADE", grade.getId().toString(), true, executionTime);
            
            ApiResponse<GradeDto> response = ApiResponse.success(grade, "Grade created successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_GRADE", null, false, executionTime);
            loggingService.logError("CREATE_GRADE", e, context);
            throw e;
        }
    }

    @PutMapping("/grades/{id}")
    public ResponseEntity<ApiResponse<GradeDto>> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGradeRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("gradeId", id);
        context.put("gradeName", request.getName());
        context.put("gradeDisplayName", request.getDisplayName());
        context.put("gradeActive", request.getActive());
        
        loggingService.logBusinessOperationStart("UPDATE_GRADE", id.toString(), context);
        
        try {
            GradeDto grade = contentManagementService.updateGrade(id, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_GRADE", id.toString(), true, executionTime);
            
            ApiResponse<GradeDto> response = ApiResponse.success(grade, "Grade updated successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_GRADE", id.toString(), false, executionTime);
            loggingService.logError("UPDATE_GRADE", e, context);
            throw e;
        }
    }

    @DeleteMapping("/grades/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGrade(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("gradeId", id);
        
        loggingService.logBusinessOperationStart("DELETE_GRADE", id.toString(), context);
        
        try {
            contentManagementService.deleteGrade(id, false); // false = soft delete
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_GRADE", id.toString(), true, executionTime);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Grade deleted successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_GRADE", id.toString(), false, executionTime);
            loggingService.logError("DELETE_GRADE", e, context);
            throw e;
        }
    }

    // ============ TEST METHOD ============
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test endpoint working");
    }

    // ============ SUBJECTS CRUD ============
    
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<Page<SubjectDto>>> getSubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("page", page);
        context.put("size", size);
        context.put("search", search);
        context.put("boardId", boardId);
        context.put("gradeId", gradeId);
        context.put("active", active);
        context.put("sortBy", sortBy);
        context.put("sortDir", sortDir);
        
        loggingService.logBusinessOperationStart("GET_SUBJECTS", null, context);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<SubjectDto> subjects = contentManagementService.getSubjects(search, boardId, gradeId, active, pageable);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECTS", null, true, executionTime);
            
            ApiResponse<Page<SubjectDto>> response = ApiResponse.success(subjects, "Subjects retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECTS", null, false, executionTime);
            loggingService.logError("GET_SUBJECTS", e, context);
            throw e;
        }
    }

    @GetMapping("/subjects/by-grade")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> getSubjectsByGrade(
            @RequestParam String board,
            @RequestParam String grade,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("board", board);
        context.put("grade", grade);
        
        loggingService.logBusinessOperationStart("GET_SUBJECTS_BY_GRADE", null, context);
        
        try {
            List<SubjectDto> subjects = contentManagementService.getSubjectsByBoardAndGrade(board, grade);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECTS_BY_GRADE", null, true, executionTime);
            
            ApiResponse<List<SubjectDto>> response = ApiResponse.success(subjects, "Subjects by grade retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECTS_BY_GRADE", null, false, executionTime);
            loggingService.logError("GET_SUBJECTS_BY_GRADE", e, context);
            throw e;
        }
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectDto>> getSubjectById(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("subjectId", id);
        
        loggingService.logBusinessOperationStart("GET_SUBJECT_BY_ID", null, context);
        
        try {
            SubjectDto subject = contentManagementService.getSubjectById(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECT_BY_ID", id.toString(), true, executionTime);
            
            ApiResponse<SubjectDto> response = ApiResponse.success(subject, "Subject retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECT_BY_ID", id.toString(), false, executionTime);
            loggingService.logError("GET_SUBJECT_BY_ID", e, context);
            throw e;
        }
    }

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectDto>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("subjectName", request.getName());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        
        loggingService.logBusinessOperationStart("CREATE_SUBJECT", null, context);
        
        try {
            SubjectDto subject = contentManagementService.createSubject(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_SUBJECT", subject.getId().toString(), true, executionTime);
            
            ApiResponse<SubjectDto> response = ApiResponse.success(subject, "Subject created successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_SUBJECT", null, false, executionTime);
            loggingService.logError("CREATE_SUBJECT", e, context);
            throw e;
        }
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectDto>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("subjectId", id);
        context.put("subjectName", request.getName());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        context.put("subjectActive", request.getActive());
        
        loggingService.logBusinessOperationStart("UPDATE_SUBJECT", id.toString(), context);
        
        try {
            SubjectDto subject = contentManagementService.updateSubject(id, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_SUBJECT", id.toString(), true, executionTime);
            
            ApiResponse<SubjectDto> response = ApiResponse.success(subject, "Subject updated successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_SUBJECT", id.toString(), false, executionTime);
            loggingService.logError("UPDATE_SUBJECT", e, context);
            throw e;
        }
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("subjectId", id);
        
        loggingService.logBusinessOperationStart("DELETE_SUBJECT", id.toString(), context);
        
        try {
            contentManagementService.deleteSubject(id, false); // false = soft delete
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_SUBJECT", id.toString(), true, executionTime);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Subject deleted successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_SUBJECT", id.toString(), false, executionTime);
            loggingService.logError("DELETE_SUBJECT", e, context);
            throw e;
        }
    }

    // ============ CHAPTERS CRUD ============
    
    @GetMapping("/chapters")
    public ResponseEntity<ApiResponse<Page<ChapterDto>>> getChapters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("page", page);
        context.put("size", size);
        context.put("search", search);
        context.put("boardId", boardId);
        context.put("gradeId", gradeId);
        context.put("subjectId", subjectId);
        context.put("active", active);
        context.put("sortBy", sortBy);
        context.put("sortDir", sortDir);
        
        loggingService.logBusinessOperationStart("GET_CHAPTERS", null, context);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ChapterDto> chapters = contentManagementService.getChapters(search, boardId, gradeId, subjectId, active, pageable);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_CHAPTERS", null, true, executionTime);
            
            ApiResponse<Page<ChapterDto>> response = ApiResponse.success(chapters, "Chapters retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_CHAPTERS", null, false, executionTime);
            loggingService.logError("GET_CHAPTERS", e, context);
            throw e;
        }
    }

    @GetMapping("/chapters/{id}")
    public ResponseEntity<ApiResponse<ChapterDto>> getChapterById(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("chapterId", id);
        
        loggingService.logBusinessOperationStart("GET_CHAPTER_BY_ID", null, context);
        
        try {
            ChapterDto chapter = contentManagementService.getChapterById(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_CHAPTER_BY_ID", id.toString(), true, executionTime);
            
            ApiResponse<ChapterDto> response = ApiResponse.success(chapter, "Chapter retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_CHAPTER_BY_ID", id.toString(), false, executionTime);
            loggingService.logError("GET_CHAPTER_BY_ID", e, context);
            throw e;
        }
    }

    @PostMapping("/chapters")
    public ResponseEntity<ApiResponse<ChapterDto>> createChapter(
            @Valid @RequestBody CreateChapterRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("chapterName", request.getName());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        context.put("subjectId", request.getSubjectId());
        
        loggingService.logBusinessOperationStart("CREATE_CHAPTER", null, context);
        
        try {
            ChapterDto chapter = contentManagementService.createChapter(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_CHAPTER", chapter.getId().toString(), true, executionTime);
            
            ApiResponse<ChapterDto> response = ApiResponse.success(chapter, "Chapter created successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_CHAPTER", null, false, executionTime);
            loggingService.logError("CREATE_CHAPTER", e, context);
            throw e;
        }
    }

    @PutMapping("/chapters/{id}")
    public ResponseEntity<ApiResponse<ChapterDto>> updateChapter(
            @PathVariable Long id,
            @Valid @RequestBody UpdateChapterRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("chapterId", id);
        context.put("chapterName", request.getName());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        context.put("subjectId", request.getSubjectId());
        context.put("chapterActive", request.getActive());
        
        loggingService.logBusinessOperationStart("UPDATE_CHAPTER", id.toString(), context);
        
        try {
            ChapterDto chapter = contentManagementService.updateChapter(id, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_CHAPTER", id.toString(), true, executionTime);
            
            ApiResponse<ChapterDto> response = ApiResponse.success(chapter, "Chapter updated successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_CHAPTER", id.toString(), false, executionTime);
            loggingService.logError("UPDATE_CHAPTER", e, context);
            throw e;
        }
    }

    @DeleteMapping("/chapters/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("chapterId", id);
        
        loggingService.logBusinessOperationStart("DELETE_CHAPTER", id.toString(), context);
        
        try {
            contentManagementService.deleteChapter(id, false); // false = soft delete
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_CHAPTER", id.toString(), true, executionTime);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Chapter deleted successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_CHAPTER", id.toString(), false, executionTime);
            loggingService.logError("DELETE_CHAPTER", e, context);
            throw e;
        }
    }

    // ============ TOPICS CRUD ============
    
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<Page<TopicDto>>> getTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("page", page);
        context.put("size", size);
        context.put("search", search);
        context.put("boardId", boardId);
        context.put("gradeId", gradeId);
        context.put("subjectId", subjectId);
        context.put("chapterId", chapterId);
        context.put("active", active);
        context.put("sortBy", sortBy);
        context.put("sortDir", sortDir);
        
        loggingService.logBusinessOperationStart("GET_TOPICS", null, context);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TopicDto> topics = contentManagementService.getTopics(search, boardId, gradeId, subjectId, chapterId, active, pageable);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPICS", null, true, executionTime);
            
            ApiResponse<Page<TopicDto>> response = ApiResponse.success(topics, "Topics retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPICS", null, false, executionTime);
            loggingService.logError("GET_TOPICS", e, context);
            throw e;
        }
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<ApiResponse<TopicDto>> getTopicById(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("topicId", id);
        
        loggingService.logBusinessOperationStart("GET_TOPIC_BY_ID", null, context);
        
        try {
            TopicDto topic = contentManagementService.getTopicById(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_BY_ID", id.toString(), true, executionTime);
            
            ApiResponse<TopicDto> response = ApiResponse.success(topic, "Topic retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_BY_ID", id.toString(), false, executionTime);
            loggingService.logError("GET_TOPIC_BY_ID", e, context);
            throw e;
        }
    }

    @PostMapping("/topics")
    public ResponseEntity<ApiResponse<TopicDto>> createTopic(
            @Valid @RequestBody CreateTopicRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("topicTitle", request.getTitle());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        context.put("subjectId", request.getSubjectId());
        context.put("chapterId", request.getChapterId());
        
        loggingService.logBusinessOperationStart("CREATE_TOPIC", null, context);
        
        try {
            TopicDto topic = contentManagementService.createTopic(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_TOPIC", topic.getId().toString(), true, executionTime);
            
            ApiResponse<TopicDto> response = ApiResponse.success(topic, "Topic created successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_TOPIC", null, false, executionTime);
            loggingService.logError("CREATE_TOPIC", e, context);
            throw e;
        }
    }

    @PutMapping("/topics/{id}")
    public ResponseEntity<ApiResponse<TopicDto>> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTopicRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("topicId", id);
        context.put("topicTitle", request.getTitle());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        context.put("subjectId", request.getSubjectId());
        context.put("chapterId", request.getChapterId());
        context.put("topicActive", request.getActive());
        
        loggingService.logBusinessOperationStart("UPDATE_TOPIC", id.toString(), context);
        
        try {
            TopicDto topic = contentManagementService.updateTopic(id, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_TOPIC", id.toString(), true, executionTime);
            
            ApiResponse<TopicDto> response = ApiResponse.success(topic, "Topic updated successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_TOPIC", id.toString(), false, executionTime);
            loggingService.logError("UPDATE_TOPIC", e, context);
            throw e;
        }
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("topicId", id);
        
        loggingService.logBusinessOperationStart("DELETE_TOPIC", id.toString(), context);
        
        try {
            contentManagementService.deleteTopic(id, false); // false = soft delete
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_TOPIC", id.toString(), true, executionTime);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Topic deleted successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_TOPIC", id.toString(), false, executionTime);
            loggingService.logError("DELETE_TOPIC", e, context);
            throw e;
        }
    }

    // ============ TOPIC NOTES CRUD ============
    
    @GetMapping("/topic-notes")
    public ResponseEntity<ApiResponse<Page<TopicNoteDto>>> getTopicNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("page", page);
        context.put("size", size);
        context.put("search", search);
        context.put("boardId", boardId);
        context.put("gradeId", gradeId);
        context.put("subjectId", subjectId);
        context.put("chapterId", chapterId);
        context.put("topicId", topicId);
        context.put("active", active);
        context.put("sortBy", sortBy);
        context.put("sortDir", sortDir);
        
        loggingService.logBusinessOperationStart("GET_TOPIC_NOTES", null, context);
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TopicNoteDto> notes = contentManagementService.getTopicNotes(search, boardId, gradeId, subjectId, chapterId, topicId, active, pageable);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_NOTES", null, true, executionTime);
            
            ApiResponse<Page<TopicNoteDto>> response = ApiResponse.success(notes, "Topic notes retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_NOTES", null, false, executionTime);
            loggingService.logError("GET_TOPIC_NOTES", e, context);
            throw e;
        }
    }

    @GetMapping("/topic-notes/{id}")
    public ResponseEntity<ApiResponse<TopicNoteDto>> getTopicNoteById(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("noteId", id);
        
        loggingService.logBusinessOperationStart("GET_TOPIC_NOTE_BY_ID", null, context);
        
        try {
            TopicNoteDto note = contentManagementService.getTopicNoteById(id);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_NOTE_BY_ID", id.toString(), true, executionTime);
            
            ApiResponse<TopicNoteDto> response = ApiResponse.success(note, "Topic note retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPIC_NOTE_BY_ID", id.toString(), false, executionTime);
            loggingService.logError("GET_TOPIC_NOTE_BY_ID", e, context);
            throw e;
        }
    }

    @PostMapping("/topic-notes")
    public ResponseEntity<ApiResponse<TopicNoteDto>> createTopicNote(
            @Valid @RequestBody CreateTopicNoteRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("noteTitle", request.getTitle());
        context.put("topicId", request.getTopicId());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        context.put("subjectId", request.getSubjectId());
        context.put("chapterId", request.getChapterId());
        
        loggingService.logBusinessOperationStart("CREATE_TOPIC_NOTE", null, context);
        
        try {
            TopicNoteDto note = contentManagementService.createTopicNote(request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_TOPIC_NOTE", note.getId().toString(), true, executionTime);
            
            ApiResponse<TopicNoteDto> response = ApiResponse.success(note, "Topic note created successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_TOPIC_NOTE", null, false, executionTime);
            loggingService.logError("CREATE_TOPIC_NOTE", e, context);
            throw e;
        }
    }

    @PutMapping("/topic-notes/{id}")
    public ResponseEntity<ApiResponse<TopicNoteDto>> updateTopicNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTopicNoteRequest request,
            HttpServletRequest httpRequest) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("noteId", id);
        context.put("noteTitle", request.getTitle());
        context.put("topicId", request.getTopicId());
        context.put("boardId", request.getBoardId());
        context.put("gradeId", request.getGradeId());
        context.put("subjectId", request.getSubjectId());
        context.put("chapterId", request.getChapterId());
        context.put("noteActive", request.getActive());
        
        loggingService.logBusinessOperationStart("UPDATE_TOPIC_NOTE", id.toString(), context);
        
        try {
            TopicNoteDto note = contentManagementService.updateTopicNote(id, request);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_TOPIC_NOTE", id.toString(), true, executionTime);
            
            ApiResponse<TopicNoteDto> response = ApiResponse.success(note, "Topic note updated successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("UPDATE_TOPIC_NOTE", id.toString(), false, executionTime);
            loggingService.logError("UPDATE_TOPIC_NOTE", e, context);
            throw e;
        }
    }

    @DeleteMapping("/topic-notes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTopicNote(@PathVariable Long id, HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("noteId", id);
        
        loggingService.logBusinessOperationStart("DELETE_TOPIC_NOTE", id.toString(), context);
        
        try {
            contentManagementService.deleteTopicNote(id, false); // false = soft delete
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_TOPIC_NOTE", id.toString(), true, executionTime);
            
            ApiResponse<Void> response = ApiResponse.success(null, "Topic note deleted successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("DELETE_TOPIC_NOTE", id.toString(), false, executionTime);
            loggingService.logError("DELETE_TOPIC_NOTE", e, context);
            throw e;
        }
    }

    // ============ DROPDOWN UTILITIES ============
    
    @GetMapping("/boards/dropdown")
    public ResponseEntity<ApiResponse<List<BoardDropdownDto>>> getBoardsDropdown(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        loggingService.logBusinessOperationStart("GET_BOARDS_DROPDOWN", null, new HashMap<>());
        
        try {
            List<BoardDropdownDto> boards = contentManagementService.getBoardsForDropdown();
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOARDS_DROPDOWN", null, true, executionTime);
            
            ApiResponse<List<BoardDropdownDto>> response = ApiResponse.success(boards, "Boards dropdown retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_BOARDS_DROPDOWN", null, false, executionTime);
            loggingService.logError("GET_BOARDS_DROPDOWN", e, new HashMap<>());
            throw e;
        }
    }

    @GetMapping("/grades/dropdown")
    public ResponseEntity<ApiResponse<List<GradeDropdownDto>>> getGradesDropdown(
            @RequestParam(required = false) Long boardId,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("boardId", boardId);
        
        loggingService.logBusinessOperationStart("GET_GRADES_DROPDOWN", null, context);
        
        try {
            List<GradeDropdownDto> grades = contentManagementService.getGradesForDropdown(boardId);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADES_DROPDOWN", null, true, executionTime);
            
            ApiResponse<List<GradeDropdownDto>> response = ApiResponse.success(grades, "Grades dropdown retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_GRADES_DROPDOWN", null, false, executionTime);
            loggingService.logError("GET_GRADES_DROPDOWN", e, context);
            throw e;
        }
    }

    @GetMapping("/subjects/dropdown")
    public ResponseEntity<ApiResponse<List<SubjectDropdownDto>>> getSubjectsDropdown(
            @RequestParam(required = false) Long gradeId,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("gradeId", gradeId);
        
        loggingService.logBusinessOperationStart("GET_SUBJECTS_DROPDOWN", null, context);
        
        try {
            List<SubjectDropdownDto> subjects = contentManagementService.getSubjectsForDropdown(gradeId);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECTS_DROPDOWN", null, true, executionTime);
            
            ApiResponse<List<SubjectDropdownDto>> response = ApiResponse.success(subjects, "Subjects dropdown retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_SUBJECTS_DROPDOWN", null, false, executionTime);
            loggingService.logError("GET_SUBJECTS_DROPDOWN", e, context);
            throw e;
        }
    }

    @GetMapping("/chapters/dropdown")
    public ResponseEntity<ApiResponse<List<ChapterDropdownDto>>> getChaptersDropdown(
            @RequestParam(required = false) Long subjectId,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("subjectId", subjectId);
        
        loggingService.logBusinessOperationStart("GET_CHAPTERS_DROPDOWN", null, context);
        
        try {
            List<ChapterDropdownDto> chapters = contentManagementService.getChaptersForDropdown(subjectId);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_CHAPTERS_DROPDOWN", null, true, executionTime);
            
            ApiResponse<List<ChapterDropdownDto>> response = ApiResponse.success(chapters, "Chapters dropdown retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_CHAPTERS_DROPDOWN", null, false, executionTime);
            loggingService.logError("GET_CHAPTERS_DROPDOWN", e, context);
            throw e;
        }
    }

    @GetMapping("/topics/dropdown")
    public ResponseEntity<ApiResponse<List<TopicDropdownDto>>> getTopicsDropdown(
            @RequestParam(required = false) Long chapterId,
            HttpServletRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();
        
        Map<String, Object> context = new HashMap<>();
        context.put("chapterId", chapterId);
        
        loggingService.logBusinessOperationStart("GET_TOPICS_DROPDOWN", null, context);
        
        try {
            List<TopicDropdownDto> topics = contentManagementService.getTopicsForDropdown(chapterId);
            
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPICS_DROPDOWN", null, true, executionTime);
            
            ApiResponse<List<TopicDropdownDto>> response = ApiResponse.success(topics, "Topics dropdown retrieved successfully");
            response.setTraceId(traceId);
            response.setRequestId(requestId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TOPICS_DROPDOWN", null, false, executionTime);
            loggingService.logError("GET_TOPICS_DROPDOWN", e, context);
            throw e;
        }
    }
}
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
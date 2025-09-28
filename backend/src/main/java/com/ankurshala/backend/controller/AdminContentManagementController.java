package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.content.*;
import com.ankurshala.backend.dto.admin.GradeDto;
import com.ankurshala.backend.dto.admin.CreateGradeRequest;
import com.ankurshala.backend.dto.admin.UpdateGradeRequest;
import com.ankurshala.backend.service.ContentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/admin/content")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminContentManagementController {

    private final ContentManagementService contentManagementService;

    // ============ BOARDS CRUD ============
    
    @GetMapping("/boards")
    public ResponseEntity<Page<BoardDto>> getBoards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BoardDto> boards = contentManagementService.getBoards(pageable, search, active);
        
        return ResponseEntity.ok(boards);
    }

    @PostMapping("/boards")
    public ResponseEntity<BoardDto> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        BoardDto board = contentManagementService.createBoard(request);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/boards/{id}")
    public ResponseEntity<BoardDto> updateBoard(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateBoardRequest request) {
        BoardDto board = contentManagementService.updateBoard(id, request);
        return ResponseEntity.ok(board);
    }

    @PatchMapping("/boards/{id}/active")
    public ResponseEntity<BoardDto> updateBoardStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> statusUpdate) {
        Boolean active = statusUpdate.get("active");
        BoardDto board = contentManagementService.updateBoardStatus(id, active);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/boards/{id}/deletion-impact")
    public ResponseEntity<Map<String, Object>> getBoardDeletionImpact(@PathVariable Long id) {
        Map<String, Object> impact = contentManagementService.getBoardDeletionImpact(id);
        return ResponseEntity.ok(impact);
    }

    @DeleteMapping("/boards/{id}")
    public ResponseEntity<Map<String, Object>> deleteBoard(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        Map<String, Object> result = contentManagementService.deleteBoard(id, force);
        return ResponseEntity.ok(result);
    }

    // ============ SUBJECTS CRUD ============
    
    @GetMapping("/subjects")
    public ResponseEntity<Page<SubjectDto>> getSubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SubjectDto> subjects = contentManagementService.getSubjects(pageable, search, active, boardId, gradeId);
        
        return ResponseEntity.ok(subjects);
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectDto> createSubject(@RequestBody CreateSubjectRequest request) {
        log.info("=== CONTROLLER DEBUG ===");
        log.info("Received createSubject request: {}", request);
        log.info("Request gradeId: {}", request.getGradeId());
        log.info("Request boardId: {}", request.getBoardId());
        log.info("Request name: {}", request.getName());
        log.info("Request active: {}", request.getActive());
        log.info("=== END CONTROLLER DEBUG ===");
        
        SubjectDto subject = contentManagementService.createSubject(request);
        return ResponseEntity.ok(subject);
    }

    @PostMapping("/subjects/debug")
    public ResponseEntity<Map<String, Object>> debugSubjectCreation(@RequestBody Map<String, Object> rawRequest) {
        log.info("=== RAW REQUEST DEBUG ===");
        log.info("Raw request: {}", rawRequest);
        log.info("Raw request type: {}", rawRequest.getClass());
        log.info("Raw request keys: {}", rawRequest.keySet());
        log.info("Raw request values: {}", rawRequest.values());
        log.info("=== END RAW REQUEST DEBUG ===");
        
        Map<String, Object> response = new HashMap<>();
        response.put("received", rawRequest);
        response.put("message", "Debug endpoint working");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<SubjectDto> updateSubject(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateSubjectRequest request) {
        SubjectDto subject = contentManagementService.updateSubject(id, request);
        return ResponseEntity.ok(subject);
    }

    @PatchMapping("/subjects/{id}/active")
    public ResponseEntity<SubjectDto> updateSubjectStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> statusUpdate) {
        Boolean active = statusUpdate.get("active");
        SubjectDto subject = contentManagementService.updateSubjectStatus(id, active);
        return ResponseEntity.ok(subject);
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Map<String, Object>> deleteSubject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        Map<String, Object> result = contentManagementService.deleteSubject(id, force);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/subjects/{id}/deletion-impact")
    public ResponseEntity<Map<String, Object>> getSubjectDeletionImpact(@PathVariable Long id) {
        Map<String, Object> impact = contentManagementService.getSubjectDeletionImpact(id);
        return ResponseEntity.ok(impact);
    }

    @GetMapping("/chapters/{id}/deletion-impact")
    public ResponseEntity<Map<String, Object>> getChapterDeletionImpact(@PathVariable Long id) {
        Map<String, Object> impact = contentManagementService.getChapterDeletionImpact(id);
        return ResponseEntity.ok(impact);
    }

    // ============ CHAPTERS CRUD ============
    
    @GetMapping("/chapters")
    public ResponseEntity<Page<ChapterDto>> getChapters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ChapterDto> chapters = contentManagementService.getChapters(pageable, search, active, subjectId);
        
        return ResponseEntity.ok(chapters);
    }

    @PostMapping("/chapters")
    public ResponseEntity<ChapterDto> createChapter(@Valid @RequestBody CreateChapterRequest request) {
        ChapterDto chapter = contentManagementService.createChapter(request);
        return ResponseEntity.ok(chapter);
    }

    @PutMapping("/chapters/{id}")
    public ResponseEntity<ChapterDto> updateChapter(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateChapterRequest request) {
        ChapterDto chapter = contentManagementService.updateChapter(id, request);
        return ResponseEntity.ok(chapter);
    }

    @PatchMapping("/chapters/{id}/active")
    public ResponseEntity<ChapterDto> updateChapterStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> statusUpdate) {
        Boolean active = statusUpdate.get("active");
        ChapterDto chapter = contentManagementService.updateChapterStatus(id, active);
        return ResponseEntity.ok(chapter);
    }

    @DeleteMapping("/chapters/{id}")
    public ResponseEntity<Map<String, Object>> deleteChapter(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        Map<String, Object> result = contentManagementService.deleteChapter(id, force);
        return ResponseEntity.ok(result);
    }

    // ============ TOPICS CRUD ============
    
    @GetMapping("/topics")
    public ResponseEntity<Page<TopicDto>> getTopics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TopicDto> topics = contentManagementService.getTopics(pageable, search, active, chapterId, subjectId);
        
        return ResponseEntity.ok(topics);
    }

    @PostMapping("/topics")
    public ResponseEntity<TopicDto> createTopic(@Valid @RequestBody CreateTopicRequest request) {
        TopicDto topic = contentManagementService.createTopic(request);
        return ResponseEntity.ok(topic);
    }

    @PutMapping("/topics/{id}")
    public ResponseEntity<TopicDto> updateTopic(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateTopicRequest request) {
        TopicDto topic = contentManagementService.updateTopic(id, request);
        return ResponseEntity.ok(topic);
    }

    @PatchMapping("/topics/{id}/active")
    public ResponseEntity<TopicDto> updateTopicStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> statusUpdate) {
        Boolean active = statusUpdate.get("active");
        TopicDto topic = contentManagementService.updateTopicStatus(id, active);
        return ResponseEntity.ok(topic);
    }

    @GetMapping("/topics/{id}/deletion-impact")
    public ResponseEntity<Map<String, Object>> getTopicDeletionImpact(@PathVariable Long id) {
        Map<String, Object> impact = contentManagementService.getTopicDeletionImpact(id);
        return ResponseEntity.ok(impact);
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Map<String, Object>> deleteTopic(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        Map<String, Object> result = contentManagementService.deleteTopic(id, force);
        return ResponseEntity.ok(result);
    }

    // ============ TOPIC NOTES CRUD ============
    
    @GetMapping("/topics/{topicId}/notes")
    public ResponseEntity<Page<TopicNoteDto>> getTopicNotes(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TopicNoteDto> notes = contentManagementService.getTopicNotes(topicId, pageable, search, active);
        
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/notes")
    public ResponseEntity<Page<TopicNoteDto>> getAllTopicNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long topicId,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TopicNoteDto> notes = contentManagementService.getAllTopicNotes(pageable, search, active, topicId);
        
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/topics/{topicId}/notes")
    public ResponseEntity<TopicNoteDto> createTopicNote(
            @PathVariable Long topicId,
            @Valid @RequestBody CreateTopicNoteRequest request) {
        TopicNoteDto note = contentManagementService.createTopicNote(topicId, request);
        return ResponseEntity.ok(note);
    }

    @PostMapping("/notes")
    public ResponseEntity<TopicNoteDto> createTopicNoteGeneral(@Valid @RequestBody CreateTopicNoteRequest request) {
        TopicNoteDto note = contentManagementService.createTopicNoteGeneral(request);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/notes/{id}")
    public ResponseEntity<TopicNoteDto> updateTopicNote(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateTopicNoteRequest request) {
        TopicNoteDto note = contentManagementService.updateTopicNote(id, request);
        return ResponseEntity.ok(note);
    }

    @PatchMapping("/notes/{id}/active")
    public ResponseEntity<TopicNoteDto> updateTopicNoteStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> statusUpdate) {
        Boolean active = statusUpdate.get("active");
        TopicNoteDto note = contentManagementService.updateTopicNoteStatus(id, active);
        return ResponseEntity.ok(note);
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Map<String, Object>> deleteTopicNote(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        Map<String, Object> result = contentManagementService.deleteTopicNote(id, force);
        return ResponseEntity.ok(result);
    }

    // ============ GRADES MANAGEMENT ============
    
    @GetMapping("/grades")
    public ResponseEntity<Page<GradeDto>> getGrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long boardId) {
        Page<GradeDto> grades = contentManagementService.getGrades(page, size, sortBy, sortDir, search, active, boardId);
        return ResponseEntity.ok(grades);
    }

    @PostMapping("/grades")
    public ResponseEntity<GradeDto> createGrade(@Valid @RequestBody CreateGradeRequest request) {
        GradeDto grade = contentManagementService.createGrade(request);
        return ResponseEntity.ok(grade);
    }

    @PutMapping("/grades/{id}")
    public ResponseEntity<GradeDto> updateGrade(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateGradeRequest request) {
        GradeDto grade = contentManagementService.updateGrade(id, request);
        return ResponseEntity.ok(grade);
    }

    @PatchMapping("/grades/{id}/active")
    public ResponseEntity<GradeDto> updateGradeStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> statusUpdate) {
        Boolean active = statusUpdate.get("active");
        GradeDto grade = contentManagementService.updateGradeStatus(id, active);
        return ResponseEntity.ok(grade);
    }

    @DeleteMapping("/grades/{id}")
    public ResponseEntity<Map<String, Object>> deleteGrade(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        Map<String, Object> result = contentManagementService.deleteGrade(id, force);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/grades/{id}/deletion-impact")
    public ResponseEntity<Map<String, Object>> getGradeDeletionImpact(@PathVariable Long id) {
        Map<String, Object> impact = contentManagementService.getGradeDeletionImpact(id);
        return ResponseEntity.ok(impact);
    }
    
    @GetMapping("/grades-list")
    public ResponseEntity<List<Map<String, Object>>> getGradesList() {
        List<Map<String, Object>> grades = List.of(
            Map.of("id", 1, "name", "7", "displayName", "Grade 7", "active", true),
            Map.of("id", 2, "name", "8", "displayName", "Grade 8", "active", true),
            Map.of("id", 3, "name", "9", "displayName", "Grade 9", "active", true),
            Map.of("id", 4, "name", "10", "displayName", "Grade 10", "active", true),
            Map.of("id", 5, "name", "11", "displayName", "Grade 11", "active", true),
            Map.of("id", 6, "name", "12", "displayName", "Grade 12", "active", true)
        );
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/content-tree")
    public ResponseEntity<Map<String, Object>> getContentTree() {
        Map<String, Object> tree = new HashMap<>();
        tree.put("test", "working");
        return ResponseEntity.ok(tree);
    }
}
package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.content.*;
import com.ankurshala.backend.service.AdminContentManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public Content Discovery Controller
 * Provides read-only access to educational content for students and public users
 * Enables cascading content discovery: Board → Grade → Subject → Chapter → Topic
 */
@Slf4j
@RestController
@RequestMapping("/content")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
public class PublicContentController {

    private final AdminContentManagementService contentManagementService;

    /**
     * Get all available boards
     * No authentication required - public endpoint
     */
    @GetMapping("/boards")
    public ResponseEntity<List<BoardDropdownDto>> getAllBoards() {
        log.info("Public API: Getting all boards");
        
        List<BoardDropdownDto> boards = contentManagementService.getBoardsForDropdown();
        return ResponseEntity.ok(boards);
    }

    /**
     * Get all grades for a specific board
     * @param boardId - The board ID
     */
    @GetMapping("/grades/by-board/{boardId}")
    public ResponseEntity<List<GradeDropdownDto>> getGradesByBoard(@PathVariable Long boardId) {
        log.info("Public API: Getting grades for board ID: {}", boardId);
        
        List<GradeDropdownDto> grades = contentManagementService.getGradesForDropdown(boardId);
        return ResponseEntity.ok(grades);
    }

    /**
     * Get all subjects for a specific grade
     * @param gradeId - The grade ID
     */
    @GetMapping("/subjects/by-grade/{gradeId}")
    public ResponseEntity<List<SubjectDropdownDto>> getSubjectsByGrade(@PathVariable Long gradeId) {
        log.info("Public API: Getting subjects for grade ID: {}", gradeId);
        
        List<SubjectDropdownDto> subjects = contentManagementService.getSubjectsForDropdown(gradeId);
        return ResponseEntity.ok(subjects);
    }

    /**
     * Get all chapters for a specific subject
     * @param subjectId - The subject ID
     */
    @GetMapping("/chapters/by-subject/{subjectId}")
    public ResponseEntity<List<ChapterDropdownDto>> getChaptersBySubject(@PathVariable Long subjectId) {
        log.info("Public API: Getting chapters for subject ID: {}", subjectId);
        
        List<ChapterDropdownDto> chapters = contentManagementService.getChaptersForDropdown(subjectId);
        return ResponseEntity.ok(chapters);
    }

    /**
     * Get all topics for a specific chapter
     * @param chapterId - The chapter ID
     */
    @GetMapping("/topics/by-chapter/{chapterId}")
    public ResponseEntity<List<TopicDropdownDto>> getTopicsByChapter(@PathVariable Long chapterId) {
        log.info("Public API: Getting topics for chapter ID: {}", chapterId);
        
        List<TopicDropdownDto> topics = contentManagementService.getTopicsForDropdown(chapterId);
        return ResponseEntity.ok(topics);
    }

    /**
     * Get a specific topic by ID
     * @param topicId - The topic ID
     */
    @GetMapping("/topics/{topicId}")
    public ResponseEntity<TopicDto> getTopicById(@PathVariable Long topicId) {
        log.info("Public API: Getting topic with ID: {}", topicId);
        
        TopicDto topic = contentManagementService.getTopicById(topicId);
        return ResponseEntity.ok(topic);
    }

    /**
     * Get a specific board by ID
     * @param boardId - The board ID
     */
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<BoardDto> getBoardById(@PathVariable Long boardId) {
        log.info("Public API: Getting board with ID: {}", boardId);
        
        BoardDto board = contentManagementService.getBoardById(boardId);
        return ResponseEntity.ok(board);
    }

    /**
     * Get a specific grade by ID
     * @param gradeId - The grade ID
     */
    @GetMapping("/grades/{gradeId}")
    public ResponseEntity<GradeDto> getGradeById(@PathVariable Long gradeId) {
        log.info("Public API: Getting grade with ID: {}", gradeId);
        
        GradeDto grade = contentManagementService.getGradeById(gradeId);
        return ResponseEntity.ok(grade);
    }

    /**
     * Get a specific subject by ID
     * @param subjectId - The subject ID
     */
    @GetMapping("/subjects/{subjectId}")
    public ResponseEntity<SubjectDto> getSubjectById(@PathVariable Long subjectId) {
        log.info("Public API: Getting subject with ID: {}", subjectId);
        
        SubjectDto subject = contentManagementService.getSubjectById(subjectId);
        return ResponseEntity.ok(subject);
    }

    /**
     * Get a specific chapter by ID
     * @param chapterId - The chapter ID
     */
    @GetMapping("/chapters/{chapterId}")
    public ResponseEntity<ChapterDto> getChapterById(@PathVariable Long chapterId) {
        log.info("Public API: Getting chapter with ID: {}", chapterId);
        
        ChapterDto chapter = contentManagementService.getChapterById(chapterId);
        return ResponseEntity.ok(chapter);
    }

    /**
     * Get subjects by board name and grade name
     * Used by students to get subjects for their registered board and grade
     * @param board - The board name (e.g., "CBSE", "ICSE")
     * @param grade - The grade name (e.g., "GRADE_7", "GRADE_8")
     */
    @GetMapping("/subjects/by-board-grade")
    public ResponseEntity<List<SubjectDto>> getSubjectsByBoardAndGrade(
            @RequestParam String board,
            @RequestParam String grade) {
        log.info("Public API: Getting subjects for board: {} and grade: {}", board, grade);
        
        List<SubjectDto> subjects = contentManagementService.getSubjectsByBoardAndGrade(board, grade);
        return ResponseEntity.ok(subjects);
    }

    /**
     * Get board by name
     * Used to resolve board ID from board name stored in student profile
     * @param boardName - The board name (e.g., "CBSE", "ICSE")
     */
    @GetMapping("/boards/by-name")
    public ResponseEntity<BoardDropdownDto> getBoardByName(@RequestParam String boardName) {
        log.info("Public API: Getting board by name: {}", boardName);
        
        List<BoardDropdownDto> boards = contentManagementService.getBoardsForDropdown();
        BoardDropdownDto board = boards.stream()
                .filter(b -> b.getName().equalsIgnoreCase(boardName))
                .findFirst()
                .orElse(null);
        
        if (board == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(board);
    }

    /**
     * Get grade by name within a specific board
     * Used to resolve grade ID from grade name stored in student profile
     * @param boardId - The board ID
     * @param gradeName - The grade name (e.g., "GRADE_7", "Class 8")
     */
    @GetMapping("/grades/by-name")
    public ResponseEntity<GradeDropdownDto> getGradeByName(
            @RequestParam Long boardId,
            @RequestParam String gradeName) {
        log.info("Public API: Getting grade by name: {} for board: {}", gradeName, boardId);
        
        List<GradeDropdownDto> grades = contentManagementService.getGradesForDropdown(boardId);
        
        // Handle multiple grade name formats (e.g., "GRADE_7", "7", "Class 7")
        String normalizedSearch = gradeName.toLowerCase().replaceAll("[^0-9]", "");
        GradeDropdownDto grade = grades.stream()
                .filter(g -> {
                    String normalizedGradeName = g.getName().toLowerCase().replaceAll("[^0-9]", "");
                    return normalizedGradeName.equals(normalizedSearch) ||
                           g.getName().equalsIgnoreCase(gradeName) ||
                           g.getDisplayName().equalsIgnoreCase(gradeName);
                })
                .findFirst()
                .orElse(null);
        
        if (grade == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(grade);
    }
}

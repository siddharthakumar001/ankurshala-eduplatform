package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.AddToStudyListRequest;
import com.ankurshala.backend.dto.student.StudyListItemDto;
import com.ankurshala.backend.dto.student.UpdateStudyListItemRequest;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudyListService;
import com.ankurshala.backend.service.ResourceAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing student study lists
 */
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RestController
@RequestMapping("/student/study-list")
@PreAuthorize("hasRole('STUDENT')")
public class StudentStudyListController {

    @Autowired
    private StudyListService studyListService;

    @Autowired
    private ResourceAuthorizationService resourceAuthorizationService;

    /**
     * Get all items in the student's study list
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudyListItemDto>> getStudyList(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify ownership
        if (!resourceAuthorizationService.canAccessStudentProfile(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<StudyListItemDto> items = studyListService.getStudyList(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Get study list items filtered by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudyListItemDto>> getStudyListByStatus(
            @PathVariable String status,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify ownership
        if (!resourceAuthorizationService.canAccessStudentProfile(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate status
        if (!isValidStatus(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        List<StudyListItemDto> items = studyListService.getStudyListByStatus(userId, status);
        return ResponseEntity.ok(items);
    }

    /**
     * Add a topic to the study list
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudyListItemDto> addTopic(
            @Valid @RequestBody AddToStudyListRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify ownership
        if (!resourceAuthorizationService.canAccessStudentProfile(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            StudyListItemDto item = studyListService.addTopic(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already in study list")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }

    /**
     * Update a study list item (status or notes)
     */
    @PatchMapping("/{itemId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudyListItemDto> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateStudyListItemRequest request,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify ownership
        if (!resourceAuthorizationService.canAccessStudentProfile(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            StudyListItemDto item = studyListService.updateItem(userId, itemId, request);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    /**
     * Mark a study list item as done
     */
    @PostMapping("/{itemId}/mark-done")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudyListItemDto> markAsDone(
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify ownership
        if (!resourceAuthorizationService.canAccessStudentProfile(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            StudyListItemDto item = studyListService.markAsDone(userId, itemId);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    /**
     * Remove a topic from the study list
     */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify ownership
        if (!resourceAuthorizationService.canAccessStudentProfile(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            studyListService.removeItem(userId, itemId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Unauthorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            throw e;
        }
    }

    /**
     * Get count of items by status
     */
    @GetMapping("/count/{status}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Long> getCountByStatus(
            @PathVariable String status,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Verify ownership
        if (!resourceAuthorizationService.canAccessStudentProfile(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Validate status
        if (!isValidStatus(status)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        long count = studyListService.getCountByStatus(userId, status);
        return ResponseEntity.ok(count);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
    
    private boolean isValidStatus(String status) {
        return status.equals("ADDED") || status.equals("IN_PROGRESS") || status.equals("DONE");
    }
}

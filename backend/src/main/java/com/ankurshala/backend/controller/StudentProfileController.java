package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.StudentDocumentDto;
import com.ankurshala.backend.dto.student.StudentProfileDto;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// NOTE: server.servlet.context-path=/api is set for the app.
// Therefore controller @RequestMapping must NOT start with "/api".
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RestController
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

    @Autowired
    private StudentProfileService studentProfileService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileDto> getProfile(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        StudentProfileDto profile = studentProfileService.getStudentProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileDto> updateProfile(
            @Valid @RequestBody StudentProfileDto profileDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        StudentProfileDto updatedProfile = studentProfileService.updateStudentProfile(userId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/profile/documents")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudentDocumentDto>> getDocuments(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<StudentDocumentDto> documents = studentProfileService.getStudentDocuments(userId);
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/profile/documents")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentDocumentDto> addDocument(
            @Valid @RequestBody StudentDocumentDto documentDto,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        StudentDocumentDto addedDocument = studentProfileService.addStudentDocument(userId, documentDto);
        return ResponseEntity.ok(addedDocument);
    }

    @DeleteMapping("/profile/documents/{documentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        studentProfileService.deleteStudentDocument(userId, documentId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}

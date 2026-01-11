package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.student.NotesDTO;
import com.ankurshala.backend.entity.StudentNote.NoteFormat;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentNotesService;
import com.ankurshala.backend.util.TraceUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Student Notes Controller - AI-generated personal notes notebook.
 * All endpoints are secured with STUDENT role and ownership validation.
 */
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002",
        "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/student/notes")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student Notes", description = "AI-generated personal notes APIs")
public class StudentNotesController {

    @Autowired
    private StudentNotesService notesService;

    // ===================== Note Generation =====================

    @PostMapping("/generate")
    @Operation(summary = "Generate AI notes for a topic",
            description = "Generates notes using RAG-grounded AI. Supports SHORT, LONG, and REVISION_SHEET formats.")
    public ResponseEntity<ApiResponse<NotesDTO.GenerateNotesResponse>> generateNotes(
            @Valid @RequestBody NotesDTO.GenerateNotesRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Generate notes request - TraceId: {}, StudentId: {}, TopicId: {}, Format: {}",
                traceId, studentId, request.getTopicId(), request.getFormat());

        NotesDTO.GenerateNotesResponse response = notesService.generateNotes(studentId, request);

        ApiResponse<NotesDTO.GenerateNotesResponse> apiResponse = 
                ApiResponse.success(response, "Notes generated successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/{noteId}/regenerate")
    @Operation(summary = "Regenerate notes with updated content",
            description = "Creates a new version and regenerates the notes content")
    public ResponseEntity<ApiResponse<NotesDTO.GenerateNotesResponse>> regenerateNotes(
            @PathVariable Long noteId,
            @RequestBody(required = false) NotesDTO.RegenerateNotesRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Regenerate notes request - TraceId: {}, StudentId: {}, NoteId: {}",
                traceId, studentId, noteId);

        if (request == null) {
            request = new NotesDTO.RegenerateNotesRequest();
        }
        request.setNoteId(noteId);

        NotesDTO.GenerateNotesResponse response = notesService.regenerateNotes(studentId, request);

        ApiResponse<NotesDTO.GenerateNotesResponse> apiResponse =
                ApiResponse.success(response, "Notes regenerated successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    // ===================== Note Retrieval =====================

    @GetMapping
    @Operation(summary = "Get all notes with optional filters",
            description = "Returns paginated list of notes. Filter by topic, subject, format, or search by title.")
    public ResponseEntity<ApiResponse<NotesDTO.NotesListResponse>> getNotes(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) NoteFormat format,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean isFavorite,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        NotesDTO.NotesFilterRequest filter = NotesDTO.NotesFilterRequest.builder()
                .topicId(topicId)
                .subjectId(subjectId)
                .format(format)
                .language(language)
                .isFavorite(isFavorite)
                .searchTerm(search)
                .page(page)
                .size(size)
                .build();

        NotesDTO.NotesListResponse response = notesService.getNotes(studentId, filter);

        ApiResponse<NotesDTO.NotesListResponse> apiResponse =
                ApiResponse.success(response, "Notes retrieved successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "Get a specific note by ID")
    public ResponseEntity<ApiResponse<NotesDTO.NoteResponse>> getNote(
            @PathVariable Long noteId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        NotesDTO.NoteResponse response = notesService.getNote(studentId, noteId);

        ApiResponse<NotesDTO.NoteResponse> apiResponse =
                ApiResponse.success(response, "Note retrieved successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{noteId}/versions")
    @Operation(summary = "Get version history for a note")
    public ResponseEntity<ApiResponse<NotesDTO.NoteVersionsResponse>> getNoteVersions(
            @PathVariable Long noteId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        NotesDTO.NoteVersionsResponse response = notesService.getNoteVersions(studentId, noteId);

        ApiResponse<NotesDTO.NoteVersionsResponse> apiResponse =
                ApiResponse.success(response, "Note versions retrieved successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notes statistics for the student")
    public ResponseEntity<ApiResponse<NotesDTO.NotesStatsResponse>> getNotesStats(
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        NotesDTO.NotesStatsResponse response = notesService.getNotesStats(studentId);

        ApiResponse<NotesDTO.NotesStatsResponse> apiResponse =
                ApiResponse.success(response, "Notes stats retrieved successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    // ===================== Note Management =====================

    @PutMapping("/{noteId}")
    @Operation(summary = "Update note metadata",
            description = "Update title and/or favorite status. Content is AI-generated only.")
    public ResponseEntity<ApiResponse<NotesDTO.NoteResponse>> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody NotesDTO.UpdateNoteRequest request,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Update note request - TraceId: {}, StudentId: {}, NoteId: {}",
                traceId, studentId, noteId);

        NotesDTO.NoteResponse response = notesService.updateNote(studentId, noteId, request);

        ApiResponse<NotesDTO.NoteResponse> apiResponse =
                ApiResponse.success(response, "Note updated successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Archive a note (soft delete)")
    public ResponseEntity<ApiResponse<Void>> archiveNote(
            @PathVariable Long noteId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Archive note request - TraceId: {}, StudentId: {}, NoteId: {}",
                traceId, studentId, noteId);

        notesService.archiveNote(studentId, noteId);

        ApiResponse<Void> apiResponse = ApiResponse.success(null, "Note archived successfully");
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }

    // ===================== Export =====================

    @GetMapping("/{noteId}/export")
    @Operation(summary = "Export note as markdown file")
    public ResponseEntity<byte[]> exportNote(
            @PathVariable Long noteId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        log.info("Export note request - TraceId: {}, StudentId: {}, NoteId: {}",
                traceId, studentId, noteId);

        NotesDTO.ExportNoteResponse export = notesService.exportNote(studentId, noteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(export.getContentType()));
        headers.setContentDispositionFormData("attachment", export.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(export.getContent().getBytes());
    }

    @PostMapping("/{noteId}/favorite")
    @Operation(summary = "Toggle favorite status for a note")
    public ResponseEntity<ApiResponse<NotesDTO.NoteResponse>> toggleFavorite(
            @PathVariable Long noteId,
            Authentication authentication) {

        String traceId = TraceUtil.getTraceId();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long studentId = userPrincipal.getId();

        // Get current note to toggle
        NotesDTO.NoteResponse currentNote = notesService.getNote(studentId, noteId);
        NotesDTO.UpdateNoteRequest request = NotesDTO.UpdateNoteRequest.builder()
                .isFavorite(!currentNote.getIsFavorite())
                .build();

        NotesDTO.NoteResponse response = notesService.updateNote(studentId, noteId, request);

        String message = response.getIsFavorite() ? "Note added to favorites" : "Note removed from favorites";
        ApiResponse<NotesDTO.NoteResponse> apiResponse = ApiResponse.success(response, message);
        apiResponse.setTraceId(traceId);

        return ResponseEntity.ok(apiResponse);
    }
}


package com.ankurshala.backend.dto.student;

import com.ankurshala.backend.entity.StudentNote.NoteFormat;
import com.ankurshala.backend.entity.StudentNote.NoteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for Student Notes feature.
 */
public class NotesDTO {

    // ===================== Request DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateNotesRequest {
        @NotNull(message = "Topic ID is required")
        private Long topicId;

        @NotNull(message = "Format is required")
        private NoteFormat format;

        @Builder.Default
        private String language = "en";  // en, hi

        @Size(max = 500, message = "Custom title must not exceed 500 characters")
        private String customTitle;  // Optional override for auto-generated title
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateNoteRequest {
        @Size(max = 500, message = "Title must not exceed 500 characters")
        private String title;

        private Boolean isFavorite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotesFilterRequest {
        private Long topicId;
        private Long subjectId;
        private NoteFormat format;
        private String language;
        private Boolean isFavorite;
        private String searchTerm;

        @Builder.Default
        private Integer page = 0;
        
        @Builder.Default
        private Integer size = 20;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegenerateNotesRequest {
        @NotNull(message = "Note ID is required")
        private Long noteId;

        private String language;  // Optional: change language during regeneration
    }

    // ===================== Response DTOs =====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteResponse {
        private Long id;
        private Long studentId;
        private Long topicId;
        private String topicTitle;
        private Long subjectId;
        private String subjectName;
        private String board;
        private String title;
        private NoteFormat format;
        private String language;
        private String contentMd;
        private String generatedBy;
        private Integer chunksUsed;
        private NoteStatus status;
        private Boolean isFavorite;
        private Integer versionCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteListItem {
        private Long id;
        private Long topicId;
        private String topicTitle;
        private Long subjectId;
        private String subjectName;
        private String title;
        private NoteFormat format;
        private String language;
        private Boolean isFavorite;
        private Integer versionCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Preview snippet (first ~200 chars)
        private String preview;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotesListResponse {
        private List<NoteListItem> notes;
        private long totalCount;
        private int page;
        private int size;
        private int totalPages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateNotesResponse {
        private NoteResponse note;
        private String message;
        private Integer tokensUsed;
        private Integer latencyMs;
        private Boolean wasGrounded;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteVersionResponse {
        private Long id;
        private Long noteId;
        private Integer version;
        private String contentMd;
        private String generatedBy;
        private Integer chunksUsed;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteVersionsResponse {
        private Long noteId;
        private String noteTitle;
        private List<NoteVersionResponse> versions;
        private Integer currentVersion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotesStatsResponse {
        private long totalNotes;
        private long shortNotes;
        private long longNotes;
        private long revisionSheets;
        private long favoriteNotes;
        private List<SubjectNotesCount> notesBySubject;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectNotesCount {
        private Long subjectId;
        private String subjectName;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportNoteRequest {
        @NotNull(message = "Note ID is required")
        private Long noteId;

        @Builder.Default
        private String exportFormat = "markdown";  // markdown, pdf (future)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportNoteResponse {
        private String filename;
        private String contentType;
        private String content;  // For markdown, this is the raw content
        private byte[] binaryContent;  // For PDF (future)
    }
}


package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.dto.student.NotesDTO;
import com.ankurshala.backend.entity.StudentNote;
import com.ankurshala.backend.entity.StudentNote.NoteFormat;
import com.ankurshala.backend.entity.StudentNote.NoteStatus;
import com.ankurshala.backend.entity.StudentNoteVersion;
import com.ankurshala.backend.entity.Subject;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.StudentNoteRepository;
import com.ankurshala.backend.repository.SubjectRepository;
import com.ankurshala.backend.repository.TopicRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating and managing AI-generated notes for students.
 * Uses RAG to ground notes in curated curriculum content.
 */
@Service
@Slf4j
@Transactional
public class StudentNotesService {

    @Autowired
    private StudentNoteRepository noteRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ContentChunkService contentChunkService;

    @Autowired(required = false)
    private ChatModel chatModel;

    @Autowired
    private AIConfig.AIProperties aiProperties;

    @Autowired
    private SafetyModerationService safetyService;

    private static final String BOARD_CBSE = "CBSE";

    // ===================== Note Generation =====================

    /**
     * Generate notes for a topic using AI with RAG grounding
     */
    public NotesDTO.GenerateNotesResponse generateNotes(Long studentId, NotesDTO.GenerateNotesRequest request) {
        String traceId = TraceUtil.getTraceId();
        long startTime = System.currentTimeMillis();

        log.info("Generating notes - TraceId: {}, StudentId: {}, TopicId: {}, Format: {}", 
                traceId, studentId, request.getTopicId(), request.getFormat());

        // Validate topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.getTopicId()));

        // Get subject info
        Long subjectId = topic.getSubjectId();
        String subjectName = subjectRepository.findById(subjectId)
                .map(Subject::getName)
                .orElse("Unknown Subject");

        // Check if AI is available
        if (!aiProperties.isAvailable() || chatModel == null) {
            log.warn("AI service not available for notes generation - TraceId: {}", traceId);
            throw new BusinessException("AI service is currently unavailable. Please try again later.");
        }

        // Retrieve relevant content chunks using RAG
        ContentChunkService.RetrievalResult retrievalResult = contentChunkService.retrieveForRAG(
                "Generate comprehensive study notes for: " + topic.getTitle(),
                request.getTopicId(),
                subjectId,
                request.getLanguage(),
                10  // More chunks for notes
        );

        // Build the prompt based on format
        String systemPrompt = buildNotesSystemPrompt(request.getFormat(), request.getLanguage());
        String userPrompt = buildNotesUserPrompt(topic, subjectName, request.getFormat(), 
                retrievalResult, request.getLanguage());

        // Generate notes using AI
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userPrompt));

        try {
            ChatResponse aiResponse = chatModel.call(new Prompt(messages));
            String notesContent = aiResponse.getResult().getOutput().getContent();

            // Safety check on generated content
            var safetyCheck = safetyService.checkAIResponse(notesContent);
            if (!safetyCheck.getIsSafe()) {
                log.warn("Generated notes flagged by safety - TraceId: {}", traceId);
                notesContent = safetyCheck.getSuggestedResponse();
            }

            // Create the note entity
            String title = request.getCustomTitle() != null ? 
                    request.getCustomTitle() : 
                    generateNoteTitle(topic.getTitle(), request.getFormat());

            StudentNote note = new StudentNote(studentId, request.getTopicId(), subjectId, title, request.getFormat());
            note.setBoard(BOARD_CBSE);
            note.setLanguage(request.getLanguage());
            note.setContentMd(notesContent);
            note.setGeneratedBy(aiProperties.getModel());
            note.setChunksUsed(retrievalResult.getRetrievalCount());

            // Save the note
            StudentNote savedNote = noteRepository.save(note);

            // Create initial version
            savedNote.addVersionSnapshot();
            noteRepository.save(savedNote);

            long latency = System.currentTimeMillis() - startTime;
            log.info("Notes generated successfully - TraceId: {}, NoteId: {}, Latency: {}ms", 
                    traceId, savedNote.getId(), latency);

            return NotesDTO.GenerateNotesResponse.builder()
                    .note(mapToNoteResponse(savedNote, topic.getTitle(), subjectName))
                    .message("Notes generated successfully")
                    .tokensUsed(aiResponse.getMetadata() != null ? 
                            (Integer) aiResponse.getMetadata().getOrDefault("total_tokens", 0) : 0)
                    .latencyMs((int) latency)
                    .wasGrounded(retrievalResult.getRetrievalCount() > 0)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate notes - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to generate notes. Please try again.");
        }
    }

    /**
     * Regenerate notes for an existing note
     */
    public NotesDTO.GenerateNotesResponse regenerateNotes(Long studentId, NotesDTO.RegenerateNotesRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Regenerating notes - TraceId: {}, StudentId: {}, NoteId: {}", 
                traceId, studentId, request.getNoteId());

        StudentNote existingNote = noteRepository.findByIdAndStudentId(request.getNoteId(), studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + request.getNoteId()));

        // Save current version before regenerating
        existingNote.addVersionSnapshot();

        // Create regeneration request
        NotesDTO.GenerateNotesRequest genRequest = NotesDTO.GenerateNotesRequest.builder()
                .topicId(existingNote.getTopicId())
                .format(existingNote.getFormat())
                .language(request.getLanguage() != null ? request.getLanguage() : existingNote.getLanguage())
                .customTitle(existingNote.getTitle())
                .build();

        // Generate new content
        Topic topic = topicRepository.findById(existingNote.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));

        String subjectName = subjectRepository.findById(existingNote.getSubjectId())
                .map(Subject::getName)
                .orElse("Unknown Subject");

        // Retrieve and regenerate
        ContentChunkService.RetrievalResult retrievalResult = contentChunkService.retrieveForRAG(
                "Generate comprehensive study notes for: " + topic.getTitle(),
                existingNote.getTopicId(),
                existingNote.getSubjectId(),
                genRequest.getLanguage(),
                10
        );

        String systemPrompt = buildNotesSystemPrompt(existingNote.getFormat(), genRequest.getLanguage());
        String userPrompt = buildNotesUserPrompt(topic, subjectName, existingNote.getFormat(), 
                retrievalResult, genRequest.getLanguage());

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userPrompt));

        try {
            long startTime = System.currentTimeMillis();
            ChatResponse aiResponse = chatModel.call(new Prompt(messages));
            String notesContent = aiResponse.getResult().getOutput().getContent();

            // Update the note
            existingNote.setContentMd(notesContent);
            existingNote.setLanguage(genRequest.getLanguage());
            existingNote.setGeneratedBy(aiProperties.getModel());
            existingNote.setChunksUsed(retrievalResult.getRetrievalCount());

            StudentNote savedNote = noteRepository.save(existingNote);

            long latency = System.currentTimeMillis() - startTime;

            return NotesDTO.GenerateNotesResponse.builder()
                    .note(mapToNoteResponse(savedNote, topic.getTitle(), subjectName))
                    .message("Notes regenerated successfully")
                    .tokensUsed(aiResponse.getMetadata() != null ? 
                            (Integer) aiResponse.getMetadata().getOrDefault("total_tokens", 0) : 0)
                    .latencyMs((int) latency)
                    .wasGrounded(retrievalResult.getRetrievalCount() > 0)
                    .build();

        } catch (Exception e) {
            log.error("Failed to regenerate notes - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to regenerate notes. Please try again.");
        }
    }

    // ===================== Note Retrieval =====================

    /**
     * Get all notes for a student with optional filters
     */
    @Transactional(readOnly = true)
    public NotesDTO.NotesListResponse getNotes(Long studentId, NotesDTO.NotesFilterRequest filter) {
        log.debug("Getting notes for student {} with filters", studentId);

        List<StudentNote> notes;
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isBlank()) {
            notes = noteRepository.searchByTitle(studentId, NoteStatus.ACTIVE, filter.getSearchTerm());
        } else {
            notes = noteRepository.findWithFilters(
                    studentId,
                    NoteStatus.ACTIVE,
                    filter.getTopicId(),
                    filter.getSubjectId(),
                    filter.getFormat(),
                    filter.getLanguage()
            );
        }

        // Apply favorites filter if specified
        if (Boolean.TRUE.equals(filter.getIsFavorite())) {
            notes = notes.stream()
                    .filter(n -> Boolean.TRUE.equals(n.getIsFavorite()))
                    .collect(Collectors.toList());
        }

        // Apply pagination manually
        int start = filter.getPage() * filter.getSize();
        int end = Math.min(start + filter.getSize(), notes.size());
        List<StudentNote> paginatedNotes = notes.subList(Math.min(start, notes.size()), end);

        List<NotesDTO.NoteListItem> noteItems = paginatedNotes.stream()
                .map(this::mapToNoteListItem)
                .collect(Collectors.toList());

        return NotesDTO.NotesListResponse.builder()
                .notes(noteItems)
                .totalCount(notes.size())
                .page(filter.getPage())
                .size(filter.getSize())
                .totalPages((int) Math.ceil((double) notes.size() / filter.getSize()))
                .build();
    }

    /**
     * Get a single note by ID
     */
    @Transactional(readOnly = true)
    public NotesDTO.NoteResponse getNote(Long studentId, Long noteId) {
        StudentNote note = noteRepository.findByIdAndStudentId(noteId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));

        String topicTitle = topicRepository.findById(note.getTopicId())
                .map(Topic::getTitle)
                .orElse("Unknown Topic");

        String subjectName = subjectRepository.findById(note.getSubjectId())
                .map(Subject::getName)
                .orElse("Unknown Subject");

        return mapToNoteResponse(note, topicTitle, subjectName);
    }

    /**
     * Get version history for a note
     */
    @Transactional(readOnly = true)
    public NotesDTO.NoteVersionsResponse getNoteVersions(Long studentId, Long noteId) {
        StudentNote note = noteRepository.findByIdAndStudentId(noteId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));

        List<NotesDTO.NoteVersionResponse> versions = note.getVersions().stream()
                .map(v -> NotesDTO.NoteVersionResponse.builder()
                        .id(v.getId())
                        .noteId(noteId)
                        .version(v.getVersion())
                        .contentMd(v.getContentMd())
                        .generatedBy(v.getGeneratedBy())
                        .chunksUsed(v.getChunksUsed())
                        .createdAt(v.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return NotesDTO.NoteVersionsResponse.builder()
                .noteId(noteId)
                .noteTitle(note.getTitle())
                .versions(versions)
                .currentVersion(note.getLatestVersion())
                .build();
    }

    // ===================== Note Management =====================

    /**
     * Update note metadata (title, favorite status)
     */
    public NotesDTO.NoteResponse updateNote(Long studentId, Long noteId, NotesDTO.UpdateNoteRequest request) {
        StudentNote note = noteRepository.findByIdAndStudentId(noteId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            note.setTitle(request.getTitle());
        }
        if (request.getIsFavorite() != null) {
            note.setIsFavorite(request.getIsFavorite());
        }

        StudentNote savedNote = noteRepository.save(note);

        String topicTitle = topicRepository.findById(note.getTopicId())
                .map(Topic::getTitle)
                .orElse("Unknown Topic");

        String subjectName = subjectRepository.findById(note.getSubjectId())
                .map(Subject::getName)
                .orElse("Unknown Subject");

        return mapToNoteResponse(savedNote, topicTitle, subjectName);
    }

    /**
     * Archive a note (soft delete)
     */
    public void archiveNote(Long studentId, Long noteId) {
        StudentNote note = noteRepository.findByIdAndStudentId(noteId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));

        note.setStatus(NoteStatus.ARCHIVED);
        noteRepository.save(note);
        log.info("Archived note - NoteId: {}, StudentId: {}", noteId, studentId);
    }

    /**
     * Export note as markdown
     */
    @Transactional(readOnly = true)
    public NotesDTO.ExportNoteResponse exportNote(Long studentId, Long noteId) {
        StudentNote note = noteRepository.findByIdAndStudentId(noteId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));

        String filename = sanitizeFilename(note.getTitle()) + ".md";

        return NotesDTO.ExportNoteResponse.builder()
                .filename(filename)
                .contentType("text/markdown")
                .content(note.getContentMd())
                .build();
    }

    /**
     * Get notes statistics for a student
     */
    @Transactional(readOnly = true)
    public NotesDTO.NotesStatsResponse getNotesStats(Long studentId) {
        long total = noteRepository.countByStudentIdAndStatus(studentId, NoteStatus.ACTIVE);
        long shortNotes = noteRepository.countByStudentIdAndFormatAndStatus(studentId, NoteFormat.SHORT, NoteStatus.ACTIVE);
        long longNotes = noteRepository.countByStudentIdAndFormatAndStatus(studentId, NoteFormat.LONG, NoteStatus.ACTIVE);
        long revisionSheets = noteRepository.countByStudentIdAndFormatAndStatus(studentId, NoteFormat.REVISION_SHEET, NoteStatus.ACTIVE);

        List<StudentNote> favoriteNotes = noteRepository.findByStudentIdAndIsFavoriteAndStatus(studentId, true, NoteStatus.ACTIVE);

        // Get notes count by subject
        List<StudentNote> allNotes = noteRepository.findByStudentIdAndStatusOrderByCreatedAtDesc(studentId, NoteStatus.ACTIVE);
        var notesBySubject = allNotes.stream()
                .collect(Collectors.groupingBy(StudentNote::getSubjectId, Collectors.counting()));

        List<NotesDTO.SubjectNotesCount> subjectCounts = notesBySubject.entrySet().stream()
                .map(entry -> {
                    String subjectName = subjectRepository.findById(entry.getKey())
                            .map(Subject::getName)
                            .orElse("Unknown");
                    return NotesDTO.SubjectNotesCount.builder()
                            .subjectId(entry.getKey())
                            .subjectName(subjectName)
                            .count(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());

        return NotesDTO.NotesStatsResponse.builder()
                .totalNotes(total)
                .shortNotes(shortNotes)
                .longNotes(longNotes)
                .revisionSheets(revisionSheets)
                .favoriteNotes(favoriteNotes.size())
                .notesBySubject(subjectCounts)
                .build();
    }

    // ===================== Helper Methods =====================

    private String buildNotesSystemPrompt(NoteFormat format, String language) {
        String langInstruction = "hi".equals(language) ? 
                "Respond in Hindi (Devanagari script). " : "Respond in English. ";

        String formatInstruction = switch (format) {
            case SHORT -> """
                    Generate SHORT study notes with:
                    - Brief overview (2-3 sentences)
                    - Key concepts as bullet points
                    - Important definitions (bold terms)
                    - 2-3 quick examples
                    - Common mistakes to avoid
                    Keep it concise - suitable for quick review in 5 minutes.
                    """;
            case LONG -> """
                    Generate DETAILED study notes with:
                    - Comprehensive introduction with context
                    - All key concepts explained with depth
                    - Multiple worked examples with step-by-step solutions
                    - Diagrams descriptions where helpful (describe as [Diagram: ...])
                    - Connections to real-world applications
                    - Common misconceptions and how to avoid them
                    - Practice questions at the end
                    - Summary section
                    Make it thorough - suitable for deep study session.
                    """;
            case REVISION_SHEET -> """
                    Generate a 1-PAGE REVISION SHEET with:
                    - Ultra-condensed bullet points only
                    - All formulas in a box format
                    - Key terms with one-line definitions
                    - Quick memory tricks/mnemonics
                    - 5 most important things to remember
                    - Common exam question patterns
                    Format for rapid exam revision - can be read in 2-3 minutes.
                    Use markdown tables for formulas and comparisons.
                    """;
        };

        return """
                You are an expert educator creating study notes for Indian school students (CBSE Board).
                
                %s
                %s
                
                Guidelines:
                1. Use simple, clear language appropriate for school students
                2. Include examples relevant to Indian context where appropriate
                3. Use markdown formatting (headers, bullets, bold, tables)
                4. Cite sources from the provided context when possible
                5. Be accurate - only include factual information
                6. Structure content for easy learning and retention
                
                DO NOT include:
                - Information not grounded in the provided context
                - Overly complex explanations
                - Content outside the topic scope
                """.formatted(langInstruction, formatInstruction);
    }

    private String buildNotesUserPrompt(Topic topic, String subjectName, NoteFormat format,
                                        ContentChunkService.RetrievalResult retrievalResult, String language) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate ").append(format.name()).append(" notes for:\n\n");
        prompt.append("**Topic:** ").append(topic.getTitle()).append("\n");
        prompt.append("**Subject:** ").append(subjectName).append("\n");
        prompt.append("**Board:** CBSE\n\n");

        if (retrievalResult.getRetrievalCount() > 0) {
            prompt.append("Use the following curriculum content as the authoritative source:\n\n");
            prompt.append("---\n");
            prompt.append(safetyService.sanitizeRetrievedContent(retrievalResult.getContextString()));
            prompt.append("\n---\n\n");
        }

        prompt.append("Create comprehensive, well-structured notes based on this content.");

        return prompt.toString();
    }

    private String generateNoteTitle(String topicTitle, NoteFormat format) {
        String formatLabel = switch (format) {
            case SHORT -> "Quick Notes";
            case LONG -> "Detailed Notes";
            case REVISION_SHEET -> "Revision Sheet";
        };
        return topicTitle + " - " + formatLabel;
    }

    private NotesDTO.NoteResponse mapToNoteResponse(StudentNote note, String topicTitle, String subjectName) {
        return NotesDTO.NoteResponse.builder()
                .id(note.getId())
                .studentId(note.getStudentId())
                .topicId(note.getTopicId())
                .topicTitle(topicTitle)
                .subjectId(note.getSubjectId())
                .subjectName(subjectName)
                .board(note.getBoard())
                .title(note.getTitle())
                .format(note.getFormat())
                .language(note.getLanguage())
                .contentMd(note.getContentMd())
                .generatedBy(note.getGeneratedBy())
                .chunksUsed(note.getChunksUsed())
                .status(note.getStatus())
                .isFavorite(note.getIsFavorite())
                .versionCount(note.getVersions().size())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    private NotesDTO.NoteListItem mapToNoteListItem(StudentNote note) {
        String topicTitle = topicRepository.findById(note.getTopicId())
                .map(Topic::getTitle)
                .orElse("Unknown Topic");

        String subjectName = subjectRepository.findById(note.getSubjectId())
                .map(Subject::getName)
                .orElse("Unknown Subject");

        // Generate preview (first 200 chars)
        String preview = note.getContentMd();
        if (preview != null && preview.length() > 200) {
            preview = preview.substring(0, 200) + "...";
        }

        return NotesDTO.NoteListItem.builder()
                .id(note.getId())
                .topicId(note.getTopicId())
                .topicTitle(topicTitle)
                .subjectId(note.getSubjectId())
                .subjectName(subjectName)
                .title(note.getTitle())
                .format(note.getFormat())
                .language(note.getLanguage())
                .isFavorite(note.getIsFavorite())
                .versionCount(note.getVersions().size())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .preview(preview)
                .build();
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}


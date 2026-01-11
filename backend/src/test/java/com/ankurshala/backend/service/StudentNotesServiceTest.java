package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.dto.student.NotesDTO;
import com.ankurshala.backend.entity.StudentNote;
import com.ankurshala.backend.entity.StudentNote.NoteFormat;
import com.ankurshala.backend.entity.StudentNote.NoteStatus;
import com.ankurshala.backend.entity.Subject;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.StudentNoteRepository;
import com.ankurshala.backend.repository.SubjectRepository;
import com.ankurshala.backend.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentNotesServiceTest {

    @Mock
    private StudentNoteRepository noteRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ContentChunkService contentChunkService;

    @Mock
    private ChatModel chatModel;

    @Mock
    private AIConfig.AIProperties aiProperties;

    @Mock
    private SafetyModerationService safetyService;

    @InjectMocks
    private StudentNotesService notesService;

    private Long studentId;
    private Long topicId;
    private Long subjectId;
    private Topic testTopic;
    private Subject testSubject;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        topicId = 100L;
        subjectId = 10L;

        testTopic = new Topic();
        testTopic.setId(topicId);
        testTopic.setTitle("Photosynthesis");
        testTopic.setSubjectId(subjectId);

        testSubject = new Subject();
        testSubject.setId(subjectId);
        testSubject.setName("Biology");
    }

    @Test
    void getNotes_ValidStudent_ReturnsNotes() {
        // Given
        StudentNote note1 = createTestNote(1L, "Note 1", NoteFormat.SHORT);
        StudentNote note2 = createTestNote(2L, "Note 2", NoteFormat.LONG);
        
        when(noteRepository.findWithFilters(eq(studentId), eq(NoteStatus.ACTIVE), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(note1, note2));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

        NotesDTO.NotesFilterRequest filter = NotesDTO.NotesFilterRequest.builder()
                .page(0)
                .size(20)
                .build();

        // When
        NotesDTO.NotesListResponse result = notesService.getNotes(studentId, filter);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalCount());
        assertEquals(2, result.getNotes().size());
    }

    @Test
    void getNote_ValidNote_ReturnsNote() {
        // Given
        StudentNote note = createTestNote(1L, "Test Note", NoteFormat.SHORT);
        note.setContentMd("# Test Content");

        when(noteRepository.findByIdAndStudentId(1L, studentId)).thenReturn(Optional.of(note));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

        // When
        NotesDTO.NoteResponse result = notesService.getNote(studentId, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Note", result.getTitle());
        assertEquals("# Test Content", result.getContentMd());
        assertEquals(NoteFormat.SHORT, result.getFormat());
    }

    @Test
    void getNote_NotFound_ThrowsException() {
        // Given
        when(noteRepository.findByIdAndStudentId(999L, studentId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            notesService.getNote(studentId, 999L);
        });
    }

    @Test
    void getNote_WrongOwner_ThrowsException() {
        // Given
        Long wrongStudentId = 999L;
        when(noteRepository.findByIdAndStudentId(1L, wrongStudentId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            notesService.getNote(wrongStudentId, 1L);
        });
    }

    @Test
    void updateNote_ValidRequest_UpdatesNote() {
        // Given
        StudentNote note = createTestNote(1L, "Original Title", NoteFormat.SHORT);

        when(noteRepository.findByIdAndStudentId(1L, studentId)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(StudentNote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

        NotesDTO.UpdateNoteRequest request = NotesDTO.UpdateNoteRequest.builder()
                .title("Updated Title")
                .isFavorite(true)
                .build();

        // When
        NotesDTO.NoteResponse result = notesService.updateNote(studentId, 1L, request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertTrue(result.getIsFavorite());
        verify(noteRepository).save(any(StudentNote.class));
    }

    @Test
    void archiveNote_ValidNote_ArchivesNote() {
        // Given
        StudentNote note = createTestNote(1L, "Test Note", NoteFormat.SHORT);

        when(noteRepository.findByIdAndStudentId(1L, studentId)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(StudentNote.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        notesService.archiveNote(studentId, 1L);

        // Then
        verify(noteRepository).save(argThat(n -> n.getStatus() == NoteStatus.ARCHIVED));
    }

    @Test
    void getNotesStats_ValidStudent_ReturnsStats() {
        // Given
        when(noteRepository.countByStudentIdAndStatus(studentId, NoteStatus.ACTIVE)).thenReturn(5L);
        when(noteRepository.countByStudentIdAndFormatAndStatus(studentId, NoteFormat.SHORT, NoteStatus.ACTIVE)).thenReturn(2L);
        when(noteRepository.countByStudentIdAndFormatAndStatus(studentId, NoteFormat.LONG, NoteStatus.ACTIVE)).thenReturn(2L);
        when(noteRepository.countByStudentIdAndFormatAndStatus(studentId, NoteFormat.REVISION_SHEET, NoteStatus.ACTIVE)).thenReturn(1L);
        when(noteRepository.findByStudentIdAndIsFavoriteAndStatus(studentId, true, NoteStatus.ACTIVE)).thenReturn(Collections.emptyList());
        when(noteRepository.findByStudentIdAndStatusOrderByCreatedAtDesc(studentId, NoteStatus.ACTIVE)).thenReturn(Collections.emptyList());

        // When
        NotesDTO.NotesStatsResponse result = notesService.getNotesStats(studentId);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getTotalNotes());
        assertEquals(2L, result.getShortNotes());
        assertEquals(2L, result.getLongNotes());
        assertEquals(1L, result.getRevisionSheets());
    }

    @Test
    void exportNote_ValidNote_ReturnsExport() {
        // Given
        StudentNote note = createTestNote(1L, "Export Test", NoteFormat.SHORT);
        note.setContentMd("# Export Content");

        when(noteRepository.findByIdAndStudentId(1L, studentId)).thenReturn(Optional.of(note));

        // When
        NotesDTO.ExportNoteResponse result = notesService.exportNote(studentId, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Export_Test.md", result.getFilename());
        assertEquals("text/markdown", result.getContentType());
        assertEquals("# Export Content", result.getContent());
    }

    @Test
    void getNoteVersions_ValidNote_ReturnsVersions() {
        // Given
        StudentNote note = createTestNote(1L, "Versioned Note", NoteFormat.SHORT);
        // Simulate adding a version
        note.setContentMd("Version 1 content");

        when(noteRepository.findByIdAndStudentId(1L, studentId)).thenReturn(Optional.of(note));

        // When
        NotesDTO.NoteVersionsResponse result = notesService.getNoteVersions(studentId, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getNoteId());
        assertEquals("Versioned Note", result.getNoteTitle());
    }

    @Test
    void getNotes_WithSearchTerm_FiltersResults() {
        // Given
        StudentNote note1 = createTestNote(1L, "Physics Notes", NoteFormat.SHORT);
        
        when(noteRepository.searchByTitle(studentId, NoteStatus.ACTIVE, "Physics"))
                .thenReturn(Collections.singletonList(note1));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

        NotesDTO.NotesFilterRequest filter = NotesDTO.NotesFilterRequest.builder()
                .searchTerm("Physics")
                .page(0)
                .size(20)
                .build();

        // When
        NotesDTO.NotesListResponse result = notesService.getNotes(studentId, filter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        verify(noteRepository).searchByTitle(studentId, NoteStatus.ACTIVE, "Physics");
    }

    @Test
    void getNotes_WithFormatFilter_FiltersResults() {
        // Given
        StudentNote note1 = createTestNote(1L, "Long Note", NoteFormat.LONG);
        
        when(noteRepository.findWithFilters(studentId, NoteStatus.ACTIVE, null, null, NoteFormat.LONG, null))
                .thenReturn(Collections.singletonList(note1));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(subjectRepository.findById(subjectId)).thenReturn(Optional.of(testSubject));

        NotesDTO.NotesFilterRequest filter = NotesDTO.NotesFilterRequest.builder()
                .format(NoteFormat.LONG)
                .page(0)
                .size(20)
                .build();

        // When
        NotesDTO.NotesListResponse result = notesService.getNotes(studentId, filter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalCount());
        assertEquals(NoteFormat.LONG, result.getNotes().get(0).getFormat());
    }

    // Helper method to create test notes
    private StudentNote createTestNote(Long id, String title, NoteFormat format) {
        StudentNote note = new StudentNote(studentId, topicId, subjectId, title, format);
        note.setId(id);
        note.setBoard("CBSE");
        note.setLanguage("en");
        note.setContentMd("Test content");
        note.setStatus(NoteStatus.ACTIVE);
        note.setIsFavorite(false);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        return note;
    }
}


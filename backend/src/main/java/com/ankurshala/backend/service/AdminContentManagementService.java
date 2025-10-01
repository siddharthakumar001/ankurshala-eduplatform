package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.content.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Comprehensive service for Admin Content Management
 * Handles CRUD operations, impact analysis, and cascading deletes for the content hierarchy:
 * Board → Grade → Subject → Chapter → Topic → TopicNote
 */
@Service
@Transactional
public class AdminContentManagementService {

    @Autowired
    private BoardRepository boardRepository;
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private TopicNoteRepository topicNoteRepository;

    // ======================== BOARD OPERATIONS ========================

    public Page<BoardDto> getBoards(String search, Boolean active, Pageable pageable) {
        Page<Board> boards = boardRepository.findBoardsWithFilters(search, active, pageable);
        return boards.map(this::convertToBoardDto);
    }

    public BoardDto getBoardById(Long id) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
        return convertToBoardDto(board);
    }

    public BoardDto createBoard(CreateBoardRequest request) {
        // Check if board name already exists
        if (boardRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Board with name '" + request.getName() + "' already exists");
        }

        Board board = new Board();
        board.setName(request.getName());
        board.setActive(request.getActive());
        
        Board savedBoard = boardRepository.save(board);
        return convertToBoardDto(savedBoard);
    }

    public BoardDto updateBoard(Long id, UpdateBoardRequest request) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        if (request.getName() != null) {
            // Check if name is unique (excluding current board)
            if (boardRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new IllegalArgumentException("Board with name '" + request.getName() + "' already exists");
            }
            board.setName(request.getName());
        }
        
        if (request.getActive() != null) {
            board.setActive(request.getActive());
        }

        Board savedBoard = boardRepository.save(board);
        return convertToBoardDto(savedBoard);
    }

    public void toggleBoardActive(Long id) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
        board.setActive(!board.getActive());
        boardRepository.save(board);
    }

    public DeletionImpactDto getBoardDeletionImpact(Long id) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        long gradesCount = boardRepository.countGradesByBoardId(id);
        long subjectsCount = boardRepository.countSubjectsByBoardId(id);
        long chaptersCount = boardRepository.countChaptersByBoardId(id);
        long topicsCount = boardRepository.countTopicsByBoardId(id);
        long notesCount = boardRepository.countTopicNotesByBoardId(id);

        DeletionImpactDto impact = DeletionImpactDto.builder()
                .level("BOARD")
                .boardId(id)
                .boardName(board.getName())
                .gradesCount(gradesCount)
                .subjectsCount(subjectsCount)
                .chaptersCount(chaptersCount)
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .build();
        impact.calculateTotalImpact();

        return impact;
    }

    public void deleteBoard(Long id, boolean force) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        if (force) {
            // Perform cascading hard delete
            cascadeDeleteBoard(id);
        } else {
            // Soft delete
            board.setSoftDeleted(true);
            board.setActive(false);
            boardRepository.save(board);
        }
    }

    private void cascadeDeleteBoard(Long boardId) {
        // Get all dependent entities and delete in reverse order (leaf to root)
        List<Grade> grades = gradeRepository.findActiveGradesByBoardId(boardId);
        
        for (Grade grade : grades) {
            cascadeDeleteGrade(grade.getId());
        }
        
        // Finally delete the board
        boardRepository.deleteById(boardId);
    }

    // ======================== GRADE OPERATIONS ========================

    public Page<GradeDto> getGrades(String search, Long boardId, Boolean active, Pageable pageable) {
        Page<Grade> grades = gradeRepository.findGradesWithFilters(search, boardId, active, pageable);
        return grades.map(this::convertToGradeDto);
    }

    public GradeDto getGradeById(Long id) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
        return convertToGradeDto(grade);
    }

    public GradeDto createGrade(CreateGradeRequest request) {
        // Verify board exists
        Board board = boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));

        // Check if grade name already exists within the board
        if (gradeRepository.existsByNameIgnoreCaseAndBoardId(request.getName(), request.getBoardId())) {
            throw new IllegalArgumentException("Grade with name '" + request.getName() + "' already exists in board '" + board.getName() + "'");
        }

        Grade grade = new Grade();
        grade.setName(request.getName());
        grade.setDisplayName(request.getDisplayName());
        grade.setBoardId(request.getBoardId());
        grade.setActive(request.getActive());
        
        Grade savedGrade = gradeRepository.save(grade);
        return convertToGradeDto(savedGrade);
    }

    public GradeDto updateGrade(Long id, UpdateGradeRequest request) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        if (request.getName() != null) {
            // Check if name is unique within board (excluding current grade)
            if (gradeRepository.existsByNameIgnoreCaseAndBoardIdAndIdNot(request.getName(), grade.getBoardId(), id)) {
                throw new IllegalArgumentException("Grade with name '" + request.getName() + "' already exists in this board");
            }
            grade.setName(request.getName());
        }
        
        if (request.getDisplayName() != null) {
            grade.setDisplayName(request.getDisplayName());
        }
        
        if (request.getBoardId() != null && !request.getBoardId().equals(grade.getBoardId())) {
            // Verify new board exists
            boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
            grade.setBoardId(request.getBoardId());
        }
        
        if (request.getActive() != null) {
            grade.setActive(request.getActive());
        }

        Grade savedGrade = gradeRepository.save(grade);
        return convertToGradeDto(savedGrade);
    }

    public void toggleGradeActive(Long id) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
        grade.setActive(!grade.getActive());
        gradeRepository.save(grade);
    }

    public DeletionImpactDto getGradeDeletionImpact(Long id) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        long subjectsCount = gradeRepository.countSubjectsByGradeId(id);
        long chaptersCount = gradeRepository.countChaptersByGradeId(id);
        long topicsCount = gradeRepository.countTopicsByGradeId(id);
        long notesCount = gradeRepository.countTopicNotesByGradeId(id);

        DeletionImpactDto impact = DeletionImpactDto.builder()
                .level("GRADE")
                .gradeId(id)
                .gradeName(grade.getDisplayName())
                .subjectsCount(subjectsCount)
                .chaptersCount(chaptersCount)
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .build();
        impact.calculateTotalImpact();

        return impact;
    }

    public void deleteGrade(Long id, boolean force) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        if (force) {
            cascadeDeleteGrade(id);
        } else {
            grade.setSoftDeleted(true);
            grade.setActive(false);
            gradeRepository.save(grade);
        }
    }

    private void cascadeDeleteGrade(Long gradeId) {
        List<Subject> subjects = subjectRepository.findByGradeIdAndSoftDeletedFalse(gradeId);
        
        for (Subject subject : subjects) {
            cascadeDeleteSubject(subject.getId());
        }
        
        gradeRepository.deleteById(gradeId);
    }

    // ======================== SUBJECT OPERATIONS ========================

    public Page<SubjectDto> getSubjects(String search, Long boardId, Long gradeId, Boolean active, Pageable pageable) {
        Page<Subject> subjects = subjectRepository.findSubjectsWithFilters(search, boardId, gradeId, active, pageable);
        return subjects.map(this::convertToSubjectDto);
    }

    public SubjectDto getSubjectById(Long id) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        return convertToSubjectDto(subject);
    }

    public SubjectDto createSubject(CreateSubjectRequest request) {
        // Verify board and grade exist
        Board board = boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
        
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));

        // Check if subject name already exists within the grade
        if (subjectRepository.existsByNameIgnoreCaseAndGradeId(request.getName(), request.getGradeId())) {
            throw new IllegalArgumentException("Subject with name '" + request.getName() + "' already exists in grade '" + grade.getDisplayName() + "'");
        }

        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setBoardId(request.getBoardId());
        subject.setGradeId(request.getGradeId());
        subject.setActive(request.getActive());
        
        Subject savedSubject = subjectRepository.save(subject);
        return convertToSubjectDto(savedSubject);
    }

    public SubjectDto updateSubject(Long id, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (request.getName() != null) {
            // Check if name is unique within grade (excluding current subject)
            if (subjectRepository.existsByNameIgnoreCaseAndGradeIdAndIdNot(request.getName(), subject.getGradeId(), id)) {
                throw new IllegalArgumentException("Subject with name '" + request.getName() + "' already exists in this grade");
            }
            subject.setName(request.getName());
        }
        
        if (request.getBoardId() != null && !request.getBoardId().equals(subject.getBoardId())) {
            // Verify new board exists
            boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
            subject.setBoardId(request.getBoardId());
        }
        
        if (request.getGradeId() != null && !request.getGradeId().equals(subject.getGradeId())) {
            // Verify new grade exists
            gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
            subject.setGradeId(request.getGradeId());
        }
        
        if (request.getActive() != null) {
            subject.setActive(request.getActive());
        }

        Subject savedSubject = subjectRepository.save(subject);
        return convertToSubjectDto(savedSubject);
    }

    public void toggleSubjectActive(Long id) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
        subject.setActive(!subject.getActive());
        subjectRepository.save(subject);
    }

    public DeletionImpactDto getSubjectDeletionImpact(Long id) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        long chaptersCount = subjectRepository.countChaptersBySubjectId(id);
        long topicsCount = subjectRepository.countTopicsBySubjectId(id);
        long notesCount = subjectRepository.countTopicNotesBySubjectId(id);

        DeletionImpactDto impact = DeletionImpactDto.builder()
                .level("SUBJECT")
                .subjectId(id)
                .subjectName(subject.getName())
                .chaptersCount(chaptersCount)
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .build();
        impact.calculateTotalImpact();

        return impact;
    }

    public void deleteSubject(Long id, boolean force) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (force) {
            cascadeDeleteSubject(id);
        } else {
            subject.setSoftDeleted(true);
            subject.setActive(false);
            subjectRepository.save(subject);
        }
    }

    // Placeholder methods for Subject, Chapter, Topic, TopicNote operations
    // These will be implemented in continuation
    private void cascadeDeleteSubject(Long subjectId) {
        List<Chapter> chapters = chapterRepository.findBySubjectIdAndSoftDeletedFalse(subjectId);
        
        for (Chapter chapter : chapters) {
            cascadeDeleteChapter(chapter.getId());
        }
        
        subjectRepository.deleteById(subjectId);
    }

    private void cascadeDeleteChapter(Long chapterId) {
        List<Topic> topics = topicRepository.findByChapterIdAndSoftDeletedFalse(chapterId);
        
        for (Topic topic : topics) {
            cascadeDeleteTopic(topic.getId());
        }
        
        chapterRepository.deleteById(chapterId);
    }

    private void cascadeDeleteTopic(Long topicId) {
        List<TopicNote> notes = topicNoteRepository.findByTopicIdNotSoftDeleted(topicId);
        
        for (TopicNote note : notes) {
            topicNoteRepository.deleteById(note.getId());
        }
        
        topicRepository.deleteById(topicId);
    }

    // ======================== CHAPTER OPERATIONS ========================

    public Page<ChapterDto> getChapters(String search, Long boardId, Long gradeId, Long subjectId, Boolean active, Pageable pageable) {
        Page<Chapter> chapters = chapterRepository.findChaptersWithFilters(boardId, gradeId, subjectId, search, active, pageable);
        return chapters.map(this::convertToChapterDto);
    }

    public ChapterDto getChapterById(Long id) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));
        return convertToChapterDto(chapter);
    }

    public ChapterDto createChapter(CreateChapterRequest request) {
        // Verify board, grade, and subject exist
        boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
        
        gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
        
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        // Check if chapter name already exists within the subject
        if (chapterRepository.existsByNameIgnoreCaseAndSubjectId(request.getName(), request.getSubjectId())) {
            throw new IllegalArgumentException("Chapter with name '" + request.getName() + "' already exists in subject '" + subject.getName() + "'");
        }

        Chapter chapter = new Chapter();
        chapter.setName(request.getName());
        chapter.setBoardId(request.getBoardId());
        chapter.setGradeId(request.getGradeId());
        chapter.setSubjectId(request.getSubjectId());
        chapter.setActive(request.getActive());
        
        Chapter savedChapter = chapterRepository.save(chapter);
        return convertToChapterDto(savedChapter);
    }

    public ChapterDto updateChapter(Long id, UpdateChapterRequest request) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        if (request.getName() != null) {
            // Check if name is unique within subject (excluding current chapter)
            if (chapterRepository.existsByNameIgnoreCaseAndSubjectIdAndIdNot(request.getName(), chapter.getSubjectId(), id)) {
                throw new IllegalArgumentException("Chapter with name '" + request.getName() + "' already exists in this subject");
            }
            chapter.setName(request.getName());
        }
        
        if (request.getBoardId() != null && !request.getBoardId().equals(chapter.getBoardId())) {
            boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
            chapter.setBoardId(request.getBoardId());
        }
        
        if (request.getGradeId() != null && !request.getGradeId().equals(chapter.getGradeId())) {
            gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
            chapter.setGradeId(request.getGradeId());
        }
        
        if (request.getSubjectId() != null && !request.getSubjectId().equals(chapter.getSubjectId())) {
            subjectRepository.findByIdAndSoftDeletedFalse(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
            chapter.setSubjectId(request.getSubjectId());
        }
        
        if (request.getActive() != null) {
            chapter.setActive(request.getActive());
        }

        Chapter savedChapter = chapterRepository.save(chapter);
        return convertToChapterDto(savedChapter);
    }

    public void toggleChapterActive(Long id) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));
        chapter.setActive(!chapter.getActive());
        chapterRepository.save(chapter);
    }

    public DeletionImpactDto getChapterDeletionImpact(Long id) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        long topicsCount = chapterRepository.countTopicsByChapterId(id);
        long notesCount = chapterRepository.countTopicNotesByChapterId(id);

        DeletionImpactDto impact = DeletionImpactDto.builder()
                .level("CHAPTER")
                .chapterId(id)
                .chapterName(chapter.getName())
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .build();
        impact.calculateTotalImpact();

        return impact;
    }

    public void deleteChapter(Long id, boolean force) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        if (force) {
            cascadeDeleteChapter(id);
        } else {
            chapter.setSoftDeleted(true);
            chapter.setActive(false);
            chapterRepository.save(chapter);
        }
    }

    // ======================== TOPIC OPERATIONS ========================

    public Page<TopicDto> getTopics(String search, Long boardId, Long gradeId, Long subjectId, Long chapterId, Boolean active, Pageable pageable) {
        Page<Topic> topics = topicRepository.findTopicsWithFilters(boardId, gradeId, subjectId, chapterId, search, active, pageable);
        return topics.map(this::convertToTopicDto);
    }

    public TopicDto getTopicById(Long id) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));
        return convertToTopicDto(topic);
    }

    public TopicDto createTopic(CreateTopicRequest request) {
        // Verify board, grade, subject, and chapter exist
        boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
        
        gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
        
        subjectRepository.findByIdAndSoftDeletedFalse(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
        
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + request.getChapterId()));

        // Check if topic title already exists within the chapter
        if (topicRepository.existsByTitleIgnoreCaseAndChapterId(request.getTitle(), request.getChapterId())) {
            throw new IllegalArgumentException("Topic with title '" + request.getTitle() + "' already exists in chapter '" + chapter.getName() + "'");
        }

        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setSummary(request.getSummary());
        topic.setExpectedTimeMins(request.getExpectedTimeMins());
        topic.setBoardId(request.getBoardId());
        topic.setGradeId(request.getGradeId());
        topic.setSubjectId(request.getSubjectId());
        topic.setChapterId(request.getChapterId());
        topic.setChapter(chapter);
        topic.setActive(request.getActive());
        
        Topic savedTopic = topicRepository.save(topic);
        return convertToTopicDto(savedTopic);
    }

    public TopicDto updateTopic(Long id, UpdateTopicRequest request) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        if (request.getTitle() != null) {
            // Check if title is unique within chapter (excluding current topic)
            if (topicRepository.existsByTitleIgnoreCaseAndChapterIdAndIdNot(request.getTitle(), topic.getChapterId(), id)) {
                throw new IllegalArgumentException("Topic with title '" + request.getTitle() + "' already exists in this chapter");
            }
            topic.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            topic.setDescription(request.getDescription());
        }
        
        if (request.getSummary() != null) {
            topic.setSummary(request.getSummary());
        }

        if (request.getExpectedTimeMins() != null) {
            topic.setExpectedTimeMins(request.getExpectedTimeMins());
        }
        
        if (request.getBoardId() != null && !request.getBoardId().equals(topic.getBoardId())) {
            boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
            topic.setBoardId(request.getBoardId());
        }
        
        if (request.getGradeId() != null && !request.getGradeId().equals(topic.getGradeId())) {
            gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
            topic.setGradeId(request.getGradeId());
        }
        
        if (request.getSubjectId() != null && !request.getSubjectId().equals(topic.getSubjectId())) {
            subjectRepository.findByIdAndSoftDeletedFalse(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
            topic.setSubjectId(request.getSubjectId());
        }
        
        if (request.getChapterId() != null && !request.getChapterId().equals(topic.getChapterId())) {
            chapterRepository.findByIdAndSoftDeletedFalse(request.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + request.getChapterId()));
            topic.setChapterId(request.getChapterId());
        }
        
        if (request.getActive() != null) {
            topic.setActive(request.getActive());
        }

        Topic savedTopic = topicRepository.save(topic);
        return convertToTopicDto(savedTopic);
    }

    public void toggleTopicActive(Long id) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));
        topic.setActive(!topic.getActive());
        topicRepository.save(topic);
    }

    public DeletionImpactDto getTopicDeletionImpact(Long id) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        long notesCount = topicRepository.countTopicNotesByTopicId(id);

        DeletionImpactDto impact = DeletionImpactDto.builder()
                .level("TOPIC")
                .topicId(id)
                .topicName(topic.getTitle())
                .notesCount(notesCount)
                .build();
        impact.calculateTotalImpact();

        return impact;
    }

    public void deleteTopic(Long id, boolean force) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        if (force) {
            cascadeDeleteTopic(id);
        } else {
            topic.setSoftDeleted(true);
            topic.setActive(false);
            topicRepository.save(topic);
        }
    }

    // ======================== TOPIC NOTE OPERATIONS ========================

    public Page<TopicNoteDto> getTopicNotes(String search, Long boardId, Long gradeId, Long subjectId, Long chapterId, Long topicId, Boolean active, Pageable pageable) {
        Page<TopicNote> notes = topicNoteRepository.findTopicNotesWithFilters(boardId, gradeId, subjectId, chapterId, topicId, search, active, pageable);
        return notes.map(this::convertToTopicNoteDto);
    }

    public TopicNoteDto getTopicNoteById(Long id) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TopicNote not found with id: " + id));
        return convertToTopicNoteDto(note);
    }

    public TopicNoteDto createTopicNote(CreateTopicNoteRequest request) {
        // Verify all parent entities exist
        boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
        
        gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
        
        subjectRepository.findByIdAndSoftDeletedFalse(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
        
        chapterRepository.findByIdAndSoftDeletedFalse(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + request.getChapterId()));
        
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + request.getTopicId()));

        // Check if note title already exists within the topic
        if (topicNoteRepository.existsByTitleIgnoreCaseAndTopicId(request.getTitle(), request.getTopicId())) {
            throw new IllegalArgumentException("TopicNote with title '" + request.getTitle() + "' already exists in topic '" + topic.getTitle() + "'");
        }

        TopicNote note = new TopicNote();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setBoardId(request.getBoardId());
        note.setGradeId(request.getGradeId());
        note.setSubjectId(request.getSubjectId());
        note.setChapterId(request.getChapterId());
        note.setTopicId(request.getTopicId());
        note.setActive(request.getActive());
        
        TopicNote savedNote = topicNoteRepository.save(note);
        return convertToTopicNoteDto(savedNote);
    }

    public TopicNoteDto updateTopicNote(Long id, UpdateTopicNoteRequest request) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TopicNote not found with id: " + id));

        if (request.getTitle() != null) {
            // Check if title is unique within topic (excluding current note)
            if (topicNoteRepository.existsByTitleIgnoreCaseAndTopicIdAndIdNot(request.getTitle(), note.getTopicId(), id)) {
                throw new IllegalArgumentException("TopicNote with title '" + request.getTitle() + "' already exists in this topic");
            }
            note.setTitle(request.getTitle());
        }
        
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        
        if (request.getBoardId() != null && !request.getBoardId().equals(note.getBoardId())) {
            boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
            note.setBoardId(request.getBoardId());
        }
        
        if (request.getGradeId() != null && !request.getGradeId().equals(note.getGradeId())) {
            gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));
            note.setGradeId(request.getGradeId());
        }
        
        if (request.getSubjectId() != null && !request.getSubjectId().equals(note.getSubjectId())) {
            subjectRepository.findByIdAndSoftDeletedFalse(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));
            note.setSubjectId(request.getSubjectId());
        }
        
        if (request.getChapterId() != null && !request.getChapterId().equals(note.getChapterId())) {
            chapterRepository.findByIdAndSoftDeletedFalse(request.getChapterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + request.getChapterId()));
            note.setChapterId(request.getChapterId());
        }
        
        if (request.getTopicId() != null && !request.getTopicId().equals(note.getTopicId())) {
            topicRepository.findByIdAndSoftDeletedFalse(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + request.getTopicId()));
            note.setTopicId(request.getTopicId());
        }
        
        if (request.getActive() != null) {
            note.setActive(request.getActive());
        }

        TopicNote savedNote = topicNoteRepository.save(note);
        return convertToTopicNoteDto(savedNote);
    }

    public void toggleTopicNoteActive(Long id) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TopicNote not found with id: " + id));
        note.setActive(!note.getActive());
        topicNoteRepository.save(note);
    }

    public DeletionImpactDto getTopicNoteDeletionImpact(Long id) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TopicNote not found with id: " + id));

        DeletionImpactDto impact = DeletionImpactDto.builder()
                .level("TOPIC_NOTE")
                .topicId(id)
                .topicName(note.getTitle())
                .build();
        impact.calculateTotalImpact(); // Will be 0 for leaf nodes

        return impact;
    }

    public void deleteTopicNote(Long id, boolean force) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("TopicNote not found with id: " + id));

        if (force) {
            topicNoteRepository.deleteById(id);
        } else {
            note.setSoftDeleted(true);
            note.setActive(false);
            topicNoteRepository.save(note);
        }
    }

    // ======================== UTILITY OPERATIONS ========================

    public List<DropdownDto> getBoardDropdown() {
        return boardRepository.findActiveBoardsForDropdown()
                .stream()
                .map(board -> new DropdownDto(board.getId(), board.getName()))
                .collect(Collectors.toList());
    }

    public List<DropdownDto> getGradeDropdown(Long boardId) {
        if (boardId == null) {
            return gradeRepository.findActiveGradesForDropdown()
                    .stream()
                    .map(grade -> new DropdownDto(grade.getId(), grade.getName()))
                    .collect(Collectors.toList());
        }
        return gradeRepository.findActiveGradesByBoardIdForDropdown(boardId)
                .stream()
                .map(grade -> new DropdownDto(grade.getId(), grade.getName()))
                .collect(Collectors.toList());
    }

    public List<DropdownDto> getSubjectDropdown(Long gradeId) {
        if (gradeId == null) {
            return subjectRepository.findActiveSubjectsForDropdown()
                    .stream()
                    .map(subject -> new DropdownDto(subject.getId(), subject.getName()))
                    .collect(Collectors.toList());
        }
        return subjectRepository.findActiveSubjectsByGradeIdForDropdown(gradeId)
                .stream()
                .map(subject -> new DropdownDto(subject.getId(), subject.getName()))
                .collect(Collectors.toList());
    }

    public List<DropdownDto> getChapterDropdown(Long subjectId) {
        if (subjectId == null) {
            return chapterRepository.findActiveChaptersForDropdown()
                    .stream()
                    .map(chapter -> new DropdownDto(chapter.getId(), chapter.getName()))
                    .collect(Collectors.toList());
        }
        return chapterRepository.findActiveChaptersBySubjectIdForDropdown(subjectId)
                .stream()
                .map(chapter -> new DropdownDto(chapter.getId(), chapter.getName()))
                .collect(Collectors.toList());
    }

    public List<DropdownDto> getTopicDropdown(Long chapterId) {
        if (chapterId == null) {
            return topicRepository.findActiveTopicsForDropdown()
                    .stream()
                    .map(topic -> new DropdownDto(topic.getId(), topic.getTitle()))
                    .collect(Collectors.toList());
        }
        return topicRepository.findActiveTopicsByChapterIdForDropdown(chapterId)
                .stream()
                .map(topic -> new DropdownDto(topic.getId(), topic.getTitle()))
                .collect(Collectors.toList());
    }

    // ======================== DROPDOWN UTILITY METHODS (for AdminContentUtilsController) ========================

    public List<BoardDropdownDto> getBoardsForDropdown() {
        return boardRepository.findActiveBoardsForDropdown()
                .stream()
                .map(board -> new BoardDropdownDto(board.getId(), board.getName()))
                .collect(Collectors.toList());
    }

    public List<GradeDropdownDto> getGradesForDropdown(Long boardId) {
        if (boardId == null) {
            return gradeRepository.findActiveGradesForDropdown()
                    .stream()
                    .map(grade -> new GradeDropdownDto(grade.getId(), grade.getName(), grade.getDisplayName(), grade.getBoardId()))
                    .collect(Collectors.toList());
        }
        return gradeRepository.findActiveGradesByBoardIdForDropdown(boardId)
                .stream()
                .map(grade -> new GradeDropdownDto(grade.getId(), grade.getName(), grade.getDisplayName(), grade.getBoardId()))
                .collect(Collectors.toList());
    }

    public List<SubjectDropdownDto> getSubjectsForDropdown(Long gradeId) {
        if (gradeId == null) {
            return subjectRepository.findActiveSubjectsForDropdown()
                    .stream()
                    .map(subject -> new SubjectDropdownDto(subject.getId(), subject.getName(), subject.getBoardId(), subject.getGradeId()))
                    .collect(Collectors.toList());
        }
        return subjectRepository.findActiveSubjectsByGradeIdForDropdown(gradeId)
                .stream()
                .map(subject -> new SubjectDropdownDto(subject.getId(), subject.getName(), subject.getBoardId(), subject.getGradeId()))
                .collect(Collectors.toList());
    }

    public List<ChapterDropdownDto> getChaptersForDropdown(Long subjectId) {
        if (subjectId == null) {
            return chapterRepository.findActiveChaptersForDropdown()
                    .stream()
                    .map(chapter -> new ChapterDropdownDto(chapter.getId(), chapter.getName(), chapter.getSubject().getId(), chapter.getSubject().getGradeId(), chapter.getBoardId()))
                    .collect(Collectors.toList());
        }
        return chapterRepository.findActiveChaptersBySubjectIdForDropdown(subjectId)
                .stream()
                .map(chapter -> new ChapterDropdownDto(chapter.getId(), chapter.getName(), chapter.getSubject().getId(), chapter.getSubject().getGradeId(), chapter.getBoardId()))
                .collect(Collectors.toList());
    }

    public List<TopicDropdownDto> getTopicsForDropdown(Long chapterId) {
        if (chapterId == null) {
            return topicRepository.findActiveTopicsForDropdown()
                    .stream()
                    .map(topic -> new TopicDropdownDto(topic.getId(), topic.getTitle(), topic.getCode(), topic.getChapter().getId(), topic.getSubjectId(), topic.getGradeId(), topic.getBoardId()))
                    .collect(Collectors.toList());
        }
        return topicRepository.findActiveTopicsByChapterIdForDropdown(chapterId)
                .stream()
                .map(topic -> new TopicDropdownDto(topic.getId(), topic.getTitle(), topic.getCode(), topic.getChapter().getId(), topic.getSubjectId(), topic.getGradeId(), topic.getBoardId()))
                .collect(Collectors.toList());
    }

    // Update the getContentTree method to match AdminContentUtilsController signature
    public ContentTreeDto getContentTree(Long boardId, Long gradeId, Long subjectId, Long chapterId) {
        return buildContentTree(boardId, gradeId, subjectId, chapterId, null);
    }

    private ContentTreeDto buildContentTree(Long boardId, Long gradeId, Long subjectId, Long chapterId, Long topicId) {
        ContentTreeDto tree = new ContentTreeDto();
        
        if (boardId != null) {
            List<Board> boards = List.of(boardRepository.findByIdAndSoftDeletedFalse(boardId)
                    .orElseThrow(() -> new ResourceNotFoundException("Board not found")));
            tree.setBoards(boards.stream().map(this::convertToBoardDto).toList());
            
            if (gradeId != null) {
                List<Grade> grades = List.of(gradeRepository.findByIdAndSoftDeletedFalse(gradeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Grade not found")));
                tree.setGrades(grades.stream().map(this::convertToGradeDto).toList());
                
                if (subjectId != null) {
                    List<Subject> subjects = List.of(subjectRepository.findByIdAndSoftDeletedFalse(subjectId)
                            .orElseThrow(() -> new ResourceNotFoundException("Subject not found")));
                    tree.setSubjects(subjects.stream().map(this::convertToSubjectDto).toList());
                    
                    if (chapterId != null) {
                        List<Chapter> chapters = List.of(chapterRepository.findByIdAndSoftDeletedFalse(chapterId)
                                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found")));
                        tree.setChapters(chapters.stream().map(this::convertToChapterDto).toList());
                        
                        if (topicId != null) {
                            List<Topic> topics = List.of(topicRepository.findByIdAndSoftDeletedFalse(topicId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found")));
                            tree.setTopics(topics.stream().map(this::convertToTopicDto).toList());
                            
                            List<TopicNote> notes = topicNoteRepository.findByTopicIdNotSoftDeleted(topicId);
                            tree.setTopicNotes(notes.stream().map(this::convertToTopicNoteDto).toList());
                        } else {
                            List<Topic> topics = topicRepository.findByChapterIdAndSoftDeletedFalse(chapterId);
                            tree.setTopics(topics.stream().map(this::convertToTopicDto).toList());
                        }
                    } else {
                        List<Chapter> chapters = chapterRepository.findBySubjectIdAndSoftDeletedFalse(subjectId);
                        tree.setChapters(chapters.stream().map(this::convertToChapterDto).toList());
                    }
                } else {
                    List<Subject> subjects = subjectRepository.findByGradeIdAndSoftDeletedFalse(gradeId);
                    tree.setSubjects(subjects.stream().map(this::convertToSubjectDto).toList());
                }
            } else {
                List<Grade> grades = gradeRepository.findByBoardIdNotSoftDeleted(boardId);
                tree.setGrades(grades.stream().map(this::convertToGradeDto).toList());
            }
        } else {
            List<Board> boards = boardRepository.findAllNotSoftDeleted();
            tree.setBoards(boards.stream().map(this::convertToBoardDto).toList());
        }
        
        return tree;
    }

    // ======================== HELPER METHODS ========================

    private BoardDto convertToBoardDto(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .name(board.getName())
                .active(board.getActive())
                .softDeleted(board.getSoftDeleted())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .gradesCount(boardRepository.countGradesByBoardId(board.getId()))
                .subjectsCount(boardRepository.countSubjectsByBoardId(board.getId()))
                .build();
    }

    private GradeDto convertToGradeDto(Grade grade) {
        // Add board name if available
        Optional<Board> board = boardRepository.findByIdAndSoftDeletedFalse(grade.getBoardId());
        String boardName = board.map(Board::getName).orElse(null);
        
        return GradeDto.builder()
                .id(grade.getId())
                .name(grade.getName())
                .displayName(grade.getDisplayName())
                .boardId(grade.getBoardId())
                .boardName(boardName)
                .active(grade.getActive())
                .softDeleted(grade.getSoftDeleted())
                .createdAt(grade.getCreatedAt())
                .updatedAt(grade.getUpdatedAt())
                .subjectsCount(gradeRepository.countSubjectsByGradeId(grade.getId()))
                .chaptersCount(gradeRepository.countChaptersByGradeId(grade.getId()))
                .build();
    }

    private SubjectDto convertToSubjectDto(Subject subject) {
        // Add parent names if available
        Optional<Board> board = boardRepository.findByIdAndSoftDeletedFalse(subject.getBoardId());
        String boardName = board.map(Board::getName).orElse(null);
        
        Optional<Grade> grade = gradeRepository.findByIdAndSoftDeletedFalse(subject.getGradeId());
        String gradeName = grade.map(Grade::getDisplayName).orElse(null);
        
        return SubjectDto.builder()
                .id(subject.getId())
                .name(subject.getName())
                .boardId(subject.getBoardId())
                .boardName(boardName)
                .gradeId(subject.getGradeId())
                .gradeName(gradeName)
                .active(subject.getActive())
                .softDeleted(subject.getSoftDeleted())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .chaptersCount(subjectRepository.countChaptersBySubjectId(subject.getId()))
                .topicsCount(subjectRepository.countTopicsBySubjectId(subject.getId()))
                .build();
    }

    private ChapterDto convertToChapterDto(Chapter chapter) {
        // Add parent names if available
        Optional<Board> board = boardRepository.findByIdAndSoftDeletedFalse(chapter.getBoardId());
        String boardName = board.map(Board::getName).orElse(null);
        
        Optional<Grade> grade = gradeRepository.findByIdAndSoftDeletedFalse(chapter.getGradeId());
        String gradeName = grade.map(Grade::getDisplayName).orElse(null);
        
        Optional<Subject> subject = subjectRepository.findByIdAndSoftDeletedFalse(chapter.getSubjectId());
        String subjectName = subject.map(Subject::getName).orElse(null);
        
        return ChapterDto.builder()
                .id(chapter.getId())
                .name(chapter.getName())
                .boardId(chapter.getBoardId())
                .boardName(boardName)
                .gradeId(chapter.getGradeId())
                .gradeName(gradeName)
                .subjectId(chapter.getSubjectId())
                .subjectName(subjectName)
                .active(chapter.getActive())
                .softDeleted(chapter.getSoftDeleted())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .topicsCount(chapterRepository.countTopicsByChapterId(chapter.getId()))
                .notesCount(chapterRepository.countTopicNotesByChapterId(chapter.getId()))
                .build();
    }

    private TopicDto convertToTopicDto(Topic topic) {
        // Add parent names if available
        Optional<Board> board = boardRepository.findByIdAndSoftDeletedFalse(topic.getBoardId());
        String boardName = board.map(Board::getName).orElse(null);
        
        Optional<Grade> grade = gradeRepository.findByIdAndSoftDeletedFalse(topic.getGradeId());
        String gradeName = grade.map(Grade::getDisplayName).orElse(null);
        
        Optional<Subject> subject = subjectRepository.findByIdAndSoftDeletedFalse(topic.getSubjectId());
        String subjectName = subject.map(Subject::getName).orElse(null);
        
        Optional<Chapter> chapter = chapterRepository.findByIdAndSoftDeletedFalse(topic.getChapterId());
        String chapterName = chapter.map(Chapter::getName).orElse(null);
        
        return TopicDto.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .code(topic.getCode())
                .description(topic.getDescription())
                .summary(topic.getSummary())
                .expectedTimeMins(topic.getExpectedTimeMins())
                .boardId(topic.getBoardId())
                .boardName(boardName)
                .gradeId(topic.getGradeId())
                .gradeName(gradeName)
                .subjectId(topic.getSubjectId())
                .subjectName(subjectName)
                .chapterId(topic.getChapterId())
                .chapterName(chapterName)
                .active(topic.getActive())
                .softDeleted(topic.getSoftDeleted())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .notesCount(topicRepository.countTopicNotesByTopicId(topic.getId()))
                .build();
    }

    private TopicNoteDto convertToTopicNoteDto(TopicNote note) {
        // Add parent names if available
        Optional<Board> board = boardRepository.findByIdAndSoftDeletedFalse(note.getBoardId());
        String boardName = board.map(Board::getName).orElse(null);
        
        Optional<Grade> grade = gradeRepository.findByIdAndSoftDeletedFalse(note.getGradeId());
        String gradeName = grade.map(Grade::getDisplayName).orElse(null);
        
        Optional<Subject> subject = subjectRepository.findByIdAndSoftDeletedFalse(note.getSubjectId());
        String subjectName = subject.map(Subject::getName).orElse(null);
        
        Optional<Chapter> chapter = chapterRepository.findByIdAndSoftDeletedFalse(note.getChapterId());
        String chapterName = chapter.map(Chapter::getName).orElse(null);
        
        Optional<Topic> topic = topicRepository.findByIdAndSoftDeletedFalse(note.getTopicId());
        String topicTitle = topic.map(Topic::getTitle).orElse(null);
        String topicCode = topic.map(Topic::getCode).orElse(null);
        
        return TopicNoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .attachments(note.getAttachments())
                .topicId(note.getTopicId())
                .topicTitle(topicTitle)
                .topicCode(topicCode)
                .active(note.getActive())
                .softDeleted(note.getSoftDeleted())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .boardId(note.getBoardId())
                .boardName(boardName)
                .gradeId(note.getGradeId())
                .gradeName(gradeName)
                .subjectId(note.getSubjectId())
                .subjectName(subjectName)
                .chapterId(note.getChapterId())
                .chapterName(chapterName)
                .build();
    }
}
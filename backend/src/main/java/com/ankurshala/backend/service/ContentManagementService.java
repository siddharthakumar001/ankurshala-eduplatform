package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.content.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;

/**
 * Comprehensive service for Admin Content Management
 * Handles CRUD operations, impact analysis, and cascading deletes for the content hierarchy:
 * Board → Grade → Subject → Chapter → Topic → TopicNote
 */
@Service
@Transactional
public class ContentManagementService {

    private static final Logger log = LoggerFactory.getLogger(ContentManagementService.class);

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

    // ============ BOARDS SERVICE ============

    @Transactional(readOnly = true)
    public Page<BoardDto> getBoards(Pageable pageable, String search, Boolean active) {
        Specification<Board> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.equal(root.get("softDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Board> boards = boardRepository.findAll(spec, pageable);
        return boards.map(this::convertToBoardDto);
    }

    public BoardDto createBoard(CreateBoardRequest request) {
        Board board = new Board();
        board.setName(request.getName());
        board.setActive(request.getActive() != null ? request.getActive() : true);
        board.setSoftDeleted(false);

        Board savedBoard = boardRepository.save(board);
        log.info("Created board: {}", savedBoard.getName());
        return convertToBoardDto(savedBoard);
    }

    public BoardDto updateBoard(Long id, UpdateBoardRequest request) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        board.setName(request.getName());
        if (request.getActive() != null) {
            board.setActive(request.getActive());
        }

        Board savedBoard = boardRepository.save(board);
        log.info("Updated board: {}", savedBoard.getName());
        return convertToBoardDto(savedBoard);
    }

    public BoardDto toggleBoardStatus(Long id) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        board.setActive(!board.getActive());
        Board savedBoard = boardRepository.save(board);
        log.info("Toggled board status: {} -> {}", board.getName(), savedBoard.getActive());
        return convertToBoardDto(savedBoard);
    }

    public DeletionImpactDto getBoardDeletionImpact(Long id) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        long gradesCount = gradeRepository.countByBoardIdAndSoftDeletedFalse(id);
        long subjectsCount = subjectRepository.countSubjectsByBoardIdAndSoftDeletedFalse(id);
        long chaptersCount = chapterRepository.countChaptersByBoardIdAndSoftDeletedFalse(id);
        long topicsCount = topicRepository.countTopicsByBoardIdAndSoftDeletedFalse(id);
        long notesCount = topicNoteRepository.countTopicNotesByBoardIdAndSoftDeletedFalse(id);

        return DeletionImpactDto.builder()
                .level("BOARD")
                .boardId(id)
                .boardName(board.getName())
                .gradesCount(gradesCount)
                .subjectsCount(subjectsCount)
                .chaptersCount(chaptersCount)
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .totalImpact(gradesCount + subjectsCount + chaptersCount + topicsCount + notesCount)
                .warnings(Arrays.asList(
                    "All grades, subjects, chapters, topics, and notes under this board will be affected",
                    "Pricing rules associated with this board will be deleted"
                ))
                .build();
    }

    public void deleteBoard(Long id, boolean force) {
        Board board = boardRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        if (force) {
            // Force delete: cascade delete all children
            log.info("Force deleting board: {} and all children", board.getName());
            
            // Delete in reverse order: TopicNotes → Topics → Chapters → Subjects → Grades → Board
            List<TopicNote> notes = topicNoteRepository.findByBoardIdAndSoftDeletedFalse(id);
            topicNoteRepository.deleteAll(notes);
            
            List<Topic> topics = topicRepository.findByBoardIdAndSoftDeletedFalse(id);
            topicRepository.deleteAll(topics);
            
            List<Chapter> chapters = chapterRepository.findByBoardIdAndSoftDeletedFalse(id);
            chapterRepository.deleteAll(chapters);
            
            List<Subject> subjects = subjectRepository.findByBoardIdAndSoftDeletedFalse(id);
            subjectRepository.deleteAll(subjects);
            
            List<Grade> grades = gradeRepository.findByBoardIdAndSoftDeletedFalse(id);
            gradeRepository.deleteAll(grades);
            
            boardRepository.delete(board);
            log.info("Force deleted board: {} and all children", board.getName());
        } else {
            // Soft delete
            board.setSoftDeleted(true);
            board.setDeletedAt(LocalDateTime.now());
            board.setActive(false);
            boardRepository.save(board);
            log.info("Soft deleted board: {}", board.getName());
        }
    }

    // ============ GRADES SERVICE ============

    @Transactional(readOnly = true)
    public Page<GradeDto> getGrades(Pageable pageable, String search, Boolean active, Long boardId) {
        Specification<Grade> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.equal(root.get("softDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchTerm);
                Predicate displayNamePredicate = cb.like(cb.lower(root.get("displayName")), searchTerm);
                predicates.add(cb.or(namePredicate, displayNamePredicate));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (boardId != null) {
                predicates.add(cb.equal(root.get("boardId"), boardId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Grade> grades = gradeRepository.findAll(spec, pageable);
        return grades.map(this::convertToGradeDto);
    }

    public GradeDto createGrade(CreateGradeRequest request) {
        // Validate board exists
        Board board = boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));

        Grade grade = new Grade();
        grade.setName(request.getName());
        grade.setDisplayName(request.getDisplayName());
        grade.setBoardId(request.getBoardId());
        grade.setActive(request.getActive() != null ? request.getActive() : true);
        grade.setSoftDeleted(false);

        Grade savedGrade = gradeRepository.save(grade);
        log.info("Created grade: {} for board: {}", savedGrade.getName(), board.getName());
        return convertToGradeDto(savedGrade);
    }

    public GradeDto updateGrade(Long id, UpdateGradeRequest request) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        grade.setName(request.getName());
        grade.setDisplayName(request.getDisplayName());
        if (request.getActive() != null) {
            grade.setActive(request.getActive());
        }

        Grade savedGrade = gradeRepository.save(grade);
        log.info("Updated grade: {}", savedGrade.getName());
        return convertToGradeDto(savedGrade);
    }

    public GradeDto toggleGradeStatus(Long id) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        grade.setActive(!grade.getActive());
        Grade savedGrade = gradeRepository.save(grade);
        log.info("Toggled grade status: {} -> {}", grade.getName(), savedGrade.getActive());
        return convertToGradeDto(savedGrade);
    }

    public DeletionImpactDto getGradeDeletionImpact(Long id) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        long subjectsCount = subjectRepository.countByGradeIdAndSoftDeletedFalse(id);
        long chaptersCount = chapterRepository.countChaptersByGradeIdAndSoftDeletedFalse(id);
        long topicsCount = topicRepository.countTopicsByGradeIdAndSoftDeletedFalse(id);
        long notesCount = topicNoteRepository.countTopicNotesByGradeIdAndSoftDeletedFalse(id);

        return DeletionImpactDto.builder()
                .level("GRADE")
                .gradeId(id)
                .gradeName(grade.getDisplayName())
                .subjectsCount(subjectsCount)
                .chaptersCount(chaptersCount)
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .totalImpact(subjectsCount + chaptersCount + topicsCount + notesCount)
                .warnings(Arrays.asList(
                    "All subjects, chapters, topics, and notes under this grade will be affected",
                    "Pricing rules associated with this grade will be deleted"
                ))
                .build();
    }

    public void deleteGrade(Long id, boolean force) {
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

        if (force) {
            // Force delete: cascade delete all children
            log.info("Force deleting grade: {} and all children", grade.getName());
            
            // Delete in reverse order: TopicNotes → Topics → Chapters → Subjects → Grade
            List<TopicNote> notes = topicNoteRepository.findByGradeIdAndSoftDeletedFalse(id);
            topicNoteRepository.deleteAll(notes);
            
            List<Topic> topics = topicRepository.findByGradeIdAndSoftDeletedFalse(id);
            topicRepository.deleteAll(topics);
            
            List<Chapter> chapters = chapterRepository.findByGradeIdAndSoftDeletedFalse(id);
            chapterRepository.deleteAll(chapters);
            
            List<Subject> subjects = subjectRepository.findByGradeIdAndSoftDeletedFalse(id);
            subjectRepository.deleteAll(subjects);
            
            gradeRepository.delete(grade);
            log.info("Force deleted grade: {} and all children", grade.getName());
        } else {
            // Soft delete
            grade.setSoftDeleted(true);
            grade.setActive(false);
            gradeRepository.save(grade);
            log.info("Soft deleted grade: {}", grade.getName());
        }
    }

    // ============ SUBJECTS SERVICE ============

    @Transactional(readOnly = true)
    public Page<SubjectDto> getSubjects(Pageable pageable, String search, Boolean active, Long boardId, Long gradeId) {
        Specification<Subject> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.equal(root.get("softDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (boardId != null) {
                predicates.add(cb.equal(root.get("boardId"), boardId));
            }

            if (gradeId != null) {
                predicates.add(cb.equal(root.get("gradeId"), gradeId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Subject> subjects = subjectRepository.findAll(spec, pageable);
        return subjects.map(this::convertToSubjectDto);
    }

    public SubjectDto createSubject(CreateSubjectRequest request) {
        // Validate board and grade exist
        Board board = boardRepository.findByIdAndSoftDeletedFalse(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + request.getBoardId()));
        
        Grade grade = gradeRepository.findByIdAndSoftDeletedFalse(request.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + request.getGradeId()));

        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setBoardId(request.getBoardId());
        subject.setGradeId(request.getGradeId());
        subject.setActive(request.getActive() != null ? request.getActive() : true);
        subject.setSoftDeleted(false);

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Created subject: {} for grade: {}", savedSubject.getName(), grade.getName());
        return convertToSubjectDto(savedSubject);
    }

    public SubjectDto updateSubject(Long id, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        subject.setName(request.getName());
        if (request.getActive() != null) {
            subject.setActive(request.getActive());
        }

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Updated subject: {}", savedSubject.getName());
        return convertToSubjectDto(savedSubject);
    }

    public SubjectDto toggleSubjectStatus(Long id) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        subject.setActive(!subject.getActive());
        Subject savedSubject = subjectRepository.save(subject);
        log.info("Toggled subject status: {} -> {}", subject.getName(), savedSubject.getActive());
        return convertToSubjectDto(savedSubject);
    }

    public DeletionImpactDto getSubjectDeletionImpact(Long id) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        long chaptersCount = chapterRepository.countBySubjectIdAndSoftDeletedFalse(id);
        long topicsCount = topicRepository.countTopicsBySubjectIdAndSoftDeletedFalse(id);
        long notesCount = topicNoteRepository.countTopicNotesBySubjectIdAndSoftDeletedFalse(id);

        return DeletionImpactDto.builder()
                .level("SUBJECT")
                .subjectId(id)
                .subjectName(subject.getName())
                .chaptersCount(chaptersCount)
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .totalImpact(chaptersCount + topicsCount + notesCount)
                .warnings(Arrays.asList(
                    "All chapters, topics, and notes under this subject will be affected",
                    "Pricing rules associated with this subject will be deleted"
                ))
                .build();
    }

    public void deleteSubject(Long id, boolean force) {
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (force) {
            // Force delete: cascade delete all children
            log.info("Force deleting subject: {} and all children", subject.getName());
            
            // Delete in reverse order: TopicNotes → Topics → Chapters → Subject
            List<TopicNote> notes = topicNoteRepository.findBySubjectIdAndSoftDeletedFalse(id);
            topicNoteRepository.deleteAll(notes);
            
            List<Topic> topics = topicRepository.findBySubjectIdAndSoftDeletedFalse(id);
            topicRepository.deleteAll(topics);
            
            List<Chapter> chapters = chapterRepository.findBySubjectIdAndSoftDeletedFalse(id);
            chapterRepository.deleteAll(chapters);
            
            subjectRepository.delete(subject);
            log.info("Force deleted subject: {} and all children", subject.getName());
        } else {
            // Soft delete
            subject.setSoftDeleted(true);
            subject.setActive(false);
            subjectRepository.save(subject);
            log.info("Soft deleted subject: {}", subject.getName());
        }
    }

    // ============ CHAPTERS SERVICE ============

    @Transactional(readOnly = true)
    public Page<ChapterDto> getChapters(Pageable pageable, String search, Boolean active, Long boardId, Long gradeId, Long subjectId) {
        Specification<Chapter> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.equal(root.get("softDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (boardId != null) {
                predicates.add(cb.equal(root.get("boardId"), boardId));
            }

            if (gradeId != null) {
                predicates.add(cb.equal(root.get("gradeId"), gradeId));
            }

            if (subjectId != null) {
                predicates.add(cb.equal(root.get("subjectId"), subjectId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Chapter> chapters = chapterRepository.findAll(spec, pageable);
        return chapters.map(this::convertToChapterDto);
    }

    public ChapterDto createChapter(CreateChapterRequest request) {
        // Validate subject exists
        Subject subject = subjectRepository.findByIdAndSoftDeletedFalse(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        Chapter chapter = new Chapter();
        chapter.setName(request.getName());
        chapter.setSubject(subject);
        chapter.setSubjectId(request.getSubjectId());
        chapter.setBoardId(subject.getBoardId());
        chapter.setGradeId(subject.getGradeId());
        chapter.setActive(request.getActive() != null ? request.getActive() : true);
        chapter.setSoftDeleted(false);

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Created chapter: {} for subject: {}", savedChapter.getName(), subject.getName());
        return convertToChapterDto(savedChapter);
    }

    public ChapterDto updateChapter(Long id, UpdateChapterRequest request) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        chapter.setName(request.getName());
        if (request.getActive() != null) {
            chapter.setActive(request.getActive());
        }

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Updated chapter: {}", savedChapter.getName());
        return convertToChapterDto(savedChapter);
    }

    public ChapterDto toggleChapterStatus(Long id) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        chapter.setActive(!chapter.getActive());
        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Toggled chapter status: {} -> {}", chapter.getName(), savedChapter.getActive());
        return convertToChapterDto(savedChapter);
    }

    public DeletionImpactDto getChapterDeletionImpact(Long id) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        long topicsCount = topicRepository.countByChapterIdAndSoftDeletedFalse(id);
        long notesCount = topicNoteRepository.countTopicNotesByChapterIdAndSoftDeletedFalse(id);

        return DeletionImpactDto.builder()
                .level("CHAPTER")
                .chapterId(id)
                .chapterName(chapter.getName())
                .topicsCount(topicsCount)
                .notesCount(notesCount)
                .totalImpact(topicsCount + notesCount)
                .warnings(Arrays.asList(
                    "All topics and notes under this chapter will be affected",
                    "Pricing rules associated with this chapter will be deleted"
                ))
                .build();
    }

    public void deleteChapter(Long id, boolean force) {
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        if (force) {
            // Force delete: cascade delete all children
            log.info("Force deleting chapter: {} and all children", chapter.getName());
            
            // Delete in reverse order: TopicNotes → Topics → Chapter
            List<TopicNote> notes = topicNoteRepository.findByChapterIdAndSoftDeletedFalse(id);
            topicNoteRepository.deleteAll(notes);
            
            List<Topic> topics = topicRepository.findByChapterIdAndSoftDeletedFalse(id);
            topicRepository.deleteAll(topics);
            
            chapterRepository.delete(chapter);
            log.info("Force deleted chapter: {} and all children", chapter.getName());
        } else {
            // Soft delete
            chapter.setSoftDeleted(true);
            chapter.setActive(false);
            chapterRepository.save(chapter);
            log.info("Soft deleted chapter: {}", chapter.getName());
        }
    }

    // ============ TOPICS SERVICE ============

    @Transactional(readOnly = true)
    public Page<TopicDto> getTopics(Pageable pageable, String search, Boolean active, Long boardId, Long gradeId, Long subjectId, Long chapterId) {
        Specification<Topic> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.equal(root.get("softDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchTerm);
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), searchTerm);
                predicates.add(cb.or(titlePredicate, descriptionPredicate));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (boardId != null) {
                predicates.add(cb.equal(root.get("boardId"), boardId));
            }

            if (gradeId != null) {
                predicates.add(cb.equal(root.get("gradeId"), gradeId));
            }

            if (subjectId != null) {
                predicates.add(cb.equal(root.get("subjectId"), subjectId));
            }

            if (chapterId != null) {
                predicates.add(cb.equal(root.get("chapterId"), chapterId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Topic> topics = topicRepository.findAll(spec, pageable);
        return topics.map(this::convertToTopicDto);
    }

    public TopicDto createTopic(CreateTopicRequest request) {
        // Validate chapter exists
        Chapter chapter = chapterRepository.findByIdAndSoftDeletedFalse(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + request.getChapterId()));

        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setSummary(request.getSummary());
        topic.setExpectedTimeMins(request.getExpectedTimeMins());
        topic.setChapter(chapter);
        topic.setChapterId(request.getChapterId());
        topic.setBoardId(chapter.getBoardId());
        topic.setGradeId(chapter.getGradeId());
        topic.setSubjectId(chapter.getSubjectId());
        topic.setActive(request.getActive() != null ? request.getActive() : true);
        topic.setSoftDeleted(false);

        // Auto-generate topic code
        String generatedCode = generateTopicCode(request.getChapterId());
        topic.setCode(generatedCode);

        Topic savedTopic = topicRepository.save(topic);
        log.info("Created topic: {} with code: {} for chapter: {}", savedTopic.getTitle(), generatedCode, chapter.getName());
        return convertToTopicDto(savedTopic);
    }

    public TopicDto updateTopic(Long id, UpdateTopicRequest request) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        topic.setTitle(request.getTitle());
        topic.setDescription(request.getDescription());
        topic.setSummary(request.getSummary());
        topic.setExpectedTimeMins(request.getExpectedTimeMins());
        if (request.getActive() != null) {
            topic.setActive(request.getActive());
        }

        Topic savedTopic = topicRepository.save(topic);
        log.info("Updated topic: {}", savedTopic.getTitle());
        return convertToTopicDto(savedTopic);
    }

    public TopicDto toggleTopicStatus(Long id) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        topic.setActive(!topic.getActive());
        Topic savedTopic = topicRepository.save(topic);
        log.info("Toggled topic status: {} -> {}", topic.getTitle(), savedTopic.getActive());
        return convertToTopicDto(savedTopic);
    }

    public DeletionImpactDto getTopicDeletionImpact(Long id) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        long notesCount = topicNoteRepository.countByTopicIdAndSoftDeletedFalse(id);

        return DeletionImpactDto.builder()
                .level("TOPIC")
                .topicId(id)
                .topicName(topic.getTitle())
                .notesCount(notesCount)
                .totalImpact(notesCount)
                .warnings(Arrays.asList(
                    "All notes under this topic will be affected",
                    "Pricing rules associated with this topic will be deleted"
                ))
                .build();
    }

    public void deleteTopic(Long id, boolean force) {
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        if (force) {
            // Force delete: cascade delete all children
            log.info("Force deleting topic: {} and all children", topic.getTitle());
            
            // Delete in reverse order: TopicNotes → Topic
            List<TopicNote> notes = topicNoteRepository.findByTopicIdAndSoftDeletedFalse(id);
            topicNoteRepository.deleteAll(notes);
            
            topicRepository.delete(topic);
            log.info("Force deleted topic: {} and all children", topic.getTitle());
        } else {
            // Soft delete
            topic.setSoftDeleted(true);
            topic.setActive(false);
            topicRepository.save(topic);
            log.info("Soft deleted topic: {}", topic.getTitle());
        }
    }

    // ============ TOPIC NOTES SERVICE ============

    @Transactional(readOnly = true)
    public Page<TopicNoteDto> getTopicNotes(Pageable pageable, String search, Boolean active, Long boardId, Long gradeId, Long subjectId, Long chapterId, Long topicId) {
        Specification<TopicNote> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.equal(root.get("softDeleted"), false));

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchTerm);
                Predicate contentPredicate = cb.like(cb.lower(root.get("content")), searchTerm);
                predicates.add(cb.or(titlePredicate, contentPredicate));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (boardId != null) {
                predicates.add(cb.equal(root.get("boardId"), boardId));
            }

            if (gradeId != null) {
                predicates.add(cb.equal(root.get("gradeId"), gradeId));
            }

            if (subjectId != null) {
                predicates.add(cb.equal(root.get("subjectId"), subjectId));
            }

            if (chapterId != null) {
                predicates.add(cb.equal(root.get("chapterId"), chapterId));
            }

            if (topicId != null) {
                predicates.add(cb.equal(root.get("topicId"), topicId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TopicNote> notes = topicNoteRepository.findAll(spec, pageable);
        return notes.map(this::convertToTopicNoteDto);
    }

    public TopicNoteDto createTopicNote(CreateTopicNoteRequest request) {
        // Validate topic exists
        Topic topic = topicRepository.findByIdAndSoftDeletedFalse(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + request.getTopicId()));

        TopicNote note = new TopicNote();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setAttachments(request.getAttachments());
        note.setTopic(topic);
        note.setTopicId(request.getTopicId());
        note.setBoardId(topic.getBoardId());
        note.setGradeId(topic.getGradeId());
        note.setSubjectId(topic.getSubjectId());
        note.setChapterId(topic.getChapterId());
        note.setActive(request.getActive() != null ? request.getActive() : true);
        note.setSoftDeleted(false);

        TopicNote savedNote = topicNoteRepository.save(note);
        log.info("Created topic note: {} for topic: {}", savedNote.getTitle(), topic.getTitle());
        return convertToTopicNoteDto(savedNote);
    }

    public TopicNoteDto updateTopicNote(Long id, UpdateTopicNoteRequest request) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic note not found with id: " + id));

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setAttachments(request.getAttachments());
        if (request.getActive() != null) {
            note.setActive(request.getActive());
        }

        TopicNote savedNote = topicNoteRepository.save(note);
        log.info("Updated topic note: {}", savedNote.getTitle());
        return convertToTopicNoteDto(savedNote);
    }

    public TopicNoteDto toggleTopicNoteStatus(Long id) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic note not found with id: " + id));

        note.setActive(!note.getActive());
        TopicNote savedNote = topicNoteRepository.save(note);
        log.info("Toggled topic note status: {} -> {}", note.getTitle(), savedNote.getActive());
        return convertToTopicNoteDto(savedNote);
    }

    public void deleteTopicNote(Long id) {
        TopicNote note = topicNoteRepository.findByIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic note not found with id: " + id));

        // Topic notes are leaf nodes, so we can safely soft delete
        note.setSoftDeleted(true);
        note.setActive(false);
        topicNoteRepository.save(note);
        log.info("Soft deleted topic note: {}", note.getTitle());
    }

    // ============ UTILITY METHODS ============

    private String generateTopicCode(Long chapterId) {
        // Find the highest existing code for this chapter
        Optional<Topic> lastTopic = topicRepository.findTopByChapterIdAndSoftDeletedFalseOrderByCodeDesc(chapterId);
        
        int nextNumber = 1;
        if (lastTopic.isPresent() && lastTopic.get().getCode() != null) {
            try {
                String lastCode = lastTopic.get().getCode();
                if (lastCode.startsWith("TP_")) {
                    String numberPart = lastCode.substring(3);
                    nextNumber = Integer.parseInt(numberPart) + 1;
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse topic code: {}", lastTopic.get().getCode());
            }
        }

        String code = String.format("TP_%03d", nextNumber);
        
        // Ensure uniqueness by checking if code already exists
        while (topicRepository.existsByCodeAndSoftDeletedFalse(code)) {
            nextNumber++;
            code = String.format("TP_%03d", nextNumber);
        }

        return code;
    }

    // ============ DTO CONVERTERS ============

    private BoardDto convertToBoardDto(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .name(board.getName())
                .active(board.getActive())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    private GradeDto convertToGradeDto(Grade grade) {
        return GradeDto.builder()
                .id(grade.getId())
                .name(grade.getName())
                .displayName(grade.getDisplayName())
                .boardId(grade.getBoardId())
                .active(grade.getActive())
                .createdAt(grade.getCreatedAt())
                .updatedAt(grade.getUpdatedAt())
                .build();
    }

    private SubjectDto convertToSubjectDto(Subject subject) {
        return SubjectDto.builder()
                .id(subject.getId())
                .name(subject.getName())
                .boardId(subject.getBoardId())
                .gradeId(subject.getGradeId())
                .active(subject.getActive())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }

    private ChapterDto convertToChapterDto(Chapter chapter) {
        return ChapterDto.builder()
                .id(chapter.getId())
                .name(chapter.getName())
                .subjectId(chapter.getSubjectId())
                .boardId(chapter.getBoardId())
                .gradeId(chapter.getGradeId())
                .active(chapter.getActive())
                .createdAt(chapter.getCreatedAt())
                .updatedAt(chapter.getUpdatedAt())
                .build();
    }

    private TopicDto convertToTopicDto(Topic topic) {
        return TopicDto.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .code(topic.getCode())
                .description(topic.getDescription())
                .summary(topic.getSummary())
                .expectedTimeMins(topic.getExpectedTimeMins())
                .chapterId(topic.getChapterId())
                .boardId(topic.getBoardId())
                .gradeId(topic.getGradeId())
                .subjectId(topic.getSubjectId())
                .active(topic.getActive())
                .createdAt(topic.getCreatedAt())
                .updatedAt(topic.getUpdatedAt())
                .build();
    }

    private TopicNoteDto convertToTopicNoteDto(TopicNote note) {
        return TopicNoteDto.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .attachments(note.getAttachments())
                .topicId(note.getTopicId())
                .boardId(note.getBoardId())
                .gradeId(note.getGradeId())
                .subjectId(note.getSubjectId())
                .chapterId(note.getChapterId())
                .active(note.getActive())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}

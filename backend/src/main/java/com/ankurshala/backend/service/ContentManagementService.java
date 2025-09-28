package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.content.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContentManagementService {

    private final BoardRepository boardRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final TopicNoteRepository topicNoteRepository;
    private final TopicLinkRepository topicLinkRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CourseContentRepository courseContentRepository;

    // ============ BOARDS SERVICE ============

    @Transactional(readOnly = true)
    public Page<BoardDto> getBoards(Pageable pageable, String search, Boolean active) {
        Specification<Board> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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
        board.setActive(request.getActive());
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());

        Board savedBoard = boardRepository.save(board);
        log.info("Created board: {}", savedBoard.getName());
        return convertToBoardDto(savedBoard);
    }

    public BoardDto updateBoard(Long id, UpdateBoardRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        board.setName(request.getName());
        if (request.getActive() != null) {
            board.setActive(request.getActive());
        }
        board.setUpdatedAt(LocalDateTime.now());

        Board savedBoard = boardRepository.save(board);
        log.info("Updated board: {}", savedBoard.getName());
        return convertToBoardDto(savedBoard);
    }

    public BoardDto updateBoardStatus(Long id, Boolean active) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        board.setActive(active);
        board.setUpdatedAt(LocalDateTime.now());

        Board savedBoard = boardRepository.save(board);
        log.info("Updated board status: {} -> {}", savedBoard.getName(), active);
        return convertToBoardDto(savedBoard);
    }

    public Map<String, Object> getBoardDeletionImpact(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        Map<String, Object> impact = new HashMap<>();
        impact.put("boardName", board.getName());
        impact.put("boardId", board.getId());
        
        // Count pricing rules associated with this board
        long pricingRulesCount = pricingRuleRepository.countByBoardId(id);
        impact.put("pricingRulesCount", pricingRulesCount);
        
        // Count student profiles with this educational board
        long studentProfilesCount = 0;
        try {
            EducationalBoard educationalBoard = EducationalBoard.valueOf(board.getName().toUpperCase().replace(" ", "_"));
            studentProfilesCount = studentProfileRepository.countByEducationalBoard(educationalBoard);
        } catch (IllegalArgumentException e) {
            // Board name doesn't match any enum value, so no student profiles are affected
            log.debug("Board name '{}' doesn't match EducationalBoard enum values", board.getName());
        }
        impact.put("studentProfilesCount", studentProfilesCount);
        
        // Count course content with this educational board
        long courseContentCount = 0;
        try {
            EducationalBoard educationalBoard = EducationalBoard.valueOf(board.getName().toUpperCase().replace(" ", "_"));
            courseContentCount = courseContentRepository.countByEducationalBoard(educationalBoard);
        } catch (IllegalArgumentException e) {
            // Board name doesn't match any enum value, so no course content is affected
            log.debug("Board name '{}' doesn't match EducationalBoard enum values for course content", board.getName());
        }
        impact.put("courseContentCount", courseContentCount);
        
        // Calculate total impact
        long totalImpact = pricingRulesCount + studentProfilesCount + courseContentCount;
        impact.put("totalImpact", totalImpact);
        
        // Determine if deletion is safe
        boolean canDelete = totalImpact == 0;
        impact.put("canDelete", canDelete);
        
        // Generate warning message
        List<String> warnings = new ArrayList<>();
        if (pricingRulesCount > 0) {
            warnings.add(pricingRulesCount + " pricing rule(s) will be affected");
        }
        if (studentProfilesCount > 0) {
            warnings.add(studentProfilesCount + " student profile(s) will be affected");
        }
        if (courseContentCount > 0) {
            warnings.add(courseContentCount + " course content record(s) will be affected");
        }
        impact.put("warnings", warnings);
        
        return impact;
    }

    public Map<String, Object> deleteBoard(Long id, boolean force) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        Map<String, Object> result = new HashMap<>();
        
        if (force) {
            // Hard delete - remove all associated data
            // Delete pricing rules first
            pricingRuleRepository.deleteByBoardId(id);
            
            // Update student profiles to remove board reference
            try {
                EducationalBoard educationalBoard = EducationalBoard.valueOf(board.getName().toUpperCase().replace(" ", "_"));
                studentProfileRepository.updateEducationalBoardToNull(educationalBoard);
            } catch (IllegalArgumentException e) {
                // Board name doesn't match any enum value, so no student profiles need updating
                log.debug("Board name '{}' doesn't match EducationalBoard enum values, skipping student profile updates", board.getName());
            }
            
            // Update course content to remove board reference
            try {
                EducationalBoard educationalBoard = EducationalBoard.valueOf(board.getName().toUpperCase().replace(" ", "_"));
                courseContentRepository.updateEducationalBoardToNull(educationalBoard);
            } catch (IllegalArgumentException e) {
                // Board name doesn't match any enum value, so no course content needs updating
                log.debug("Board name '{}' doesn't match EducationalBoard enum values, skipping course content updates", board.getName());
            }
            
            // Finally delete the board
            boardRepository.delete(board);
            result.put("hardDeleted", true);
            log.info("Hard deleted board: {} and all associated data", board.getName());
        } else {
            board.setActive(false);
            board.setUpdatedAt(LocalDateTime.now());
            boardRepository.save(board);
            result.put("softDeleted", true);
            log.info("Soft deleted board: {}", board.getName());
        }

        return result;
    }

    // ============ SUBJECTS SERVICE ============

    @Transactional(readOnly = true)
    public Page<SubjectDto> getSubjects(Pageable pageable, String search, Boolean active) {
        Specification<Subject> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Subject> subjects = subjectRepository.findAll(spec, pageable);
        return subjects.map(this::convertToSubjectDto);
    }

    public SubjectDto createSubject(CreateSubjectRequest request) {
        // Check for duplicate subject name within the same board
        Optional<Subject> existingSubject = subjectRepository.findByBoardIdAndName(request.getBoardId(), request.getName());
        if (existingSubject.isPresent()) {
            throw new IllegalArgumentException("Subject with name '" + request.getName() + "' already exists for this board");
        }
        
        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setBoardId(request.getBoardId());
        subject.setActive(request.getActive());
        subject.setCreatedAt(LocalDateTime.now());
        subject.setUpdatedAt(LocalDateTime.now());

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Created subject: {} for board ID: {}", savedSubject.getName(), savedSubject.getBoardId());
        return convertToSubjectDto(savedSubject);
    }

    public SubjectDto updateSubject(Long id, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        subject.setName(request.getName());
        if (request.getActive() != null) {
            subject.setActive(request.getActive());
        }
        subject.setUpdatedAt(LocalDateTime.now());

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Updated subject: {}", savedSubject.getName());
        return convertToSubjectDto(savedSubject);
    }

    public SubjectDto updateSubjectStatus(Long id, Boolean active) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        subject.setActive(active);
        subject.setUpdatedAt(LocalDateTime.now());

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Updated subject status: {} -> {}", savedSubject.getName(), active);
        return convertToSubjectDto(savedSubject);
    }

    public Map<String, Object> getSubjectDeletionImpact(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        Map<String, Object> impact = new HashMap<>();
        impact.put("subjectName", subject.getName());
        impact.put("subjectId", subject.getId());
        
        // Count chapters associated with this subject
        long chaptersCount = subjectRepository.countChaptersBySubjectId(id);
        impact.put("chaptersCount", chaptersCount);
        
        // Calculate total impact
        long totalImpact = chaptersCount;
        impact.put("totalImpact", totalImpact);
        
        // Subject can be deleted even with chapters (cascade delete)
        boolean canDelete = true;
        impact.put("canDelete", canDelete);
        
        List<String> warnings = new ArrayList<>();
        if (chaptersCount > 0) {
            warnings.add(chaptersCount + " chapter(s) will be deleted");
            warnings.add("All topics and notes in these chapters will also be deleted");
        }
        impact.put("warnings", warnings);
        
        return impact;
    }

    public Map<String, Object> getChapterDeletionImpact(Long id) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        Map<String, Object> impact = new HashMap<>();
        impact.put("chapterName", chapter.getName());
        impact.put("chapterId", chapter.getId());
        
        // Count topics associated with this chapter
        long topicsCount = chapterRepository.countTopicsByChapterId(id);
        impact.put("topicsCount", topicsCount);
        
        // Count notes associated with topics in this chapter
        long notesCount = chapterRepository.countNotesByChapterId(id);
        impact.put("notesCount", notesCount);
        
        // Calculate total impact
        long totalImpact = topicsCount + notesCount;
        impact.put("totalImpact", totalImpact);
        
        // Chapter can be deleted even if topics exist (cascade delete)
        boolean canDelete = true;
        impact.put("canDelete", canDelete);
        
        List<String> warnings = new ArrayList<>();
        if (topicsCount > 0) {
            warnings.add(topicsCount + " topic(s) will be deleted");
        }
        if (notesCount > 0) {
            warnings.add(notesCount + " note(s) will be deleted");
        }
        impact.put("warnings", warnings);
        
        return impact;
    }

    public Map<String, Object> deleteSubject(Long id, boolean force) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        Map<String, Object> result = new HashMap<>();
        
        if (force) {
            // Cascade delete: First delete all notes, then topics, then chapters, then subject
            // Get all chapters for this subject
            List<Chapter> chapters = chapterRepository.findBySubjectIdAndDeletedAtIsNull(id);
            
            for (Chapter chapter : chapters) {
                // Delete all notes associated with topics in this chapter
                topicNoteRepository.deleteByChapterId(chapter.getId());
                
                // Delete all topics in this chapter
                topicRepository.deleteByChapterId(chapter.getId());
            }
            
            // Delete all chapters in this subject
            chapterRepository.deleteBySubjectId(id);
            
            // Finally delete the subject
            subjectRepository.delete(subject);
            result.put("hardDeleted", true);
            log.info("Hard deleted subject: {} and all associated chapters, topics, and notes", subject.getName());
        } else {
            subject.setActive(false);
            subject.setUpdatedAt(LocalDateTime.now());
            subjectRepository.save(subject);
            result.put("softDeleted", true);
            log.info("Soft deleted subject: {}", subject.getName());
        }

        return result;
    }

    // ============ CHAPTERS SERVICE ============

    @Transactional(readOnly = true)
    public Page<ChapterDto> getChapters(Pageable pageable, String search, Boolean active, Long subjectId) {
        Specification<Chapter> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (subjectId != null) {
                predicates.add(cb.equal(root.get("subject").get("id"), subjectId));
            }

            // Only show non-deleted chapters unless explicitly requested
            predicates.add(cb.isNull(root.get("deletedAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Chapter> chapters = chapterRepository.findAll(spec, pageable);
        return chapters.map(this::convertToChapterDto);
    }

    public ChapterDto createChapter(CreateChapterRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        // Validate that the subject belongs to the specified board
        if (!subject.getBoardId().equals(request.getBoardId())) {
            throw new IllegalArgumentException("Subject does not belong to the specified board");
        }

        // Check for duplicate chapter name within the same subject
        Optional<Chapter> existingChapter = chapterRepository.findBySubjectIdAndName(request.getSubjectId(), request.getName());
        if (existingChapter.isPresent()) {
            throw new IllegalArgumentException("Chapter with name '" + request.getName() + "' already exists for this subject");
        }

        Chapter chapter = new Chapter();
        chapter.setName(request.getName());
        chapter.setSubject(subject);
        chapter.setBoardId(request.getBoardId());
        chapter.setActive(request.getActive());
        chapter.setCreatedAt(LocalDateTime.now());
        chapter.setUpdatedAt(LocalDateTime.now());

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Created chapter: {} for subject: {} in board ID: {}", savedChapter.getName(), subject.getName(), request.getBoardId());
        return convertToChapterDto(savedChapter);
    }

    public ChapterDto updateChapter(Long id, UpdateChapterRequest request) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.getSubjectId()));

        chapter.setName(request.getName());
        chapter.setSubject(subject);
        if (request.getActive() != null) {
            chapter.setActive(request.getActive());
        }
        chapter.setUpdatedAt(LocalDateTime.now());

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Updated chapter: {}", savedChapter.getName());
        return convertToChapterDto(savedChapter);
    }

    public ChapterDto updateChapterStatus(Long id, Boolean active) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        chapter.setActive(active);
        chapter.setUpdatedAt(LocalDateTime.now());

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Updated chapter status: {} -> {}", savedChapter.getName(), active);
        return convertToChapterDto(savedChapter);
    }

    public Map<String, Object> deleteChapter(Long id, boolean force) {
        Chapter chapter = chapterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + id));

        Map<String, Object> result = new HashMap<>();
        
        if (force) {
            // Cascade delete: First delete all notes, then topics, then chapter
            // Delete all notes associated with topics in this chapter
            topicNoteRepository.deleteByChapterId(id);
            
            // Delete all topics in this chapter
            topicRepository.deleteByChapterId(id);
            
            // Finally delete the chapter
            chapterRepository.delete(chapter);
            result.put("hardDeleted", true);
            log.info("Hard deleted chapter: {} and all associated topics and notes", chapter.getName());
        } else {
            chapter.setDeletedAt(LocalDateTime.now());
            chapter.setUpdatedAt(LocalDateTime.now());
            chapterRepository.save(chapter);
            result.put("softDeleted", true);
            log.info("Soft deleted chapter: {}", chapter.getName());
        }

        return result;
    }

    // ============ TOPICS SERVICE ============

    @Transactional(readOnly = true)
    public Page<TopicDto> getTopics(Pageable pageable, String search, Boolean active, Long chapterId, Long subjectId) {
        Specification<Topic> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchTerm);
                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), searchTerm);
                Predicate summaryPredicate = cb.like(cb.lower(root.get("summary")), searchTerm);
                predicates.add(cb.or(titlePredicate, descriptionPredicate, summaryPredicate));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (chapterId != null) {
                predicates.add(cb.equal(root.get("chapter").get("id"), chapterId));
            }

            if (subjectId != null) {
                predicates.add(cb.equal(root.get("chapter").get("subject").get("id"), subjectId));
            }

            // Only show non-deleted topics unless explicitly requested
            predicates.add(cb.isNull(root.get("deletedAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Topic> topics = topicRepository.findAll(spec, pageable);
        return topics.map(this::convertToTopicDto);
    }

    public TopicDto createTopic(CreateTopicRequest request) {
        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + request.getChapterId()));

        // Validate that the chapter belongs to the specified subject
        if (!chapter.getSubject().getId().equals(request.getSubjectId())) {
            throw new IllegalArgumentException("Chapter does not belong to the specified subject");
        }

        // Validate that the subject belongs to the specified board
        if (!chapter.getSubject().getBoardId().equals(request.getBoardId())) {
            throw new IllegalArgumentException("Subject does not belong to the specified board");
        }

        // Check for duplicate topic title within the same chapter
        Optional<Topic> existingTopic = topicRepository.findByChapterIdAndTitle(request.getChapterId(), request.getTitle());
        if (existingTopic.isPresent()) {
            throw new IllegalArgumentException("Topic with title '" + request.getTitle() + "' already exists in this chapter");
        }

        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        
        // Generate topic code automatically if not provided
        String topicCode = request.getCode();
        if (topicCode == null || topicCode.trim().isEmpty()) {
            topicCode = generateTopicCode(chapter.getSubject().getBoardId(), request.getSubjectId(), request.getChapterId(), request.getTitle());
        }
        topic.setCode(topicCode);
        
        topic.setDescription(request.getDescription());
        topic.setSummary(request.getSummary());
        topic.setExpectedTimeMins(request.getExpectedTimeMins());
        topic.setChapter(chapter);
        topic.setBoardId(request.getBoardId());
        topic.setSubjectId(request.getSubjectId());
        topic.setActive(request.getActive());
        topic.setCreatedAt(LocalDateTime.now());
        topic.setUpdatedAt(LocalDateTime.now());

        Topic savedTopic = topicRepository.save(topic);
        log.info("Created topic: {} for chapter: {}", savedTopic.getTitle(), chapter.getName());
        return convertToTopicDto(savedTopic);
    }

    public TopicDto updateTopic(Long id, UpdateTopicRequest request) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        Chapter chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + request.getChapterId()));

        // Validate relationships
        if (!chapter.getSubject().getId().equals(request.getSubjectId())) {
            throw new IllegalArgumentException("Chapter does not belong to the specified subject");
        }
        if (!chapter.getSubject().getBoardId().equals(request.getBoardId())) {
            throw new IllegalArgumentException("Subject does not belong to the specified board");
        }

        topic.setTitle(request.getTitle());
        
        // Generate topic code automatically if not provided
        String topicCode = request.getCode();
        if (topicCode == null || topicCode.trim().isEmpty()) {
            topicCode = generateTopicCode(request.getBoardId(), request.getSubjectId(), request.getChapterId(), request.getTitle());
        }
        topic.setCode(topicCode);
        
        topic.setDescription(request.getDescription());
        topic.setSummary(request.getSummary());
        if (request.getExpectedTimeMins() != null) {
            topic.setExpectedTimeMins(request.getExpectedTimeMins());
        }
        topic.setChapter(chapter);
        topic.setBoardId(request.getBoardId());
        topic.setSubjectId(request.getSubjectId());
        if (request.getActive() != null) {
            topic.setActive(request.getActive());
        }
        topic.setUpdatedAt(LocalDateTime.now());

        Topic savedTopic = topicRepository.save(topic);
        log.info("Updated topic: {}", savedTopic.getTitle());
        return convertToTopicDto(savedTopic);
    }

    public TopicDto updateTopicStatus(Long id, Boolean active) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        topic.setActive(active);
        topic.setUpdatedAt(LocalDateTime.now());

        Topic savedTopic = topicRepository.save(topic);
        log.info("Updated topic status: {} -> {}", savedTopic.getTitle(), active);
        return convertToTopicDto(savedTopic);
    }

    public Map<String, Object> deleteTopic(Long id, boolean force) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        Map<String, Object> result = new HashMap<>();
        
        if (force) {
            // Cascade delete: First delete all notes and links, then topic
            topicNoteRepository.deleteByTopicId(id);
            topicLinkRepository.deleteByTopicId(id);
            topicRepository.delete(topic);
            result.put("hardDeleted", true);
            log.info("Hard deleted topic: {} and all associated notes and links", topic.getTitle());
        } else {
            topic.setDeletedAt(LocalDateTime.now());
            topic.setUpdatedAt(LocalDateTime.now());
            topicRepository.save(topic);
            result.put("softDeleted", true);
            log.info("Soft deleted topic: {}", topic.getTitle());
        }

        return result;
    }

    public Map<String, Object> getTopicDeletionImpact(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        Map<String, Object> impact = new HashMap<>();
        impact.put("topicId", id);
        impact.put("topicTitle", topic.getTitle());
        
        // Count associated notes and links
        long notesCount = topicRepository.countNotesByTopicId(id);
        long linksCount = topicRepository.countLinksByTopicId(id);
        
        impact.put("notesCount", notesCount);
        impact.put("linksCount", linksCount);
        impact.put("totalImpact", notesCount + linksCount);
        
        List<String> warnings = new ArrayList<>();
        if (notesCount > 0) {
            warnings.add(notesCount + " note(s) are associated with this topic");
        }
        if (linksCount > 0) {
            warnings.add(linksCount + " link(s) are associated with this topic");
        }
        
        if (notesCount > 0 || linksCount > 0) {
            warnings.add("Topic notes and links will be deleted");
        }
        
        impact.put("warnings", warnings);
        impact.put("canDelete", true); // Topics can always be deleted with confirmation
        
        return impact;
    }

    // ============ TOPIC NOTES SERVICE ============

    @Transactional(readOnly = true)
    public Page<TopicNoteDto> getTopicNotes(Long topicId, Pageable pageable, String search, Boolean active) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

        Specification<TopicNote> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("topic").get("id"), topicId));

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchTerm);
                Predicate contentPredicate = cb.like(cb.lower(root.get("content")), searchTerm);
                predicates.add(cb.or(titlePredicate, contentPredicate));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            // Only show non-deleted notes unless explicitly requested
            predicates.add(cb.isNull(root.get("deletedAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TopicNote> notes = topicNoteRepository.findAll(spec, pageable);
        return notes.map(this::convertToTopicNoteDto);
    }

    @Transactional(readOnly = true)
    public Page<TopicNoteDto> getAllTopicNotes(Pageable pageable, String search, Boolean active, Long topicId) {
        Specification<TopicNote> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (topicId != null) {
                predicates.add(cb.equal(root.get("topic").get("id"), topicId));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchTerm);
                Predicate contentPredicate = cb.like(cb.lower(root.get("content")), searchTerm);
                predicates.add(cb.or(titlePredicate, contentPredicate));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            // Only show non-deleted notes unless explicitly requested
            predicates.add(cb.isNull(root.get("deletedAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TopicNote> notes = topicNoteRepository.findAll(spec, pageable);
        return notes.map(this::convertToTopicNoteDto);
    }

    public TopicNoteDto createTopicNote(Long topicId, CreateTopicNoteRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));

        TopicNote note = new TopicNote();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTopic(topic);
        // Set attachments as string - database column is now TEXT
        note.setAttachments(request.getAttachments());
        note.setActive(request.getActive());
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        TopicNote savedNote = topicNoteRepository.save(note);
        log.info("Created topic note: {} for topic: {}", savedNote.getTitle(), topic.getTitle());
        return convertToTopicNoteDto(savedNote);
    }

    public TopicNoteDto createTopicNoteGeneral(CreateTopicNoteRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + request.getTopicId()));

        TopicNote note = new TopicNote();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTopic(topic);
        // Set attachments as string - database column is now TEXT
        note.setAttachments(request.getAttachments());
        note.setActive(request.getActive());
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        TopicNote savedNote = topicNoteRepository.save(note);
        log.info("Created topic note: {} for topic: {}", savedNote.getTitle(), topic.getTitle());
        return convertToTopicNoteDto(savedNote);
    }

    public TopicNoteDto updateTopicNote(Long id, UpdateTopicNoteRequest request) {
        TopicNote note = topicNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic note not found with id: " + id));

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setAttachments(request.getAttachments());
        if (request.getActive() != null) {
            note.setActive(request.getActive());
        }
        note.setUpdatedAt(LocalDateTime.now());

        TopicNote savedNote = topicNoteRepository.save(note);
        log.info("Updated topic note: {}", savedNote.getTitle());
        return convertToTopicNoteDto(savedNote);
    }

    public TopicNoteDto updateTopicNoteStatus(Long id, Boolean active) {
        TopicNote note = topicNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic note not found with id: " + id));

        note.setActive(active);
        note.setUpdatedAt(LocalDateTime.now());

        TopicNote savedNote = topicNoteRepository.save(note);
        log.info("Updated topic note status: {} -> {}", savedNote.getTitle(), active);
        return convertToTopicNoteDto(savedNote);
    }

    public Map<String, Object> deleteTopicNote(Long id, boolean force) {
        TopicNote note = topicNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic note not found with id: " + id));

        Map<String, Object> result = new HashMap<>();
        
        if (force) {
            topicNoteRepository.delete(note);
            result.put("hardDeleted", true);
            log.info("Hard deleted topic note: {}", note.getTitle());
        } else {
            note.setDeletedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            topicNoteRepository.save(note);
            result.put("softDeleted", true);
            log.info("Soft deleted topic note: {}", note.getTitle());
        }

        return result;
    }

    // ============ CONVERTER METHODS ============

    private BoardDto convertToBoardDto(Board board) {
        return new BoardDto(
                board.getId(),
                board.getName(),
                board.getActive(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }

    private SubjectDto convertToSubjectDto(Subject subject) {
        return new SubjectDto(
                subject.getId(),
                subject.getName(),
                subject.getActive(),
                subject.getBoardId(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }

    private ChapterDto convertToChapterDto(Chapter chapter) {
        return new ChapterDto(
                chapter.getId(),
                chapter.getName(),
                chapter.getSubject().getId(),
                chapter.getSubject().getName(),
                chapter.getBoardId(),
                chapter.getActive(),
                chapter.getDeletedAt(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt()
        );
    }

    private TopicDto convertToTopicDto(Topic topic) {
        return new TopicDto(
                topic.getId(),
                topic.getTitle(),
                topic.getCode(),
                topic.getDescription(),
                topic.getSummary(),
                topic.getExpectedTimeMins(),
                topic.getChapter().getId(),
                topic.getChapter().getName(),
                topic.getChapter().getSubject().getName(),
                topic.getBoardId(),
                topic.getSubjectId(),
                topic.getActive(),
                topic.getDeletedAt(),
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }

    private String generateTopicCode(Long boardId, Long subjectId, Long chapterId, String topicTitle) {
        // Get board, subject, and chapter names for code generation
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + boardId));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found with id: " + chapterId));

        // Generate prefixes
        String boardPrefix = getBoardPrefix(board.getName());
        String subjectPrefix = getSubjectPrefix(subject.getName());
        String chapterPrefix = getChapterPrefix(chapter.getName());
        String topicPrefix = getTopicPrefix(topicTitle);
        
        // Create base code: BOARD_SUBJECT_CHAPTER_TOPIC (max 20 chars total)
        String baseCode = String.format("%s_%s_%s_%s", 
            boardPrefix, subjectPrefix, chapterPrefix, topicPrefix);
        
        // Ensure code is not too long (max 20 characters)
        if (baseCode.length() > 20) {
            baseCode = baseCode.substring(0, 20);
        }
        
        // Ensure uniqueness by checking existing codes
        String uniqueCode = baseCode;
        int counter = 1;
        while (topicRepository.findByCode(uniqueCode).isPresent()) {
            String suffix = "_" + counter;
            if (baseCode.length() + suffix.length() > 20) {
                baseCode = baseCode.substring(0, 20 - suffix.length());
            }
            uniqueCode = baseCode + suffix;
            counter++;
        }
        
        return uniqueCode;
    }
    
    private String getBoardPrefix(String boardName) {
        if (boardName == null) return "BD";
        
        String normalized = boardName.toUpperCase().trim();
        if (normalized.contains("CBSE")) return "CB";
        if (normalized.contains("ICSE")) return "IC";
        if (normalized.contains("STATE")) return "ST";
        if (normalized.contains("IB")) return "IB";
        if (normalized.contains("IGCSE")) return "IG";
        
        // Default: take first 2 characters
        return normalized.length() >= 2 ? normalized.substring(0, 2) : normalized;
    }
    
    private String getSubjectPrefix(String subjectName) {
        if (subjectName == null) return "SUB";
        
        String normalized = subjectName.toUpperCase().trim();
        if (normalized.contains("PHYSICS")) return "PHY";
        if (normalized.contains("CHEMISTRY")) return "CHE";
        if (normalized.contains("MATHEMATICS") || normalized.contains("MATH")) return "MAT";
        if (normalized.contains("BIOLOGY")) return "BIO";
        if (normalized.contains("ENGLISH")) return "ENG";
        if (normalized.contains("HISTORY")) return "HIS";
        if (normalized.contains("GEOGRAPHY")) return "GEO";
        if (normalized.contains("ECONOMICS")) return "ECO";
        if (normalized.contains("POLITICAL") || normalized.contains("POLITICS")) return "POL";
        
        // Default: take first 3 characters
        return normalized.length() >= 3 ? normalized.substring(0, 3) : normalized;
    }
    
    private String getChapterPrefix(String chapterName) {
        if (chapterName == null) return "CH";
        
        String normalized = chapterName.toUpperCase().trim();
        // Extract numbers from chapter name (e.g., "Chapter 1" -> "1")
        String numbers = normalized.replaceAll("[^0-9]", "");
        if (!numbers.isEmpty()) {
            return "CH" + numbers;
        }
        
        // Default: take first 2 characters
        return normalized.length() >= 2 ? normalized.substring(0, 2) : normalized;
    }
    
    private String getTopicPrefix(String topicTitle) {
        if (topicTitle == null) return "TP";
        
        String normalized = topicTitle.toUpperCase().trim();
        // Extract first meaningful word
        String[] words = normalized.split("\\s+");
        if (words.length > 0) {
            String firstWord = words[0];
            // Take first 2 characters of first word
            return firstWord.length() >= 2 ? firstWord.substring(0, 2) : firstWord;
        }
        
        return "TP";
    }

    private TopicNoteDto convertToTopicNoteDto(TopicNote note) {
        return new TopicNoteDto(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getTopic().getId(),
                note.getTopic().getTitle(),
                note.getActive(),
                note.getAttachments(),
                note.getDeletedAt(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    // ============ HIERARCHICAL DATA FOR BROWSE PAGE ============

    @Transactional(readOnly = true)
    public Map<String, Object> getContentTree() {
        Map<String, Object> tree = new HashMap<>();
        
        // Get all boards
        List<Board> boards = boardRepository.findByActiveTrue();
        tree.put("boards", boards.stream().map(this::convertToBoardDto).collect(Collectors.toList()));
        
        // Get all grades (hardcoded for now)
        List<Map<String, Object>> grades = List.of(
            Map.of("id", 1, "name", "9", "displayName", "Grade 9", "active", true),
            Map.of("id", 2, "name", "10", "displayName", "Grade 10", "active", true),
            Map.of("id", 3, "name", "11", "displayName", "Grade 11", "active", true),
            Map.of("id", 4, "name", "12", "displayName", "Grade 12", "active", true)
        );
        tree.put("grades", grades);
        
        // Get all subjects
        List<Subject> subjects = subjectRepository.findByActiveTrue();
        tree.put("subjects", subjects.stream().map(this::convertToSubjectDto).collect(Collectors.toList()));
        
        // Get all chapters
        List<Chapter> chapters = chapterRepository.findByActiveTrue();
        tree.put("chapters", chapters.stream().map(this::convertToChapterDto).collect(Collectors.toList()));
        
        // Get all topics
        List<Topic> topics = topicRepository.findByActiveTrue();
        tree.put("topics", topics.stream().map(this::convertToTopicDto).collect(Collectors.toList()));
        
        return tree;
    }
}
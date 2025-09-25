package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.content.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    public Map<String, Object> deleteBoard(Long id, boolean force) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        Map<String, Object> result = new HashMap<>();
        
        if (force) {
            boardRepository.delete(board);
            result.put("hardDeleted", true);
            log.info("Hard deleted board: {}", board.getName());
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
        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setActive(request.getActive());
        subject.setCreatedAt(LocalDateTime.now());
        subject.setUpdatedAt(LocalDateTime.now());

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Created subject: {}", savedSubject.getName());
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

    public Map<String, Object> deleteSubject(Long id, boolean force) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        Map<String, Object> result = new HashMap<>();
        
        if (force) {
            subjectRepository.delete(subject);
            result.put("hardDeleted", true);
            log.info("Hard deleted subject: {}", subject.getName());
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

        Chapter chapter = new Chapter();
        chapter.setName(request.getName());
        chapter.setSubject(subject);
        chapter.setActive(request.getActive());
        chapter.setCreatedAt(LocalDateTime.now());
        chapter.setUpdatedAt(LocalDateTime.now());

        Chapter savedChapter = chapterRepository.save(chapter);
        log.info("Created chapter: {} for subject: {}", savedChapter.getName(), subject.getName());
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
            chapterRepository.delete(chapter);
            result.put("hardDeleted", true);
            log.info("Hard deleted chapter: {}", chapter.getName());
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

        Topic topic = new Topic();
        topic.setTitle(request.getTitle());
        topic.setCode(request.getCode());
        topic.setDescription(request.getDescription());
        topic.setSummary(request.getSummary());
        topic.setExpectedTimeMins(request.getExpectedTimeMins());
        topic.setChapter(chapter);
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

        topic.setTitle(request.getTitle());
        topic.setCode(request.getCode());
        topic.setDescription(request.getDescription());
        topic.setSummary(request.getSummary());
        if (request.getExpectedTimeMins() != null) {
            topic.setExpectedTimeMins(request.getExpectedTimeMins());
        }
        topic.setChapter(chapter);
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
            topicRepository.delete(topic);
            result.put("hardDeleted", true);
            log.info("Hard deleted topic: {}", topic.getTitle());
        } else {
            topic.setDeletedAt(LocalDateTime.now());
            topic.setUpdatedAt(LocalDateTime.now());
            topicRepository.save(topic);
            result.put("softDeleted", true);
            log.info("Soft deleted topic: {}", topic.getTitle());
        }

        return result;
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
                topic.getActive(),
                topic.getDeletedAt(),
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
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
}
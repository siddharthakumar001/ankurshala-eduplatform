package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.QuizDTO;
import com.ankurshala.backend.dto.student.CompanionDTO.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Session Companion (Live Class Companion) feature.
 * Manages pre/during/post session support content.
 */
@Service
@Transactional
@Slf4j
public class SessionCompanionService {

    @Autowired
    private SessionCompanionRepository companionRepository;

    @Autowired
    private SessionCompanionNoteRepository noteRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalizedQuizService quizService;

    @Autowired
    private AITutorService aiTutorService;

    @Autowired
    private ContentChunkRepository contentChunkRepository;

    // ==================== GET COMPANION ====================

    /**
     * Get or create companion for a booking
     */
    public CompanionResponse getOrCreateCompanion(Long studentId, Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting companion - TraceId: {}, StudentId: {}, BookingId: {}", traceId, studentId, bookingId);

        // Get booking and verify ownership
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getStudentId().equals(studentId)) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN, "NOT_OWNER");
        }

        // Get or create companion
        SessionCompanion companion = companionRepository.findByBookingId(bookingId)
                .orElseGet(() -> createCompanion(booking));

        return mapToCompanionResponse(companion, booking);
    }

    /**
     * Get companion by ID
     */
    @Transactional(readOnly = true)
    public CompanionResponse getCompanionById(Long studentId, Long companionId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting companion by ID - TraceId: {}, StudentId: {}, CompanionId: {}", traceId, studentId, companionId);

        SessionCompanion companion = companionRepository.findById(companionId)
                .orElseThrow(() -> new ResourceNotFoundException("Companion not found: " + companionId));

        if (!companion.getStudentId().equals(studentId)) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN, "NOT_OWNER");
        }

        Booking booking = bookingRepository.findById(companion.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        return mapToCompanionResponse(companion, booking);
    }

    /**
     * List all companions for a student
     */
    @Transactional(readOnly = true)
    public CompanionListResponse getCompanions(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Listing companions - TraceId: {}, StudentId: {}", traceId, studentId);

        List<SessionCompanion> companions = companionRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        CompanionListResponse response = new CompanionListResponse();
        response.setCompanions(companions.stream().map(this::mapToCompanionSummary).collect(Collectors.toList()));

        return response;
    }

    // ==================== PREP (BEFORE SESSION) ====================

    /**
     * Generate pre-session plan and warmup quiz
     */
    public CompanionResponse generatePrep(Long studentId, Long bookingId, GeneratePrepRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Generating prep - TraceId: {}, StudentId: {}, BookingId: {}", traceId, studentId, bookingId);

        SessionCompanion companion = getOrCreateCompanionEntity(studentId, bookingId);

        // Get booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Get topic
        Topic topic = null;
        if (companion.getTopicId() != null) {
            topic = topicRepository.findById(companion.getTopicId()).orElse(null);
        }

        // Generate pre-session plan using AI
        String prepPlan = generatePrepPlan(topic, request.getLanguage());
        companion.setPreSessionPlanMd(prepPlan);

        // Generate warmup quiz if requested
        if (Boolean.TRUE.equals(request.getGenerateWarmupQuiz()) && topic != null) {
            try {
                QuizDTO.GenerateRequest quizRequest = new QuizDTO.GenerateRequest();
                quizRequest.setTopicId(topic.getId());
                quizRequest.setNumQuestions(3);
                quizRequest.setDifficulty("EASY");
                quizRequest.setQuizType("WARMUP");
                quizRequest.setLanguage(request.getLanguage());

                QuizDTO.QuizResponse quiz = quizService.generateQuiz(studentId, quizRequest);
                companion.setWarmupQuizId(quiz.getQuizId());
            } catch (Exception e) {
                log.warn("Failed to generate warmup quiz - TraceId: {}, Error: {}", traceId, e.getMessage());
            }
        }

        companion.markPrepReady();
        SessionCompanion saved = companionRepository.save(companion);

        log.info("Prep generated - TraceId: {}, CompanionId: {}", traceId, saved.getId());
        return mapToCompanionResponse(saved, booking);
    }

    /**
     * Get warmup quiz for a booking
     */
    @Transactional(readOnly = true)
    public WarmupQuizResponse getWarmupQuiz(Long studentId, Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting warmup quiz - TraceId: {}, StudentId: {}, BookingId: {}", traceId, studentId, bookingId);

        SessionCompanion companion = companionRepository.findByBookingIdAndStudentId(bookingId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Companion not found for booking: " + bookingId));

        if (companion.getWarmupQuizId() == null) {
            throw new BusinessException("No warmup quiz available", HttpStatus.NOT_FOUND, "NO_WARMUP_QUIZ");
        }

        QuizDTO.QuizResponse quiz = quizService.getQuiz(companion.getWarmupQuizId());

        WarmupQuizResponse response = new WarmupQuizResponse();
        response.setQuizId(quiz.getQuizId());
        response.setTopicName(quiz.getTopicTitle());
        response.setTotalQuestions(quiz.getTotalQuestions());
        response.setQuestions(quiz.getQuestions().stream().map(q -> {
            WarmupQuestionResponse wq = new WarmupQuestionResponse();
            wq.setId(q.getQuestionId());
            wq.setQuestionText(q.getQuestionText());
            wq.setOptions(q.getOptions() != null 
                    ? q.getOptions().stream().map(QuizDTO.OptionResponse::getText).collect(Collectors.toList())
                    : Collections.emptyList());
            wq.setQuestionType(q.getQuestionType());
            return wq;
        }).collect(Collectors.toList()));

        return response;
    }

    /**
     * Mark warmup quiz as completed
     */
    public void markWarmupCompleted(Long studentId, Long bookingId) {
        SessionCompanion companion = companionRepository.findByBookingIdAndStudentId(bookingId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Companion not found for booking: " + bookingId));

        companion.setWarmupCompleted(true);
        companionRepository.save(companion);
    }

    // ==================== LIVE NOTES (DURING SESSION) ====================

    /**
     * Add a note during live session
     */
    public CompanionNoteResponse addLiveNote(Long studentId, Long bookingId, AddLiveNoteRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Adding live note - TraceId: {}, StudentId: {}, BookingId: {}", traceId, studentId, bookingId);

        SessionCompanion companion = getOrCreateCompanionEntity(studentId, bookingId);

        // Update companion status to LIVE if not already
        if (companion.getStatus() == SessionCompanion.CompanionStatus.CREATED ||
            companion.getStatus() == SessionCompanion.CompanionStatus.PREP_READY) {
            companion.startLive();
        }

        // Parse note type
        SessionCompanionNote.NoteType noteType;
        try {
            noteType = SessionCompanionNote.NoteType.valueOf(request.getNoteType().toUpperCase());
        } catch (IllegalArgumentException e) {
            noteType = SessionCompanionNote.NoteType.NOTE;
        }

        // Create note
        SessionCompanionNote note = new SessionCompanionNote(
                companion.getId(),
                studentId,
                noteType,
                request.getContent()
        );
        note.setTimestampInSession(request.getTimestampInSession());

        // If it's a question, generate AI response
        if (noteType == SessionCompanionNote.NoteType.QUESTION && companion.getTopicId() != null) {
            String aiResponse = generateQuickAnswer(companion.getTopicId(), request.getContent());
            note.setAiResponse(aiResponse);
        }

        SessionCompanionNote savedNote = noteRepository.save(note);

        // Also append to companion's live notes
        companion.appendLiveNote("**" + noteType.name() + "**: " + request.getContent());
        if (noteType == SessionCompanionNote.NoteType.HIGHLIGHT) {
            companion.addHighlight(request.getContent());
        } else if (noteType == SessionCompanionNote.NoteType.QUESTION) {
            companion.addQuestion(request.getContent());
        }
        companionRepository.save(companion);

        log.info("Live note added - TraceId: {}, NoteId: {}", traceId, savedNote.getId());
        return mapToNoteResponse(savedNote);
    }

    /**
     * Get all notes for a session
     */
    @Transactional(readOnly = true)
    public List<CompanionNoteResponse> getSessionNotes(Long studentId, Long bookingId) {
        SessionCompanion companion = companionRepository.findByBookingIdAndStudentId(bookingId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Companion not found for booking: " + bookingId));

        List<SessionCompanionNote> notes = noteRepository.findByCompanionIdOrderByCreatedAtAsc(companion.getId());
        return notes.stream().map(this::mapToNoteResponse).collect(Collectors.toList());
    }

    // ==================== POST-SESSION ====================

    /**
     * Generate post-session summary and homework
     */
    public CompanionResponse generatePost(Long studentId, Long bookingId, GeneratePostRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Generating post - TraceId: {}, StudentId: {}, BookingId: {}", traceId, studentId, bookingId);

        SessionCompanion companion = companionRepository.findByBookingIdAndStudentId(bookingId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Companion not found for booking: " + bookingId));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Get topic
        Topic topic = null;
        if (companion.getTopicId() != null) {
            topic = topicRepository.findById(companion.getTopicId()).orElse(null);
        }

        // Get notes from session
        List<SessionCompanionNote> notes = noteRepository.findByCompanionIdOrderByCreatedAtAsc(companion.getId());

        // Generate post-session summary
        String summary = generatePostSummary(topic, notes, request.getLanguage());
        companion.setPostSessionSummaryMd(summary);

        // Generate homework plan if requested
        if (Boolean.TRUE.equals(request.getGenerateHomework()) && topic != null) {
            String homework = generateHomeworkPlan(topic, notes, request.getLanguage());
            companion.setHomeworkPlanMd(homework);

            // Recommend related topics
            companion.setRecommendedTopics(findRelatedTopicIds(topic));
        }

        companion.markPostReady();
        SessionCompanion saved = companionRepository.save(companion);

        log.info("Post generated - TraceId: {}, CompanionId: {}", traceId, saved.getId());
        return mapToCompanionResponse(saved, booking);
    }

    // ==================== HELPER METHODS ====================

    private SessionCompanion createCompanion(Booking booking) {
        SessionCompanion companion = new SessionCompanion(booking.getId(), booking.getStudentId());
        companion.setTeacherId(booking.getTeacherId());
        companion.setTopicId(booking.getTopicId());

        // Get subject from topic
        if (booking.getTopicId() != null) {
            topicRepository.findById(booking.getTopicId()).ifPresent(topic -> {
                companion.setSubjectId(topic.getSubjectId());
            });
        }

        return companionRepository.save(companion);
    }

    private SessionCompanion getOrCreateCompanionEntity(Long studentId, Long bookingId) {
        // Get booking and verify ownership
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getStudentId().equals(studentId)) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN, "NOT_OWNER");
        }

        return companionRepository.findByBookingId(bookingId)
                .orElseGet(() -> createCompanion(booking));
    }

    private String generatePrepPlan(Topic topic, String language) {
        if (topic == null) {
            return "## Pre-Session Preparation\n\nPrepare for your upcoming session by reviewing your previous notes and questions.";
        }

        StringBuilder plan = new StringBuilder();
        plan.append("## Pre-Session Preparation: ").append(topic.getTitle()).append("\n\n");
        plan.append("### What to Review\n");
        plan.append("- Review key concepts from ").append(topic.getTitle()).append("\n");
        plan.append("- Go through any previous notes on this topic\n");
        plan.append("- Prepare questions you want to ask\n\n");

        plan.append("### Session Objectives\n");
        plan.append("- Clear doubts on fundamental concepts\n");
        plan.append("- Practice problem-solving techniques\n");
        plan.append("- Understand practical applications\n\n");

        plan.append("### Warmup Exercise\n");
        plan.append("Complete the warmup quiz to refresh your memory before the session.\n");

        return plan.toString();
    }

    private String generateQuickAnswer(Long topicId, String question) {
        // Stub AI response - in production, this would call the AI service
        return "This is a quick AI response to help you during the session. " +
               "Your teacher can provide a more detailed explanation.";
    }

    private String generatePostSummary(Topic topic, List<SessionCompanionNote> notes, String language) {
        StringBuilder summary = new StringBuilder();
        summary.append("## Session Summary\n\n");

        if (topic != null) {
            summary.append("**Topic:** ").append(topic.getTitle()).append("\n\n");
        }

        // Key highlights
        List<String> highlights = notes.stream()
                .filter(n -> n.getNoteType() == SessionCompanionNote.NoteType.HIGHLIGHT)
                .map(SessionCompanionNote::getContent)
                .collect(Collectors.toList());

        if (!highlights.isEmpty()) {
            summary.append("### Key Highlights\n");
            highlights.forEach(h -> summary.append("- ").append(h).append("\n"));
            summary.append("\n");
        }

        // Questions discussed
        List<String> questions = notes.stream()
                .filter(n -> n.getNoteType() == SessionCompanionNote.NoteType.QUESTION)
                .map(SessionCompanionNote::getContent)
                .collect(Collectors.toList());

        if (!questions.isEmpty()) {
            summary.append("### Questions Discussed\n");
            questions.forEach(q -> summary.append("- ").append(q).append("\n"));
            summary.append("\n");
        }

        // Action items
        List<String> actionItems = notes.stream()
                .filter(n -> n.getNoteType() == SessionCompanionNote.NoteType.ACTION_ITEM)
                .map(SessionCompanionNote::getContent)
                .collect(Collectors.toList());

        if (!actionItems.isEmpty()) {
            summary.append("### Action Items\n");
            actionItems.forEach(a -> summary.append("- [ ] ").append(a).append("\n"));
            summary.append("\n");
        }

        summary.append("### Next Steps\n");
        summary.append("- Review the concepts covered in this session\n");
        summary.append("- Complete the assigned homework\n");
        summary.append("- Practice with additional exercises\n");

        return summary.toString();
    }

    private String generateHomeworkPlan(Topic topic, List<SessionCompanionNote> notes, String language) {
        StringBuilder homework = new StringBuilder();
        homework.append("## Homework Plan: ").append(topic.getTitle()).append("\n\n");

        homework.append("### Practice Exercises\n");
        homework.append("1. Review the key concepts from the session\n");
        homework.append("2. Complete 5 practice problems on ").append(topic.getTitle()).append("\n");
        homework.append("3. Take a practice quiz to test your understanding\n\n");

        homework.append("### Additional Reading\n");
        homework.append("- NCERT Chapter on ").append(topic.getTitle()).append("\n");
        homework.append("- Practice previous year questions\n\n");

        homework.append("### Due Date\n");
        homework.append("Complete before your next session.\n");

        return homework.toString();
    }

    private List<Long> findRelatedTopicIds(Topic topic) {
        // In production, this would find related topics based on chapter/subject
        return new ArrayList<>();
    }

    private CompanionResponse mapToCompanionResponse(SessionCompanion companion, Booking booking) {
        CompanionResponse response = new CompanionResponse();
        response.setId(companion.getId());
        response.setBookingId(companion.getBookingId());
        response.setStudentId(companion.getStudentId());
        response.setTeacherId(companion.getTeacherId());
        response.setTopicId(companion.getTopicId());
        response.setSubjectId(companion.getSubjectId());

        // Get names
        if (companion.getTeacherId() != null) {
            userRepository.findById(companion.getTeacherId()).ifPresent(teacher -> {
                response.setTeacherName(teacher.getName());
            });
        }
        if (companion.getTopicId() != null) {
            topicRepository.findById(companion.getTopicId()).ifPresent(topic -> {
                response.setTopicName(topic.getTitle());
            });
        }
        if (companion.getSubjectId() != null) {
            subjectRepository.findById(companion.getSubjectId()).ifPresent(subject -> {
                response.setSubjectName(subject.getName());
            });
        }

        // Booking details
        if (booking != null) {
            response.setScheduledStartTime(booking.getStartTs() != null 
                    ? booking.getStartTs().toLocalDateTime() : null);
            response.setScheduledEndTime(booking.getEndTs() != null 
                    ? booking.getEndTs().toLocalDateTime() : null);
            response.setBookingStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        }

        // Pre-session
        response.setPreSessionPlanMd(companion.getPreSessionPlanMd());
        response.setWarmupQuizId(companion.getWarmupQuizId());
        response.setWarmupCompleted(companion.getWarmupCompleted());

        // Live session
        response.setLiveNotesMd(companion.getLiveNotesMd());
        response.setSessionHighlights(companion.getSessionHighlights() != null && !companion.getSessionHighlights().isEmpty()
                ? companion.getSessionHighlights() : null);
        response.setQuestionsAsked(companion.getQuestionsAsked() != null && !companion.getQuestionsAsked().isEmpty()
                ? companion.getQuestionsAsked() : null);

        // Get notes
        List<SessionCompanionNote> notes = noteRepository.findByCompanionIdOrderByCreatedAtAsc(companion.getId());
        response.setNotes(notes.stream().map(this::mapToNoteResponse).collect(Collectors.toList()));

        // Post-session
        response.setPostSessionSummaryMd(companion.getPostSessionSummaryMd());
        response.setHomeworkPlanMd(companion.getHomeworkPlanMd());
        response.setRecommendedTopics(companion.getRecommendedTopics() != null && !companion.getRecommendedTopics().isEmpty()
                ? companion.getRecommendedTopics() : null);

        // Get recommended topic names
        if (companion.getRecommendedTopics() != null && !companion.getRecommendedTopics().isEmpty()) {
            List<String> topicNames = new ArrayList<>();
            for (Long topicId : companion.getRecommendedTopics()) {
                topicRepository.findById(topicId).ifPresent(t -> topicNames.add(t.getTitle()));
            }
            response.setRecommendedTopicNames(topicNames);
        }

        // Status
        response.setStatus(companion.getStatus().name());
        response.setPrepGeneratedAt(companion.getPrepGeneratedAt());
        response.setLiveStartedAt(companion.getLiveStartedAt());
        response.setPostGeneratedAt(companion.getPostGeneratedAt());
        response.setCreatedAt(companion.getCreatedAt());

        return response;
    }

    private CompanionSummary mapToCompanionSummary(SessionCompanion companion) {
        CompanionSummary summary = new CompanionSummary();
        summary.setId(companion.getId());
        summary.setBookingId(companion.getBookingId());
        summary.setStatus(companion.getStatus().name());
        summary.setHasPrepPlan(companion.getPreSessionPlanMd() != null);
        summary.setHasPostSummary(companion.getPostSessionSummaryMd() != null);
        summary.setCreatedAt(companion.getCreatedAt());

        // Get booking details
        bookingRepository.findById(companion.getBookingId()).ifPresent(booking -> {
            summary.setBookingStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
            summary.setScheduledStartTime(booking.getStartTs() != null 
                    ? booking.getStartTs().toLocalDateTime() : null);
        });

        // Get names
        if (companion.getTeacherId() != null) {
            userRepository.findById(companion.getTeacherId()).ifPresent(teacher -> {
                summary.setTeacherName(teacher.getName());
            });
        }
        if (companion.getTopicId() != null) {
            topicRepository.findById(companion.getTopicId()).ifPresent(topic -> {
                summary.setTopicName(topic.getTitle());
            });
        }
        if (companion.getSubjectId() != null) {
            subjectRepository.findById(companion.getSubjectId()).ifPresent(subject -> {
                summary.setSubjectName(subject.getName());
            });
        }

        return summary;
    }

    private CompanionNoteResponse mapToNoteResponse(SessionCompanionNote note) {
        CompanionNoteResponse response = new CompanionNoteResponse();
        response.setId(note.getId());
        response.setNoteType(note.getNoteType().name());
        response.setContent(note.getContent());
        response.setTimestampInSession(note.getTimestampInSession());
        response.setAiResponse(note.getAiResponse());
        response.setIsResolved(note.getIsResolved());
        response.setCreatedAt(note.getCreatedAt());
        return response;
    }
}


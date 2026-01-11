package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AIConfig;
import com.ankurshala.backend.dto.student.FocusDTO;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.entity.StudentFocusSession.SessionStatus;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Focus Mode + Study Sprints feature.
 * Manages focus sessions, check-ins, and AI coaching integration.
 */
@Service
@Slf4j
@Transactional
public class StudentFocusService {

    @Autowired
    private StudentFocusSettingsRepository settingsRepository;

    @Autowired
    private StudentFocusSessionRepository sessionRepository;

    @Autowired
    private SprintCheckinRepository checkinRepository;

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

    // ===================== Settings Management =====================

    /**
     * Get or create focus settings for a student
     */
    public FocusDTO.FocusSettingsResponse getSettings(Long studentId) {
        StudentFocusSettings settings = settingsRepository.findByStudentId(studentId)
                .orElseGet(() -> {
                    StudentFocusSettings newSettings = new StudentFocusSettings(studentId);
                    return settingsRepository.save(newSettings);
                });
        return mapToSettingsResponse(settings);
    }

    /**
     * Update focus settings
     */
    public FocusDTO.FocusSettingsResponse updateSettings(Long studentId, FocusDTO.UpdateFocusSettingsRequest request) {
        StudentFocusSettings settings = settingsRepository.findByStudentId(studentId)
                .orElseGet(() -> new StudentFocusSettings(studentId));

        if (request.getFocusEnabled() != null) {
            settings.setFocusEnabled(request.getFocusEnabled());
        }
        if (request.getDefaultSprintMinutes() != null) {
            settings.setDefaultSprintMinutes(request.getDefaultSprintMinutes());
        }
        if (request.getLanguagePref() != null) {
            settings.setLanguagePref(request.getLanguagePref());
        }
        if (request.getReminderEnabled() != null) {
            settings.setReminderEnabled(request.getReminderEnabled());
        }
        if (request.getReminderBeforeMinutes() != null) {
            settings.setReminderBeforeMinutes(request.getReminderBeforeMinutes());
        }
        if (request.getSoundEnabled() != null) {
            settings.setSoundEnabled(request.getSoundEnabled());
        }

        StudentFocusSettings savedSettings = settingsRepository.save(settings);
        return mapToSettingsResponse(savedSettings);
    }

    // ===================== Session Management =====================

    /**
     * Start a new focus session
     */
    public FocusDTO.SessionResponse startSession(Long studentId, FocusDTO.StartSessionRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Starting focus session - TraceId: {}, StudentId: {}, TopicId: {}", 
                traceId, studentId, request.getTopicId());

        // Check if there's already an active session
        if (sessionRepository.existsByStudentIdAndStatus(studentId, SessionStatus.ACTIVE)) {
            throw new BusinessException("You already have an active focus session. End it before starting a new one.");
        }

        // Validate topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.getTopicId()));

        Long subjectId = topic.getSubjectId();

        // Create the session
        StudentFocusSession session = new StudentFocusSession(studentId, request.getGoalText());
        session.setTopicId(request.getTopicId());
        session.setSubjectId(subjectId);
        session.setSprintMinutes(request.getSprintMinutes());
        session.setLanguage(request.getLanguage());

        // Generate session plan using AI
        Map<String, Object> sessionPlan = generateSessionPlan(topic, request.getGoalText(), 
                request.getSprintMinutes(), request.getLanguage());
        session.setSessionPlanJson(sessionPlan);

        StudentFocusSession savedSession = sessionRepository.save(session);

        // Generate first check-in
        SprintCheckin firstCheckin = generateCheckin(savedSession, 1);
        savedSession.addCheckin(firstCheckin);
        sessionRepository.save(savedSession);

        return mapToSessionResponse(savedSession);
    }

    /**
     * Get session by ID
     */
    @Transactional(readOnly = true)
    public FocusDTO.SessionResponse getSession(Long studentId, Long sessionId) {
        StudentFocusSession session = sessionRepository.findByIdAndStudentId(sessionId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        return mapToSessionResponse(session);
    }

    /**
     * Get active session for student
     */
    @Transactional(readOnly = true)
    public FocusDTO.SessionResponse getActiveSession(Long studentId) {
        return sessionRepository.findByStudentIdAndStatus(studentId, SessionStatus.ACTIVE)
                .map(this::mapToSessionResponse)
                .orElse(null);
    }

    /**
     * Generate next check-in for a session
     */
    public FocusDTO.GenerateCheckinResponse generateNextCheckin(Long studentId, Long sessionId) {
        String traceId = TraceUtil.getTraceId();
        long startTime = System.currentTimeMillis();

        StudentFocusSession session = sessionRepository.findByIdAndStudentId(sessionId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BusinessException("Session is not active");
        }

        int nextStep = session.getCurrentStep() + 1;
        SprintCheckin checkin = generateCheckin(session, nextStep);
        session.addCheckin(checkin);
        session.setCurrentStep(nextStep);
        sessionRepository.save(session);

        long latency = System.currentTimeMillis() - startTime;

        return FocusDTO.GenerateCheckinResponse.builder()
                .checkin(mapToCheckinResponse(checkin))
                .message("Check-in generated for step " + nextStep)
                .latencyMs((int) latency)
                .build();
    }

    /**
     * Submit response to a check-in
     */
    public FocusDTO.SubmitCheckinResponse submitCheckinResponse(Long studentId, Long sessionId, 
            Integer stepNumber, FocusDTO.CheckinRequest request) {
        
        StudentFocusSession session = sessionRepository.findByIdAndStudentId(sessionId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        SprintCheckin checkin = checkinRepository.findByFocusSessionIdAndStepNumber(sessionId, stepNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in not found for step: " + stepNumber));

        // Evaluate the response using AI
        boolean isCorrect = evaluateResponse(checkin, request.getResponseText());
        String feedback = generateFeedback(checkin, request.getResponseText(), isCorrect);

        checkin.recordResponse(request.getResponseText(), isCorrect);
        checkinRepository.save(checkin);

        // Update session stats
        session.setQuestionsAnswered(session.getQuestionsAnswered() + 1);
        if (isCorrect) {
            session.setQuestionsCorrect(session.getQuestionsCorrect() + 1);
        }
        session.setStepsCompleted(session.getStepsCompleted() + 1);

        // Check if session should end (based on time or steps)
        boolean sessionComplete = shouldEndSession(session);
        if (sessionComplete) {
            session.end();
        }

        sessionRepository.save(session);

        FocusDTO.SubmitCheckinResponse.SubmitCheckinResponseBuilder responseBuilder = 
                FocusDTO.SubmitCheckinResponse.builder()
                .checkin(mapToCheckinResponse(checkin))
                .isCorrect(isCorrect)
                .feedback(feedback)
                .sessionComplete(sessionComplete);

        // Generate next check-in if session is not complete
        if (!sessionComplete) {
            SprintCheckin nextCheckin = generateCheckin(session, stepNumber + 1);
            session.addCheckin(nextCheckin);
            session.setCurrentStep(stepNumber + 1);
            sessionRepository.save(session);
            responseBuilder.nextCheckin(mapToCheckinResponse(nextCheckin));
        }

        return responseBuilder.build();
    }

    /**
     * End a focus session
     */
    public FocusDTO.SessionResponse endSession(Long studentId, Long sessionId) {
        StudentFocusSession session = sessionRepository.findByIdAndStudentId(sessionId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.ENDED || session.getStatus() == SessionStatus.ABANDONED) {
            throw new BusinessException("Session is already ended");
        }

        // Calculate total active time
        if (session.getStartedAt() != null) {
            long seconds = java.time.Duration.between(session.getStartedAt(), LocalDateTime.now()).getSeconds();
            session.setTotalActiveSeconds((int) seconds);
        }

        session.end();
        StudentFocusSession savedSession = sessionRepository.save(session);

        log.info("Focus session ended - SessionId: {}, StudentId: {}, Steps: {}", 
                sessionId, studentId, session.getStepsCompleted());

        return mapToSessionResponse(savedSession);
    }

    /**
     * Get session history
     */
    @Transactional(readOnly = true)
    public FocusDTO.SessionsListResponse getSessionHistory(Long studentId, int page, int size) {
        Page<StudentFocusSession> sessions = sessionRepository
                .findByStudentIdOrderByStartedAtDesc(studentId, PageRequest.of(page, size));

        List<FocusDTO.SessionListItem> items = sessions.getContent().stream()
                .map(this::mapToSessionListItem)
                .collect(Collectors.toList());

        return FocusDTO.SessionsListResponse.builder()
                .sessions(items)
                .totalCount(sessions.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Get focus statistics
     */
    @Transactional(readOnly = true)
    public FocusDTO.FocusStatsResponse getStats(Long studentId) {
        long totalSessions = sessionRepository.countByStudentIdAndStatus(studentId, SessionStatus.ENDED);
        Long totalSeconds = sessionRepository.getTotalFocusTimeSeconds(studentId);
        totalSeconds = totalSeconds != null ? totalSeconds : 0L;

        // Get recent sessions for detailed stats
        List<StudentFocusSession> recentSessions = sessionRepository
                .findRecentCompletedSessions(studentId, PageRequest.of(0, 50));

        long totalQuestions = recentSessions.stream()
                .mapToLong(s -> s.getQuestionsAnswered() != null ? s.getQuestionsAnswered() : 0)
                .sum();
        long correctQuestions = recentSessions.stream()
                .mapToLong(s -> s.getQuestionsCorrect() != null ? s.getQuestionsCorrect() : 0)
                .sum();

        double accuracy = totalQuestions > 0 ? (double) correctQuestions / totalQuestions * 100 : 0;

        return FocusDTO.FocusStatsResponse.builder()
                .totalSessions(totalSessions)
                .completedSessions(totalSessions)
                .totalFocusTimeMinutes(totalSeconds / 60)
                .totalQuestionsAnswered(totalQuestions)
                .totalQuestionsCorrect(correctQuestions)
                .averageAccuracy(Math.round(accuracy * 10) / 10.0)
                .build();
    }

    // ===================== AI Integration =====================

    private Map<String, Object> generateSessionPlan(Topic topic, String goal, int sprintMinutes, String language) {
        if (!aiProperties.isAvailable() || chatModel == null) {
            // Return default plan if AI is not available
            return createDefaultSessionPlan(topic.getTitle(), goal, sprintMinutes);
        }

        String langInstruction = "hi".equals(language) ? "Respond in Hindi. " : "Respond in English. ";

        String systemPrompt = """
                You are a study coach helping Indian school students (CBSE board) focus and learn effectively.
                %s
                Create a brief, focused study plan for a %d-minute sprint session.
                Keep responses concise and actionable.
                """.formatted(langInstruction, sprintMinutes);

        String userPrompt = """
                Topic: %s
                Student's Goal: %s
                Duration: %d minutes
                
                Create a simple study plan with 3-4 steps. For each step include:
                - A clear objective
                - Estimated time
                
                Format as JSON: {"overview": "...", "steps": [{"stepNumber": 1, "title": "...", "objective": "...", "estimatedMinutes": 5}]}
                """.formatted(topic.getTitle(), goal, sprintMinutes);

        try {
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.add(new UserMessage(userPrompt));

            ChatResponse response = chatModel.call(new Prompt(messages));
            String content = response.getResult().getOutput().getContent();

            // Parse JSON response (simplified)
            Map<String, Object> plan = new HashMap<>();
            plan.put("overview", "Study plan for: " + goal);
            plan.put("rawPlan", content);
            return plan;

        } catch (Exception e) {
            log.error("Failed to generate session plan: {}", e.getMessage());
            return createDefaultSessionPlan(topic.getTitle(), goal, sprintMinutes);
        }
    }

    private Map<String, Object> createDefaultSessionPlan(String topicTitle, String goal, int sprintMinutes) {
        Map<String, Object> plan = new HashMap<>();
        plan.put("overview", "Focus session on: " + topicTitle);
        plan.put("goal", goal);
        plan.put("duration", sprintMinutes);
        return plan;
    }

    private SprintCheckin generateCheckin(StudentFocusSession session, int stepNumber) {
        Map<String, Object> aiPlan = new HashMap<>();

        if (aiProperties.isAvailable() && chatModel != null) {
            try {
                // Retrieve context from RAG
                ContentChunkService.RetrievalResult retrievalResult = contentChunkService.retrieveForRAG(
                        session.getGoalText(),
                        session.getTopicId(),
                        session.getSubjectId(),
                        session.getLanguage(),
                        3
                );

                String langInstruction = "hi".equals(session.getLanguage()) ? "Respond in Hindi. " : "Respond in English. ";

                String systemPrompt = """
                        You are a study coach for Indian school students (CBSE).
                        %s
                        Create a focused check-in for step %d of a study sprint.
                        Include: brief explanation, one practice question, and keep the student on track.
                        """.formatted(langInstruction, stepNumber);

                String userPrompt = """
                        Topic: %s
                        Goal: %s
                        Step: %d
                        
                        Context from curriculum:
                        %s
                        
                        Generate:
                        1. Brief explanation (2-3 sentences max)
                        2. One quick check question (MCQ preferred)
                        3. Correct answer
                        
                        Keep the student focused on their goal.
                        """.formatted(
                        session.getTopic() != null ? session.getTopic().getTitle() : "Topic",
                        session.getGoalText(),
                        stepNumber,
                        retrievalResult.getContextString()
                );

                List<Message> messages = new ArrayList<>();
                messages.add(new SystemMessage(systemPrompt));
                messages.add(new UserMessage(userPrompt));

                ChatResponse response = chatModel.call(new Prompt(messages));
                String content = response.getResult().getOutput().getContent();

                aiPlan.put("explanation", "Step " + stepNumber + " - Stay focused on: " + session.getGoalText());
                aiPlan.put("question", "What did you learn about " + session.getGoalText() + " so far?");
                aiPlan.put("questionType", "SHORT_ANSWER");
                aiPlan.put("rawContent", content);
                aiPlan.put("grounded", retrievalResult.getRetrievalCount() > 0);

            } catch (Exception e) {
                log.error("Failed to generate check-in: {}", e.getMessage());
                aiPlan = createDefaultCheckinPlan(session, stepNumber);
            }
        } else {
            aiPlan = createDefaultCheckinPlan(session, stepNumber);
        }

        return new SprintCheckin(session, stepNumber, aiPlan);
    }

    private Map<String, Object> createDefaultCheckinPlan(StudentFocusSession session, int stepNumber) {
        Map<String, Object> plan = new HashMap<>();
        plan.put("explanation", "Step " + stepNumber + ": Continue focusing on your goal.");
        plan.put("question", "What progress have you made on: " + session.getGoalText() + "?");
        plan.put("questionType", "SHORT_ANSWER");
        plan.put("suggestedActions", List.of(
                Map.of("actionType", "TAKE_MICRO_QUIZ", "actionLabel", "Take a quick quiz"),
                Map.of("actionType", "GENERATE_NOTES", "actionLabel", "Generate notes")
        ));
        return plan;
    }

    private boolean evaluateResponse(SprintCheckin checkin, String response) {
        // Simple evaluation - in production, use AI for semantic evaluation
        if (response == null || response.trim().isEmpty()) {
            return false;
        }
        // For SHORT_ANSWER, any reasonable response is accepted
        return response.trim().length() >= 10;
    }

    private String generateFeedback(SprintCheckin checkin, String response, boolean isCorrect) {
        if (isCorrect) {
            return "Great work! Keep up the focus. 💪";
        } else {
            return "That's a good start. Try to be more specific. What key concept stands out to you?";
        }
    }

    private boolean shouldEndSession(StudentFocusSession session) {
        // End after a reasonable number of steps or if time is up
        int maxSteps = session.getSprintMinutes() / 5 + 1;  // Roughly one step per 5 minutes
        return session.getStepsCompleted() >= maxSteps;
    }

    // ===================== Mapping Methods =====================

    private FocusDTO.FocusSettingsResponse mapToSettingsResponse(StudentFocusSettings settings) {
        return FocusDTO.FocusSettingsResponse.builder()
                .id(settings.getId())
                .studentId(settings.getStudentId())
                .focusEnabled(settings.getFocusEnabled())
                .defaultSprintMinutes(settings.getDefaultSprintMinutes())
                .languagePref(settings.getLanguagePref())
                .reminderEnabled(settings.getReminderEnabled())
                .reminderBeforeMinutes(settings.getReminderBeforeMinutes())
                .soundEnabled(settings.getSoundEnabled())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private FocusDTO.SessionResponse mapToSessionResponse(StudentFocusSession session) {
        String topicTitle = topicRepository.findById(session.getTopicId())
                .map(Topic::getTitle)
                .orElse("Unknown Topic");

        String subjectName = session.getSubjectId() != null ?
                subjectRepository.findById(session.getSubjectId())
                        .map(Subject::getName)
                        .orElse("Unknown Subject") : null;

        List<FocusDTO.CheckinResponse> checkins = session.getCheckins().stream()
                .map(this::mapToCheckinResponse)
                .collect(Collectors.toList());

        return FocusDTO.SessionResponse.builder()
                .id(session.getId())
                .studentId(session.getStudentId())
                .subjectId(session.getSubjectId())
                .subjectName(subjectName)
                .topicId(session.getTopicId())
                .topicTitle(topicTitle)
                .goalText(session.getGoalText())
                .sprintMinutes(session.getSprintMinutes())
                .language(session.getLanguage())
                .status(session.getStatus())
                .currentStep(session.getCurrentStep())
                .startedAt(session.getStartedAt())
                .pausedAt(session.getPausedAt())
                .endedAt(session.getEndedAt())
                .totalActiveSeconds(session.getTotalActiveSeconds())
                .stepsCompleted(session.getStepsCompleted())
                .questionsAnswered(session.getQuestionsAnswered())
                .questionsCorrect(session.getQuestionsCorrect())
                .sessionPlan(session.getSessionPlanJson())
                .checkins(checkins)
                .createdAt(session.getCreatedAt())
                .build();
    }

    private FocusDTO.SessionListItem mapToSessionListItem(StudentFocusSession session) {
        String topicTitle = topicRepository.findById(session.getTopicId())
                .map(Topic::getTitle)
                .orElse("Unknown Topic");

        String subjectName = session.getSubjectId() != null ?
                subjectRepository.findById(session.getSubjectId())
                        .map(Subject::getName)
                        .orElse(null) : null;

        return FocusDTO.SessionListItem.builder()
                .id(session.getId())
                .topicId(session.getTopicId())
                .topicTitle(topicTitle)
                .subjectName(subjectName)
                .goalText(session.getGoalText())
                .status(session.getStatus())
                .sprintMinutes(session.getSprintMinutes())
                .stepsCompleted(session.getStepsCompleted())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private FocusDTO.CheckinResponse mapToCheckinResponse(SprintCheckin checkin) {
        Map<String, Object> plan = checkin.getAiPlanJson();

        return FocusDTO.CheckinResponse.builder()
                .id(checkin.getId())
                .sessionId(checkin.getFocusSession().getId())
                .stepNumber(checkin.getStepNumber())
                .explanation((String) plan.get("explanation"))
                .question((String) plan.get("question"))
                .questionType((String) plan.getOrDefault("questionType", "SHORT_ANSWER"))
                .options((List<String>) plan.get("options"))
                .correctAnswer((String) plan.get("correctAnswer"))
                .studentResponseText(checkin.getStudentResponseText())
                .responseCorrect(checkin.getResponseCorrect())
                .startedAt(checkin.getStartedAt())
                .respondedAt(checkin.getRespondedAt())
                .timeSpentSeconds(checkin.getTimeSpentSeconds())
                .build();
    }
}


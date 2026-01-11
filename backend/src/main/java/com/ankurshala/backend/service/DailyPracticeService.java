package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.PracticeDTO.*;
import com.ankurshala.backend.dto.ai.QuizDTO;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Daily Practice feature.
 * Handles practice preferences, daily queue generation, and submission.
 */
@Service
@Transactional
@Slf4j
public class DailyPracticeService {

    private static final BigDecimal WEAK_TOPIC_THRESHOLD = new BigDecimal("0.65");
    private static final String DEFAULT_BOARD = "CBSE";

    @Autowired
    private StudentPracticePreferencesRepository preferencesRepository;

    @Autowired
    private DailyPracticeQueueRepository queueRepository;

    @Autowired
    private StudentTopicMasteryRepository masteryRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private PersonalizedQuizService quizService;

    @Autowired
    private NotificationService notificationService;

    // ==================== PREFERENCES ====================

    /**
     * Get or create practice preferences for a student
     */
    public PracticePreferencesResponse getOrCreatePreferences(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting practice preferences - TraceId: {}, StudentId: {}", traceId, studentId);

        StudentPracticePreferences prefs = preferencesRepository.findByStudentId(studentId)
                .orElseGet(() -> {
                    StudentPracticePreferences newPrefs = new StudentPracticePreferences(studentId);
                    newPrefs.setEnabled(true);
                    newPrefs.setDailyQuestionCount(5);
                    newPrefs.setPreferredTimeLocal("08:00");
                    newPrefs.setLanguage("en");
                    newPrefs.setNotificationEnabled(true);
                    return preferencesRepository.save(newPrefs);
                });

        return mapToPreferencesResponse(prefs);
    }

    /**
     * Update practice preferences
     */
    public PracticePreferencesResponse updatePreferences(Long studentId, UpdatePracticePreferencesRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Updating practice preferences - TraceId: {}, StudentId: {}", traceId, studentId);

        StudentPracticePreferences prefs = preferencesRepository.findByStudentId(studentId)
                .orElseGet(() -> new StudentPracticePreferences(studentId));

        if (request.getEnabled() != null) {
            prefs.setEnabled(request.getEnabled());
        }
        if (request.getDailyQuestionCount() != null) {
            prefs.setDailyQuestionCount(request.getDailyQuestionCount());
        }
        if (request.getPreferredTimeLocal() != null) {
            prefs.setPreferredTimeLocal(request.getPreferredTimeLocal());
        }
        if (request.getLanguage() != null) {
            prefs.setLanguage(request.getLanguage());
        }
        if (request.getNotificationEnabled() != null) {
            prefs.setNotificationEnabled(request.getNotificationEnabled());
        }

        StudentPracticePreferences saved = preferencesRepository.save(prefs);
        log.info("Practice preferences updated - TraceId: {}, StudentId: {}", traceId, studentId);

        return mapToPreferencesResponse(saved);
    }

    // ==================== TODAY'S PRACTICE ====================

    /**
     * Get today's practice items for a student
     */
    @Transactional(readOnly = true)
    public TodayPracticeResponse getTodayPractice(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        LocalDate today = LocalDate.now();
        log.info("Getting today's practice - TraceId: {}, StudentId: {}, Date: {}", traceId, studentId, today);

        List<DailyPracticeQueue> items = queueRepository.findByStudentIdAndScheduledForDateOrderByCreatedAtAsc(
                studentId, today);

        TodayPracticeResponse response = new TodayPracticeResponse();
        response.setDate(today);
        response.setTotalItems(items.size());
        response.setCompletedItems((int) items.stream()
                .filter(i -> i.getStatus() == DailyPracticeQueue.PracticeStatus.COMPLETED)
                .count());
        response.setPendingItems((int) items.stream()
                .filter(i -> i.getStatus() == DailyPracticeQueue.PracticeStatus.PENDING)
                .count());
        response.setItems(items.stream().map(this::mapToPracticeItemResponse).collect(Collectors.toList()));

        // Get next recommended topic if all items completed
        if (response.getPendingItems() == 0 && !items.isEmpty()) {
            findNextRecommendedTopic(studentId).ifPresent(topic -> {
                response.setNextRecommendedTopicId(topic.getId());
                response.setNextRecommendedTopicName(topic.getTitle());
            });
        }

        return response;
    }

    /**
     * Generate today's practice for a student if not exists
     * Called lazily when student accesses practice or by scheduler
     */
    public TodayPracticeResponse generateTodayPracticeIfNeeded(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        LocalDate today = LocalDate.now();
        log.info("Checking/generating today's practice - TraceId: {}, StudentId: {}", traceId, studentId);

        // Check if practice already exists for today
        List<DailyPracticeQueue> existing = queueRepository.findByStudentIdAndScheduledForDateOrderByCreatedAtAsc(
                studentId, today);
        
        if (!existing.isEmpty()) {
            log.info("Practice already exists for today - TraceId: {}, StudentId: {}, Count: {}", 
                    traceId, studentId, existing.size());
            return getTodayPractice(studentId);
        }

        // Check if practice is enabled for this student
        StudentPracticePreferences prefs = preferencesRepository.findByStudentId(studentId).orElse(null);
        if (prefs == null || !prefs.getEnabled()) {
            log.info("Practice not enabled for student - TraceId: {}, StudentId: {}", traceId, studentId);
            TodayPracticeResponse empty = new TodayPracticeResponse();
            empty.setDate(today);
            empty.setItems(Collections.emptyList());
            return empty;
        }

        // Generate practice items from weak topics
        List<DailyPracticeQueue> generated = generatePracticeItems(studentId, prefs);
        log.info("Generated practice items - TraceId: {}, StudentId: {}, Count: {}", 
                traceId, studentId, generated.size());

        return getTodayPractice(studentId);
    }

    // ==================== START & SUBMIT PRACTICE ====================

    /**
     * Start a practice item (generates quiz)
     */
    public StartPracticeResponse startPractice(Long studentId, Long practiceId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Starting practice - TraceId: {}, StudentId: {}, PracticeId: {}", traceId, studentId, practiceId);

        DailyPracticeQueue practice = queueRepository.findByIdAndStudentId(practiceId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice item not found: " + practiceId));

        // Validate status
        if (practice.getStatus() == DailyPracticeQueue.PracticeStatus.COMPLETED) {
            throw new BusinessException("Practice already completed", HttpStatus.BAD_REQUEST, "PRACTICE_COMPLETED");
        }

        // Get topic
        Topic topic = topicRepository.findById(practice.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + practice.getTopicId()));

        // Generate quiz for this topic
        StudentPracticePreferences prefs = preferencesRepository.findByStudentId(studentId).orElse(null);
        int questionCount = prefs != null ? prefs.getDailyQuestionCount() : 5;
        String language = prefs != null ? prefs.getLanguage() : "en";

        QuizDTO.GenerateRequest quizRequest = new QuizDTO.GenerateRequest();
        quizRequest.setTopicId(topic.getId());
        quizRequest.setNumQuestions(questionCount);
        quizRequest.setDifficulty("MEDIUM");
        quizRequest.setQuizType("PRACTICE");
        quizRequest.setLanguage(language);

        QuizDTO.QuizResponse quiz = quizService.generateQuiz(studentId, quizRequest);

        // Start an attempt on this quiz (quiz attempt will be created/reused)
        quizService.startAttempt(studentId, quiz.getQuizId());

        // Update practice item
        practice.start();
        practice.setQuizId(quiz.getQuizId());
        queueRepository.save(practice);

        // Build response
        StartPracticeResponse response = new StartPracticeResponse();
        response.setPracticeId(practice.getId());
        response.setQuizId(quiz.getQuizId());
        response.setTopicName(topic.getTitle());
        response.setStartedAt(practice.getStartedAt());
        response.setQuestions(quiz.getQuestions().stream().map(q -> {
            QuizQuestionResponse qr = new QuizQuestionResponse();
            qr.setId(q.getQuestionId());
            qr.setQuestionText(q.getQuestionText());
            qr.setOptions(q.getOptions() != null 
                    ? q.getOptions().stream().map(QuizDTO.OptionResponse::getText).collect(Collectors.toList())
                    : Collections.emptyList());
            qr.setQuestionType(q.getQuestionType());
            return qr;
        }).collect(Collectors.toList()));

        log.info("Practice started - TraceId: {}, PracticeId: {}, QuizId: {}", traceId, practiceId, quiz.getQuizId());
        return response;
    }

    /**
     * Submit practice answers and update mastery
     */
    public SubmitPracticeResponse submitPractice(Long studentId, Long practiceId, SubmitPracticeRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Submitting practice - TraceId: {}, StudentId: {}, PracticeId: {}", traceId, studentId, practiceId);

        DailyPracticeQueue practice = queueRepository.findByIdAndStudentId(practiceId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice item not found: " + practiceId));

        // Validate status
        if (practice.getStatus() == DailyPracticeQueue.PracticeStatus.COMPLETED) {
            throw new BusinessException("Practice already completed", HttpStatus.BAD_REQUEST, "PRACTICE_COMPLETED");
        }
        if (practice.getStatus() != DailyPracticeQueue.PracticeStatus.IN_PROGRESS) {
            throw new BusinessException("Practice not started", HttpStatus.BAD_REQUEST, "PRACTICE_NOT_STARTED");
        }

        // Get topic
        Topic topic = topicRepository.findById(practice.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + practice.getTopicId()));

        // Get attempt ID for this quiz
        QuizDTO.AttemptResponse attemptInfo = quizService.startAttempt(studentId, practice.getQuizId());
        
        // Grade the answers (using existing quiz service)
        QuizDTO.SubmitAttemptRequest quizSubmit = QuizDTO.SubmitAttemptRequest.builder()
                .attemptId(attemptInfo.getAttemptId())
                .answers(request.getAnswers().stream().map(a -> 
                        QuizDTO.SubmitAnswerRequest.builder()
                                .questionId(a.getQuestionId())
                                .selectedOptionIds(Collections.singletonList(a.getSelectedAnswer()))
                                .build()
                ).collect(Collectors.toList()))
                .build();

        QuizDTO.GradeAttemptResponse quizResult = quizService.submitAttempt(studentId, quizSubmit);

        // Calculate score from quiz result
        int answered = quizResult.getTotalQuestions();
        int correct = quizResult.getCorrectCount();
        BigDecimal scorePct = quizResult.getPercentage() != null 
                ? quizResult.getPercentage() 
                : BigDecimal.ZERO;

        // Get mastery delta from quiz result (already updated by quiz service)
        BigDecimal masteryDelta = quizResult.getMasteryDelta() != null 
                ? quizResult.getMasteryDelta() 
                : BigDecimal.ZERO;
        BigDecimal newMasteryScore = quizResult.getMasteryAfter() != null 
                ? quizResult.getMasteryAfter() 
                : BigDecimal.ZERO;

        // Complete practice
        practice.complete(answered, correct, scorePct);
        practice.setMasteryDelta(masteryDelta);
        queueRepository.save(practice);

        // Build response
        SubmitPracticeResponse response = new SubmitPracticeResponse();
        response.setPracticeId(practice.getId());
        response.setScore(scorePct);
        response.setQuestionsAnswered(answered);
        response.setQuestionsCorrect(correct);
        response.setMasteryDelta(masteryDelta);
        response.setNewMasteryScore(newMasteryScore);
        response.setFeedback(quizResult.getOverallFeedback());

        // Map question results
        if (quizResult.getGradedAnswers() != null) {
            response.setQuestionResults(quizResult.getGradedAnswers().stream().map(ar -> {
                QuestionResultResponse qr = new QuestionResultResponse();
                qr.setQuestionId(ar.getQuestionId());
                qr.setQuestionText(ar.getQuestionText());
                qr.setSelectedAnswer(ar.getSelectedOptionIds() != null && !ar.getSelectedOptionIds().isEmpty() 
                        ? ar.getSelectedOptionIds().get(0) : null);
                qr.setCorrectAnswer(ar.getCorrectAnswer());
                qr.setCorrect(ar.getIsCorrect() != null && ar.getIsCorrect());
                qr.setExplanation(ar.getExplanation());
                return qr;
            }).collect(Collectors.toList()));
        }

        // Generate next recommendation
        StudentTopicMastery currentMastery = masteryRepository.findByStudentIdAndTopicId(studentId, practice.getTopicId())
                .orElse(null);
        if (currentMastery != null) {
            response.setNextRecommendation(generateNextRecommendation(studentId, topic, currentMastery));
        }

        log.info("Practice submitted - TraceId: {}, PracticeId: {}, Score: {}, MasteryDelta: {}", 
                traceId, practiceId, scorePct, masteryDelta);
        
        return response;
    }

    /**
     * Skip a practice item
     */
    public void skipPractice(Long studentId, Long practiceId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Skipping practice - TraceId: {}, StudentId: {}, PracticeId: {}", traceId, studentId, practiceId);

        DailyPracticeQueue practice = queueRepository.findByIdAndStudentId(practiceId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Practice item not found: " + practiceId));

        if (practice.getStatus() == DailyPracticeQueue.PracticeStatus.COMPLETED) {
            throw new BusinessException("Cannot skip completed practice", HttpStatus.BAD_REQUEST, "PRACTICE_COMPLETED");
        }

        practice.skip();
        queueRepository.save(practice);
        log.info("Practice skipped - TraceId: {}, PracticeId: {}", traceId, practiceId);
    }

    // ==================== PRACTICE HISTORY ====================

    /**
     * Get practice history for a student (last 30 days)
     */
    @Transactional(readOnly = true)
    public PracticeHistoryResponse getPracticeHistory(Long studentId, int days) {
        String traceId = TraceUtil.getTraceId();
        LocalDate startDate = LocalDate.now().minusDays(days);
        log.info("Getting practice history - TraceId: {}, StudentId: {}, Days: {}", traceId, studentId, days);

        List<DailyPracticeQueue> history = queueRepository.findHistoryAfterDate(studentId, startDate);

        // Group by date
        Map<LocalDate, List<DailyPracticeQueue>> byDate = history.stream()
                .collect(Collectors.groupingBy(DailyPracticeQueue::getScheduledForDate));

        List<PracticeDaySummary> daySummaries = byDate.entrySet().stream()
                .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .map(entry -> {
                    PracticeDaySummary summary = new PracticeDaySummary();
                    summary.setDate(entry.getKey());
                    summary.setTotalItems(entry.getValue().size());
                    summary.setCompletedItems((int) entry.getValue().stream()
                            .filter(i -> i.getStatus() == DailyPracticeQueue.PracticeStatus.COMPLETED)
                            .count());
                    
                    // Calculate average score for completed items
                    List<BigDecimal> scores = entry.getValue().stream()
                            .filter(i -> i.getScore() != null)
                            .map(DailyPracticeQueue::getScore)
                            .collect(Collectors.toList());
                    if (!scores.isEmpty()) {
                        BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        summary.setAverageScore(sum.divide(new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP));
                    }
                    
                    summary.setItems(entry.getValue().stream()
                            .map(this::mapToPracticeItemResponse)
                            .collect(Collectors.toList()));
                    
                    return summary;
                })
                .collect(Collectors.toList());

        // Calculate stats
        PracticeStats stats = calculateStats(history);

        PracticeHistoryResponse response = new PracticeHistoryResponse();
        response.setDays(daySummaries);
        response.setStats(stats);

        return response;
    }

    // ==================== SCHEDULER ====================

    /**
     * Scheduled job to generate daily practice for all enabled students
     * Runs at 6 AM every day
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void generateDailyPracticeForAllStudents() {
        String traceId = TraceUtil.getTraceId();
        LocalDate today = LocalDate.now();
        log.info("Starting daily practice generation - TraceId: {}, Date: {}", traceId, today);

        List<Long> studentsNeedingPractice = queueRepository.findStudentsWithoutPracticeForDate(today);
        log.info("Students needing practice - TraceId: {}, Count: {}", traceId, studentsNeedingPractice.size());

        int generated = 0;
        for (Long studentId : studentsNeedingPractice) {
            try {
                StudentPracticePreferences prefs = preferencesRepository.findByStudentId(studentId).orElse(null);
                if (prefs != null && prefs.getEnabled()) {
                    List<DailyPracticeQueue> items = generatePracticeItems(studentId, prefs);
                    if (!items.isEmpty()) {
                        generated++;
                        
                        // Send notification if enabled
                        if (prefs.getNotificationEnabled()) {
                            sendPracticeReadyNotification(studentId, items.size());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to generate practice for student - TraceId: {}, StudentId: {}, Error: {}", 
                        traceId, studentId, e.getMessage());
            }
        }

        log.info("Daily practice generation completed - TraceId: {}, StudentsProcessed: {}, Generated: {}", 
                traceId, studentsNeedingPractice.size(), generated);
    }

    // ==================== HELPER METHODS ====================

    private List<DailyPracticeQueue> generatePracticeItems(Long studentId, StudentPracticePreferences prefs) {
        LocalDate today = LocalDate.now();
        
        // Get weak topics (mastery < 0.65)
        List<StudentTopicMastery> weakTopics = masteryRepository
                .findWeakTopics(studentId, WEAK_TOPIC_THRESHOLD);

        if (weakTopics.isEmpty()) {
            // No weak topics, get topics with lowest mastery instead
            weakTopics = masteryRepository.findByStudentIdOrderByMasteryScoreAsc(studentId);
        }

        // Filter to CBSE topics and limit by preference
        int targetCount = Math.min(prefs.getDailyQuestionCount() / 3 + 1, 3); // 1-3 topics
        List<DailyPracticeQueue> items = new ArrayList<>();

        for (StudentTopicMastery mastery : weakTopics) {
            if (items.size() >= targetCount) break;
            
            // Verify topic is CBSE
            Topic topic = topicRepository.findById(mastery.getTopicId()).orElse(null);
            if (topic == null) continue;
            
            Board board = topic.getBoard();
            if (board == null || !DEFAULT_BOARD.equalsIgnoreCase(board.getName())) continue;

            // Check if not already scheduled
            if (queueRepository.existsByStudentIdAndTopicIdAndScheduledForDate(studentId, topic.getId(), today)) {
                continue;
            }

            DailyPracticeQueue item = new DailyPracticeQueue(studentId, topic.getId(), today);
            items.add(queueRepository.save(item));
        }

        return items;
    }

    private Optional<Topic> findNextRecommendedTopic(Long studentId) {
        List<StudentTopicMastery> weakTopics = masteryRepository
                .findWeakTopics(studentId, WEAK_TOPIC_THRESHOLD);

        for (StudentTopicMastery mastery : weakTopics) {
            // Skip topics already practiced today
            if (queueRepository.existsByStudentIdAndTopicIdAndScheduledForDate(
                    studentId, mastery.getTopicId(), LocalDate.now())) {
                continue;
            }
            return topicRepository.findById(mastery.getTopicId());
        }
        
        return Optional.empty();
    }

    private NextRecommendation generateNextRecommendation(Long studentId, Topic currentTopic, StudentTopicMastery mastery) {
        NextRecommendation rec = new NextRecommendation();
        
        BigDecimal masteryScore = mastery.getMasteryScore();
        
        if (masteryScore.compareTo(new BigDecimal("0.85")) >= 0) {
            // Topic mastered, move to next
            rec.setType("MASTERED");
            rec.setMessage(String.format("Great! You've mastered %s. Time to move to the next topic!", currentTopic.getTitle()));
            
            // Find next topic to recommend
            findNextRecommendedTopic(studentId).ifPresent(nextTopic -> {
                rec.setTopicId(nextTopic.getId());
                rec.setTopicName(nextTopic.getTitle());
                rec.setType("MOVE_TO_NEXT");
                rec.setMessage(String.format("You've done well on %s! Try practicing %s next.", 
                        currentTopic.getTitle(), nextTopic.getTitle()));
            });
        } else {
            // Continue practicing this topic
            rec.setType("CONTINUE_TOPIC");
            rec.setTopicId(currentTopic.getId());
            rec.setTopicName(currentTopic.getTitle());
            rec.setMessage(String.format("Keep practicing %s to improve your mastery from %.0f%% to 85%%+", 
                    currentTopic.getTitle(), masteryScore.multiply(new BigDecimal(100))));
        }
        
        return rec;
    }

    private PracticeStats calculateStats(List<DailyPracticeQueue> history) {
        PracticeStats stats = new PracticeStats();
        
        // Count days active
        Set<LocalDate> activeDays = history.stream()
                .filter(h -> h.getStatus() == DailyPracticeQueue.PracticeStatus.COMPLETED)
                .map(DailyPracticeQueue::getScheduledForDate)
                .collect(Collectors.toSet());
        stats.setTotalDaysActive(activeDays.size());

        // Calculate streaks
        List<LocalDate> sortedDays = activeDays.stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        
        int currentStreak = 0;
        int longestStreak = 0;
        int tempStreak = 1;
        LocalDate today = LocalDate.now();
        
        if (!sortedDays.isEmpty() && (sortedDays.get(0).equals(today) || sortedDays.get(0).equals(today.minusDays(1)))) {
            currentStreak = 1;
            for (int i = 1; i < sortedDays.size(); i++) {
                if (sortedDays.get(i).equals(sortedDays.get(i - 1).minusDays(1))) {
                    currentStreak++;
                } else {
                    break;
                }
            }
        }
        
        // Calculate longest streak
        for (int i = 1; i < sortedDays.size(); i++) {
            if (sortedDays.get(i).equals(sortedDays.get(i - 1).minusDays(1))) {
                tempStreak++;
            } else {
                longestStreak = Math.max(longestStreak, tempStreak);
                tempStreak = 1;
            }
        }
        longestStreak = Math.max(longestStreak, Math.max(tempStreak, currentStreak));
        
        stats.setCurrentStreak(currentStreak);
        stats.setLongestStreak(longestStreak);

        // Calculate accuracy
        int totalAnswered = history.stream()
                .filter(h -> h.getQuestionsAnswered() != null)
                .mapToInt(DailyPracticeQueue::getQuestionsAnswered)
                .sum();
        int totalCorrect = history.stream()
                .filter(h -> h.getQuestionsCorrect() != null)
                .mapToInt(DailyPracticeQueue::getQuestionsCorrect)
                .sum();
        
        stats.setTotalQuestionsAnswered(totalAnswered);
        stats.setTotalQuestionsCorrect(totalCorrect);
        
        if (totalAnswered > 0) {
            stats.setAverageAccuracy(new BigDecimal(totalCorrect * 100)
                    .divide(new BigDecimal(totalAnswered), 2, RoundingMode.HALF_UP));
        } else {
            stats.setAverageAccuracy(BigDecimal.ZERO);
        }

        return stats;
    }

    private void sendPracticeReadyNotification(Long studentId, int itemCount) {
        try {
            notificationService.createNotification(
                    studentId,
                    NotificationType.GENERAL_ANNOUNCEMENT,
                    "Daily Practice Ready",
                    String.format("You have %d practice %s ready for today. Start practicing to improve your weak topics!", 
                            itemCount, itemCount == 1 ? "item" : "items"),
                    null
            );
        } catch (Exception e) {
            log.warn("Failed to send practice ready notification - StudentId: {}", studentId, e);
        }
    }

    private PracticePreferencesResponse mapToPreferencesResponse(StudentPracticePreferences prefs) {
        return new PracticePreferencesResponse(
                prefs.getEnabled(),
                prefs.getDailyQuestionCount(),
                prefs.getPreferredTimeLocal(),
                prefs.getLanguage(),
                prefs.getNotificationEnabled()
        );
    }

    private PracticeItemResponse mapToPracticeItemResponse(DailyPracticeQueue item) {
        PracticeItemResponse response = new PracticeItemResponse();
        response.setId(item.getId());
        response.setTopicId(item.getTopicId());
        response.setScheduledForDate(item.getScheduledForDate());
        response.setStatus(item.getStatus().name());
        response.setQuizId(item.getQuizId());
        response.setScore(item.getScore());
        response.setQuestionsAnswered(item.getQuestionsAnswered());
        response.setQuestionsCorrect(item.getQuestionsCorrect());
        response.setMasteryDelta(item.getMasteryDelta());
        response.setStartedAt(item.getStartedAt());
        response.setCompletedAt(item.getCompletedAt());
        response.setTimeSpentSeconds(item.getTimeSpentSeconds());

        // Get topic and subject names
        topicRepository.findById(item.getTopicId()).ifPresent(topic -> {
            response.setTopicName(topic.getTitle());
            if (topic.getSubjectId() != null) {
                subjectRepository.findById(topic.getSubjectId()).ifPresent(subject -> {
                    response.setSubjectName(subject.getName());
                });
            }
        });

        return response;
    }
}


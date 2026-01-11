package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.MasteryDTO;
import com.ankurshala.backend.dto.ai.QuizDTO;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for personalized quiz generation, management, and grading.
 * Supports AI-generated quizzes and mastery updates.
 */
@Service
@Transactional
@Slf4j
public class PersonalizedQuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private QuizAnswerRepository answerRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private MasteryService masteryService;

    @Autowired
    private AIInteractionRepository aiInteractionRepository;

    /**
     * Generate a new quiz for a topic
     */
    public QuizDTO.QuizResponse generateQuiz(Long studentId, QuizDTO.GenerateRequest request) {
        String traceId = TraceUtil.getTraceId();
        long startTime = System.currentTimeMillis();
        log.info("Generating quiz - TraceId: {}, StudentId: {}, TopicId: {}", traceId, studentId, request.getTopicId());

        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.getTopicId()));

        // Create quiz entity
        Quiz quiz = new Quiz();
        quiz.setTitle("Quiz: " + topic.getTitle());
        quiz.setDescription("Practice quiz for " + topic.getTitle());
        quiz.setTopicId(topic.getId());
        quiz.setSubjectId(topic.getSubjectId());
        quiz.setGradeId(topic.getGradeId());
        quiz.setBoardId(topic.getBoardId());
        quiz.setDifficulty(parseDifficulty(request.getDifficulty()));
        quiz.setBloomLevel(parseBloomLevel(request.getBloomLevel()));
        quiz.setQuestionCount(request.getNumQuestions());
        quiz.setTimeLimitMinutes(request.getTimeLimitMinutes());
        quiz.setQuizType(parseQuizType(request.getQuizType()));
        quiz.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        quiz.setStatus(Quiz.QuizStatus.ACTIVE);
        quiz.setGeneratedBy("SYSTEM"); // Will be replaced by AI model name when integrated

        // Generate questions (stub - will be replaced by AI generation)
        List<QuizQuestion> questions = generateQuestions(topic, request);
        
        Quiz savedQuiz = quizRepository.save(quiz);
        
        // Save questions with quiz reference
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            question.setQuizId(savedQuiz.getId());
            question.setSequenceNumber(i + 1);
            questionRepository.save(question);
        }

        // Log AI interaction
        long latency = System.currentTimeMillis() - startTime;
        AIInteraction interaction = new AIInteraction(AIInteraction.InteractionType.QUIZ_GENERATE, studentId)
                .withTopic(topic.getId())
                .withSubject(topic.getSubjectId())
                .withModel("SYSTEM", "stub-generator", "1.0")
                .withLatency((int) latency);
        aiInteractionRepository.save(interaction);

        log.info("Quiz generated - TraceId: {}, QuizId: {}, Questions: {}", traceId, savedQuiz.getId(), questions.size());

        return mapToQuizResponse(savedQuiz, questions, topic);
    }

    /**
     * Get quiz by ID
     */
    @Transactional(readOnly = true)
    public QuizDTO.QuizResponse getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        Topic topic = quiz.getTopicId() != null ? 
                topicRepository.findById(quiz.getTopicId()).orElse(null) : null;

        return mapToQuizResponse(quiz, quiz.getQuestions(), topic);
    }

    /**
     * Start a quiz attempt
     */
    public QuizDTO.AttemptResponse startAttempt(Long studentId, Long quizId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Starting quiz attempt - TraceId: {}, StudentId: {}, QuizId: {}", traceId, studentId, quizId);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        if (quiz.getStatus() != Quiz.QuizStatus.ACTIVE) {
            throw new BusinessException("Quiz is not available", HttpStatus.BAD_REQUEST, "QUIZ_NOT_ACTIVE");
        }

        // Check for existing in-progress attempt
        List<QuizAttempt> existingAttempts = attemptRepository.findByStudentIdAndStatus(
                studentId, QuizAttempt.AttemptStatus.IN_PROGRESS);
        
        for (QuizAttempt existing : existingAttempts) {
            if (existing.getQuizId().equals(quizId)) {
                log.info("Returning existing attempt - AttemptId: {}", existing.getId());
                return mapToAttemptResponse(existing, quiz);
            }
        }

        // Create new attempt
        QuizAttempt attempt = new QuizAttempt(quizId, studentId);
        QuizAttempt savedAttempt = attemptRepository.save(attempt);

        log.info("Quiz attempt started - TraceId: {}, AttemptId: {}", traceId, savedAttempt.getId());

        return mapToAttemptResponse(savedAttempt, quiz);
    }

    /**
     * Submit quiz attempt with answers
     */
    public QuizDTO.GradeAttemptResponse submitAttempt(Long studentId, QuizDTO.SubmitAttemptRequest request) {
        String traceId = TraceUtil.getTraceId();
        long startTime = System.currentTimeMillis();
        log.info("Submitting quiz attempt - TraceId: {}, StudentId: {}, AttemptId: {}", 
                traceId, studentId, request.getAttemptId());

        QuizAttempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found: " + request.getAttemptId()));

        // Verify ownership
        if (!attempt.getStudentId().equals(studentId)) {
            throw new BusinessException("Access denied", HttpStatus.FORBIDDEN, "NOT_OWNER");
        }

        if (attempt.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BusinessException("Attempt already submitted", HttpStatus.BAD_REQUEST, "ALREADY_SUBMITTED");
        }

        Quiz quiz = quizRepository.findByIdWithQuestions(attempt.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        // Process answers
        List<QuizAnswer> gradedAnswers = new ArrayList<>();
        Map<Long, QuizQuestion> questionMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        int correctCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = BigDecimal.ZERO;

        for (QuizDTO.SubmitAnswerRequest answerReq : request.getAnswers()) {
            QuizQuestion question = questionMap.get(answerReq.getQuestionId());
            if (question == null) continue;

            QuizAnswer answer = new QuizAnswer(attempt.getId(), answerReq.getQuestionId());
            answer.setAnswerText(answerReq.getAnswerText());
            answer.setSelectedOptionIds(answerReq.getSelectedOptionIds());
            answer.setTimeSpentSeconds(answerReq.getTimeSpentSeconds());
            answer.setAnsweredAt(LocalDateTime.now());

            // Grade the answer
            gradeAnswer(answer, question);
            
            QuizAnswer savedAnswer = answerRepository.save(answer);
            gradedAnswers.add(savedAnswer);

            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                correctCount++;
            }
            if (answer.getScore() != null) {
                totalScore = totalScore.add(answer.getScore());
            }
            if (answer.getMaxScore() != null) {
                maxScore = maxScore.add(answer.getMaxScore());
            }
        }

        // Update attempt
        attempt.submit();
        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore);
        attempt.setPercentage(maxScore.compareTo(BigDecimal.ZERO) > 0 ? 
                totalScore.multiply(new BigDecimal("100")).divide(maxScore, 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO);
        attempt.setStatus(QuizAttempt.AttemptStatus.GRADED);
        attempt.setGradedAt(LocalDateTime.now());
        attempt.setGradedBy("AUTO");

        // Generate feedback
        String feedback = generateFeedback(attempt.getPercentage(), correctCount, quiz.getQuestions().size());
        attempt.setAiFeedback(feedback);

        // Identify strengths and weaknesses
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        analyzePerformance(gradedAnswers, questionMap, strengths, weaknesses);
        attempt.setStrengths(strengths);
        attempt.setWeaknesses(weaknesses);

        // Update mastery
        BigDecimal masteryBefore = BigDecimal.ZERO;
        BigDecimal masteryAfter = BigDecimal.ZERO;
        if (quiz.getTopicId() != null) {
            MasteryDTO.TopicMastery currentMastery = masteryService.getTopicMastery(studentId, quiz.getTopicId());
            masteryBefore = currentMastery.getMasteryScore();

            BigDecimal attemptScore = attempt.getPercentage().divide(new BigDecimal("100"), 3, RoundingMode.HALF_UP);
            MasteryDTO.MasteryUpdateResponse masteryUpdate = masteryService.updateMastery(studentId, 
                    MasteryDTO.MasteryUpdateRequest.builder()
                            .topicId(quiz.getTopicId())
                            .scoreAchieved(attemptScore)
                            .questionsAnswered(gradedAnswers.size())
                            .correctAnswers(correctCount)
                            .source("QUIZ")
                            .build());

            masteryAfter = masteryUpdate.getNewScore();
            attempt.setMasteryDelta(masteryUpdate.getDelta());
        }

        attemptRepository.save(attempt);

        // Log AI interaction
        long latency = System.currentTimeMillis() - startTime;
        AIInteraction interaction = new AIInteraction(AIInteraction.InteractionType.QUIZ_GRADE, studentId)
                .withTopic(quiz.getTopicId())
                .withSubject(quiz.getSubjectId())
                .withModel("SYSTEM", "auto-grader", "1.0")
                .withLatency((int) latency);
        aiInteractionRepository.save(interaction);

        log.info("Quiz graded - TraceId: {}, Score: {}/{}, Percentage: {}", 
                traceId, totalScore, maxScore, attempt.getPercentage());

        // Build response
        List<QuizDTO.AnswerResponse> answerResponses = gradedAnswers.stream()
                .map(a -> mapToAnswerResponse(a, questionMap.get(a.getQuestionId())))
                .collect(Collectors.toList());

        return QuizDTO.GradeAttemptResponse.builder()
                .attemptId(attempt.getId())
                .totalScore(attempt.getTotalScore())
                .maxScore(attempt.getMaxScore())
                .percentage(attempt.getPercentage())
                .correctCount(correctCount)
                .totalQuestions(quiz.getQuestions().size())
                .overallFeedback(feedback)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .recommendations(generateRecommendations(weaknesses))
                .masteryBefore(masteryBefore)
                .masteryAfter(masteryAfter)
                .masteryDelta(attempt.getMasteryDelta())
                .gradedAnswers(answerResponses)
                .build();
    }

    /**
     * Get quiz attempt history for a student
     */
    @Transactional(readOnly = true)
    public List<QuizDTO.QuizHistoryItem> getQuizHistory(Long studentId, int limit) {
        List<QuizAttempt> attempts = attemptRepository.findRecentGradedAttempts(
                studentId, 
                org.springframework.data.domain.PageRequest.of(0, limit));

        return attempts.stream()
                .map(attempt -> {
                    Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
                    if (quiz == null) return null;

                    Topic topic = quiz.getTopicId() != null ? 
                            topicRepository.findById(quiz.getTopicId()).orElse(null) : null;

                    Long correctCount = answerRepository.countCorrectAnswers(attempt.getId());
                    Long totalCount = answerRepository.countByAttemptId(attempt.getId());

                    return QuizDTO.QuizHistoryItem.builder()
                            .attemptId(attempt.getId())
                            .quizId(quiz.getId())
                            .quizTitle(quiz.getTitle())
                            .topicTitle(topic != null ? topic.getTitle() : null)
                            .attemptedAt(attempt.getSubmittedAt())
                            .percentage(attempt.getPercentage())
                            .correctCount(correctCount != null ? correctCount.intValue() : 0)
                            .totalQuestions(totalCount != null ? totalCount.intValue() : 0)
                            .status(attempt.getStatus().name())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Helper methods

    private List<QuizQuestion> generateQuestions(Topic topic, QuizDTO.GenerateRequest request) {
        // Stub implementation - generates sample questions
        // TODO: Replace with AI-generated questions using Spring AI
        
        List<QuizQuestion> questions = new ArrayList<>();
        int numQuestions = request.getNumQuestions() != null ? request.getNumQuestions() : 5;

        for (int i = 0; i < numQuestions; i++) {
            QuizQuestion question = new QuizQuestion();
            question.setQuestionText("Sample question " + (i + 1) + " about " + topic.getTitle() + "?");
            question.setQuestionType(QuizQuestion.QuestionType.MCQ);
            question.setPoints(1);
            question.setTopicId(topic.getId());
            question.setDifficulty(request.getDifficulty());
            question.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");

            // Generate MCQ options
            List<Map<String, Object>> options = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                Map<String, Object> option = new HashMap<>();
                option.put("id", String.valueOf(j));
                option.put("text", "Option " + (char)('A' + j));
                option.put("isCorrect", j == (i % 4));  // Rotate correct answer
                options.add(option);
            }
            question.setOptions(options);
            question.setExplanation("This is the explanation for question " + (i + 1));

            questions.add(question);
        }

        return questions;
    }

    private void gradeAnswer(QuizAnswer answer, QuizQuestion question) {
        answer.setMaxScore(BigDecimal.valueOf(question.getPoints()));

        if (question.getQuestionType() == QuizQuestion.QuestionType.MCQ) {
            answer.gradeAsMcq(question);
        } else {
            // For non-MCQ, mark as pending AI grading
            answer.setScore(BigDecimal.ZERO);
            answer.setIsCorrect(false);
            answer.setGradingFeedback("Requires manual/AI grading");
        }
    }

    private String generateFeedback(BigDecimal percentage, int correct, int total) {
        if (percentage == null) return "Quiz completed.";
        
        double pct = percentage.doubleValue();
        if (pct >= 90) return "Excellent! You've mastered this topic.";
        if (pct >= 80) return "Great job! You have a strong understanding.";
        if (pct >= 70) return "Good work! A bit more practice will help.";
        if (pct >= 60) return "Fair performance. Review the concepts you missed.";
        return "Keep studying! Focus on understanding the fundamentals.";
    }

    private void analyzePerformance(List<QuizAnswer> answers, Map<Long, QuizQuestion> questionMap,
                                    List<String> strengths, List<String> weaknesses) {
        for (QuizAnswer answer : answers) {
            QuizQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) continue;

            String topicContext = question.getTopicId() != null ? 
                    "Topic " + question.getTopicId() : "General";

            if (Boolean.TRUE.equals(answer.getIsCorrect())) {
                strengths.add("Strong understanding of " + topicContext);
            } else {
                weaknesses.add("Review needed: " + topicContext);
            }
        }

        // Deduplicate and limit
        strengths.clear();
        weaknesses.clear();
        
        long correctCount = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        if (correctCount > answers.size() * 0.7) {
            strengths.add("Good overall understanding");
        }
        if (correctCount < answers.size() * 0.5) {
            weaknesses.add("Fundamentals need review");
        }
    }

    private List<String> generateRecommendations(List<String> weaknesses) {
        List<String> recommendations = new ArrayList<>();
        if (weaknesses.isEmpty()) {
            recommendations.add("Continue with more advanced topics");
        } else {
            recommendations.add("Review the weak areas identified");
            recommendations.add("Take another practice quiz after reviewing");
        }
        return recommendations;
    }

    private QuizDTO.QuizResponse mapToQuizResponse(Quiz quiz, List<QuizQuestion> questions, Topic topic) {
        List<QuizDTO.QuestionResponse> questionResponses = questions.stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());

        return QuizDTO.QuizResponse.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .topicId(quiz.getTopicId())
                .topicTitle(topic != null ? topic.getTitle() : null)
                .subjectId(quiz.getSubjectId())
                .difficulty(quiz.getDifficulty() != null ? quiz.getDifficulty().name() : null)
                .bloomLevel(quiz.getBloomLevel() != null ? quiz.getBloomLevel().name() : null)
                .totalQuestions(questions.size())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .quizType(quiz.getQuizType() != null ? quiz.getQuizType().name() : null)
                .language(quiz.getLanguage())
                .questions(questionResponses)
                .createdAt(quiz.getCreatedAt())
                .build();
    }

    private QuizDTO.QuestionResponse mapToQuestionResponse(QuizQuestion question) {
        List<QuizDTO.OptionResponse> options = null;
        if (question.getOptions() != null) {
            options = question.getOptions().stream()
                    .map(opt -> QuizDTO.OptionResponse.builder()
                            .id(String.valueOf(opt.get("id")))
                            .text(String.valueOf(opt.get("text")))
                            // Note: isCorrect is NOT included for student view
                            .build())
                    .collect(Collectors.toList());
        }

        return QuizDTO.QuestionResponse.builder()
                .questionId(question.getId())
                .sequenceNumber(question.getSequenceNumber())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .options(options)
                .points(question.getPoints())
                .difficulty(question.getDifficulty())
                .bloomLevel(question.getBloomLevel() != null ? question.getBloomLevel().name() : null)
                .hint(question.getHint())
                .build();
    }

    private QuizDTO.AttemptResponse mapToAttemptResponse(QuizAttempt attempt, Quiz quiz) {
        return QuizDTO.AttemptResponse.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .studentId(attempt.getStudentId())
                .startedAt(attempt.getStartedAt())
                .submittedAt(attempt.getSubmittedAt())
                .status(attempt.getStatus().name())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .totalScore(attempt.getTotalScore())
                .maxScore(attempt.getMaxScore())
                .percentage(attempt.getPercentage())
                .aiFeedback(attempt.getAiFeedback())
                .strengths(attempt.getStrengths())
                .weaknesses(attempt.getWeaknesses())
                .masteryDelta(attempt.getMasteryDelta())
                .build();
    }

    private QuizDTO.AnswerResponse mapToAnswerResponse(QuizAnswer answer, QuizQuestion question) {
        return QuizDTO.AnswerResponse.builder()
                .answerId(answer.getId())
                .questionId(answer.getQuestionId())
                .questionText(question != null ? question.getQuestionText() : null)
                .answerText(answer.getAnswerText())
                .selectedOptionIds(answer.getSelectedOptionIds())
                .isCorrect(answer.getIsCorrect())
                .score(answer.getScore())
                .maxScore(answer.getMaxScore())
                .correctAnswer(question != null ? question.getCorrectAnswer() : null)
                .explanation(question != null ? question.getExplanation() : null)
                .gradingFeedback(answer.getGradingFeedback())
                .build();
    }

    private Quiz.QuizDifficulty parseDifficulty(String difficulty) {
        if (difficulty == null) return Quiz.QuizDifficulty.MEDIUM;
        try {
            return Quiz.QuizDifficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Quiz.QuizDifficulty.MEDIUM;
        }
    }

    private Quiz.BloomLevel parseBloomLevel(String level) {
        if (level == null) return null;
        try {
            return Quiz.BloomLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Quiz.QuizType parseQuizType(String type) {
        if (type == null) return Quiz.QuizType.PRACTICE;
        try {
            return Quiz.QuizType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Quiz.QuizType.PRACTICE;
        }
    }
}


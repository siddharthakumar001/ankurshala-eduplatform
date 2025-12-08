package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AppProperties;
import com.ankurshala.backend.dto.ai.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Service
@Transactional
@Slf4j
public class AIService {

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private StudentQuizResultRepository studentQuizResultRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ObjectMapper objectMapper;

    public GeneratedQuiz generateQuiz(GenerateQuizRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Generating AI quiz - TraceId: {}, SubjectId: {}, TopicId: {}", 
                traceId, request.getSubjectId(), request.getTopicId());

        if (!appProperties.getFeatures().isAi()) {
            throw new BusinessException("AI features are disabled", HttpStatus.SERVICE_UNAVAILABLE, "AI_DISABLED");
        }

        try {
            // Get subject details
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new BusinessException("Subject not found", HttpStatus.NOT_FOUND, "SUBJECT_NOT_FOUND"));

            // Get topic details if specified
            Topic topic = null;
            if (request.getTopicId() != null) {
                topic = topicRepository.findById(request.getTopicId())
                        .orElseThrow(() -> new BusinessException("Topic not found", HttpStatus.NOT_FOUND, "TOPIC_NOT_FOUND"));
            }

            // Generate quiz questions (stub implementation)
            List<QuizQuestion> questions = generateQuizQuestions(subject, topic, request.getNumQuestions(), request.getDifficulty());

            GeneratedQuiz quiz = new GeneratedQuiz();
            quiz.setQuizId(System.currentTimeMillis()); // Stub ID
            quiz.setTitle("Quiz: " + subject.getName() + (topic != null ? " - " + topic.getTitle() : ""));
            quiz.setDescription("AI-generated quiz for " + subject.getName());
            quiz.setQuestions(questions);
            quiz.setTotalQuestions(questions.size());
            quiz.setDifficulty(request.getDifficulty());

            log.info("AI quiz generated successfully - TraceId: {}, Questions: {}", traceId, questions.size());
            return quiz;

        } catch (BusinessException e) {
            log.error("AI quiz generation failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI quiz generation failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to generate quiz", HttpStatus.INTERNAL_SERVER_ERROR, "QUIZ_GENERATION_FAILED");
        }
    }

    public QuizResult submitQuiz(Long studentId, SubmitQuizRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Submitting AI quiz - TraceId: {}, StudentId: {}, QuizId: {}", 
                traceId, studentId, request.getQuizId());

        if (!appProperties.getFeatures().isAi()) {
            throw new BusinessException("AI features are disabled", HttpStatus.SERVICE_UNAVAILABLE, "AI_DISABLED");
        }

        try {
            // Calculate score (stub implementation - in real scenario, you'd validate against correct answers)
            int correct = calculateCorrectAnswers(request.getAnswers());
            int total = request.getAnswers().size();
            BigDecimal score = BigDecimal.valueOf(correct).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            // Create quiz result
            StudentQuizResult result = new StudentQuizResult();
            result.setStudentId(studentId);
            result.setSubjectId(1L); // Stub - would come from quiz metadata
            result.setTopicId(request.getQuizId()); // Using quiz ID as topic ID for stub
            result.setScore(score);
            result.setTotal(total);

            // Create breakdown
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("correct", correct);
            breakdown.put("incorrect", total - correct);
            breakdown.put("answers", request.getAnswers());
            breakdown.put("metadata", request.getMetadata());

            try {
                result.setBreakdown(objectMapper.writeValueAsString(breakdown));
            } catch (Exception e) {
                log.warn("Failed to serialize quiz breakdown - Error: {}", e.getMessage());
            }

            StudentQuizResult savedResult = studentQuizResultRepository.save(result);

            // Generate feedback
            String feedback = generateFeedback(score, correct, total);

            QuizResult quizResult = new QuizResult();
            quizResult.setResultId(savedResult.getId());
            quizResult.setStudentId(studentId);
            quizResult.setSubjectId(result.getSubjectId());
            quizResult.setTopicId(result.getTopicId());
            quizResult.setScore(score);
            quizResult.setTotal(total);
            quizResult.setCorrect(correct);
            quizResult.setBreakdown(breakdown);
            quizResult.setFeedback(feedback);

            log.info("AI quiz submitted successfully - TraceId: {}, Score: {}/{}", traceId, correct, total);
            return quizResult;

        } catch (BusinessException e) {
            log.error("AI quiz submission failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI quiz submission failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to submit quiz", HttpStatus.INTERNAL_SERVER_ERROR, "QUIZ_SUBMISSION_FAILED");
        }
    }

    public RecommendationsResponse getTopicRecommendations(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting AI recommendations - TraceId: {}, StudentId: {}", traceId, studentId);

        if (!appProperties.getFeatures().isAi()) {
            throw new BusinessException("AI features are disabled", HttpStatus.SERVICE_UNAVAILABLE, "AI_DISABLED");
        }

        try {
            // Get student's quiz history
            List<StudentQuizResult> quizResults = studentQuizResultRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

            // Generate recommendations based on performance (stub implementation)
            List<TopicRecommendation> recommendations = generateRecommendations(studentId, quizResults);

            RecommendationsResponse response = new RecommendationsResponse();
            response.setRecommendations(recommendations);
            response.setAlgorithm("performance-based");
            response.setStudentId(studentId);

            log.info("AI recommendations generated - TraceId: {}, Count: {}", traceId, recommendations.size());
            return response;

        } catch (BusinessException e) {
            log.error("AI recommendations failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI recommendations failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to get recommendations", HttpStatus.INTERNAL_SERVER_ERROR, "RECOMMENDATIONS_FAILED");
        }
    }

    public ClassSummary generateClassSummary(Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Generating AI class summary - TraceId: {}, BookingId: {}", traceId, bookingId);

        if (!appProperties.getFeatures().isAi()) {
            throw new BusinessException("AI features are disabled", HttpStatus.SERVICE_UNAVAILABLE, "AI_DISABLED");
        }

        try {
            // Get booking details
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

            // Get topic details
            Topic topic = topicRepository.findById(booking.getTopicId())
                    .orElseThrow(() -> new BusinessException("Topic not found", HttpStatus.NOT_FOUND, "TOPIC_NOT_FOUND"));

            // Generate summary (stub implementation)
            ClassSummary summary = generateSummary(booking, topic);

            log.info("AI class summary generated - TraceId: {}, BookingId: {}", traceId, bookingId);
            return summary;

        } catch (BusinessException e) {
            log.error("AI class summary failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI class summary failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to generate class summary", HttpStatus.INTERNAL_SERVER_ERROR, "SUMMARY_GENERATION_FAILED");
        }
    }

    // Stub implementations for AI functionality
    private List<QuizQuestion> generateQuizQuestions(Subject subject, Topic topic, Integer numQuestions, String difficulty) {
        List<QuizQuestion> questions = new ArrayList<>();
        
        for (int i = 0; i < numQuestions; i++) {
            QuizQuestion question = new QuizQuestion();
            question.setQuestion("Sample question " + (i + 1) + " about " + subject.getName() + "?");
            question.setOptions(Arrays.asList("Option A", "Option B", "Option C", "Option D"));
            question.setCorrectAnswer(i % 4); // Stub correct answer
            question.setExplanation("This is a sample explanation for question " + (i + 1));
            questions.add(question);
        }
        
        return questions;
    }

    private int calculateCorrectAnswers(List<Integer> answers) {
        // Stub implementation - randomly assign correct answers
        Random random = new Random();
        int correct = 0;
        for (Integer answer : answers) {
            if (random.nextBoolean()) {
                correct++;
            }
        }
        return correct;
    }

    private String generateFeedback(BigDecimal score, int correct, int total) {
        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "Excellent work! You have a strong understanding of this topic.";
        } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return "Good job! Consider reviewing the topics you missed.";
        } else {
            return "Keep studying! Focus on understanding the fundamental concepts.";
        }
    }

    private List<TopicRecommendation> generateRecommendations(Long studentId, List<StudentQuizResult> quizResults) {
        List<TopicRecommendation> recommendations = new ArrayList<>();
        
        // Stub implementation - generate sample recommendations
        for (int i = 0; i < 3; i++) {
            TopicRecommendation rec = new TopicRecommendation();
            rec.setTopicId((long) (i + 1));
            rec.setTitle("Recommended Topic " + (i + 1));
            rec.setDescription("This topic is recommended based on your learning progress");
            rec.setConfidence(0.8 - (i * 0.1));
            rec.setReason("Based on your quiz performance and learning patterns");
            recommendations.add(rec);
        }
        
        return recommendations;
    }

    private ClassSummary generateSummary(Booking booking, Topic topic) {
        ClassSummary summary = new ClassSummary();
        summary.setBookingId(booking.getId());
        summary.setSummary("This class covered " + topic.getTitle() + " with focus on key concepts and practical applications.");
        summary.setKeyPoints(Arrays.asList("Key concept 1", "Key concept 2", "Key concept 3"));
        summary.setActionItems(Arrays.asList("Review notes", "Practice problems", "Read additional material"));
        summary.setDifficulty("MEDIUM");
        summary.setNextSteps("Continue with the next topic in the sequence");
        return summary;
    }
}

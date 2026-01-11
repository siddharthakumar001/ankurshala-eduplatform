package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.MasteryDTO;
import com.ankurshala.backend.dto.ai.RecommendationDTO;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.entity.TopicLink;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.QuizRepository;
import com.ankurshala.backend.repository.TopicLinkRepository;
import com.ankurshala.backend.repository.TopicRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating personalized recommendations.
 * Handles prerequisite graph traversal and weak topic detection.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class RecommendationService {

    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("0.650");

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TopicLinkRepository topicLinkRepository;

    @Autowired
    private MasteryService masteryService;

    @Autowired
    private QuizRepository quizRepository;

    /**
     * Get prerequisite recommendations for a target topic.
     * Returns prerequisites that the student hasn't mastered yet.
     */
    public RecommendationDTO.TopicRecommendationResponse getPrerequisiteRecommendations(
            Long studentId, 
            Long topicId,
            BigDecimal threshold,
            boolean includeOptional) {
        
        String traceId = TraceUtil.getTraceId();
        log.info("Getting prerequisite recommendations - TraceId: {}, StudentId: {}, TopicId: {}", 
                traceId, studentId, topicId);

        if (threshold == null) threshold = DEFAULT_THRESHOLD;

        Topic targetTopic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        // Get prerequisite links
        List<TopicLink> prerequisiteLinks = topicLinkRepository.findByTopicIdAndType(
                topicId, TopicLink.TopicLinkType.PREREQUISITE);

        // Get related links if requested
        List<TopicLink> relatedLinks = includeOptional ? 
                topicLinkRepository.findByTopicIdAndType(topicId, TopicLink.TopicLinkType.RELATED) :
                Collections.emptyList();

        // Get all prerequisite topic IDs
        List<Long> prereqTopicIds = prerequisiteLinks.stream()
                .map(link -> link.getLinkedTopic().getId())
                .collect(Collectors.toList());

        List<Long> relatedTopicIds = relatedLinks.stream()
                .map(link -> link.getLinkedTopic().getId())
                .collect(Collectors.toList());

        // Get mastery scores for all prerequisite topics
        Map<Long, BigDecimal> masteryScores = masteryService.getMasteryScoresForTopics(
                studentId, 
                new ArrayList<>(prereqTopicIds));

        if (!relatedTopicIds.isEmpty()) {
            masteryScores.putAll(masteryService.getMasteryScoresForTopics(studentId, relatedTopicIds));
        }

        // Build required prerequisite recommendations (those below threshold)
        List<RecommendationDTO.PrerequisiteRecommendation> requiredPrereqs = new ArrayList<>();
        List<RecommendationDTO.PrerequisiteRecommendation> optionalPrereqs = new ArrayList<>();

        int priority = 1;
        for (TopicLink link : prerequisiteLinks) {
            Topic prereqTopic = link.getLinkedTopic();
            BigDecimal mastery = masteryScores.getOrDefault(prereqTopic.getId(), new BigDecimal("0.300"));
            
            if (mastery.compareTo(threshold) < 0) {
                requiredPrereqs.add(buildPrerequisiteRecommendation(prereqTopic, mastery, priority++, true));
            }
        }

        // Build optional prerequisite recommendations
        for (TopicLink link : relatedLinks) {
            Topic relatedTopic = link.getLinkedTopic();
            BigDecimal mastery = masteryScores.getOrDefault(relatedTopic.getId(), new BigDecimal("0.300"));
            
            if (mastery.compareTo(threshold) < 0) {
                optionalPrereqs.add(buildPrerequisiteRecommendation(relatedTopic, mastery, priority++, false));
            }
        }

        // Sort by mastery score (lowest first) then priority
        requiredPrereqs.sort(Comparator.comparing(RecommendationDTO.PrerequisiteRecommendation::getCurrentMastery));
        optionalPrereqs.sort(Comparator.comparing(RecommendationDTO.PrerequisiteRecommendation::getCurrentMastery));

        // Calculate readiness score
        BigDecimal readinessScore = calculateReadinessScore(masteryScores, prereqTopicIds, threshold);
        boolean isReady = requiredPrereqs.isEmpty();

        // Build suggested actions
        List<String> suggestedActions = buildSuggestedActions(requiredPrereqs, optionalPrereqs, isReady);

        String readinessMessage = isReady ? 
                "You're ready to learn " + targetTopic.getTitle() + "!" :
                "You should review " + requiredPrereqs.size() + " prerequisite topic(s) first.";

        return RecommendationDTO.TopicRecommendationResponse.builder()
                .targetTopicId(topicId)
                .targetTopicTitle(targetTopic.getTitle())
                .isReadyToLearn(isReady)
                .readinessMessage(readinessMessage)
                .requiredPrerequisites(requiredPrereqs)
                .recommendedPrerequisites(optionalPrereqs)
                .overallReadinessScore(readinessScore)
                .suggestedActions(suggestedActions)
                .build();
    }

    /**
     * Get weak topics for a student
     */
    public RecommendationDTO.WeakTopicsOverview getWeakTopics(
            Long studentId,
            Long subjectId,
            Long gradeId,
            BigDecimal threshold,
            int limit) {
        
        String traceId = TraceUtil.getTraceId();
        log.info("Getting weak topics - TraceId: {}, StudentId: {}", traceId, studentId);

        if (threshold == null) threshold = DEFAULT_THRESHOLD;
        if (limit <= 0) limit = 10;

        List<MasteryDTO.TopicMastery> weakMasteries = masteryService.getWeakTopics(studentId, threshold, limit);

        // Convert to weak topic responses
        List<RecommendationDTO.WeakTopicResponse> weakTopics = weakMasteries.stream()
                .map(mastery -> {
                    Topic topic = topicRepository.findById(mastery.getTopicId()).orElse(null);
                    if (topic == null) return null;

                    // Check if subject filter matches
                    if (subjectId != null && !topic.getSubjectId().equals(subjectId)) return null;
                    if (gradeId != null && !topic.getGradeId().equals(gradeId)) return null;

                    boolean hasQuiz = quizRepository.countByTopicIdAndStatus(
                            topic.getId(), 
                            com.ankurshala.backend.entity.Quiz.QuizStatus.ACTIVE) > 0;

                    return RecommendationDTO.WeakTopicResponse.builder()
                            .topicId(topic.getId())
                            .topicTitle(topic.getTitle())
                            .masteryScore(mastery.getMasteryScore())
                            .masteryLevel(mastery.getMasteryLevel())
                            .attemptCount(mastery.getTotalAttempts())
                            .improvementSuggestion(generateImprovementSuggestion(mastery))
                            .hasAvailableQuiz(hasQuiz)
                            .build();
                })
                .filter(Objects::nonNull)
                .limit(limit)
                .collect(Collectors.toList());

        // Group weak topics by subject
        Map<Long, List<RecommendationDTO.WeakTopicResponse>> bySubject = weakTopics.stream()
                .collect(Collectors.groupingBy(wt -> {
                    Topic topic = topicRepository.findById(wt.getTopicId()).orElse(null);
                    return topic != null ? topic.getSubjectId() : 0L;
                }));

        List<RecommendationDTO.SubjectWeakness> subjectWeaknesses = bySubject.entrySet().stream()
                .filter(e -> e.getKey() != 0L)
                .map(e -> {
                    BigDecimal avgMastery = e.getValue().stream()
                            .map(RecommendationDTO.WeakTopicResponse::getMasteryScore)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(e.getValue().size()), 3, java.math.RoundingMode.HALF_UP);

                    return RecommendationDTO.SubjectWeakness.builder()
                            .subjectId(e.getKey())
                            .weakTopicCount(e.getValue().size())
                            .averageMastery(avgMastery)
                            .build();
                })
                .sorted(Comparator.comparing(RecommendationDTO.SubjectWeakness::getAverageMastery))
                .collect(Collectors.toList());

        String overallRecommendation = generateOverallRecommendation(weakTopics.size(), subjectWeaknesses);

        return RecommendationDTO.WeakTopicsOverview.builder()
                .studentId(studentId)
                .totalWeakTopics(weakTopics.size())
                .weakTopics(weakTopics)
                .weakestSubjects(subjectWeaknesses)
                .overallRecommendation(overallRecommendation)
                .build();
    }

    /**
     * Generate a personalized study plan for a target topic
     */
    public RecommendationDTO.PersonalizedStudyPlan generateStudyPlan(
            Long studentId,
            RecommendationDTO.GenerateStudyPlanRequest request) {
        
        String traceId = TraceUtil.getTraceId();
        log.info("Generating study plan - TraceId: {}, StudentId: {}, TopicId: {}", 
                traceId, studentId, request.getTargetTopicId());

        Topic targetTopic = topicRepository.findById(request.getTargetTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.getTargetTopicId()));

        // Get prerequisite recommendations
        RecommendationDTO.TopicRecommendationResponse prereqs = getPrerequisiteRecommendations(
                studentId, request.getTargetTopicId(), DEFAULT_THRESHOLD, true);

        List<RecommendationDTO.StudyPlanItem> dailyPlan = new ArrayList<>();
        int dayNumber = 1;
        int availableDays = request.getAvailableDays() != null ? request.getAvailableDays() : 7;
        int minutesPerDay = request.getMinutesPerDay() != null ? request.getMinutesPerDay() : 60;

        // Add prerequisite review days
        for (RecommendationDTO.PrerequisiteRecommendation prereq : prereqs.getRequiredPrerequisites()) {
            if (dayNumber > availableDays) break;
            
            dailyPlan.add(RecommendationDTO.StudyPlanItem.builder()
                    .dayNumber(dayNumber++)
                    .topicId(prereq.getTopicId())
                    .topicTitle(prereq.getTopicTitle())
                    .activity("REVIEW")
                    .estimatedMinutes(Math.min(minutesPerDay, prereq.getEstimatedTimeMinutes() != null ? prereq.getEstimatedTimeMinutes() : 30))
                    .reason("Prerequisite topic - " + prereq.getReason())
                    .isCompleted(false)
                    .build());
        }

        // Add practice quiz day for prerequisites
        if (!prereqs.getRequiredPrerequisites().isEmpty() && dayNumber <= availableDays) {
            dailyPlan.add(RecommendationDTO.StudyPlanItem.builder()
                    .dayNumber(dayNumber++)
                    .topicId(null)
                    .topicTitle("Prerequisite Review Quiz")
                    .activity("QUIZ")
                    .estimatedMinutes(30)
                    .reason("Test your understanding of prerequisites")
                    .isCompleted(false)
                    .build());
        }

        // Add target topic learning
        if (dayNumber <= availableDays) {
            dailyPlan.add(RecommendationDTO.StudyPlanItem.builder()
                    .dayNumber(dayNumber++)
                    .topicId(targetTopic.getId())
                    .topicTitle(targetTopic.getTitle())
                    .activity("LEARN")
                    .estimatedMinutes(minutesPerDay)
                    .reason("Main topic to learn")
                    .isCompleted(false)
                    .build());
        }

        // Add practice day
        if (dayNumber <= availableDays) {
            dailyPlan.add(RecommendationDTO.StudyPlanItem.builder()
                    .dayNumber(dayNumber++)
                    .topicId(targetTopic.getId())
                    .topicTitle(targetTopic.getTitle())
                    .activity("PRACTICE")
                    .estimatedMinutes(minutesPerDay)
                    .reason("Practice and reinforce understanding")
                    .isCompleted(false)
                    .build());
        }

        // Add assessment day
        if (dayNumber <= availableDays) {
            dailyPlan.add(RecommendationDTO.StudyPlanItem.builder()
                    .dayNumber(dayNumber)
                    .topicId(targetTopic.getId())
                    .topicTitle(targetTopic.getTitle() + " Assessment")
                    .activity("QUIZ")
                    .estimatedMinutes(30)
                    .reason("Final assessment to confirm mastery")
                    .isCompleted(false)
                    .build());
        }

        int totalMinutes = dailyPlan.stream()
                .mapToInt(item -> item.getEstimatedMinutes() != null ? item.getEstimatedMinutes() : 0)
                .sum();

        List<String> tips = Arrays.asList(
                "Review each prerequisite topic before moving on",
                "Take notes and create summaries",
                "Practice with quizzes to reinforce learning",
                "Don't skip days - consistency is key"
        );

        return RecommendationDTO.PersonalizedStudyPlan.builder()
                .studentId(studentId)
                .targetTopicId(targetTopic.getId())
                .targetTopicTitle(targetTopic.getTitle())
                .planType(request.getFocusArea() != null ? request.getFocusArea() : "BALANCED")
                .totalDays(dailyPlan.size())
                .estimatedTotalMinutes(totalMinutes)
                .dailyPlan(dailyPlan)
                .summary(String.format("Complete this %d-day plan to master %s", dailyPlan.size(), targetTopic.getTitle()))
                .tips(tips)
                .build();
    }

    // Helper methods

    private RecommendationDTO.PrerequisiteRecommendation buildPrerequisiteRecommendation(
            Topic topic, BigDecimal mastery, int priority, boolean isRequired) {
        
        return RecommendationDTO.PrerequisiteRecommendation.builder()
                .topicId(topic.getId())
                .topicTitle(topic.getTitle())
                .currentMastery(mastery)
                .masteryLevel(getMasteryLevel(mastery))
                .reason(isRequired ? "Required prerequisite" : "Related topic for better understanding")
                .estimatedTimeMinutes(topic.getExpectedTimeMins() != null ? topic.getExpectedTimeMins() : 30)
                .isRequired(isRequired)
                .priority(priority)
                .build();
    }

    private BigDecimal calculateReadinessScore(Map<Long, BigDecimal> masteryScores, List<Long> prereqIds, BigDecimal threshold) {
        if (prereqIds.isEmpty()) return BigDecimal.ONE;

        long masteredCount = prereqIds.stream()
                .map(id -> masteryScores.getOrDefault(id, new BigDecimal("0.300")))
                .filter(score -> score.compareTo(threshold) >= 0)
                .count();

        return BigDecimal.valueOf(masteredCount)
                .divide(BigDecimal.valueOf(prereqIds.size()), 3, java.math.RoundingMode.HALF_UP);
    }

    private List<String> buildSuggestedActions(
            List<RecommendationDTO.PrerequisiteRecommendation> required,
            List<RecommendationDTO.PrerequisiteRecommendation> optional,
            boolean isReady) {
        
        List<String> actions = new ArrayList<>();
        
        if (isReady) {
            actions.add("Start learning the topic");
            actions.add("Take a diagnostic quiz to assess your starting point");
        } else {
            if (!required.isEmpty()) {
                actions.add("Review the " + required.size() + " required prerequisite(s)");
                actions.add("Take practice quizzes on prerequisite topics");
            }
        }
        
        if (!optional.isEmpty()) {
            actions.add("Consider reviewing " + optional.size() + " related topic(s) for deeper understanding");
        }
        
        return actions;
    }

    private String generateImprovementSuggestion(MasteryDTO.TopicMastery mastery) {
        double score = mastery.getMasteryScore().doubleValue();
        int attempts = mastery.getTotalAttempts();
        
        if (attempts == 0) {
            return "Start with a practice quiz to assess your understanding";
        } else if (score < 0.4) {
            return "Review the fundamentals and try simpler practice problems";
        } else if (score < 0.65) {
            return "Good progress! Practice more problems to reinforce concepts";
        } else {
            return "Almost there! Focus on advanced applications";
        }
    }

    private String generateOverallRecommendation(int weakCount, List<RecommendationDTO.SubjectWeakness> subjectWeaknesses) {
        if (weakCount == 0) {
            return "Great job! You're performing well across all topics.";
        } else if (weakCount <= 3) {
            return "Focus on strengthening these " + weakCount + " topic(s) with targeted practice.";
        } else if (!subjectWeaknesses.isEmpty()) {
            String weakestSubject = subjectWeaknesses.get(0).getSubjectId().toString(); // Would need subject name lookup
            return "You have " + weakCount + " topics to improve. Consider focusing on your weakest subject first.";
        } else {
            return "You have several topics to improve. Create a study plan to systematically address them.";
        }
    }

    private String getMasteryLevel(BigDecimal score) {
        if (score == null) return "UNKNOWN";
        double s = score.doubleValue();
        if (s < 0.4) return "WEAK";
        if (s < 0.65) return "DEVELOPING";
        if (s < 0.85) return "PROFICIENT";
        return "MASTERED";
    }
}


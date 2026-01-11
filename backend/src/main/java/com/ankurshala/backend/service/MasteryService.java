package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.MasteryDTO;
import com.ankurshala.backend.entity.StudentTopicMastery;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.StudentTopicMasteryRepository;
import com.ankurshala.backend.repository.TopicRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing student topic mastery scores.
 * Handles mastery tracking, updates, and retrieval.
 */
@Service
@Transactional
@Slf4j
public class MasteryService {

    private static final BigDecimal DEFAULT_MASTERY = new BigDecimal("0.300");
    private static final BigDecimal MASTERY_THRESHOLD = new BigDecimal("0.650");
    private static final BigDecimal MASTERED_THRESHOLD = new BigDecimal("0.850");

    @Autowired
    private StudentTopicMasteryRepository masteryRepository;

    @Autowired
    private TopicRepository topicRepository;

    /**
     * Get or create mastery record for a student-topic pair
     */
    public StudentTopicMastery getOrCreateMastery(Long studentId, Long topicId) {
        return masteryRepository.findByStudentIdAndTopicId(studentId, topicId)
                .orElseGet(() -> {
                    log.info("Creating new mastery record - StudentId: {}, TopicId: {}", studentId, topicId);
                    StudentTopicMastery mastery = new StudentTopicMastery(studentId, topicId);
                    return masteryRepository.save(mastery);
                });
    }

    /**
     * Get mastery for a specific topic
     */
    public MasteryDTO.TopicMastery getTopicMastery(Long studentId, Long topicId) {
        String traceId = TraceUtil.getTraceId();
        log.debug("Getting topic mastery - TraceId: {}, StudentId: {}, TopicId: {}", traceId, studentId, topicId);

        StudentTopicMastery mastery = getOrCreateMastery(studentId, topicId);
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        return mapToTopicMastery(mastery, topic);
    }

    /**
     * Get mastery overview for a student
     */
    @Transactional(readOnly = true)
    public MasteryDTO.MasteryOverview getMasteryOverview(Long studentId, Long subjectId, Long gradeId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting mastery overview - TraceId: {}, StudentId: {}", traceId, studentId);

        List<StudentTopicMastery> masteries = masteryRepository.findByStudentIdWithFilters(studentId, subjectId, gradeId);
        
        // Calculate overall statistics
        BigDecimal overallMastery = masteryRepository.calculateAverageMastery(studentId);
        if (overallMastery == null) overallMastery = DEFAULT_MASTERY;

        Long weakCount = masteryRepository.countWeakTopics(studentId, MASTERY_THRESHOLD);
        Long proficientCount = masteryRepository.countStrongTopics(studentId, MASTERY_THRESHOLD);
        Long masteredCount = masteryRepository.countStrongTopics(studentId, MASTERED_THRESHOLD);

        // Group by subject
        Map<Long, List<StudentTopicMastery>> bySubject = masteries.stream()
                .collect(Collectors.groupingBy(m -> {
                    Topic topic = topicRepository.findById(m.getTopicId()).orElse(null);
                    return topic != null ? topic.getSubjectId() : 0L;
                }));

        List<MasteryDTO.SubjectMastery> subjectMasteries = new ArrayList<>();
        for (Map.Entry<Long, List<StudentTopicMastery>> entry : bySubject.entrySet()) {
            if (entry.getKey() == 0L) continue;
            
            BigDecimal avgMastery = entry.getValue().stream()
                    .map(StudentTopicMastery::getMasteryScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(entry.getValue().size()), 3, RoundingMode.HALF_UP);

            long subjectWeakCount = entry.getValue().stream()
                    .filter(m -> m.getMasteryScore().compareTo(MASTERY_THRESHOLD) < 0)
                    .count();

            long subjectMasteredCount = entry.getValue().stream()
                    .filter(m -> m.getMasteryScore().compareTo(MASTERED_THRESHOLD) >= 0)
                    .count();

            subjectMasteries.add(MasteryDTO.SubjectMastery.builder()
                    .subjectId(entry.getKey())
                    .averageMastery(avgMastery)
                    .totalTopics(entry.getValue().size())
                    .weakTopics((int) subjectWeakCount)
                    .masteredTopics((int) subjectMasteredCount)
                    .build());
        }

        // Get weakest topics
        List<StudentTopicMastery> weakestMasteries = masteryRepository.findWeakTopics(studentId, MASTERY_THRESHOLD);
        List<MasteryDTO.TopicMastery> weakestTopics = weakestMasteries.stream()
                .limit(5)
                .map(m -> {
                    Topic topic = topicRepository.findById(m.getTopicId()).orElse(null);
                    return mapToTopicMastery(m, topic);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Get recently assessed
        List<MasteryDTO.TopicMastery> recentlyAssessed = masteries.stream()
                .filter(m -> m.getLastAssessedAt() != null)
                .sorted((a, b) -> b.getLastAssessedAt().compareTo(a.getLastAssessedAt()))
                .limit(5)
                .map(m -> {
                    Topic topic = topicRepository.findById(m.getTopicId()).orElse(null);
                    return mapToTopicMastery(m, topic);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return MasteryDTO.MasteryOverview.builder()
                .studentId(studentId)
                .overallMastery(overallMastery)
                .totalTopicsStudied(masteries.size())
                .weakTopicsCount(weakCount.intValue())
                .proficientTopicsCount(proficientCount.intValue() - masteredCount.intValue())
                .masteredTopicsCount(masteredCount.intValue())
                .subjectMasteries(subjectMasteries)
                .weakestTopics(weakestTopics)
                .recentlyAssessed(recentlyAssessed)
                .build();
    }

    /**
     * Update mastery based on quiz attempt or other assessment
     */
    public MasteryDTO.MasteryUpdateResponse updateMastery(Long studentId, MasteryDTO.MasteryUpdateRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Updating mastery - TraceId: {}, StudentId: {}, TopicId: {}", 
                traceId, studentId, request.getTopicId());

        StudentTopicMastery mastery = getOrCreateMastery(studentId, request.getTopicId());
        BigDecimal previousScore = mastery.getMasteryScore();

        // Update mastery using the entity's method
        mastery.updateMasteryFromAttempt(
                request.getScoreAchieved(),
                request.getQuestionsAnswered(),
                request.getCorrectAnswers()
        );

        StudentTopicMastery updated = masteryRepository.save(mastery);

        BigDecimal delta = updated.getMasteryScore().subtract(previousScore);

        log.info("Mastery updated - TraceId: {}, Previous: {}, New: {}, Delta: {}", 
                traceId, previousScore, updated.getMasteryScore(), delta);

        return MasteryDTO.MasteryUpdateResponse.builder()
                .topicId(request.getTopicId())
                .previousScore(previousScore)
                .newScore(updated.getMasteryScore())
                .delta(delta)
                .newLevel(getMasteryLevel(updated.getMasteryScore()))
                .build();
    }

    /**
     * Get weak topics for a student (mastery below threshold)
     */
    @Transactional(readOnly = true)
    public List<MasteryDTO.TopicMastery> getWeakTopics(Long studentId, BigDecimal threshold, int limit) {
        if (threshold == null) threshold = MASTERY_THRESHOLD;
        
        List<StudentTopicMastery> weakMasteries = masteryRepository.findWeakTopics(studentId, threshold);
        
        return weakMasteries.stream()
                .limit(limit)
                .map(m -> {
                    Topic topic = topicRepository.findById(m.getTopicId()).orElse(null);
                    return mapToTopicMastery(m, topic);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get mastery scores for a list of topic IDs
     */
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> getMasteryScoresForTopics(Long studentId, List<Long> topicIds) {
        List<StudentTopicMastery> masteries = masteryRepository.findByStudentIdAndTopicIds(studentId, topicIds);
        
        Map<Long, BigDecimal> scores = new HashMap<>();
        
        // Add existing mastery scores
        for (StudentTopicMastery mastery : masteries) {
            scores.put(mastery.getTopicId(), mastery.getMasteryScore());
        }
        
        // Add default score for topics without mastery records
        for (Long topicId : topicIds) {
            if (!scores.containsKey(topicId)) {
                scores.put(topicId, DEFAULT_MASTERY);
            }
        }
        
        return scores;
    }

    /**
     * Check if a student is ready to learn a topic (all prerequisites mastered)
     */
    @Transactional(readOnly = true)
    public boolean isReadyForTopic(Long studentId, Long topicId, List<Long> prerequisiteTopicIds) {
        if (prerequisiteTopicIds == null || prerequisiteTopicIds.isEmpty()) {
            return true;
        }

        Map<Long, BigDecimal> scores = getMasteryScoresForTopics(studentId, prerequisiteTopicIds);
        
        return scores.values().stream()
                .allMatch(score -> score.compareTo(MASTERY_THRESHOLD) >= 0);
    }

    // Helper methods

    private MasteryDTO.TopicMastery mapToTopicMastery(StudentTopicMastery mastery, Topic topic) {
        if (topic == null) return null;

        return MasteryDTO.TopicMastery.builder()
                .topicId(mastery.getTopicId())
                .topicTitle(topic.getTitle())
                .subjectId(topic.getSubjectId())
                .chapterId(topic.getChapterId())
                .masteryScore(mastery.getMasteryScore())
                .confidence(mastery.getConfidence())
                .totalAttempts(mastery.getTotalAttempts())
                .correctAttempts(mastery.getCorrectAttempts())
                .lastAssessedAt(mastery.getLastAssessedAt())
                .build();
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


package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.WeakTopicRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WeakTopicRecommendation entity
 */
@Repository
public interface WeakTopicRecommendationRepository extends JpaRepository<WeakTopicRecommendation, Long> {
    
    /**
     * Find active recommendations for a student, ordered by confidence score
     */
    List<WeakTopicRecommendation> findByStudentIdAndIsActiveTrueOrderByConfidenceScoreDesc(
        Long studentId
    );
    
    /**
     * Find the top recommendation for a student
     */
    @Query("SELECT wtr FROM WeakTopicRecommendation wtr " +
           "WHERE wtr.studentId = :studentId " +
           "AND wtr.isActive = TRUE " +
           "AND (wtr.expiresAt IS NULL OR wtr.expiresAt > :now) " +
           "ORDER BY wtr.confidenceScore DESC")
    Optional<WeakTopicRecommendation> findTopRecommendation(
        @Param("studentId") Long studentId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find recommendation for a specific topic
     */
    Optional<WeakTopicRecommendation> findByStudentIdAndTopicIdAndIsActiveTrue(
        Long studentId,
        Long topicId
    );
    
    /**
     * Find expired recommendations that should be deactivated
     */
    @Query("SELECT wtr FROM WeakTopicRecommendation wtr " +
           "WHERE wtr.isActive = TRUE " +
           "AND wtr.expiresAt IS NOT NULL " +
           "AND wtr.expiresAt <= :now")
    List<WeakTopicRecommendation> findExpiredRecommendations(@Param("now") LocalDateTime now);
}

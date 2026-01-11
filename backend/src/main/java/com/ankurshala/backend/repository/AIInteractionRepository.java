package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.AIInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AIInteractionRepository extends JpaRepository<AIInteraction, Long> {

    List<AIInteraction> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    Page<AIInteraction> findByInteractionType(AIInteraction.InteractionType type, Pageable pageable);

    /**
     * Find interactions by student within a date range
     */
    @Query("SELECT ai FROM AIInteraction ai " +
           "WHERE ai.studentId = :studentId " +
           "AND ai.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ai.createdAt DESC")
    List<AIInteraction> findByStudentIdBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find interactions with errors
     */
    @Query("SELECT ai FROM AIInteraction ai " +
           "WHERE ai.hadError = true " +
           "ORDER BY ai.createdAt DESC")
    Page<AIInteraction> findWithErrors(Pageable pageable);

    /**
     * Find refused interactions
     */
    @Query("SELECT ai FROM AIInteraction ai " +
           "WHERE ai.wasRefused = true " +
           "ORDER BY ai.createdAt DESC")
    Page<AIInteraction> findRefused(Pageable pageable);

    /**
     * Calculate average latency by interaction type
     */
    @Query("SELECT AVG(ai.latencyMs) FROM AIInteraction ai " +
           "WHERE ai.interactionType = :type " +
           "AND ai.hadError = false " +
           "AND ai.createdAt > :since")
    Double calculateAverageLatency(
            @Param("type") AIInteraction.InteractionType type,
            @Param("since") LocalDateTime since);

    /**
     * Calculate total tokens used by a student
     */
    @Query("SELECT SUM(ai.totalTokens) FROM AIInteraction ai " +
           "WHERE ai.studentId = :studentId " +
           "AND ai.createdAt BETWEEN :startDate AND :endDate")
    Long calculateTotalTokensUsed(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count interactions by type within a date range
     */
    @Query("SELECT ai.interactionType, COUNT(ai) FROM AIInteraction ai " +
           "WHERE ai.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY ai.interactionType")
    List<Object[]> countByTypeInRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count errors by type within a date range
     */
    @Query("SELECT ai.errorType, COUNT(ai) FROM AIInteraction ai " +
           "WHERE ai.hadError = true " +
           "AND ai.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY ai.errorType")
    List<Object[]> countErrorsByType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate average retrieval score
     */
    @Query("SELECT AVG(ai.retrievalScoreAvg) FROM AIInteraction ai " +
           "WHERE ai.retrievalScoreAvg IS NOT NULL " +
           "AND ai.createdAt > :since")
    Double calculateAverageRetrievalScore(@Param("since") LocalDateTime since);

    /**
     * Count daily interactions
     */
    @Query("SELECT COUNT(ai) FROM AIInteraction ai " +
           "WHERE ai.studentId = :studentId " +
           "AND ai.createdAt >= :startOfDay")
    Long countDailyInteractions(
            @Param("studentId") Long studentId,
            @Param("startOfDay") LocalDateTime startOfDay);
}


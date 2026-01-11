package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.SprintCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SprintCheckin entity.
 */
@Repository
public interface SprintCheckinRepository extends JpaRepository<SprintCheckin, Long> {

    /**
     * Find all check-ins for a session
     */
    List<SprintCheckin> findByFocusSessionIdOrderByStepNumberAsc(Long focusSessionId);

    /**
     * Find latest check-in for a session
     */
    Optional<SprintCheckin> findFirstByFocusSessionIdOrderByStepNumberDesc(Long focusSessionId);

    /**
     * Find check-in by session and step number
     */
    Optional<SprintCheckin> findByFocusSessionIdAndStepNumber(Long focusSessionId, Integer stepNumber);

    /**
     * Count check-ins for a session
     */
    long countByFocusSessionId(Long focusSessionId);

    /**
     * Count correct responses in a session
     */
    @Query("SELECT COUNT(c) FROM SprintCheckin c WHERE c.focusSession.id = :sessionId " +
           "AND c.responseCorrect = true")
    long countCorrectResponsesInSession(@Param("sessionId") Long sessionId);

    /**
     * Get answered check-ins count
     */
    @Query("SELECT COUNT(c) FROM SprintCheckin c WHERE c.focusSession.id = :sessionId " +
           "AND c.studentResponseText IS NOT NULL")
    long countAnsweredInSession(@Param("sessionId") Long sessionId);
}


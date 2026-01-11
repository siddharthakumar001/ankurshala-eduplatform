package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentFocusSession;
import com.ankurshala.backend.entity.StudentFocusSession.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentFocusSession entity.
 */
@Repository
public interface StudentFocusSessionRepository extends JpaRepository<StudentFocusSession, Long> {

    /**
     * Find active session for a student
     */
    Optional<StudentFocusSession> findByStudentIdAndStatus(Long studentId, SessionStatus status);

    /**
     * Find session by ID and student (for ownership validation)
     */
    Optional<StudentFocusSession> findByIdAndStudentId(Long id, Long studentId);

    /**
     * Get all active or paused sessions for a student
     */
    @Query("SELECT s FROM StudentFocusSession s WHERE s.studentId = :studentId " +
           "AND s.status IN ('ACTIVE', 'PAUSED') ORDER BY s.startedAt DESC")
    List<StudentFocusSession> findActiveOrPausedSessions(@Param("studentId") Long studentId);

    /**
     * Get session history for a student
     */
    Page<StudentFocusSession> findByStudentIdOrderByStartedAtDesc(Long studentId, Pageable pageable);

    /**
     * Get sessions for a specific topic
     */
    List<StudentFocusSession> findByStudentIdAndTopicIdOrderByStartedAtDesc(Long studentId, Long topicId);

    /**
     * Count completed sessions
     */
    long countByStudentIdAndStatus(Long studentId, SessionStatus status);

    /**
     * Get sessions within a time range
     */
    @Query("SELECT s FROM StudentFocusSession s WHERE s.studentId = :studentId " +
           "AND s.startedAt >= :startDate AND s.startedAt <= :endDate " +
           "ORDER BY s.startedAt DESC")
    List<StudentFocusSession> findByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Check if student has an active session
     */
    boolean existsByStudentIdAndStatus(Long studentId, SessionStatus status);

    /**
     * Get total time spent in focus sessions (completed)
     */
    @Query("SELECT COALESCE(SUM(s.totalActiveSeconds), 0) FROM StudentFocusSession s " +
           "WHERE s.studentId = :studentId AND s.status = 'ENDED'")
    Long getTotalFocusTimeSeconds(@Param("studentId") Long studentId);

    /**
     * Get recent completed sessions
     */
    @Query("SELECT s FROM StudentFocusSession s WHERE s.studentId = :studentId " +
           "AND s.status = 'ENDED' ORDER BY s.endedAt DESC")
    List<StudentFocusSession> findRecentCompletedSessions(@Param("studentId") Long studentId, Pageable pageable);
}


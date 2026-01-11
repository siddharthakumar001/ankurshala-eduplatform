package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.DailyPracticeQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPracticeQueueRepository extends JpaRepository<DailyPracticeQueue, Long> {

    /**
     * Find today's practice items for a student
     */
    List<DailyPracticeQueue> findByStudentIdAndScheduledForDateOrderByCreatedAtAsc(
            Long studentId, LocalDate scheduledForDate);

    /**
     * Find a specific practice item by ID and student (ownership check)
     */
    Optional<DailyPracticeQueue> findByIdAndStudentId(Long id, Long studentId);

    /**
     * Check if student already has practice scheduled for a date and topic
     */
    boolean existsByStudentIdAndTopicIdAndScheduledForDate(Long studentId, Long topicId, LocalDate date);

    /**
     * Count pending practice items for today
     */
    @Query("SELECT COUNT(p) FROM DailyPracticeQueue p WHERE p.studentId = :studentId " +
           "AND p.scheduledForDate = :date AND p.status = 'PENDING'")
    int countPendingForDate(@Param("studentId") Long studentId, @Param("date") LocalDate date);

    /**
     * Count completed practice items for today
     */
    @Query("SELECT COUNT(p) FROM DailyPracticeQueue p WHERE p.studentId = :studentId " +
           "AND p.scheduledForDate = :date AND p.status = 'COMPLETED'")
    int countCompletedForDate(@Param("studentId") Long studentId, @Param("date") LocalDate date);

    /**
     * Get practice history for a student (last N days)
     */
    @Query("SELECT p FROM DailyPracticeQueue p WHERE p.studentId = :studentId " +
           "AND p.scheduledForDate >= :startDate ORDER BY p.scheduledForDate DESC, p.createdAt DESC")
    List<DailyPracticeQueue> findHistoryAfterDate(@Param("studentId") Long studentId, 
                                                   @Param("startDate") LocalDate startDate);

    /**
     * Get students who don't have practice scheduled for a date yet
     */
    @Query("SELECT DISTINCT p.studentId FROM StudentPracticePreferences p " +
           "WHERE p.enabled = true AND NOT EXISTS (" +
           "SELECT 1 FROM DailyPracticeQueue q WHERE q.studentId = p.studentId AND q.scheduledForDate = :date)")
    List<Long> findStudentsWithoutPracticeForDate(@Param("date") LocalDate date);
}


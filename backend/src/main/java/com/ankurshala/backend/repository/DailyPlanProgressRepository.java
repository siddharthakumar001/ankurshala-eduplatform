package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.DailyPlanProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DailyPlanProgress entity
 */
@Repository
public interface DailyPlanProgressRepository extends JpaRepository<DailyPlanProgress, Long> {
    
    /**
     * Find all completed steps for a student on a specific date
     */
    List<DailyPlanProgress> findByStudentIdAndPlanDateOrderByCompletedAtDesc(
        Long studentId, 
        LocalDate planDate
    );
    
    /**
     * Count completed steps for a student on a specific date
     */
    Long countByStudentIdAndPlanDate(Long studentId, LocalDate planDate);
    
    /**
     * Check if a specific step was completed
     */
    Optional<DailyPlanProgress> findByStudentIdAndPlanDateAndStepTypeAndStepIdentifier(
        Long studentId,
        LocalDate planDate,
        String stepType,
        String stepIdentifier
    );
    
    /**
     * Get recent completed steps for a student (last 7 days)
     */
    @Query("SELECT dpp FROM DailyPlanProgress dpp " +
           "WHERE dpp.studentId = :studentId " +
           "AND dpp.planDate >= :fromDate " +
           "ORDER BY dpp.completedAt DESC")
    List<DailyPlanProgress> findRecentProgress(
        @Param("studentId") Long studentId,
        @Param("fromDate") LocalDate fromDate
    );
    
    /**
     * Count total steps completed by student (all time)
     */
    Long countByStudentId(Long studentId);
}

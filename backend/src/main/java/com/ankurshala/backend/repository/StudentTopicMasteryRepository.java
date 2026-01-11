package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentTopicMastery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentTopicMasteryRepository extends JpaRepository<StudentTopicMastery, Long> {

    Optional<StudentTopicMastery> findByStudentIdAndTopicId(Long studentId, Long topicId);

    List<StudentTopicMastery> findByStudentId(Long studentId);

    List<StudentTopicMastery> findByStudentIdOrderByMasteryScoreAsc(Long studentId);

    List<StudentTopicMastery> findByStudentIdOrderByMasteryScoreDesc(Long studentId);

    /**
     * Find all weak topics for a student (mastery below threshold)
     */
    @Query("SELECT stm FROM StudentTopicMastery stm " +
           "WHERE stm.studentId = :studentId AND stm.masteryScore < :threshold " +
           "ORDER BY stm.masteryScore ASC")
    List<StudentTopicMastery> findWeakTopics(
            @Param("studentId") Long studentId,
            @Param("threshold") BigDecimal threshold);

    /**
     * Find mastery for topics in a specific subject
     */
    @Query("SELECT stm FROM StudentTopicMastery stm " +
           "JOIN Topic t ON stm.topicId = t.id " +
           "WHERE stm.studentId = :studentId AND t.subjectId = :subjectId " +
           "ORDER BY stm.masteryScore ASC")
    List<StudentTopicMastery> findByStudentIdAndSubjectId(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId);

    /**
     * Find mastery for topics in a specific grade
     */
    @Query("SELECT stm FROM StudentTopicMastery stm " +
           "JOIN Topic t ON stm.topicId = t.id " +
           "WHERE stm.studentId = :studentId AND t.gradeId = :gradeId " +
           "ORDER BY stm.masteryScore ASC")
    List<StudentTopicMastery> findByStudentIdAndGradeId(
            @Param("studentId") Long studentId,
            @Param("gradeId") Long gradeId);

    /**
     * Find mastery for topics filtered by subject and grade
     */
    @Query("SELECT stm FROM StudentTopicMastery stm " +
           "JOIN Topic t ON stm.topicId = t.id " +
           "WHERE stm.studentId = :studentId " +
           "AND (:subjectId IS NULL OR t.subjectId = :subjectId) " +
           "AND (:gradeId IS NULL OR t.gradeId = :gradeId) " +
           "ORDER BY stm.masteryScore ASC")
    List<StudentTopicMastery> findByStudentIdWithFilters(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId,
            @Param("gradeId") Long gradeId);

    /**
     * Calculate average mastery across all topics for a student
     */
    @Query("SELECT AVG(stm.masteryScore) FROM StudentTopicMastery stm " +
           "WHERE stm.studentId = :studentId")
    BigDecimal calculateAverageMastery(@Param("studentId") Long studentId);

    /**
     * Calculate average mastery for a subject
     */
    @Query("SELECT AVG(stm.masteryScore) FROM StudentTopicMastery stm " +
           "JOIN Topic t ON stm.topicId = t.id " +
           "WHERE stm.studentId = :studentId AND t.subjectId = :subjectId")
    BigDecimal calculateAverageMasteryForSubject(
            @Param("studentId") Long studentId,
            @Param("subjectId") Long subjectId);

    /**
     * Count topics with mastery above threshold (strong topics)
     */
    @Query("SELECT COUNT(stm) FROM StudentTopicMastery stm " +
           "WHERE stm.studentId = :studentId AND stm.masteryScore >= :threshold")
    Long countStrongTopics(
            @Param("studentId") Long studentId,
            @Param("threshold") BigDecimal threshold);

    /**
     * Count topics with mastery below threshold (weak topics)
     */
    @Query("SELECT COUNT(stm) FROM StudentTopicMastery stm " +
           "WHERE stm.studentId = :studentId AND stm.masteryScore < :threshold")
    Long countWeakTopics(
            @Param("studentId") Long studentId,
            @Param("threshold") BigDecimal threshold);

    /**
     * Find all mastery records for a list of topic IDs
     */
    @Query("SELECT stm FROM StudentTopicMastery stm " +
           "WHERE stm.studentId = :studentId AND stm.topicId IN :topicIds")
    List<StudentTopicMastery> findByStudentIdAndTopicIds(
            @Param("studentId") Long studentId,
            @Param("topicIds") List<Long> topicIds);

    /**
     * Check if student has any mastery records
     */
    boolean existsByStudentId(Long studentId);
}


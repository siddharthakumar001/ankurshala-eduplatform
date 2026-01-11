package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.QuizAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<QuizAttempt> findByQuizIdOrderByCreatedAtDesc(Long quizId);

    Page<QuizAttempt> findByStudentId(Long studentId, Pageable pageable);

    /**
     * Find attempt with answers loaded
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
           "LEFT JOIN FETCH qa.answers " +
           "WHERE qa.id = :attemptId")
    Optional<QuizAttempt> findByIdWithAnswers(@Param("attemptId") Long attemptId);

    /**
     * Find attempts by student and quiz
     */
    List<QuizAttempt> findByStudentIdAndQuizId(Long studentId, Long quizId);

    /**
     * Find the most recent attempt for a student on a quiz
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.studentId = :studentId AND qa.quizId = :quizId " +
           "ORDER BY qa.createdAt DESC")
    List<QuizAttempt> findLatestAttempt(
            @Param("studentId") Long studentId,
            @Param("quizId") Long quizId,
            Pageable pageable);

    /**
     * Find in-progress attempts for a student
     */
    List<QuizAttempt> findByStudentIdAndStatus(Long studentId, QuizAttempt.AttemptStatus status);

    /**
     * Find completed attempts for a student within a date range
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.studentId = :studentId " +
           "AND qa.status = 'GRADED' " +
           "AND qa.submittedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY qa.submittedAt DESC")
    List<QuizAttempt> findCompletedAttemptsBetween(
            @Param("studentId") Long studentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate average score for a student across all quizzes
     */
    @Query("SELECT AVG(qa.percentage) FROM QuizAttempt qa " +
           "WHERE qa.studentId = :studentId AND qa.status = 'GRADED'")
    BigDecimal calculateAverageScore(@Param("studentId") Long studentId);

    /**
     * Calculate average score for a student on a specific topic
     */
    @Query("SELECT AVG(qa.percentage) FROM QuizAttempt qa " +
           "JOIN Quiz q ON qa.quizId = q.id " +
           "WHERE qa.studentId = :studentId " +
           "AND q.topicId = :topicId " +
           "AND qa.status = 'GRADED'")
    BigDecimal calculateAverageScoreForTopic(
            @Param("studentId") Long studentId,
            @Param("topicId") Long topicId);

    /**
     * Count attempts by student
     */
    Long countByStudentId(Long studentId);

    /**
     * Count graded attempts for a student
     */
    Long countByStudentIdAndStatus(Long studentId, QuizAttempt.AttemptStatus status);

    /**
     * Find recent graded attempts for a student
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.studentId = :studentId AND qa.status = 'GRADED' " +
           "ORDER BY qa.submittedAt DESC")
    List<QuizAttempt> findRecentGradedAttempts(
            @Param("studentId") Long studentId,
            Pageable pageable);

    /**
     * Find attempts for quizzes on specific topics
     */
    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN Quiz q ON qa.quizId = q.id " +
           "WHERE qa.studentId = :studentId " +
           "AND q.topicId IN :topicIds " +
           "AND qa.status = 'GRADED' " +
           "ORDER BY qa.submittedAt DESC")
    List<QuizAttempt> findGradedAttemptsForTopics(
            @Param("studentId") Long studentId,
            @Param("topicIds") List<Long> topicIds);
}


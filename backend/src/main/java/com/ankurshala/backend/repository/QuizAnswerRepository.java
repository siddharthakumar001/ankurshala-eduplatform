package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByAttemptId(Long attemptId);

    Optional<QuizAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    /**
     * Find all answers for an attempt with question details
     */
    @Query("SELECT qa FROM QuizAnswer qa " +
           "JOIN FETCH qa.question " +
           "WHERE qa.attemptId = :attemptId")
    List<QuizAnswer> findByAttemptIdWithQuestions(@Param("attemptId") Long attemptId);

    /**
     * Count correct answers in an attempt
     */
    @Query("SELECT COUNT(qa) FROM QuizAnswer qa " +
           "WHERE qa.attemptId = :attemptId AND qa.isCorrect = true")
    Long countCorrectAnswers(@Param("attemptId") Long attemptId);

    /**
     * Count total answers in an attempt
     */
    Long countByAttemptId(Long attemptId);

    /**
     * Delete all answers for an attempt
     */
    void deleteByAttemptId(Long attemptId);

    /**
     * Find answers for a specific question across all attempts (for analytics)
     */
    @Query("SELECT qa FROM QuizAnswer qa " +
           "WHERE qa.questionId = :questionId " +
           "ORDER BY qa.createdAt DESC")
    List<QuizAnswer> findByQuestionId(@Param("questionId") Long questionId);

    /**
     * Calculate success rate for a question
     */
    @Query("SELECT " +
           "CAST(SUM(CASE WHEN qa.isCorrect = true THEN 1 ELSE 0 END) AS double) / COUNT(qa) " +
           "FROM QuizAnswer qa WHERE qa.questionId = :questionId")
    Double calculateQuestionSuccessRate(@Param("questionId") Long questionId);
}


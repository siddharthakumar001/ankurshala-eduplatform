package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByTopicIdAndStatus(Long topicId, Quiz.QuizStatus status);

    List<Quiz> findBySubjectIdAndStatus(Long subjectId, Quiz.QuizStatus status);

    Page<Quiz> findByStatus(Quiz.QuizStatus status, Pageable pageable);

    /**
     * Find quizzes by topic with eager loading of questions
     */
    @Query("SELECT DISTINCT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions " +
           "WHERE q.topicId = :topicId AND q.status = :status")
    List<Quiz> findByTopicIdWithQuestions(
            @Param("topicId") Long topicId,
            @Param("status") Quiz.QuizStatus status);

    /**
     * Find a quiz by ID with questions loaded
     */
    @Query("SELECT q FROM Quiz q " +
           "LEFT JOIN FETCH q.questions " +
           "WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") Long quizId);

    /**
     * Find quizzes by filters
     */
    @Query("SELECT q FROM Quiz q WHERE q.status = :status " +
           "AND (:topicId IS NULL OR q.topicId = :topicId) " +
           "AND (:subjectId IS NULL OR q.subjectId = :subjectId) " +
           "AND (:gradeId IS NULL OR q.gradeId = :gradeId) " +
           "AND (:boardId IS NULL OR q.boardId = :boardId) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty) " +
           "ORDER BY q.createdAt DESC")
    Page<Quiz> findByFilters(
            @Param("status") Quiz.QuizStatus status,
            @Param("topicId") Long topicId,
            @Param("subjectId") Long subjectId,
            @Param("gradeId") Long gradeId,
            @Param("boardId") Long boardId,
            @Param("difficulty") Quiz.QuizDifficulty difficulty,
            Pageable pageable);

    /**
     * Find practice quizzes for a topic
     */
    @Query("SELECT q FROM Quiz q WHERE q.topicId = :topicId " +
           "AND q.status = 'ACTIVE' AND q.quizType = 'PRACTICE' " +
           "ORDER BY q.createdAt DESC")
    List<Quiz> findPracticeQuizzesForTopic(@Param("topicId") Long topicId);

    /**
     * Count quizzes by topic
     */
    Long countByTopicIdAndStatus(Long topicId, Quiz.QuizStatus status);

    /**
     * Find recent quizzes for a subject
     */
    @Query("SELECT q FROM Quiz q WHERE q.subjectId = :subjectId " +
           "AND q.status = 'ACTIVE' ORDER BY q.createdAt DESC")
    List<Quiz> findRecentBySubject(@Param("subjectId") Long subjectId, Pageable pageable);
}


package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizIdOrderBySequenceNumberAsc(Long quizId);

    List<QuizQuestion> findByTopicId(Long topicId);

    /**
     * Find questions by quiz ID with specific question types
     */
    @Query("SELECT qq FROM QuizQuestion qq " +
           "WHERE qq.quizId = :quizId AND qq.questionType IN :types " +
           "ORDER BY qq.sequenceNumber ASC")
    List<QuizQuestion> findByQuizIdAndTypes(
            @Param("quizId") Long quizId,
            @Param("types") List<QuizQuestion.QuestionType> types);

    /**
     * Count questions by quiz ID
     */
    Long countByQuizId(Long quizId);

    /**
     * Find MCQ questions for a topic (for bank selection)
     */
    @Query("SELECT qq FROM QuizQuestion qq " +
           "WHERE qq.topicId = :topicId AND qq.questionType = 'MCQ'")
    List<QuizQuestion> findMcqQuestionsForTopic(@Param("topicId") Long topicId);

    /**
     * Delete all questions for a quiz
     */
    void deleteByQuizId(Long quizId);
}


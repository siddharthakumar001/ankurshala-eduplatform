package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentQuizResultRepository extends JpaRepository<StudentQuizResult, Long> {
    List<StudentQuizResult> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<StudentQuizResult> findByStudentIdAndSubjectIdOrderByCreatedAtDesc(Long studentId, Long subjectId);
    List<StudentQuizResult> findByStudentIdAndTopicIdOrderByCreatedAtDesc(Long studentId, Long topicId);
    
    @Query("SELECT AVG(sqr.score) FROM StudentQuizResult sqr WHERE sqr.studentId = :studentId AND sqr.subjectId = :subjectId")
    Double getAverageScoreByStudentAndSubject(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherSubjectExpertise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TeacherSubjectExpertise entity.
 * Provides methods to query teachers by their subject expertise for booking matching.
 */
@Repository
public interface TeacherSubjectExpertiseRepository extends JpaRepository<TeacherSubjectExpertise, Long> {
    
    /**
     * Find all subject expertise entries for a specific teacher
     */
    List<TeacherSubjectExpertise> findByTeacherId(Long teacherId);
    
    /**
     * Find all subject expertise entries for a specific teacher that are active
     */
    List<TeacherSubjectExpertise> findByTeacherIdAndIsActiveTrue(Long teacherId);
    
    /**
     * Delete all subject expertise entries for a teacher
     */
    void deleteByTeacherId(Long teacherId);
    
    /**
     * Find teachers who can teach a specific subject
     */
    @Query("SELECT tse FROM TeacherSubjectExpertise tse " +
           "WHERE tse.subject.id = :subjectId " +
           "AND tse.isActive = true")
    List<TeacherSubjectExpertise> findBySubjectId(@Param("subjectId") Long subjectId);
    
    /**
     * Find teachers who can teach a specific subject and grade combination
     */
    @Query("SELECT tse FROM TeacherSubjectExpertise tse " +
           "WHERE tse.subject.id = :subjectId " +
           "AND tse.grade.id = :gradeId " +
           "AND tse.isActive = true")
    List<TeacherSubjectExpertise> findBySubjectIdAndGradeId(
            @Param("subjectId") Long subjectId, 
            @Param("gradeId") Long gradeId);
    
    /**
     * Find teachers who can teach a specific board, subject, and grade combination
     */
    @Query("SELECT tse FROM TeacherSubjectExpertise tse " +
           "WHERE tse.subject.id = :subjectId " +
           "AND tse.grade.id = :gradeId " +
           "AND tse.board.id = :boardId " +
           "AND tse.isActive = true")
    List<TeacherSubjectExpertise> findBySubjectIdAndGradeIdAndBoardId(
            @Param("subjectId") Long subjectId, 
            @Param("gradeId") Long gradeId,
            @Param("boardId") Long boardId);
    
    /**
     * Find eligible teachers for a booking based on subject, grade, board, and teacher status
     */
    @Query("SELECT DISTINCT tse.teacher.id FROM TeacherSubjectExpertise tse " +
           "JOIN tse.teacher t " +
           "WHERE tse.subject.id = :subjectId " +
           "AND (:gradeId IS NULL OR tse.grade.id = :gradeId) " +
           "AND (:boardId IS NULL OR tse.board.id = :boardId) " +
           "AND tse.isActive = true " +
           "AND t.status = 'ACTIVE'")
    List<Long> findEligibleTeacherIds(
            @Param("subjectId") Long subjectId, 
            @Param("gradeId") Long gradeId,
            @Param("boardId") Long boardId);
    
    /**
     * Check if a teacher has expertise for a specific subject/grade/board combination
     */
    @Query("SELECT COUNT(tse) > 0 FROM TeacherSubjectExpertise tse " +
           "WHERE tse.teacher.id = :teacherId " +
           "AND tse.subject.id = :subjectId " +
           "AND (:gradeId IS NULL OR tse.grade.id = :gradeId) " +
           "AND (:boardId IS NULL OR tse.board.id = :boardId) " +
           "AND tse.isActive = true")
    boolean hasExpertise(
            @Param("teacherId") Long teacherId,
            @Param("subjectId") Long subjectId,
            @Param("gradeId") Long gradeId,
            @Param("boardId") Long boardId);
}

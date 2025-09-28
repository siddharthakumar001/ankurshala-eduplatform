package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.CourseContent;
import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    
    // Find by class level and subject
    List<CourseContent> findByClassLevelAndSubject(ClassLevel classLevel, String subject);
    
    // Find by class level, subject, and chapter
    List<CourseContent> findByClassLevelAndSubjectAndChapter(ClassLevel classLevel, String subject, String chapter);
    
    // Find active content
    List<CourseContent> findByIsActiveTrue();
    
    // Find by educational board
    List<CourseContent> findByEducationalBoard(EducationalBoard educationalBoard);
    
    // Update educational board to null for course content with specific board
    @Modifying
    @Query("UPDATE CourseContent cc SET cc.educationalBoard = NULL WHERE cc.educationalBoard = :educationalBoard")
    void updateEducationalBoardToNull(@Param("educationalBoard") EducationalBoard educationalBoard);
    
    // Search with filters
    @Query("SELECT cc FROM CourseContent cc WHERE " +
           "(:classLevel IS NULL OR cc.classLevel = :classLevel) AND " +
           "(:subject IS NULL OR LOWER(cc.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) AND " +
           "(:chapter IS NULL OR LOWER(cc.chapter) LIKE LOWER(CONCAT('%', :chapter, '%'))) AND " +
           "(:topic IS NULL OR LOWER(cc.topic) LIKE LOWER(CONCAT('%', :topic, '%'))) AND " +
           "(:educationalBoard IS NULL OR cc.educationalBoard = :educationalBoard) AND " +
           "(:isActive IS NULL OR cc.isActive = :isActive)")
    Page<CourseContent> findWithFilters(
            @Param("classLevel") ClassLevel classLevel,
            @Param("subject") String subject,
            @Param("chapter") String chapter,
            @Param("topic") String topic,
            @Param("educationalBoard") EducationalBoard educationalBoard,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
    
    // Count by class level
    long countByClassLevel(ClassLevel classLevel);
    
    // Count by subject
    long countBySubject(String subject);
    
    // Count by educational board
    long countByEducationalBoard(EducationalBoard educationalBoard);
}

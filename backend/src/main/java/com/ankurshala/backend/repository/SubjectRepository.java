package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    Optional<Subject> findByName(String name);
    
    Optional<Subject> findByBoardIdAndName(Long boardId, String name);
    
    Optional<Subject> findByGradeIdAndName(Long gradeId, String name);
    
    // Find by ID excluding soft deleted
    @Query("SELECT s FROM Subject s WHERE s.id = :id AND s.softDeleted = false")
    Optional<Subject> findByIdAndSoftDeletedFalse(@Param("id") Long id);
    
    // Filter with soft delete consideration
    @Query("SELECT s FROM Subject s WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:boardId IS NULL OR s.boardId = :boardId) AND " +
           "(:gradeId IS NULL OR s.gradeId = :gradeId) AND " +
           "(:active IS NULL OR s.active = :active) AND " +
           "s.softDeleted = false")
    Page<Subject> findSubjectsWithFilters(@Param("search") String search, 
                                         @Param("boardId") Long boardId,
                                         @Param("gradeId") Long gradeId,
                                         @Param("active") Boolean active, 
                                         Pageable pageable);
    
    // Check if subject name exists within grade (excluding current subject for updates)
    @Query("SELECT COUNT(s) > 0 FROM Subject s WHERE LOWER(s.name) = LOWER(:name) AND s.gradeId = :gradeId AND s.id != :excludeId AND s.softDeleted = false")
    boolean existsByNameIgnoreCaseAndGradeIdAndIdNot(@Param("name") String name, @Param("gradeId") Long gradeId, @Param("excludeId") Long excludeId);
    
    // Check if subject name exists within grade for new subject
    @Query("SELECT COUNT(s) > 0 FROM Subject s WHERE LOWER(s.name) = LOWER(:name) AND s.gradeId = :gradeId AND s.softDeleted = false")
    boolean existsByNameIgnoreCaseAndGradeId(@Param("name") String name, @Param("gradeId") Long gradeId);
    
    // Find active subjects by board and grade
    @Query("SELECT s FROM Subject s WHERE s.boardId = :boardId AND s.gradeId = :gradeId AND s.active = true AND s.softDeleted = false ORDER BY s.name")
    List<Subject> findActiveSubjectsByBoardIdAndGradeId(@Param("boardId") Long boardId, @Param("gradeId") Long gradeId);
    
    // Count methods for analytics
    long countByActiveTrueAndSoftDeletedFalse();
    
    // Count methods for impact analysis
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.subjectId = :subjectId AND c.softDeleted = false")
    long countBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.subjectId = :subjectId AND t.softDeleted = false")
    long countTopicsBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.boardId = :boardId AND s.softDeleted = false")
    long countSubjectsByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    // Find subjects by board for cascading operations
    @Query("SELECT s FROM Subject s WHERE s.boardId = :boardId AND s.softDeleted = false")
    List<Subject> findByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    // Additional methods for cascading operations
    @Query("SELECT s FROM Subject s WHERE s.gradeId = :gradeId AND s.softDeleted = false")
    List<Subject> findByGradeIdAndDeletedAtIsNull(@Param("gradeId") Long gradeId);
    
    @Query("DELETE FROM Subject s WHERE s.gradeId = :gradeId")
    void deleteByGradeId(@Param("gradeId") Long gradeId);
    
    // Additional methods for service compatibility
    @Query("SELECT s FROM Subject s WHERE s.active = true AND s.softDeleted = false ORDER BY s.name")
    List<Subject> findByActiveTrue();
    
    @Query("SELECT s FROM Subject s WHERE s.active = true AND s.softDeleted = false ORDER BY s.name")
    List<Subject> findActiveSubjectsForDropdown();
    
    @Query("SELECT s FROM Subject s WHERE s.gradeId = :gradeId AND s.active = true AND s.softDeleted = false ORDER BY s.name")
    List<Subject> findActiveSubjectsByGradeIdForDropdown(@Param("gradeId") Long gradeId);
    
    @Query("SELECT s FROM Subject s WHERE s.gradeId = :gradeId AND s.softDeleted = false")
    List<Subject> findByGradeId(@Param("gradeId") Long gradeId);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.subjectId = :subjectId AND c.softDeleted = false")
    Long countChaptersBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.chapter.subjectId = :subjectId AND t.softDeleted = false")
    Long countTopicsBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.topic.chapter.subjectId = :subjectId AND tn.softDeleted = false")
    Long countTopicNotesBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(s) FROM Subject s WHERE s.gradeId = :gradeId AND s.softDeleted = false")
    Long countByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    @Query("SELECT s FROM Subject s WHERE s.gradeId = :gradeId AND s.softDeleted = false")
    List<Subject> findByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
}

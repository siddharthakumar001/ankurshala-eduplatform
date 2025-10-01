package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Grade;
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
public interface GradeRepository extends JpaRepository<Grade, Long>, JpaSpecificationExecutor<Grade> {
    Optional<Grade> findByName(String name);
    
    // Find by ID excluding soft deleted
    @Query("SELECT g FROM Grade g WHERE g.id = :id AND g.softDeleted = false")
    Optional<Grade> findByIdAndSoftDeletedFalse(@Param("id") Long id);
    
    // Filter with soft delete consideration
    @Query("SELECT g FROM Grade g WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(g.displayName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:boardId IS NULL OR g.boardId = :boardId) AND " +
           "(:active IS NULL OR g.active = :active) AND " +
           "g.softDeleted = false")
    Page<Grade> findGradesWithFilters(@Param("search") String search, 
                                     @Param("boardId") Long boardId,
                                     @Param("active") Boolean active, 
                                     Pageable pageable);
    
    // Check if grade name exists within board (excluding current grade for updates)
    @Query("SELECT COUNT(g) > 0 FROM Grade g WHERE LOWER(g.name) = LOWER(:name) AND g.boardId = :boardId AND g.id != :excludeId AND g.softDeleted = false")
    boolean existsByNameIgnoreCaseAndBoardIdAndIdNot(@Param("name") String name, @Param("boardId") Long boardId, @Param("excludeId") Long excludeId);
    
    // Check if grade name exists within board for new grade
    @Query("SELECT COUNT(g) > 0 FROM Grade g WHERE LOWER(g.name) = LOWER(:name) AND g.boardId = :boardId AND g.softDeleted = false")
    boolean existsByNameIgnoreCaseAndBoardId(@Param("name") String name, @Param("boardId") Long boardId);
    
    // Find active grades by board
    @Query("SELECT g FROM Grade g WHERE g.boardId = :boardId AND g.active = true AND g.softDeleted = false ORDER BY g.displayName")
    List<Grade> findActiveGradesByBoardId(@Param("boardId") Long boardId);
    
    // Count methods for impact analysis
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.gradeId = :gradeId AND s.softDeleted = false")
    Long countSubjectsByGradeId(@Param("gradeId") Long gradeId);

    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.subject.gradeId = :gradeId AND c.softDeleted = false")
    Long countChaptersByGradeId(@Param("gradeId") Long gradeId);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.chapter.subject.gradeId = :gradeId AND t.softDeleted = false")
    Long countTopicsByGradeId(@Param("gradeId") Long gradeId);

    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.topic.chapter.subject.gradeId = :gradeId AND tn.softDeleted = false")
    Long countTopicNotesByGradeId(@Param("gradeId") Long gradeId);

    @Query("SELECT COUNT(g) FROM Grade g WHERE g.boardId = :boardId AND g.softDeleted = false")
    Long countByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.gradeId = :gradeId AND s.softDeleted = false")
    long countByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.gradeId = :gradeId AND c.softDeleted = false")
    long countChaptersByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.gradeId = :gradeId AND t.softDeleted = false")
    long countTopicsByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.gradeId = :gradeId AND tn.softDeleted = false")
    long countTopicNotesByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    // Find grades by board for cascading operations
    @Query("SELECT g FROM Grade g WHERE g.boardId = :boardId AND g.softDeleted = false")
    List<Grade> findByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(s) FROM Subject s JOIN s.grade g WHERE g.boardId = :boardId AND s.softDeleted = false")
    long countSubjectsByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(c) FROM Chapter c JOIN c.subject s JOIN s.grade g WHERE g.boardId = :boardId AND c.softDeleted = false")
    long countChaptersByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(t) FROM Topic t JOIN t.chapter c JOIN c.subject s JOIN s.grade g WHERE g.boardId = :boardId AND t.softDeleted = false")
    long countTopicsByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn JOIN tn.topic t JOIN t.chapter c JOIN c.subject s JOIN s.grade g WHERE g.boardId = :boardId AND tn.softDeleted = false")
    long countNotesByBoardId(@Param("boardId") Long boardId);
    
    // Methods for cascading operations
    @Query("SELECT g FROM Grade g WHERE g.boardId = :boardId AND g.softDeleted = false")
    List<Grade> findByBoardIdAndDeletedAtIsNull(@Param("boardId") Long boardId);
    
    @Query("DELETE FROM Grade g WHERE g.boardId = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);
    
    // Additional methods for service compatibility  
    @Query("SELECT g FROM Grade g WHERE g.active = true AND g.softDeleted = false ORDER BY g.displayName")
    List<Grade> findActiveGradesForDropdown();
    
    @Query("SELECT g FROM Grade g WHERE g.boardId = :boardId AND g.active = true AND g.softDeleted = false ORDER BY g.displayName")
    List<Grade> findActiveGradesByBoardIdForDropdown(@Param("boardId") Long boardId);
    
    @Query("SELECT g FROM Grade g WHERE g.boardId = :boardId AND g.softDeleted = false")
    List<Grade> findByBoardIdNotSoftDeleted(@Param("boardId") Long boardId);
}

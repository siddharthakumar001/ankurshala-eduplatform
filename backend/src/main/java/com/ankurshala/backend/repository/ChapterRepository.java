package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Chapter;
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
public interface ChapterRepository extends JpaRepository<Chapter, Long>, JpaSpecificationExecutor<Chapter> {
    Optional<Chapter> findBySubjectIdAndName(Long subjectId, String name);
    
    // Find by ID excluding soft deleted
    @Query("SELECT c FROM Chapter c WHERE c.id = :id AND c.softDeleted = false")
    Optional<Chapter> findByIdAndSoftDeletedFalse(@Param("id") Long id);
    
    // Filter with soft delete consideration
    @Query("SELECT c FROM Chapter c WHERE " +
           "(:boardId IS NULL OR c.boardId = :boardId) AND " +
           "(:gradeId IS NULL OR c.gradeId = :gradeId) AND " +
           "(:subjectId IS NULL OR c.subjectId = :subjectId) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR c.active = :active) AND " +
           "c.softDeleted = false")
    Page<Chapter> findChaptersWithFilters(@Param("boardId") Long boardId,
                                         @Param("gradeId") Long gradeId,
                                         @Param("subjectId") Long subjectId,
                                         @Param("search") String search, 
                                         @Param("active") Boolean active, 
                                         Pageable pageable);
    
    // Check if chapter name exists within subject (excluding current chapter for updates)
    @Query("SELECT COUNT(c) > 0 FROM Chapter c WHERE LOWER(c.name) = LOWER(:name) AND c.subjectId = :subjectId AND c.id != :excludeId AND c.softDeleted = false")
    boolean existsByNameIgnoreCaseAndSubjectIdAndIdNot(@Param("name") String name, @Param("subjectId") Long subjectId, @Param("excludeId") Long excludeId);
    
    // Check if chapter name exists within subject for new chapter
    @Query("SELECT COUNT(c) > 0 FROM Chapter c WHERE LOWER(c.name) = LOWER(:name) AND c.subjectId = :subjectId AND c.softDeleted = false")
    boolean existsByNameIgnoreCaseAndSubjectId(@Param("name") String name, @Param("subjectId") Long subjectId);
    
    // Find active chapters by hierarchy
    @Query("SELECT c FROM Chapter c WHERE c.boardId = :boardId AND c.gradeId = :gradeId AND c.subjectId = :subjectId AND c.active = true AND c.softDeleted = false ORDER BY c.name")
    List<Chapter> findActiveChaptersByBoardIdAndGradeIdAndSubjectId(@Param("boardId") Long boardId, @Param("gradeId") Long gradeId, @Param("subjectId") Long subjectId);
    
    // Count methods for analytics
    long countByActiveTrueAndSoftDeletedFalse();
    
    // Count methods for impact analysis
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.chapterId = :chapterId AND t.softDeleted = false")
    Long countTopicsByChapterId(@Param("chapterId") Long chapterId);

    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.topic.chapterId = :chapterId AND tn.softDeleted = false")
    Long countTopicNotesByChapterId(@Param("chapterId") Long chapterId);

    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.subjectId = :subjectId AND c.softDeleted = false")
    Long countBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    @Query("SELECT c FROM Chapter c WHERE c.subjectId = :subjectId AND c.softDeleted = false")
    List<Chapter> findBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.boardId = :boardId AND c.softDeleted = false")
    long countChaptersByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.gradeId = :gradeId AND c.softDeleted = false")
    long countChaptersByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    // Find chapters by board for cascading operations
    @Query("SELECT c FROM Chapter c WHERE c.boardId = :boardId AND c.softDeleted = false")
    List<Chapter> findByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    // Find chapters by grade for cascading operations
    @Query("SELECT c FROM Chapter c WHERE c.gradeId = :gradeId AND c.softDeleted = false")
    List<Chapter> findByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    // Additional methods for cascading operations and impact analysis
    @Query("SELECT c FROM Chapter c WHERE c.subjectId = :subjectId AND c.softDeleted = false")
    List<Chapter> findBySubjectIdAndDeletedAtIsNull(@Param("subjectId") Long subjectId);
    
    @Query("DELETE FROM Chapter c WHERE c.subjectId = :subjectId")
    void deleteBySubjectId(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn JOIN tn.topic t WHERE t.chapterId = :chapterId AND tn.softDeleted = false")
    long countNotesByChapterId(@Param("chapterId") Long chapterId);
    
    // Additional methods for service compatibility
    @Query("SELECT c FROM Chapter c WHERE c.active = true AND c.softDeleted = false ORDER BY c.name")
    List<Chapter> findByActiveTrue();
    
    @Query("SELECT c FROM Chapter c WHERE c.active = true AND c.softDeleted = false ORDER BY c.name")
    List<Chapter> findActiveChaptersForDropdown();
    
    @Query("SELECT c FROM Chapter c WHERE c.subjectId = :subjectId AND c.active = true AND c.softDeleted = false ORDER BY c.name")
    List<Chapter> findActiveChaptersBySubjectIdForDropdown(@Param("subjectId") Long subjectId);
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Topic;
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
public interface TopicRepository extends JpaRepository<Topic, Long>, JpaSpecificationExecutor<Topic> {
    Optional<Topic> findByChapterIdAndTitle(Long chapterId, String title);
    Optional<Topic> findByCode(String code);
    
    // Find by ID excluding soft deleted
    @Query("SELECT t FROM Topic t WHERE t.id = :id AND t.softDeleted = false")
    Optional<Topic> findByIdAndSoftDeletedFalse(@Param("id") Long id);
    
    // Filter with soft delete consideration
    @Query("SELECT t FROM Topic t WHERE " +
           "(:boardId IS NULL OR t.boardId = :boardId) AND " +
           "(:gradeId IS NULL OR t.gradeId = :gradeId) AND " +
           "(:subjectId IS NULL OR t.subjectId = :subjectId) AND " +
           "(:chapterId IS NULL OR t.chapterId = :chapterId) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.summary) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR t.active = :active) AND " +
           "t.softDeleted = false")
    Page<Topic> findTopicsWithFilters(@Param("boardId") Long boardId,
                                     @Param("gradeId") Long gradeId,
                                     @Param("subjectId") Long subjectId,
                                     @Param("chapterId") Long chapterId,
                                     @Param("search") String search, 
                                     @Param("active") Boolean active, 
                                     Pageable pageable);
    
    // Check if topic code exists within chapter (excluding current topic for updates)
    @Query("SELECT COUNT(t) > 0 FROM Topic t WHERE LOWER(t.code) = LOWER(:code) AND t.chapterId = :chapterId AND t.id != :excludeId AND t.softDeleted = false")
    boolean existsByCodeIgnoreCaseAndChapterIdAndIdNot(@Param("code") String code, @Param("chapterId") Long chapterId, @Param("excludeId") Long excludeId);
    
    // Check if topic code exists within chapter for new topic
    @Query("SELECT COUNT(t) > 0 FROM Topic t WHERE LOWER(t.code) = LOWER(:code) AND t.chapterId = :chapterId AND t.softDeleted = false")
    boolean existsByCodeIgnoreCaseAndChapterId(@Param("code") String code, @Param("chapterId") Long chapterId);
    
    // Check if topic title exists within chapter (excluding current topic for updates)
    @Query("SELECT COUNT(t) > 0 FROM Topic t WHERE LOWER(t.title) = LOWER(:title) AND t.chapterId = :chapterId AND t.id != :excludeId AND t.softDeleted = false")
    boolean existsByTitleIgnoreCaseAndChapterIdAndIdNot(@Param("title") String title, @Param("chapterId") Long chapterId, @Param("excludeId") Long excludeId);
    
    // Check if topic title exists within chapter for new topic
    @Query("SELECT COUNT(t) > 0 FROM Topic t WHERE LOWER(t.title) = LOWER(:title) AND t.chapterId = :chapterId AND t.softDeleted = false")
    boolean existsByTitleIgnoreCaseAndChapterId(@Param("title") String title, @Param("chapterId") Long chapterId);
    
    // Generate next topic code for chapter
    @Query("SELECT MAX(CAST(SUBSTRING(t.code, 4) AS INTEGER)) FROM Topic t WHERE t.chapterId = :chapterId AND t.code LIKE 'TP_%' AND t.softDeleted = false")
    Integer findMaxTopicCodeNumberByChapterId(@Param("chapterId") Long chapterId);
    
    // Find active topics by hierarchy
    @Query("SELECT t FROM Topic t WHERE t.boardId = :boardId AND t.gradeId = :gradeId AND t.subjectId = :subjectId AND t.chapterId = :chapterId AND t.active = true AND t.softDeleted = false ORDER BY t.code")
    List<Topic> findActiveTopicsByBoardIdAndGradeIdAndSubjectIdAndChapterId(@Param("boardId") Long boardId, @Param("gradeId") Long gradeId, @Param("subjectId") Long subjectId, @Param("chapterId") Long chapterId);
    
    // Count methods for analytics
    long countByActiveTrueAndSoftDeletedFalse();
    
    // Count methods for impact analysis
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.boardId = :boardId AND t.softDeleted = false")
    long countTopicsByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.subjectId = :subjectId AND t.softDeleted = false")
    long countTopicsBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.topicId = :topicId AND tn.softDeleted = false")
    Long countTopicNotesByTopicId(@Param("topicId") Long topicId);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.chapter.subject.gradeId = :gradeId AND t.softDeleted = false")
    Long countTopicsByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);

    @Query("SELECT COUNT(t) FROM Topic t WHERE t.chapterId = :chapterId AND t.softDeleted = false")
    Long countByChapterIdAndSoftDeletedFalse(@Param("chapterId") Long chapterId);
    
    // Find topics by chapter for cascading operations
    @Query("SELECT t FROM Topic t WHERE t.chapterId = :chapterId AND t.softDeleted = false")
    List<Topic> findByChapterIdAndSoftDeletedFalse(@Param("chapterId") Long chapterId);
    
    // Find topics by subject for cascading operations
    @Query("SELECT t FROM Topic t WHERE t.subjectId = :subjectId AND t.softDeleted = false")
    List<Topic> findBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    // Find topics by grade for cascading operations
    @Query("SELECT t FROM Topic t WHERE t.gradeId = :gradeId AND t.softDeleted = false")
    List<Topic> findByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    // Find topics by board for cascading operations
    @Query("SELECT t FROM Topic t WHERE t.boardId = :boardId AND t.softDeleted = false")
    List<Topic> findByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    // Check if topic code exists (excluding soft deleted)
    @Query("SELECT COUNT(t) > 0 FROM Topic t WHERE t.code = :code AND t.softDeleted = false")
    boolean existsByCodeAndSoftDeletedFalse(@Param("code") String code);
    
    // Find top topic by chapter ordered by code for auto-generation
    @Query("SELECT t FROM Topic t WHERE t.chapterId = :chapterId AND t.softDeleted = false ORDER BY t.code DESC")
    Optional<Topic> findTopByChapterIdAndSoftDeletedFalseOrderByCodeDesc(@Param("chapterId") Long chapterId);
    
    // Additional methods for cascading operations and impact analysis
    @Query("DELETE FROM Topic t WHERE t.chapterId = :chapterId")
    void deleteByChapterId(@Param("chapterId") Long chapterId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.topicId = :topicId AND tn.softDeleted = false")
    long countNotesByTopicId(@Param("topicId") Long topicId);
    
    @Query("SELECT COUNT(tl) FROM TopicLink tl WHERE tl.topic.id = :topicId")
    long countLinksByTopicId(@Param("topicId") Long topicId);
    
    // Additional methods for service compatibility
    @Query("SELECT t FROM Topic t WHERE t.active = true AND t.softDeleted = false ORDER BY t.title")
    List<Topic> findByActiveTrue();
    
    @Query("SELECT t FROM Topic t WHERE t.active = true AND t.softDeleted = false ORDER BY t.title")
    List<Topic> findActiveTopicsForDropdown();
    
    @Query("SELECT t FROM Topic t WHERE t.chapterId = :chapterId AND t.active = true AND t.softDeleted = false ORDER BY t.code")
    List<Topic> findActiveTopicsByChapterIdForDropdown(@Param("chapterId") Long chapterId);
    
    @Query("SELECT t FROM Topic t WHERE t.chapterId = :chapterId AND t.softDeleted = false")
    List<Topic> findByChapterIdAndDeletedAtIsNull(@Param("chapterId") Long chapterId);
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TopicNote;
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
public interface TopicNoteRepository extends JpaRepository<TopicNote, Long>, JpaSpecificationExecutor<TopicNote> {
    
    // Find by ID excluding soft deleted
    @Query("SELECT tn FROM TopicNote tn WHERE tn.id = :id AND tn.softDeleted = false")
    Optional<TopicNote> findByIdAndSoftDeletedFalse(@Param("id") Long id);
    
    // Filter with soft delete and hierarchy consideration
    @Query("SELECT tn FROM TopicNote tn JOIN tn.topic t WHERE " +
           "(:boardId IS NULL OR t.boardId = :boardId) AND " +
           "(:gradeId IS NULL OR t.gradeId = :gradeId) AND " +
           "(:subjectId IS NULL OR t.subjectId = :subjectId) AND " +
           "(:chapterId IS NULL OR t.chapterId = :chapterId) AND " +
           "(:topicId IS NULL OR tn.topicId = :topicId) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(tn.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(tn.content) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR tn.active = :active) AND " +
           "tn.softDeleted = false AND t.softDeleted = false")
    Page<TopicNote> findTopicNotesWithFilters(@Param("boardId") Long boardId,
                                             @Param("gradeId") Long gradeId,
                                             @Param("subjectId") Long subjectId,
                                             @Param("chapterId") Long chapterId,
                                             @Param("topicId") Long topicId,
                                             @Param("search") String search,
                                             @Param("active") Boolean active,
                                             Pageable pageable);
    
    // Find notes by topic
    @Query("SELECT tn FROM TopicNote tn WHERE tn.topicId = :topicId AND tn.softDeleted = false ORDER BY tn.createdAt DESC")
    List<TopicNote> findByTopicIdNotSoftDeleted(@Param("topicId") Long topicId);
    
    // Find active notes by topic
    @Query("SELECT tn FROM TopicNote tn WHERE tn.topicId = :topicId AND tn.active = true AND tn.softDeleted = false ORDER BY tn.createdAt DESC")
    List<TopicNote> findActiveByTopicId(@Param("topicId") Long topicId);
    
    // Count methods for analytics
    long countByActiveTrueAndSoftDeletedFalse();
    
    // Count methods for impact analysis
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.chapterId = :chapterId AND tn.softDeleted = false")
    long countByChapterIdAndSoftDeletedFalse(@Param("chapterId") Long chapterId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.subjectId = :subjectId AND tn.softDeleted = false")
    long countBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.gradeId = :gradeId AND tn.softDeleted = false")
    long countByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.topicId = :topicId AND tn.softDeleted = false")
    long countByTopicIdAndSoftDeletedFalse(@Param("topicId") Long topicId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.boardId = :boardId AND tn.softDeleted = false")
    long countTopicNotesByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.gradeId = :gradeId AND tn.softDeleted = false")
    long countTopicNotesByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.subjectId = :subjectId AND tn.softDeleted = false")
    long countTopicNotesBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.chapterId = :chapterId AND tn.softDeleted = false")
    long countTopicNotesByChapterIdAndSoftDeletedFalse(@Param("chapterId") Long chapterId);
    
    // Find notes by topic for cascading operations
    @Query("SELECT tn FROM TopicNote tn WHERE tn.topicId = :topicId AND tn.softDeleted = false")
    List<TopicNote> findByTopicIdAndSoftDeletedFalse(@Param("topicId") Long topicId);
    
    // Find notes by chapter for cascading operations
    @Query("SELECT tn FROM TopicNote tn WHERE tn.chapterId = :chapterId AND tn.softDeleted = false")
    List<TopicNote> findByChapterIdAndSoftDeletedFalse(@Param("chapterId") Long chapterId);
    
    // Find notes by subject for cascading operations
    @Query("SELECT tn FROM TopicNote tn WHERE tn.subjectId = :subjectId AND tn.softDeleted = false")
    List<TopicNote> findBySubjectIdAndSoftDeletedFalse(@Param("subjectId") Long subjectId);
    
    // Find notes by grade for cascading operations
    @Query("SELECT tn FROM TopicNote tn WHERE tn.gradeId = :gradeId AND tn.softDeleted = false")
    List<TopicNote> findByGradeIdAndSoftDeletedFalse(@Param("gradeId") Long gradeId);
    
    // Find notes by board for cascading operations
    @Query("SELECT tn FROM TopicNote tn WHERE tn.boardId = :boardId AND tn.softDeleted = false")
    List<TopicNote> findByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    // Additional methods for cascading operations
    @Query("DELETE FROM TopicNote tn WHERE tn.topicId = :topicId")
    void deleteByTopicId(@Param("topicId") Long topicId);
    
    @Query("DELETE FROM TopicNote tn WHERE tn.topicId IN (SELECT t.id FROM Topic t WHERE t.chapterId = :chapterId)")
    void deleteByChapterId(@Param("chapterId") Long chapterId);
    
    // Additional methods for service compatibility
    @Query("SELECT COUNT(tn) > 0 FROM TopicNote tn WHERE LOWER(tn.title) = LOWER(:title) AND tn.topicId = :topicId AND tn.softDeleted = false")
    boolean existsByTitleIgnoreCaseAndTopicId(@Param("title") String title, @Param("topicId") Long topicId);
    
    @Query("SELECT COUNT(tn) > 0 FROM TopicNote tn WHERE LOWER(tn.title) = LOWER(:title) AND tn.topicId = :topicId AND tn.id != :excludeId AND tn.softDeleted = false")
    boolean existsByTitleIgnoreCaseAndTopicIdAndIdNot(@Param("title") String title, @Param("topicId") Long topicId, @Param("excludeId") Long excludeId);
}
package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long>, JpaSpecificationExecutor<Chapter> {
    Optional<Chapter> findBySubjectIdAndName(Long subjectId, String name);
    
    @Query("SELECT c FROM Chapter c WHERE " +
           "c.subject.id = :subjectId AND " +
           "(:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR c.active = :active) AND " +
           "c.deletedAt IS NULL")
    Page<Chapter> findChaptersWithFilters(@Param("subjectId") Long subjectId,
                                         @Param("search") String search, 
                                         @Param("active") Boolean active, 
                                         Pageable pageable);
    
    // Count methods for analytics
    long countByActiveTrue();
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.chapter.id = :chapterId")
    long countTopicsByChapterId(@Param("chapterId") Long chapterId);

    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.topic.chapter.id = :chapterId")
    long countNotesByChapterId(@Param("chapterId") Long chapterId);
    
    @Query("SELECT c FROM Chapter c WHERE c.subject.id = :subjectId AND c.deletedAt IS NULL")
    List<Chapter> findBySubjectIdAndDeletedAtIsNull(@Param("subjectId") Long subjectId);
    
    @Modifying
    @Query("DELETE FROM Chapter c WHERE c.subject.id = :subjectId")
    void deleteBySubjectId(@Param("subjectId") Long subjectId);
    
    // Find active chapters for tree structure
    List<Chapter> findByActiveTrue();
}

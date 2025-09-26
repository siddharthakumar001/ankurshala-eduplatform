package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long>, JpaSpecificationExecutor<Topic> {
    Optional<Topic> findByChapterIdAndTitle(Long chapterId, String title);
    Optional<Topic> findByCode(String code);
    
    @Query("SELECT t FROM Topic t WHERE " +
           "(:chapterId IS NULL OR t.chapter.id = :chapterId) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.summary) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR t.active = :active) AND " +
           "t.deletedAt IS NULL")
    Page<Topic> findTopicsWithFilters(@Param("chapterId") Long chapterId,
                                     @Param("search") String search, 
                                     @Param("active") Boolean active, 
                                     Pageable pageable);
    
    // Count methods for analytics
    long countByActiveTrue();
}

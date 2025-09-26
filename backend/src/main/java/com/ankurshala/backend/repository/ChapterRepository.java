package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Chapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

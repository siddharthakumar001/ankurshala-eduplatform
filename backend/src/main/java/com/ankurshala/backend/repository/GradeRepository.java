package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Grade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findByName(String name);
    
    @Query("SELECT g FROM Grade g WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(g.displayName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:boardId IS NULL OR g.boardId = :boardId) AND " +
           "(:active IS NULL OR g.active = :active)")
    Page<Grade> findGradesWithFilters(@Param("search") String search, 
                                     @Param("boardId") Long boardId,
                                     @Param("active") Boolean active, 
                                     Pageable pageable);
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    Optional<Board> findByName(String name);
    
    @Query("SELECT b FROM Board b WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR b.active = :active)")
    Page<Board> findBoardsWithFilters(@Param("search") String search, 
                                     @Param("active") Boolean active, 
                                     Pageable pageable);
}

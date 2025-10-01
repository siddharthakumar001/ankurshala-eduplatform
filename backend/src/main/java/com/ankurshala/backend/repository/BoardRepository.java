package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Board;
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
public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
    Optional<Board> findByName(String name);
    
    // Find by ID excluding soft deleted
    @Query("SELECT b FROM Board b WHERE b.id = :id AND b.softDeleted = false")
    Optional<Board> findByIdAndSoftDeletedFalse(@Param("id") Long id);
    
    // Filter with soft delete consideration
    @Query("SELECT b FROM Board b WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:active IS NULL OR b.active = :active) AND " +
           "b.softDeleted = false")
    Page<Board> findBoardsWithFilters(@Param("search") String search, 
                                     @Param("active") Boolean active, 
                                     Pageable pageable);
    
    // Check if board name exists (excluding current board for updates)
    @Query("SELECT COUNT(b) > 0 FROM Board b WHERE LOWER(b.name) = LOWER(:name) AND b.id != :excludeId AND b.softDeleted = false")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);
    
    // Check if board name exists for new board
    @Query("SELECT COUNT(b) > 0 FROM Board b WHERE LOWER(b.name) = LOWER(:name) AND b.softDeleted = false")
    boolean existsByNameIgnoreCase(@Param("name") String name);
    
    // Count methods for impact analysis
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.boardId = :boardId AND g.softDeleted = false")
    long countByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.boardId = :boardId AND s.softDeleted = false")
    long countSubjectsByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.boardId = :boardId AND c.softDeleted = false")
    long countChaptersByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.boardId = :boardId AND t.softDeleted = false")
    long countTopicsByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn WHERE tn.boardId = :boardId AND tn.softDeleted = false")
    long countTopicNotesByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    // Count methods for analytics
    long countByActiveTrueAndSoftDeletedFalse();
    
    // Find boards by ID for cascading operations
    @Query("SELECT b FROM Board b WHERE b.id = :boardId AND b.softDeleted = false")
    List<Board> findByBoardIdAndSoftDeletedFalse(@Param("boardId") Long boardId);
    
    // Count dependent entities for impact analysis
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.boardId = :boardId AND g.softDeleted = false")
    long countGradesByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.boardId = :boardId AND s.softDeleted = false")
    long countSubjectsByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.boardId = :boardId AND c.softDeleted = false")
    long countChaptersByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.boardId = :boardId AND t.softDeleted = false")
    long countTopicsByBoardId(@Param("boardId") Long boardId);
    
    @Query("SELECT COUNT(tn) FROM TopicNote tn JOIN tn.topic t WHERE t.boardId = :boardId AND tn.softDeleted = false")
    long countTopicNotesByBoardId(@Param("boardId") Long boardId);
    
    // Additional methods for service compatibility
    @Query("SELECT b FROM Board b WHERE b.active = true AND b.softDeleted = false ORDER BY b.name")
    List<Board> findByActiveTrue();
    
    @Query("SELECT b FROM Board b WHERE b.softDeleted = false ORDER BY b.name")
    List<Board> findAllNotSoftDeleted();
    
    @Query("SELECT b FROM Board b WHERE b.active = true AND b.softDeleted = false ORDER BY b.name")
    List<Board> findActiveBoardsForDropdown();
}

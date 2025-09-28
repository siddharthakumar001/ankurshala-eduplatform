package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Subject;
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
public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    Optional<Subject> findByName(String name);
    
    Optional<Subject> findByBoardIdAndName(Long boardId, String name);
    
    Optional<Subject> findByGradeIdAndName(Long gradeId, String name);
    
    @Query("SELECT s FROM Subject s WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:boardId IS NULL OR s.boardId = :boardId) AND " +
           "(:gradeId IS NULL OR s.gradeId = :gradeId) AND " +
           "(:active IS NULL OR s.active = :active)")
    Page<Subject> findSubjectsWithFilters(@Param("search") String search, 
                                         @Param("boardId") Long boardId,
                                         @Param("gradeId") Long gradeId,
                                         @Param("active") Boolean active, 
                                         Pageable pageable);
    
    // Count methods for analytics
    long countByActiveTrue();
    
    // Count chapters by subject ID for deletion impact analysis
    @Query("SELECT COUNT(c) FROM Chapter c WHERE c.subject.id = :subjectId")
    long countChaptersBySubjectId(@Param("subjectId") Long subjectId);
    
    // Count subjects by grade ID for deletion impact analysis
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.gradeId = :gradeId")
    long countByGradeId(@Param("gradeId") Long gradeId);
    
    // Find subjects by grade ID for cascading delete
    @Query("SELECT s FROM Subject s WHERE s.gradeId = :gradeId")
    List<Subject> findByGradeId(@Param("gradeId") Long gradeId);
    
    // Delete subjects by grade ID for cascading delete
    @Modifying
    @Query("DELETE FROM Subject s WHERE s.gradeId = :gradeId")
    void deleteByGradeId(@Param("gradeId") Long gradeId);
    
    // Find active subjects for tree structure
    List<Subject> findByActiveTrue();
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    
    @Query("SELECT pr FROM PricingRule pr WHERE " +
           "(:boardId IS NULL OR pr.board.id = :boardId) AND " +
           "(:gradeId IS NULL OR pr.grade.id = :gradeId) AND " +
           "(:subjectId IS NULL OR pr.subject.id = :subjectId) AND " +
           "(:chapterId IS NULL OR pr.chapter.id = :chapterId) AND " +
           "(:topicId IS NULL OR pr.topic.id = :topicId) AND " +
           "(:active IS NULL OR pr.active = :active)")
    Page<PricingRule> findPricingRulesWithFilters(
            @Param("boardId") Long boardId,
            @Param("gradeId") Long gradeId,
            @Param("subjectId") Long subjectId,
            @Param("chapterId") Long chapterId,
            @Param("topicId") Long topicId,
            @Param("active") Boolean active,
            Pageable pageable);

    @Query("SELECT pr FROM PricingRule pr WHERE " +
           "pr.active = true AND " +
           "(:boardId IS NULL OR pr.board.id = :boardId) AND " +
           "(:gradeId IS NULL OR pr.grade.id = :gradeId) AND " +
           "(:subjectId IS NULL OR pr.subject.id = :subjectId) AND " +
           "(:chapterId IS NULL OR pr.chapter.id = :chapterId) AND " +
           "(:topicId IS NULL OR pr.topic.id = :topicId) " +
           "ORDER BY " +
           "CASE WHEN pr.topic.id IS NOT NULL THEN 1 " +
           "WHEN pr.chapter.id IS NOT NULL THEN 2 " +
           "WHEN pr.subject.id IS NOT NULL THEN 3 " +
           "WHEN pr.grade.id IS NOT NULL THEN 4 " +
           "WHEN pr.board.id IS NOT NULL THEN 5 " +
           "ELSE 6 END")
    List<PricingRule> findBestMatchPricingRule(
            @Param("boardId") Long boardId,
            @Param("gradeId") Long gradeId,
            @Param("subjectId") Long subjectId,
            @Param("chapterId") Long chapterId,
            @Param("topicId") Long topicId);

    @Query("SELECT COUNT(pr) FROM PricingRule pr WHERE pr.active = true")
    long countActiveRules();
    
    // Count pricing rules by board ID
    long countByBoardId(Long boardId);
    
    // Delete pricing rules by board ID
    void deleteByBoardId(Long boardId);
}

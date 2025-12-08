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

    /**
     * Find pricing rules with filters
     */
    @Query("SELECT pr FROM PricingRule pr WHERE " +
           "(:boardId IS NULL OR pr.board.id = :boardId) AND " +
           "(:gradeId IS NULL OR pr.grade.id = :gradeId) AND " +
           "(:subjectId IS NULL OR pr.subject.id = :subjectId) AND " +
           "(:chapterId IS NULL OR pr.chapter.id = :chapterId) AND " +
           "(:topicId IS NULL OR pr.topic.id = :topicId) AND " +
           "(:active IS NULL OR pr.active = :active)")
    Page<PricingRule> findPricingRulesWithFilters(@Param("boardId") Long boardId,
                                                  @Param("gradeId") Long gradeId,
                                                  @Param("subjectId") Long subjectId,
                                                  @Param("chapterId") Long chapterId,
                                                  @Param("topicId") Long topicId,
                                                  @Param("active") Boolean active,
                                                  Pageable pageable);

    /**
     * Find pricing rule by topic ID and category
     */
    @Query("SELECT pr FROM PricingRule pr WHERE pr.topic.id = :topicId AND pr.active = true")
    Optional<PricingRule> findByTopicId(@Param("topicId") Long topicId);

    /**
     * Find pricing rule by chapter ID
     */
    @Query("SELECT pr FROM PricingRule pr WHERE pr.chapter.id = :chapterId AND pr.active = true")
    Optional<PricingRule> findByChapterId(@Param("chapterId") Long chapterId);

    /**
     * Find pricing rule by subject ID
     */
    @Query("SELECT pr FROM PricingRule pr WHERE pr.subject.id = :subjectId AND pr.active = true")
    Optional<PricingRule> findBySubjectId(@Param("subjectId") Long subjectId);

    /**
     * Find pricing rule by grade ID
     */
    @Query("SELECT pr FROM PricingRule pr WHERE pr.grade.id = :gradeId AND pr.active = true")
    Optional<PricingRule> findByGradeId(@Param("gradeId") Long gradeId);

    /**
     * Find pricing rule by board ID
     */
    @Query("SELECT pr FROM PricingRule pr WHERE pr.board.id = :boardId AND pr.active = true")
    Optional<PricingRule> findByBoardId(@Param("boardId") Long boardId);

    /**
     * Find pricing rules by active status
     */
    List<PricingRule> findByActiveTrueOrderByCreatedAtDesc();

    /**
     * Find pricing rules by active status
     */
    List<PricingRule> findByActiveOrderByCreatedAtDesc(Boolean active);

    /**
     * Find most specific pricing rule for given parameters
     */
    @Query("SELECT pr FROM PricingRule pr WHERE " +
           "pr.active = true AND " +
           "(:topicId IS NULL OR pr.topic.id = :topicId) AND " +
           "(:chapterId IS NULL OR pr.chapter.id = :chapterId) AND " +
           "(:subjectId IS NULL OR pr.subject.id = :subjectId) AND " +
           "(:gradeId IS NULL OR pr.grade.id = :gradeId) AND " +
           "(:boardId IS NULL OR pr.board.id = :boardId) " +
           "ORDER BY " +
           "CASE WHEN pr.topic.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.chapter.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.subject.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.grade.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.board.id IS NOT NULL THEN 1 ELSE 2 END")
    List<PricingRule> findMostSpecificRule(@Param("boardId") Long boardId,
                                         @Param("gradeId") Long gradeId,
                                         @Param("subjectId") Long subjectId,
                                         @Param("chapterId") Long chapterId,
                                         @Param("topicId") Long topicId);

    /**
     * Check if pricing rule exists for given scope
     */
    @Query("SELECT COUNT(pr) > 0 FROM PricingRule pr WHERE " +
           "pr.active = true AND " +
           "COALESCE(pr.board.id, 0) = COALESCE(:boardId, 0) AND " +
           "COALESCE(pr.grade.id, 0) = COALESCE(:gradeId, 0) AND " +
           "COALESCE(pr.subject.id, 0) = COALESCE(:subjectId, 0) AND " +
           "COALESCE(pr.chapter.id, 0) = COALESCE(:chapterId, 0) AND " +
           "COALESCE(pr.topic.id, 0) = COALESCE(:topicId, 0)")
    boolean existsByScope(@Param("boardId") Long boardId,
                         @Param("gradeId") Long gradeId,
                         @Param("subjectId") Long subjectId,
                         @Param("chapterId") Long chapterId,
                         @Param("topicId") Long topicId);

    /**
     * Find best match pricing rule for given parameters
     */
    @Query("SELECT pr FROM PricingRule pr WHERE " +
           "pr.active = true AND " +
           "(:topicId IS NULL OR pr.topic.id = :topicId) AND " +
           "(:chapterId IS NULL OR pr.chapter.id = :chapterId) AND " +
           "(:subjectId IS NULL OR pr.subject.id = :subjectId) AND " +
           "(:gradeId IS NULL OR pr.grade.id = :gradeId) AND " +
           "(:boardId IS NULL OR pr.board.id = :boardId) " +
           "ORDER BY " +
           "CASE WHEN pr.topic.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.chapter.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.subject.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.grade.id IS NOT NULL THEN 1 ELSE 2 END, " +
           "CASE WHEN pr.board.id IS NOT NULL THEN 1 ELSE 2 END " +
           "LIMIT 1")
    Optional<PricingRule> findBestMatchPricingRule(@Param("boardId") Long boardId,
                                                  @Param("gradeId") Long gradeId,
                                                  @Param("subjectId") Long subjectId,
                                                  @Param("chapterId") Long chapterId,
                                                  @Param("topicId") Long topicId);
}
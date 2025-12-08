package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Policy;
import com.ankurshala.backend.entity.PolicyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    
    @Query("SELECT p FROM Policy p WHERE p.active = true AND p.effectiveDate <= :now AND (p.expiryDate IS NULL OR p.expiryDate > :now)")
    List<Policy> findByActiveTrueAndEffectiveDateLessThanEqualAndExpiryDateAfterOrExpiryDateIsNull(
            @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Policy p WHERE p.category = :category AND p.active = true AND p.effectiveDate <= :now AND (p.expiryDate IS NULL OR p.expiryDate > :now)")
    List<Policy> findByCategoryAndActiveTrueAndEffectiveDateLessThanEqualAndExpiryDateAfterOrExpiryDateIsNull(
            @Param("category") PolicyCategory category, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Policy p WHERE p.category = :category AND p.active = true AND p.effectiveDate <= :now AND (p.expiryDate IS NULL OR p.expiryDate > :now) ORDER BY p.effectiveDate DESC")
    Optional<Policy> findTopByCategoryAndActiveTrueAndEffectiveDateLessThanEqualAndExpiryDateAfterOrExpiryDateIsNullOrderByEffectiveDateDesc(
            @Param("category") PolicyCategory category, @Param("now") LocalDateTime now);
}

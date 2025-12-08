package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    
    @Query("SELECT fs FROM FeeStructure fs WHERE fs.category.name = :categoryName AND fs.active = true AND fs.effectiveDate <= :now AND (fs.expiryDate IS NULL OR fs.expiryDate > :now)")
    Optional<FeeStructure> findActiveByCategoryNameAndEffectiveDate(@Param("categoryName") String categoryName, @Param("now") LocalDateTime now);
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PolicyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyCategoryRepository extends JpaRepository<PolicyCategory, Long> {
    Optional<PolicyCategory> findByNameAndActiveTrue(String name);
}

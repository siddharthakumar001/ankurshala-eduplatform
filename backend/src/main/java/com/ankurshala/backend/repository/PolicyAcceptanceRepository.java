package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PolicyAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyAcceptanceRepository extends JpaRepository<PolicyAcceptance, Long> {
    Optional<PolicyAcceptance> findByUserIdAndPolicyId(Long userId, Long policyId);
    boolean existsByUserIdAndPolicyId(Long userId, Long policyId);
    List<PolicyAcceptance> findByUserIdOrderByAcceptedAtDesc(Long userId);
}

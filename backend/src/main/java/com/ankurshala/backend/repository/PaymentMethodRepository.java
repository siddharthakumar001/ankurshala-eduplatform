package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(Long userId);
    List<PaymentMethod> findByUserIdAndIsActiveTrueAndIdNotOrderByCreatedAtAsc(Long userId, Long excludeId);
    Optional<PaymentMethod> findByProviderId(String providerId);
    List<PaymentMethod> findByUserIdAndIsDefaultTrueAndIsActiveTrue(Long userId);
}

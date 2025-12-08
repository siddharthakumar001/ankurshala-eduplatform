package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.entity.PaymentRefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    List<PaymentRefund> findByPaymentIntentIdOrderByCreatedAtDesc(Long paymentIntentId);
    List<PaymentRefund> findByStatusOrderByCreatedAtDesc(PaymentRefundStatus status);
    Optional<PaymentRefund> findByProviderRefundId(String providerRefundId);
}

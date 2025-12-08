package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentIntentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
    List<PaymentIntent> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PaymentIntent> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    List<PaymentIntent> findByStatusOrderByCreatedAtDesc(PaymentIntentStatus status);
    Optional<PaymentIntent> findByProviderPaymentId(String providerPaymentId);
    Optional<PaymentIntent> findByProviderOrderId(String providerOrderId);
    Optional<PaymentIntent> findByBookingIdAndProviderOrderId(Long bookingId, String providerOrderId);
}

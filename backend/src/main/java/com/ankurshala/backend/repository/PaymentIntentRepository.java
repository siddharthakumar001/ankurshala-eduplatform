package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentIntentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
    List<PaymentIntent> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PaymentIntent> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    List<PaymentIntent> findByStatusOrderByCreatedAtDesc(PaymentIntentStatus status);
    List<PaymentIntent> findTop10ByStatusOrderByCreatedAtDesc(PaymentIntentStatus status);
    Optional<PaymentIntent> findByProviderPaymentId(String providerPaymentId);
    Optional<PaymentIntent> findByProviderOrderId(String providerOrderId);
    Optional<PaymentIntent> findByBookingIdAndProviderOrderId(Long bookingId, String providerOrderId);

    @Query("SELECT COALESCE(SUM(pi.amountCents), 0) FROM PaymentIntent pi WHERE pi.status = :status")
    Long sumAmountCentsByStatus(@Param("status") PaymentIntentStatus status);

    @Query("SELECT COALESCE(SUM(pi.amountCents), 0) FROM PaymentIntent pi WHERE pi.status = :status AND pi.createdAt >= :start AND pi.createdAt < :end")
    Long sumAmountCentsByStatusAndCreatedAtBetween(
        @Param("status") PaymentIntentStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.PaymentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentMetricRepository extends JpaRepository<PaymentMetric, Long> {

    List<PaymentMetric> findByMetricTypeAndTimestampBetween(
            String metricType, 
            LocalDateTime start, 
            LocalDateTime end
    );

    List<PaymentMetric> findByProviderAndTimestampBetween(
            String provider, 
            LocalDateTime start, 
            LocalDateTime end
    );

    List<PaymentMetric> findByUserIdAndTimestampBetween(
            String userId, 
            LocalDateTime start, 
            LocalDateTime end
    );

    @Query("SELECT pm FROM PaymentMetric pm WHERE pm.success = false AND pm.timestamp >= :since")
    List<PaymentMetric> findRecentFailures(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(pm.executionTime) FROM PaymentMetric pm WHERE pm.operation = :operation AND pm.timestamp >= :since")
    Double getAverageExecutionTime(@Param("operation") String operation, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(pm) FROM PaymentMetric pm WHERE pm.provider = :provider AND pm.success = true AND pm.timestamp >= :since")
    Long countSuccessfulTransactions(@Param("provider") String provider, @Param("since") LocalDateTime since);
}

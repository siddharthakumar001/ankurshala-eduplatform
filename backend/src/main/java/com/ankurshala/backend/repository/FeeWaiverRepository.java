package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.FeeWaiver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeeWaiverRepository extends JpaRepository<FeeWaiver, Long> {
    
    @Query("SELECT fw FROM FeeWaiver fw WHERE " +
           "(:userId IS NULL OR fw.user.id = :userId) AND " +
           "(:bookingId IS NULL OR fw.bookingId = :bookingId)")
    Page<FeeWaiver> findFeeWaiversWithFilters(
            @Param("userId") Long userId,
            @Param("bookingId") Long bookingId,
            Pageable pageable);

    @Query("SELECT COUNT(fw) FROM FeeWaiver fw WHERE fw.createdAt >= :from AND fw.createdAt <= :to")
    long countByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT SUM(fw.amount) FROM FeeWaiver fw WHERE fw.createdAt >= :from AND fw.createdAt <= :to")
    Double sumAmountByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}

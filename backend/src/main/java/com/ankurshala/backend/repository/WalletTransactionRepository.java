package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.WalletTransaction;
import com.ankurshala.backend.entity.WalletOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
            WalletOwnerType ownerType, Long ownerId);

    @Query("SELECT SUM(wt.amountCents) FROM WalletTransaction wt WHERE wt.ownerType = :ownerType AND wt.ownerId = :ownerId AND wt.type = 'CREDIT'")
    Long getTotalCredits(@Param("ownerType") WalletOwnerType ownerType,
                        @Param("ownerId") Long ownerId);

    @Query("SELECT SUM(wt.amountCents) FROM WalletTransaction wt WHERE wt.ownerType = :ownerType AND wt.ownerId = :ownerId AND wt.type = 'DEBIT'")
    Long getTotalDebits(@Param("ownerType") WalletOwnerType ownerType,
                      @Param("ownerId") Long ownerId);

    List<WalletTransaction> findByOwnerTypeAndOwnerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            WalletOwnerType ownerType, Long ownerId,
            LocalDateTime startDate, LocalDateTime endDate);
}

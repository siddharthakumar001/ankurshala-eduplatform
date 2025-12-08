package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.FeeWaiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeWaiverRepository extends JpaRepository<FeeWaiver, Long> {
    List<FeeWaiver> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<FeeWaiver> findByStatusOrderByCreatedAtDesc(FeeWaiver.WaiverStatus status);
    boolean existsByUser_IdAndStatus(Long userId, FeeWaiver.WaiverStatus status);
}

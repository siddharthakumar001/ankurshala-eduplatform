package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.FeeApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeApplicationRepository extends JpaRepository<FeeApplication, Long> {
    List<FeeApplication> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
}

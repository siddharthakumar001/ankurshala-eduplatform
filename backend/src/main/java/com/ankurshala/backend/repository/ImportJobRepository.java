package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.ImportJob;
import com.ankurshala.backend.entity.ImportJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    
    // Find by status
    List<ImportJob> findByStatus(ImportJobStatus status);
    
    // Find by file name
    List<ImportJob> findByFileNameContainingIgnoreCase(String fileName);
    
    // Find recent jobs
    List<ImportJob> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime dateTime);
    
    // Find jobs by user
    @Query("SELECT ij FROM ImportJob ij WHERE ij.createdBy.id = :userId ORDER BY ij.createdAt DESC")
    List<ImportJob> findByCreatedByUserId(@Param("userId") Long userId);
    
    // Find active jobs (not completed)
    @Query("SELECT ij FROM ImportJob ij WHERE ij.status IN ('PENDING', 'PROCESSING') ORDER BY ij.createdAt DESC")
    List<ImportJob> findActiveJobs();
    
    // Find completed jobs
    @Query("SELECT ij FROM ImportJob ij WHERE ij.status IN ('COMPLETED', 'FAILED', 'CANCELLED') ORDER BY ij.createdAt DESC")
    Page<ImportJob> findCompletedJobs(Pageable pageable);
    
    // Count by status
    long countByStatus(ImportJobStatus status);
    
    // Find latest job
    Optional<ImportJob> findFirstByOrderByCreatedAtDesc();
}

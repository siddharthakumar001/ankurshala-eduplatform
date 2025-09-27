package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.TeacherStatus;
import com.ankurshala.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUser(User user);
    Optional<TeacherProfile> findByUserId(Long userId);
    long countByUserEnabledTrue();
    long countByUserEnabledFalse();
    long countByUserCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Simple query to get all teacher profiles first
    @Query("SELECT tp FROM TeacherProfile tp")
    Page<TeacherProfile> findAllTeacherProfiles(Pageable pageable);

    // Admin queries for teacher management - simplified version first
    @Query("SELECT tp FROM TeacherProfile tp LEFT JOIN tp.user u LEFT JOIN tp.teacher t WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "COALESCE(LOWER(tp.firstName), '') LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) OR " +
           "COALESCE(LOWER(tp.lastName), '') LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) OR " +
           "COALESCE(LOWER(u.email), '') LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%')) OR " +
           "COALESCE(LOWER(tp.specialization), '') LIKE LOWER(CONCAT('%', COALESCE(:search, ''), '%'))) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:verified IS NULL OR tp.verified = :verified)")
    Page<TeacherProfile> findTeachersWithFilters(
            @Param("search") String search,
            @Param("enabled") Boolean enabled,
            @Param("status") TeacherStatus status,
            @Param("verified") Boolean verified,
            Pageable pageable);
    
    // Check if mobile number exists
    boolean existsByMobileNumber(String mobileNumber);
}

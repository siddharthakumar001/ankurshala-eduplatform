package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser(User user);
    Optional<StudentProfile> findByUserId(Long userId);
    long countByUserEnabledTrue();
    long countByUserCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

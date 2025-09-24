package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUser(User user);
    Optional<TeacherProfile> findByUserId(Long userId);
    long countByUserEnabledTrue();
    long countByUserCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

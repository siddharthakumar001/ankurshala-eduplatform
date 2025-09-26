package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
    // Methods for notification service
    List<User> findByRole(String role);
    List<User> findByRoleIn(List<String> roles);
}

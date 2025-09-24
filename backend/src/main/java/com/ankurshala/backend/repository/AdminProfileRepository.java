package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.AdminProfile;
import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, Long> {
    Optional<AdminProfile> findByUser(User user);
    Optional<AdminProfile> findByUserId(Long userId);
}

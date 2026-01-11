package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentFocusSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for StudentFocusSettings entity.
 */
@Repository
public interface StudentFocusSettingsRepository extends JpaRepository<StudentFocusSettings, Long> {

    Optional<StudentFocusSettings> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);
}


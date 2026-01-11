package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentPracticePreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPracticePreferencesRepository extends JpaRepository<StudentPracticePreferences, Long> {

    Optional<StudentPracticePreferences> findByStudentId(Long studentId);

    /**
     * Find all students with daily practice enabled
     */
    @Query("SELECT p FROM StudentPracticePreferences p WHERE p.enabled = true")
    List<StudentPracticePreferences> findAllEnabled();

    /**
     * Find all students with daily practice enabled and matching preferred time
     */
    @Query("SELECT p FROM StudentPracticePreferences p WHERE p.enabled = true AND p.preferredTimeLocal = :time")
    List<StudentPracticePreferences> findAllEnabledByPreferredTime(@Param("time") String time);
}


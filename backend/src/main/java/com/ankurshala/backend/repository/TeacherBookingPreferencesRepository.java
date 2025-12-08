package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherBookingPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherBookingPreferencesRepository extends JpaRepository<TeacherBookingPreferences, Long> {
    
    Optional<TeacherBookingPreferences> findByTeacherId(Long teacherId);
    
    void deleteByTeacherId(Long teacherId);
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    boolean existsByTeacher(Teacher teacher);
}

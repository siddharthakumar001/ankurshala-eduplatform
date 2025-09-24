package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherExperienceRepository extends JpaRepository<TeacherExperience, Long> {
}

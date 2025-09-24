package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherQualificationRepository extends JpaRepository<TeacherQualification, Long> {
}

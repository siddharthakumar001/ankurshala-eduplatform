package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherCertificationRepository extends JpaRepository<TeacherCertification, Long> {
}

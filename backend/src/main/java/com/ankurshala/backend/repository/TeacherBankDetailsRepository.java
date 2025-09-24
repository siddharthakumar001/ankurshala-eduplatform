package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherBankDetailsRepository extends JpaRepository<TeacherBankDetails, Long> {
}

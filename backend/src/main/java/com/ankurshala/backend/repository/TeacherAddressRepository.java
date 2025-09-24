package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherAddressRepository extends JpaRepository<TeacherAddress, Long> {
}

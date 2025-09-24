package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User user);
    Optional<Teacher> findByUserId(Long userId);
}

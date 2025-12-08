package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherWalletRepository extends JpaRepository<TeacherWallet, Long> {
    Optional<TeacherWallet> findByTeacherId(Long teacherId);
}
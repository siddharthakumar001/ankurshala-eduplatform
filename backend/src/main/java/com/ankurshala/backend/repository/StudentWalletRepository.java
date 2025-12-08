package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentWalletRepository extends JpaRepository<StudentWallet, Long> {
    Optional<StudentWallet> findByStudentId(Long studentId);
}
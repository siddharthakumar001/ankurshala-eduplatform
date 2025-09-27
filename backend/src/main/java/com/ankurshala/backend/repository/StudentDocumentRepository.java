package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
}


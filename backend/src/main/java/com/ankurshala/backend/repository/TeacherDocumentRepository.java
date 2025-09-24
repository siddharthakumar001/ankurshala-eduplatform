package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherDocumentRepository extends JpaRepository<TeacherDocument, Long> {
}

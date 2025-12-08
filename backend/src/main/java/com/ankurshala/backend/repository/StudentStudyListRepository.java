package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.StudentStudyList;
import com.ankurshala.backend.entity.StudentStudyList.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentStudyListRepository extends JpaRepository<StudentStudyList, Long> {
    
    /**
     * Find all study list items for a student
     */
    List<StudentStudyList> findByStudentIdOrderByAddedAtDesc(Long studentId);
    
    /**
     * Find study list items by student and status
     */
    List<StudentStudyList> findByStudentIdAndStatusOrderByAddedAtDesc(Long studentId, StudyStatus status);
    
    /**
     * Find study list items by student with pagination
     */
    Page<StudentStudyList> findByStudentIdOrderByAddedAtDesc(Long studentId, Pageable pageable);
    
    /**
     * Find a specific study list item by student and topic
     */
    Optional<StudentStudyList> findByStudentIdAndTopicId(Long studentId, Long topicId);
    
    /**
     * Check if a topic is already in student's study list
     */
    boolean existsByStudentIdAndTopicId(Long studentId, Long topicId);
    
    /**
     * Count study list items by student and status
     */
    long countByStudentIdAndStatus(Long studentId, StudyStatus status);
    
    /**
     * Count total study list items for a student
     */
    long countByStudentId(Long studentId);
    
    /**
     * Delete study list item by student and id
     */
    void deleteByIdAndStudentId(Long id, Long studentId);
    
    /**
     * Find study list with topic details (JOIN with topics table)
     */
    @Query("SELECT s FROM StudentStudyList s JOIN FETCH s.topicId WHERE s.studentId = :studentId ORDER BY s.addedAt DESC")
    List<StudentStudyList> findByStudentIdWithTopics(@Param("studentId") Long studentId);
}

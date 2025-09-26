package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TopicNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicNoteRepository extends JpaRepository<TopicNote, Long>, JpaSpecificationExecutor<TopicNote> {
    
    @Query("SELECT tn FROM TopicNote tn WHERE tn.topic.id = :topicId AND tn.deletedAt IS NULL")
    Page<TopicNote> findByTopicIdAndNotDeleted(@Param("topicId") Long topicId, Pageable pageable);
    
    @Query("SELECT tn FROM TopicNote tn WHERE tn.topic.id = :topicId AND tn.active = :active AND tn.deletedAt IS NULL")
    Page<TopicNote> findByTopicIdAndActiveAndNotDeleted(@Param("topicId") Long topicId, @Param("active") Boolean active, Pageable pageable);
    
    @Query("SELECT tn FROM TopicNote tn WHERE tn.deletedAt IS NULL")
    Page<TopicNote> findAllNotDeleted(Pageable pageable);
    
    @Query("SELECT tn FROM TopicNote tn WHERE tn.active = :active AND tn.deletedAt IS NULL")
    Page<TopicNote> findByActiveAndNotDeleted(@Param("active") Boolean active, Pageable pageable);
    
    List<TopicNote> findByTopicIdAndDeletedAtIsNull(Long topicId);
}
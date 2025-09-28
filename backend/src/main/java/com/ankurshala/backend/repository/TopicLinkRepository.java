package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TopicLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicLinkRepository extends JpaRepository<TopicLink, Long> {
    
    List<TopicLink> findByTopicId(Long topicId);
    
    @Modifying
    @Query("DELETE FROM TopicLink tl WHERE tl.topic.id = :topicId")
    void deleteByTopicId(@Param("topicId") Long topicId);
    
    @Modifying
    @Query("DELETE FROM TopicLink tl WHERE tl.topic.chapter.id = :chapterId")
    void deleteByChapterId(@Param("chapterId") Long chapterId);
}

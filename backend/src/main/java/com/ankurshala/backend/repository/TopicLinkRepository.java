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
    
    /**
     * Find links by topic and type (PREREQUISITE or RELATED)
     */
    @Query("SELECT tl FROM TopicLink tl WHERE tl.topic.id = :topicId AND tl.type = :type")
    List<TopicLink> findByTopicIdAndType(@Param("topicId") Long topicId, @Param("type") TopicLink.TopicLinkType type);
    
    /**
     * Find all prerequisite topics for a given topic
     */
    @Query("SELECT tl.linkedTopic FROM TopicLink tl WHERE tl.topic.id = :topicId AND tl.type = 'PREREQUISITE'")
    List<com.ankurshala.backend.entity.Topic> findPrerequisiteTopics(@Param("topicId") Long topicId);
    
    /**
     * Find topics that have this topic as a prerequisite (reverse lookup)
     */
    @Query("SELECT tl.topic FROM TopicLink tl WHERE tl.linkedTopic.id = :topicId AND tl.type = 'PREREQUISITE'")
    List<com.ankurshala.backend.entity.Topic> findTopicsRequiringThis(@Param("topicId") Long topicId);
    
    @Modifying
    @Query("DELETE FROM TopicLink tl WHERE tl.topic.id = :topicId")
    void deleteByTopicId(@Param("topicId") Long topicId);
    
    @Modifying
    @Query("DELETE FROM TopicLink tl WHERE tl.topic.chapter.id = :chapterId")
    void deleteByChapterId(@Param("chapterId") Long chapterId);
}

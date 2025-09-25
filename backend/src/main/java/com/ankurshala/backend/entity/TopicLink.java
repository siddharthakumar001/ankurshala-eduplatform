package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_links")
public class TopicLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TopicLinkType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_topic_id", nullable = false)
    private Topic linkedTopic;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TopicLink() {}

    public TopicLink(Topic topic, TopicLinkType type, Topic linkedTopic) {
        this.topic = topic;
        this.type = type;
        this.linkedTopic = linkedTopic;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public TopicLinkType getType() { return type; }
    public void setType(TopicLinkType type) { this.type = type; }

    public Topic getLinkedTopic() { return linkedTopic; }
    public void setLinkedTopic(Topic linkedTopic) { this.linkedTopic = linkedTopic; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public enum TopicLinkType {
        PREREQUISITE, RELATED
    }
}

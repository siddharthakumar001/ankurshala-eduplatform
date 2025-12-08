package com.ankurshala.backend.dto.ai;

import lombok.Data;

@Data
public class TopicRecommendation {
    private Long topicId;
    private String title;
    private String description;
    private Double confidence;
    private String reason;
}

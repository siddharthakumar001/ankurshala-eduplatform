package com.ankurshala.backend.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class RecommendationsResponse {
    private List<TopicRecommendation> recommendations;
    private String algorithm;
    private Long studentId;
}

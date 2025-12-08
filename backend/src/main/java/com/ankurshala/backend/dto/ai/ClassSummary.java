package com.ankurshala.backend.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class ClassSummary {
    private Long bookingId;
    private String summary;
    private List<String> keyPoints;
    private List<String> actionItems;
    private String difficulty;
    private String nextSteps;
}

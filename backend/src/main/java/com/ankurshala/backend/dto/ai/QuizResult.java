package com.ankurshala.backend.dto.ai;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class QuizResult {
    private Long resultId;
    private Long studentId;
    private Long subjectId;
    private Long topicId;
    private BigDecimal score;
    private Integer total;
    private Integer correct;
    private Map<String, Object> breakdown;
    private String feedback;
}

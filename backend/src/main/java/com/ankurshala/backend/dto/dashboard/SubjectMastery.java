package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

@Data
public class SubjectMastery {
    private Long subjectId;
    private String subjectName;
    private Double masteryPercentage;
    private Long totalQuizzes;
    private Double averageScore;
}

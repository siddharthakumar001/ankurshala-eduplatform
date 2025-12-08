package com.ankurshala.backend.dto.ai;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerateQuizRequest {
    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private Long topicId;

    @NotNull(message = "Number of questions is required")
    private Integer numQuestions = 5;

    private String difficulty = "MEDIUM"; // EASY, MEDIUM, HARD
}

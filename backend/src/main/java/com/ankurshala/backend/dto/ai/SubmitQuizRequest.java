package com.ankurshala.backend.dto.ai;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SubmitQuizRequest {
    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    @NotEmpty(message = "Answers are required")
    private List<Integer> answers; // Student's answers (0-based indices)

    private Map<String, Object> metadata; // Additional quiz metadata
}

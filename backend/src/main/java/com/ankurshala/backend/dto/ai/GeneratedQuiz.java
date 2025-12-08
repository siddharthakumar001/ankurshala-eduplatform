package com.ankurshala.backend.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class GeneratedQuiz {
    private Long quizId;
    private String title;
    private String description;
    private List<QuizQuestion> questions;
    private Integer totalQuestions;
    private String difficulty;
}

package com.ankurshala.backend.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class QuizQuestion {
    private String question;
    private List<String> options;
    private Integer correctAnswer; // Index of correct option (0-based)
    private String explanation;
}

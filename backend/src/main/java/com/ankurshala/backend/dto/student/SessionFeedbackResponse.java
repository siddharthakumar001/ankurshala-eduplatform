package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionFeedbackResponse {
    private boolean success;
    private String message;
    private LocalDateTime submittedAt;
    private Long feedbackId;
}

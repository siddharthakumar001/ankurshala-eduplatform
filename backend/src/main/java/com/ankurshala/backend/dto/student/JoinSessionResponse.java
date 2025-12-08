package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JoinSessionResponse {
    private boolean success;
    private String sessionUrl;
    private String sessionId;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private String message;
    private boolean canJoin;
    private String reason;
}

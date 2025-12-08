package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JoinSessionRequest {
    private Long bookingId;
    private String joinToken;
    private String deviceInfo;
    private String browserInfo;
}

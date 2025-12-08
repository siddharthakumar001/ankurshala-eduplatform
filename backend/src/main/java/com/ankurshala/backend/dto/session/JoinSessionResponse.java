package com.ankurshala.backend.dto.session;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JoinSessionResponse {
    private String zoomJoinLink;
    private String zoomHostLink;
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String topicTitle;
    private String teacherName;
}

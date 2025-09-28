package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingNoteResponse {
    
    private Long id;
    private Long authorId;
    private String authorName;
    private String content;
    private String noteType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

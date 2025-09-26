package com.ankurshala.backend.dto.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicNoteDto {
    private Long id;
    private String title;
    private String content;
    private Long topicId;
    private String topicTitle;
    private Boolean active;
    private String attachments; // JSON string for now
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

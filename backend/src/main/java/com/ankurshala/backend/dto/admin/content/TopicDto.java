package com.ankurshala.backend.dto.admin.content;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response DTO for Topic entity
 */
@Data
@Builder
public class TopicDto {
    private Long id;
    private String title;
    private String code;
    private String description;
    private String summary;
    private Integer expectedTimeMins;
    private Long boardId;
    private String boardName;
    private Long gradeId;
    private String gradeName;
    private Long subjectId;
    private String subjectName;
    private Long chapterId;
    private String chapterName;
    private Boolean active;
    private Boolean softDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Count fields for statistics
    private Long notesCount;
}

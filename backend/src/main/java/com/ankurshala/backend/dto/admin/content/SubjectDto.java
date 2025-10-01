package com.ankurshala.backend.dto.admin.content;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response DTO for Subject entity
 */
@Data
@Builder
public class SubjectDto {
    private Long id;
    private String name;
    private Long boardId;
    private String boardName;
    private Long gradeId;
    private String gradeName;
    private Boolean active;
    private Boolean softDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Count fields for statistics
    private Long chaptersCount;
    private Long topicsCount;
}

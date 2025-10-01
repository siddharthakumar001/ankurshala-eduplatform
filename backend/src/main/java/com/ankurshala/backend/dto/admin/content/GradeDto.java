package com.ankurshala.backend.dto.admin.content;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response DTO for Grade entity
 */
@Data
@Builder
public class GradeDto {
    private Long id;
    private String name;
    private String displayName;
    private Long boardId;
    private String boardName;
    private Boolean active;
    private Boolean softDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Count fields for statistics
    private Long subjectsCount;
    private Long chaptersCount;
}

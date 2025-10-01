package com.ankurshala.backend.dto.admin.content;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response DTO for Board entity
 */
@Data
@Builder
public class BoardDto {
    private Long id;
    private String name;
    private Boolean active;
    private Boolean softDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Count fields for statistics
    private Long gradesCount;
    private Long subjectsCount;
}

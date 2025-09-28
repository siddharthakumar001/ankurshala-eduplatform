package com.ankurshala.backend.dto.content;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTopicRequest {
    
    @NotBlank(message = "Topic title is required")
    @Size(min = 2, max = 500, message = "Topic title must be between 2 and 500 characters")
    private String title;
    
    @Size(max = 50, message = "Topic code must not exceed 50 characters")
    private String code;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @Size(max = 1000, message = "Summary must not exceed 1000 characters")
    private String summary;
    
    @Min(value = 0, message = "Expected time must be non-negative")
    private Integer expectedTimeMins = 0;
    
    @NotNull(message = "Chapter ID is required")
    private Long chapterId;
    
    @NotNull(message = "Board ID is required")
    private Long boardId;
    
    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    
    private Boolean active = true;
}


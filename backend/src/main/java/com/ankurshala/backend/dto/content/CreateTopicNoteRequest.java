package com.ankurshala.backend.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTopicNoteRequest {
    
    @NotBlank(message = "Note title is required")
    @Size(min = 2, max = 200, message = "Note title must be between 2 and 200 characters")
    private String title;
    
    @NotBlank(message = "Note content is required")
    @Size(min = 10, max = 10000, message = "Note content must be between 10 and 10000 characters")
    private String content;
    
    @NotNull(message = "Topic ID is required")
    private Long topicId;
    
    private String attachments; // JSON string for now
    
    private Boolean active = true;
}

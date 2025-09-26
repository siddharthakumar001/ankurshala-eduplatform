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
public class UpdateChapterRequest {
    
    @NotBlank(message = "Chapter name is required")
    @Size(min = 2, max = 200, message = "Chapter name must be between 2 and 200 characters")
    private String name;
    
    @NotNull(message = "Subject ID is required")
    private Long subjectId;
    
    private Boolean active;
}

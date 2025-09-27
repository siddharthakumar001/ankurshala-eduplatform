package com.ankurshala.backend.dto.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDto {
    private Long id;
    private String name;
    private Long subjectId;
    private String subjectName;
    private Boolean active;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


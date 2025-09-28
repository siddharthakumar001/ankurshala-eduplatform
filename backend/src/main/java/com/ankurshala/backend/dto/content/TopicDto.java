package com.ankurshala.backend.dto.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDto {
    private Long id;
    private String title;
    private String code;
    private String description;
    private String summary;
    private Integer expectedTimeMins;
    private Long chapterId;
    private String chapterName;
    private String subjectName;
    private Long boardId;
    private Long subjectId;
    private Boolean active;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


package com.ankurshala.backend.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PricingRuleDto {
    private Long id;
    private Long boardId;
    private String boardName;
    private Long gradeId;
    private String gradeName;
    private Long subjectId;
    private String subjectName;
    private Long chapterId;
    private String chapterName;
    private Long topicId;
    private String topicTitle;
    private BigDecimal hourlyRate;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PricingRuleDto(Long id, Long boardId, String boardName, Long gradeId, String gradeName,
                         Long subjectId, String subjectName, Long chapterId, String chapterName,
                         Long topicId, String topicTitle, BigDecimal hourlyRate, Boolean active,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.boardId = boardId;
        this.boardName = boardName;
        this.gradeId = gradeId;
        this.gradeName = gradeName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.chapterId = chapterId;
        this.chapterName = chapterName;
        this.topicId = topicId;
        this.topicTitle = topicTitle;
        this.hourlyRate = hourlyRate;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

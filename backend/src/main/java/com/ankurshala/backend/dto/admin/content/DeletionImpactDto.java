package com.ankurshala.backend.dto.admin.content;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for deletion impact analysis
 */
@Data
@Builder
public class DeletionImpactDto {
    private String level;
    private Long boardId;
    private String boardName;
    private Long gradeId;
    private String gradeName;
    private Long subjectId;
    private String subjectName;
    private Long chapterId;
    private String chapterName;
    private Long topicId;
    private String topicName;
    
    // Counts of dependent records that will be affected
    private Long gradesCount = 0L;
    private Long subjectsCount = 0L;
    private Long chaptersCount = 0L;
    private Long topicsCount = 0L;
    private Long notesCount = 0L;
    private Long totalImpact = 0L;
    
    private List<String> warnings;
    private LocalDateTime analysisTimestamp;

    public DeletionImpactDto() {
        this.analysisTimestamp = LocalDateTime.now();
    }

    public DeletionImpactDto(String level, Long boardId, String boardName, Long gradeId, String gradeName, 
                           Long subjectId, String subjectName, Long chapterId, String chapterName, 
                           Long topicId, String topicName, Long gradesCount, Long subjectsCount, 
                           Long chaptersCount, Long topicsCount, Long notesCount, Long totalImpact, 
                           List<String> warnings, LocalDateTime analysisTimestamp) {
        this.level = level;
        this.boardId = boardId;
        this.boardName = boardName;
        this.gradeId = gradeId;
        this.gradeName = gradeName;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.chapterId = chapterId;
        this.chapterName = chapterName;
        this.topicId = topicId;
        this.topicName = topicName;
        this.gradesCount = gradesCount;
        this.subjectsCount = subjectsCount;
        this.chaptersCount = chaptersCount;
        this.topicsCount = topicsCount;
        this.notesCount = notesCount;
        this.totalImpact = totalImpact;
        this.warnings = warnings;
        this.analysisTimestamp = analysisTimestamp != null ? analysisTimestamp : LocalDateTime.now();
    }

    public void calculateTotalImpact() {
        this.totalImpact = gradesCount + subjectsCount + chaptersCount + topicsCount + notesCount;
    }
}
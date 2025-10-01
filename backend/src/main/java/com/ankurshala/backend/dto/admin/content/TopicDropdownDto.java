package com.ankurshala.backend.dto.admin.content;

/**
 * Dropdown DTO for Topic entity
 */
public class TopicDropdownDto {
    private Long id;
    private String title;
    private String code;
    private Long chapterId;
    private Long subjectId;
    private Long gradeId;
    private Long boardId;

    public TopicDropdownDto() {}

    public TopicDropdownDto(Long id, String title, String code, Long chapterId, Long subjectId, Long gradeId, Long boardId) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.chapterId = chapterId;
        this.subjectId = subjectId;
        this.gradeId = gradeId;
        this.boardId = boardId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
}

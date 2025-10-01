package com.ankurshala.backend.dto.admin.content;

/**
 * Dropdown DTO for Chapter entity
 */
public class ChapterDropdownDto {
    private Long id;
    private String name;
    private Long subjectId;
    private Long gradeId;
    private Long boardId;

    public ChapterDropdownDto() {}

    public ChapterDropdownDto(Long id, String name, Long subjectId, Long gradeId, Long boardId) {
        this.id = id;
        this.name = name;
        this.subjectId = subjectId;
        this.gradeId = gradeId;
        this.boardId = boardId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
}

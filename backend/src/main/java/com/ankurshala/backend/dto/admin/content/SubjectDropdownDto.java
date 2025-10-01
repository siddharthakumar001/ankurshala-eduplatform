package com.ankurshala.backend.dto.admin.content;

/**
 * Dropdown DTO for Subject entity
 */
public class SubjectDropdownDto {
    private Long id;
    private String name;
    private Long gradeId;
    private Long boardId;

    public SubjectDropdownDto() {}

    public SubjectDropdownDto(Long id, String name, Long gradeId, Long boardId) {
        this.id = id;
        this.name = name;
        this.gradeId = gradeId;
        this.boardId = boardId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getGradeId() { return gradeId; }
    public void setGradeId(Long gradeId) { this.gradeId = gradeId; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
}

package com.ankurshala.backend.dto.admin.content;

/**
 * Dropdown DTO for Grade entity
 */
public class GradeDropdownDto {
    private Long id;
    private String name;
    private String displayName;
    private Long boardId;

    public GradeDropdownDto() {}

    public GradeDropdownDto(Long id, String name, String displayName, Long boardId) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.boardId = boardId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
}

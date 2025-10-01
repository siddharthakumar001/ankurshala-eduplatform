package com.ankurshala.backend.dto.admin.content;

import java.util.List;

/**
 * DTO for hierarchical content tree structure
 */
public class ContentTreeDto {
    private Long id;
    private String name;
    private String type; // BOARD, GRADE, SUBJECT, CHAPTER, TOPIC
    private Boolean active;
    private List<ContentTreeDto> children;
    
    // Hierarchical data lists for easy access
    private List<BoardDto> boards;
    private List<GradeDto> grades;
    private List<SubjectDto> subjects;
    private List<ChapterDto> chapters;
    private List<TopicDto> topics;
    private List<TopicNoteDto> topicNotes;

    public ContentTreeDto() {}

    public ContentTreeDto(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.active = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public List<ContentTreeDto> getChildren() { return children; }
    public void setChildren(List<ContentTreeDto> children) { this.children = children; }

    // Hierarchical data getters and setters
    public List<BoardDto> getBoards() { return boards; }
    public void setBoards(List<BoardDto> boards) { this.boards = boards; }

    public List<GradeDto> getGrades() { return grades; }
    public void setGrades(List<GradeDto> grades) { this.grades = grades; }

    public List<SubjectDto> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectDto> subjects) { this.subjects = subjects; }

    public List<ChapterDto> getChapters() { return chapters; }
    public void setChapters(List<ChapterDto> chapters) { this.chapters = chapters; }

    public List<TopicDto> getTopics() { return topics; }
    public void setTopics(List<TopicDto> topics) { this.topics = topics; }

    public List<TopicNoteDto> getTopicNotes() { return topicNotes; }
    public void setTopicNotes(List<TopicNoteDto> topicNotes) { this.topicNotes = topicNotes; }
}

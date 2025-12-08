package com.ankurshala.backend.dto.student;

import java.time.ZonedDateTime;

public class StudyListItemDto {
    private Long id;
    private Long studentId;
    private Long topicId;
    private String topicName;
    private Long chapterId;
    private String chapterName;
    private Long subjectId;
    private String subjectName;
    private String status; // ADDED, IN_PROGRESS, DONE
    private ZonedDateTime addedAt;
    private ZonedDateTime doneAt;
    private String notes;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ZonedDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(ZonedDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public ZonedDateTime getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(ZonedDateTime doneAt) {
        this.doneAt = doneAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

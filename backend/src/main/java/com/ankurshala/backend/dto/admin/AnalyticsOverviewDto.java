package com.ankurshala.backend.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class AnalyticsOverviewDto implements Serializable {
    // User counts
    private long totalStudents;
    private long totalTeachers;
    private long activeStudents;
    private long activeTeachers;
    
    // Content counts
    private long totalBoards;
    private long totalSubjects;
    private long totalChapters;
    private long totalTopics;
    
    // Import analytics
    private long totalImports;
    private long successfulImports;
    private long failedImports;
    
    // Registration trends
    private long newStudents;
    private long newTeachers;

    public AnalyticsOverviewDto(long totalStudents, long totalTeachers, long activeStudents, long activeTeachers,
                               long totalBoards, long totalSubjects, long totalChapters, long totalTopics,
                               long totalImports, long successfulImports, long failedImports,
                               long newStudents, long newTeachers) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.activeStudents = activeStudents;
        this.activeTeachers = activeTeachers;
        this.totalBoards = totalBoards;
        this.totalSubjects = totalSubjects;
        this.totalChapters = totalChapters;
        this.totalTopics = totalTopics;
        this.totalImports = totalImports;
        this.successfulImports = successfulImports;
        this.failedImports = failedImports;
        this.newStudents = newStudents;
        this.newTeachers = newTeachers;
    }
}

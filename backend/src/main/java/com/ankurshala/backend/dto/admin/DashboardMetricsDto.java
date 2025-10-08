package com.ankurshala.backend.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DashboardMetricsDto implements Serializable {
    // User counts
    private long totalStudents;
    private long totalTeachers;
    private long activeStudents;
    private long activeTeachers;
    private long inactiveStudents;
    private long inactiveTeachers;
    
    // Registration trends
    private long newStudentsLast7Days;
    private long newStudentsLast30Days;
    private long newTeachersLast7Days;
    private long newTeachersLast30Days;
    
    // Content counts (placeholders for now)
    private long totalBoards;
    private long totalGrades;
    private long totalSubjects;
    private long totalChapters;
    private long totalTopics;
    
    // Course counts (placeholders for now)
    private long activeCourses;
    private long completedCourses;
    
    // Import analytics
    private long totalImports;
    private long successfulImports;
    private long failedImports;
    private long pendingImports;
    private long runningImports;

    // Explicit constructor for all fields
    public DashboardMetricsDto(long totalStudents, long totalTeachers, long activeStudents, long activeTeachers,
                               long inactiveStudents, long inactiveTeachers, long newStudentsLast7Days,
                               long newStudentsLast30Days, long newTeachersLast7Days, long newTeachersLast30Days,
                               long totalBoards, long totalGrades, long totalSubjects, long totalChapters,
                               long totalTopics, long activeCourses, long completedCourses,
                               long totalImports, long successfulImports, long failedImports,
                               long pendingImports, long runningImports) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.activeStudents = activeStudents;
        this.activeTeachers = activeTeachers;
        this.inactiveStudents = inactiveStudents;
        this.inactiveTeachers = inactiveTeachers;
        this.newStudentsLast7Days = newStudentsLast7Days;
        this.newStudentsLast30Days = newStudentsLast30Days;
        this.newTeachersLast7Days = newTeachersLast7Days;
        this.newTeachersLast30Days = newTeachersLast30Days;
        this.totalBoards = totalBoards;
        this.totalGrades = totalGrades;
        this.totalSubjects = totalSubjects;
        this.totalChapters = totalChapters;
        this.totalTopics = totalTopics;
        this.activeCourses = activeCourses;
        this.completedCourses = completedCourses;
        this.totalImports = totalImports;
        this.successfulImports = successfulImports;
        this.failedImports = failedImports;
        this.pendingImports = pendingImports;
        this.runningImports = runningImports;
    }
}

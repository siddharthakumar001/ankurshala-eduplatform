package com.ankurshala.backend.dto.admin;

public class DashboardMetricsDto {
    private long totalStudents;
    private long totalTeachers;
    private long newStudentsLast30Days;
    private long newTeachersLast30Days;
    private long activeCourses;
    private long completedCourses;

    // Constructors
    public DashboardMetricsDto() {}

    public DashboardMetricsDto(long totalStudents, long totalTeachers, 
                              long newStudentsLast30Days, long newTeachersLast30Days,
                              long activeCourses, long completedCourses) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.newStudentsLast30Days = newStudentsLast30Days;
        this.newTeachersLast30Days = newTeachersLast30Days;
        this.activeCourses = activeCourses;
        this.completedCourses = completedCourses;
    }

    // Getters and Setters
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public long getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }

    public long getNewStudentsLast30Days() { return newStudentsLast30Days; }
    public void setNewStudentsLast30Days(long newStudentsLast30Days) { this.newStudentsLast30Days = newStudentsLast30Days; }

    public long getNewTeachersLast30Days() { return newTeachersLast30Days; }
    public void setNewTeachersLast30Days(long newTeachersLast30Days) { this.newTeachersLast30Days = newTeachersLast30Days; }

    public long getActiveCourses() { return activeCourses; }
    public void setActiveCourses(long activeCourses) { this.activeCourses = activeCourses; }

    public long getCompletedCourses() { return completedCourses; }
    public void setCompletedCourses(long completedCourses) { this.completedCourses = completedCourses; }
}

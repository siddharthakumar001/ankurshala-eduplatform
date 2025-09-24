package com.ankurshala.backend.dto.admin;

public class DashboardSeriesDto {
    private String date;
    private long students;
    private long teachers;

    // Constructors
    public DashboardSeriesDto() {}

    public DashboardSeriesDto(String date, long students, long teachers) {
        this.date = date;
        this.students = students;
        this.teachers = teachers;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getStudents() { return students; }
    public void setStudents(long students) { this.students = students; }

    public long getTeachers() { return teachers; }
    public void setTeachers(long teachers) { this.teachers = teachers; }
}

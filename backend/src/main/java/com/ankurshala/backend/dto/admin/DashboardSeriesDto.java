package com.ankurshala.backend.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DashboardSeriesDto implements Serializable {
    private String date;
    private long students;
    private long teachers;

    // Explicit constructor for all fields
    public DashboardSeriesDto(String date, long students, long teachers) {
        this.date = date;
        this.students = students;
        this.teachers = teachers;
    }
}

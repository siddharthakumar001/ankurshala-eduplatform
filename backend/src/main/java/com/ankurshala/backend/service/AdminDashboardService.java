package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.DashboardMetricsDto;
import com.ankurshala.backend.dto.admin.DashboardSeriesDto;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Cacheable(value = "dashboardMetrics", unless = "#result == null")
    public DashboardMetricsDto getDashboardMetrics() {
        long totalStudents = studentProfileRepository.countByUserEnabledTrue();
        long totalTeachers = teacherProfileRepository.countByUserEnabledTrue();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newStudentsLast30Days = studentProfileRepository.countByUserCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now());
        long newTeachersLast30Days = teacherProfileRepository.countByUserCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now());

        // Placeholder for active/completed courses as these entities are not yet implemented
        long activeCourses = 0;
        long completedCourses = 0;

        return new DashboardMetricsDto(
                totalStudents,
                totalTeachers,
                newStudentsLast30Days,
                newTeachersLast30Days,
                activeCourses,
                completedCourses
        );
    }

    @Cacheable(value = "dashboardSeries", unless = "#result == null")
    public List<DashboardSeriesDto> getDashboardSeries() {
        List<DashboardSeriesDto> series = new ArrayList<>();
        
        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);
            
            // Get registrations for this day
            long students = studentProfileRepository.countByUserCreatedAtBetween(startOfDay, endOfDay);
            long teachers = teacherProfileRepository.countByUserCreatedAtBetween(startOfDay, endOfDay);
            
            series.add(new DashboardSeriesDto(
                date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                students,
                teachers
            ));
        }
        
        return series;
    }
}

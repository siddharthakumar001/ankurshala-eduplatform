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

    // @Cacheable(value = "dashboardMetrics", unless = "#result == null")
    public DashboardMetricsDto getDashboardMetrics() {
        // Total counts
        long totalStudents = studentProfileRepository.countByUserEnabledTrue();
        long totalTeachers = teacherProfileRepository.countByUserEnabledTrue();
        
        // Active/Inactive counts
        long activeStudents = studentProfileRepository.countByUserEnabledTrue();
        long activeTeachers = teacherProfileRepository.countByUserEnabledTrue();
        long inactiveStudents = studentProfileRepository.countByUserEnabledFalse();
        long inactiveTeachers = teacherProfileRepository.countByUserEnabledFalse();

        // Registration trends
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        long newStudentsLast7Days = studentProfileRepository.countByUserCreatedAtBetween(sevenDaysAgo, LocalDateTime.now());
        long newStudentsLast30Days = studentProfileRepository.countByUserCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now());
        long newTeachersLast7Days = teacherProfileRepository.countByUserCreatedAtBetween(sevenDaysAgo, LocalDateTime.now());
        long newTeachersLast30Days = teacherProfileRepository.countByUserCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now());

        // Content counts (placeholders for now - will be implemented in content management stages)
        long totalBoards = 0;
        long totalGrades = 0;
        long totalSubjects = 0;
        long totalChapters = 0;
        long totalTopics = 0;

        // Course counts (placeholders for now)
        long activeCourses = 0;
        long completedCourses = 0;

        return new DashboardMetricsDto(
                totalStudents,
                totalTeachers,
                activeStudents,
                activeTeachers,
                inactiveStudents,
                inactiveTeachers,
                newStudentsLast7Days,
                newStudentsLast30Days,
                newTeachersLast7Days,
                newTeachersLast30Days,
                totalBoards,
                totalGrades,
                totalSubjects,
                totalChapters,
                totalTopics,
                activeCourses,
                completedCourses
        );
    }

    // @Cacheable(value = "dashboardSeries", unless = "#result == null")
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

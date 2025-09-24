package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.DashboardMetricsDto;
import com.ankurshala.backend.dto.admin.DashboardSeriesDto;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdminDashboardService.
 * Tests the dashboard metrics and series data retrieval functionality.
 */
@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        // Reset mocks
    }

    @Test
    void testGetDashboardMetrics_WithNoData_ReturnsZeroValues() {
        // Given
        when(studentProfileRepository.countByUserEnabledTrue()).thenReturn(0L);
        when(teacherProfileRepository.countByUserEnabledTrue()).thenReturn(0L);
        when(studentProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(0L);
        when(teacherProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(0L);

        // When
        DashboardMetricsDto metrics = adminDashboardService.getDashboardMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalStudents()).isEqualTo(0);
        assertThat(metrics.getTotalTeachers()).isEqualTo(0);
        assertThat(metrics.getNewStudentsLast30Days()).isEqualTo(0);
        assertThat(metrics.getNewTeachersLast30Days()).isEqualTo(0);
        assertThat(metrics.getActiveCourses()).isEqualTo(0);
        assertThat(metrics.getCompletedCourses()).isEqualTo(0);
    }

    @Test
    void testGetDashboardMetrics_WithSampleData_ReturnsCorrectCounts() {
        // Given
        when(studentProfileRepository.countByUserEnabledTrue()).thenReturn(150L);
        when(teacherProfileRepository.countByUserEnabledTrue()).thenReturn(25L);
        when(studentProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(12L);
        when(teacherProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(3L);

        // When
        DashboardMetricsDto metrics = adminDashboardService.getDashboardMetrics();

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalStudents()).isEqualTo(150);
        assertThat(metrics.getTotalTeachers()).isEqualTo(25);
        assertThat(metrics.getNewStudentsLast30Days()).isEqualTo(12);
        assertThat(metrics.getNewTeachersLast30Days()).isEqualTo(3);
        assertThat(metrics.getActiveCourses()).isEqualTo(0); // Placeholder value
        assertThat(metrics.getCompletedCourses()).isEqualTo(0); // Placeholder value
    }

    @Test
    void testGetDashboardSeries_ReturnsCorrectNumberOfDays() {
        // Given
        when(studentProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(5L);
        when(teacherProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(2L);

        // When
        List<DashboardSeriesDto> series = adminDashboardService.getDashboardSeries();

        // Then
        assertThat(series).isNotNull();
        assertThat(series).hasSize(30); // Should return 30 days of data
        
        // Check that all entries have the expected structure
        assertThat(series).allMatch(s -> s.getDate() != null);
        assertThat(series).allMatch(s -> s.getStudents() >= 0);
        assertThat(series).allMatch(s -> s.getTeachers() >= 0);
    }

    @Test
    void testGetDashboardSeries_WithMockData_ReturnsExpectedValues() {
        // Given
        when(studentProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(10L);
        when(teacherProfileRepository.countByUserCreatedAtBetween(
            org.mockito.ArgumentMatchers.any(LocalDateTime.class),
            org.mockito.ArgumentMatchers.any(LocalDateTime.class)
        )).thenReturn(5L);

        // When
        List<DashboardSeriesDto> series = adminDashboardService.getDashboardSeries();

        // Then
        assertThat(series).isNotNull();
        assertThat(series).hasSize(30);
        
        // Check that we have some non-zero values (since we're mocking the same values for all days)
        assertThat(series).allMatch(s -> s.getStudents() == 10);
        assertThat(series).allMatch(s -> s.getTeachers() == 5);
    }
}

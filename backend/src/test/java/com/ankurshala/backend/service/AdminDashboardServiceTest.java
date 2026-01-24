package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.DashboardMetricsDto;
import com.ankurshala.backend.dto.admin.DashboardSeriesDto;
import com.ankurshala.backend.entity.ImportJobStatus;
import com.ankurshala.backend.entity.PaymentIntentStatus;
import com.ankurshala.backend.repository.BoardRepository;
import com.ankurshala.backend.repository.ChapterRepository;
import com.ankurshala.backend.repository.GradeRepository;
import com.ankurshala.backend.repository.ImportJobRepository;
import com.ankurshala.backend.repository.PaymentIntentRepository;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.SubjectRepository;
import com.ankurshala.backend.repository.TopicRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDashboardService Tests")
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private PaymentIntentRepository paymentIntentRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(userRepository, studentProfileRepository, teacherProfileRepository,
                boardRepository, gradeRepository, subjectRepository, chapterRepository,
                topicRepository, importJobRepository, paymentIntentRepository);
    }

    @Test
    @DisplayName("Should get dashboard metrics successfully")
    void testGetDashboardMetrics() {
        // Given
        when(studentProfileRepository.countByUserEnabledTrue()).thenReturn(100L);
        when(teacherProfileRepository.countByUserEnabledTrue()).thenReturn(50L);
        when(studentProfileRepository.countByUserEnabledFalse()).thenReturn(10L);
        when(teacherProfileRepository.countByUserEnabledFalse()).thenReturn(5L);
        when(studentProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(15L);
        when(teacherProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8L);
        when(boardRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(gradeRepository.count()).thenReturn(0L);
        when(subjectRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(chapterRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(topicRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(paymentIntentRepository.sumAmountCentsByStatus(PaymentIntentStatus.COMPLETED)).thenReturn(0L);
        when(paymentIntentRepository.sumAmountCentsByStatusAndCreatedAtBetween(
                eq(PaymentIntentStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(importJobRepository.count()).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.SUCCEEDED)).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.FAILED)).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.PENDING)).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.RUNNING)).thenReturn(0L);

        // When
        DashboardMetricsDto result = adminDashboardService.getDashboardMetrics();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalStudents());
        assertEquals(50L, result.getTotalTeachers());
        assertEquals(100L, result.getActiveStudents());
        assertEquals(50L, result.getActiveTeachers());
        assertEquals(10L, result.getInactiveStudents());
        assertEquals(5L, result.getInactiveTeachers());
        assertEquals(15L, result.getNewStudentsLast7Days());
        assertEquals(15L, result.getNewStudentsLast30Days());
        assertEquals(8L, result.getNewTeachersLast7Days());
        assertEquals(8L, result.getNewTeachersLast30Days());
        assertEquals(0L, result.getTotalBoards());
        assertEquals(0L, result.getTotalGrades());
        assertEquals(0L, result.getTotalSubjects());
        assertEquals(0L, result.getTotalChapters());
        assertEquals(0L, result.getTotalTopics());
        assertEquals(0L, result.getActiveCourses());
        assertEquals(0L, result.getCompletedCourses());

        // Verify repository interactions
        verify(studentProfileRepository, times(2)).countByUserEnabledTrue();
        verify(teacherProfileRepository, times(2)).countByUserEnabledTrue();
        verify(studentProfileRepository, times(1)).countByUserEnabledFalse();
        verify(teacherProfileRepository, times(1)).countByUserEnabledFalse();
        verify(studentProfileRepository, times(2)).countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(teacherProfileRepository, times(2)).countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get dashboard metrics with zero counts")
    void testGetDashboardMetricsWithZeroCounts() {
        // Given
        when(studentProfileRepository.countByUserEnabledTrue()).thenReturn(0L);
        when(teacherProfileRepository.countByUserEnabledTrue()).thenReturn(0L);
        when(studentProfileRepository.countByUserEnabledFalse()).thenReturn(0L);
        when(teacherProfileRepository.countByUserEnabledFalse()).thenReturn(0L);
        when(studentProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(teacherProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(boardRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(gradeRepository.count()).thenReturn(0L);
        when(subjectRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(chapterRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(topicRepository.countByActiveTrueAndSoftDeletedFalse()).thenReturn(0L);
        when(paymentIntentRepository.sumAmountCentsByStatus(PaymentIntentStatus.COMPLETED)).thenReturn(0L);
        when(paymentIntentRepository.sumAmountCentsByStatusAndCreatedAtBetween(
                eq(PaymentIntentStatus.COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(importJobRepository.count()).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.SUCCEEDED)).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.FAILED)).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.PENDING)).thenReturn(0L);
        when(importJobRepository.countByStatus(ImportJobStatus.RUNNING)).thenReturn(0L);

        // When
        DashboardMetricsDto result = adminDashboardService.getDashboardMetrics();

        // Then
        assertNotNull(result);
        assertEquals(0L, result.getTotalStudents());
        assertEquals(0L, result.getTotalTeachers());
        assertEquals(0L, result.getActiveStudents());
        assertEquals(0L, result.getActiveTeachers());
        assertEquals(0L, result.getInactiveStudents());
        assertEquals(0L, result.getInactiveTeachers());
        assertEquals(0L, result.getNewStudentsLast7Days());
        assertEquals(0L, result.getNewStudentsLast30Days());
        assertEquals(0L, result.getNewTeachersLast7Days());
        assertEquals(0L, result.getNewTeachersLast30Days());
    }

    @Test
    @DisplayName("Should get dashboard series successfully")
    void testGetDashboardSeries() {
        // Given
        when(studentProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(teacherProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2L);

        // When
        List<DashboardSeriesDto> result = adminDashboardService.getDashboardSeries();

        // Then
        assertNotNull(result);
        assertEquals(30, result.size()); // 30 days of data

        // Verify first and last entries
        DashboardSeriesDto firstEntry = result.get(0);
        assertNotNull(firstEntry.getDate());
        assertEquals(5L, firstEntry.getStudents());
        assertEquals(2L, firstEntry.getTeachers());

        DashboardSeriesDto lastEntry = result.get(29);
        assertNotNull(lastEntry.getDate());
        assertEquals(5L, lastEntry.getStudents());
        assertEquals(2L, lastEntry.getTeachers());

        // Verify repository interactions
        verify(studentProfileRepository, times(30)).countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
        verify(teacherProfileRepository, times(30)).countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get dashboard series with varying counts")
    void testGetDashboardSeriesWithVaryingCounts() {
        // Given
        when(studentProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L, 5L, 15L, 0L, 8L); // Different counts for different days
        when(teacherProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L, 1L, 7L, 0L, 4L);

        // When
        List<DashboardSeriesDto> result = adminDashboardService.getDashboardSeries();

        // Then
        assertNotNull(result);
        assertEquals(30, result.size());

        // Verify that all entries have valid data
        for (DashboardSeriesDto entry : result) {
            assertNotNull(entry.getDate());
            assertTrue(entry.getStudents() >= 0);
            assertTrue(entry.getTeachers() >= 0);
        }
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void testGetDashboardMetricsWithRepositoryException() {
        // Given
        when(studentProfileRepository.countByUserEnabledTrue()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adminDashboardService.getDashboardMetrics();
        });

        verify(studentProfileRepository, times(1)).countByUserEnabledTrue();
    }

    @Test
    @DisplayName("Should handle repository exceptions in dashboard series gracefully")
    void testGetDashboardSeriesWithRepositoryException() {
        // Given
        when(studentProfileRepository.countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adminDashboardService.getDashboardSeries();
        });

        verify(studentProfileRepository, times(1)).countByUserCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}

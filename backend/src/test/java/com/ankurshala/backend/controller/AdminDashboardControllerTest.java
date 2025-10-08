package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.admin.DashboardMetricsDto;
import com.ankurshala.backend.dto.admin.DashboardSeriesDto;
import com.ankurshala.backend.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private AdminDashboardService dashboardService;

    @InjectMocks
    private AdminDashboardController adminDashboardController;

    private DashboardMetricsDto mockMetrics;
    private List<DashboardSeriesDto> mockSeries;

    @BeforeEach
    void setUp() {
        // Setup mock metrics
        mockMetrics = new DashboardMetricsDto(100L, 50L, 80L, 40L, 20L, 10L, 
            10L, 30L, 5L, 15L, 5L, 10L, 20L, 50L, 100L, 25L, 10L);

        // Setup mock series
        DashboardSeriesDto series1 = new DashboardSeriesDto("2023-01-01", 10L, 5L);
        DashboardSeriesDto series2 = new DashboardSeriesDto("2023-01-02", 20L, 10L);

        mockSeries = Arrays.asList(series1, series2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetMetrics_Success() {
        // Given
        when(dashboardService.getDashboardMetrics()).thenReturn(mockMetrics);

        // When
        ResponseEntity<DashboardMetricsDto> response = adminDashboardController.getMetrics();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().getTotalStudents());
        assertEquals(50L, response.getBody().getTotalTeachers());
        assertEquals(80L, response.getBody().getActiveStudents());
        assertEquals(40L, response.getBody().getActiveTeachers());

        verify(dashboardService, times(1)).getDashboardMetrics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetMetrics_ServiceReturnsNull() {
        // Given
        when(dashboardService.getDashboardMetrics()).thenReturn(null);

        // When
        ResponseEntity<DashboardMetricsDto> response = adminDashboardController.getMetrics();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(dashboardService, times(1)).getDashboardMetrics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSeries_Success() {
        // Given
        when(dashboardService.getDashboardSeries()).thenReturn(mockSeries);

        // When
        ResponseEntity<List<DashboardSeriesDto>> response = adminDashboardController.getSeries();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("2023-01-01", response.getBody().get(0).getDate());
        assertEquals("2023-01-02", response.getBody().get(1).getDate());

        verify(dashboardService, times(1)).getDashboardSeries();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSeries_EmptyList() {
        // Given
        when(dashboardService.getDashboardSeries()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<DashboardSeriesDto>> response = adminDashboardController.getSeries();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(dashboardService, times(1)).getDashboardSeries();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSeries_ServiceReturnsNull() {
        // Given
        when(dashboardService.getDashboardSeries()).thenReturn(null);

        // When
        ResponseEntity<List<DashboardSeriesDto>> response = adminDashboardController.getSeries();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(dashboardService, times(1)).getDashboardSeries();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetMetrics_ServiceThrowsException() {
        // Given
        when(dashboardService.getDashboardMetrics()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adminDashboardController.getMetrics();
        });

        verify(dashboardService, times(1)).getDashboardMetrics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSeries_ServiceThrowsException() {
        // Given
        when(dashboardService.getDashboardSeries()).thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            adminDashboardController.getSeries();
        });

        verify(dashboardService, times(1)).getDashboardSeries();
    }
}

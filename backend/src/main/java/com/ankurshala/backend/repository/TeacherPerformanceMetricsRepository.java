package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherPerformanceMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherPerformanceMetricsRepository extends JpaRepository<TeacherPerformanceMetrics, Long> {
    
    List<TeacherPerformanceMetrics> findByTeacherId(Long teacherId);
    
    Optional<TeacherPerformanceMetrics> findByTeacherIdAndMetricDate(Long teacherId, LocalDate metricDate);
    
    @Query("SELECT tpm FROM TeacherPerformanceMetrics tpm WHERE tpm.teacher.id = :teacherId AND tpm.metricDate BETWEEN :startDate AND :endDate ORDER BY tpm.metricDate DESC")
    List<TeacherPerformanceMetrics> findByTeacherIdAndDateRange(@Param("teacherId") Long teacherId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT AVG(tpm.averageRating) FROM TeacherPerformanceMetrics tpm WHERE tpm.teacher.id = :teacherId AND tpm.metricDate BETWEEN :startDate AND :endDate")
    Double findAverageRatingByTeacherIdAndDateRange(@Param("teacherId") Long teacherId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(tpm.totalSessions) FROM TeacherPerformanceMetrics tpm WHERE tpm.teacher.id = :teacherId AND tpm.metricDate BETWEEN :startDate AND :endDate")
    Integer findTotalSessionsByTeacherIdAndDateRange(@Param("teacherId") Long teacherId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(tpm.totalEarnings) FROM TeacherPerformanceMetrics tpm WHERE tpm.teacher.id = :teacherId AND tpm.metricDate BETWEEN :startDate AND :endDate")
    Double findTotalEarningsByTeacherIdAndDateRange(@Param("teacherId") Long teacherId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

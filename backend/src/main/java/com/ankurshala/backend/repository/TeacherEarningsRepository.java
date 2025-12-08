package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherEarnings;
import com.ankurshala.backend.entity.TeacherEarnings.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherEarningsRepository extends JpaRepository<TeacherEarnings, Long> {
    
    List<TeacherEarnings> findByTeacherId(Long teacherId);
    
    List<TeacherEarnings> findByTeacherIdAndPaymentStatus(Long teacherId, PaymentStatus paymentStatus);
    
    Optional<TeacherEarnings> findByBookingId(Long bookingId);
    
    @Query("SELECT SUM(te.netEarnings) FROM TeacherEarnings te WHERE te.teacher.id = :teacherId AND te.paymentStatus = :status")
    Double findTotalEarningsByTeacherIdAndStatus(@Param("teacherId") Long teacherId, @Param("status") PaymentStatus status);
    
    @Query("SELECT SUM(te.netEarnings) FROM TeacherEarnings te WHERE te.teacher.id = :teacherId AND te.sessionDate BETWEEN :startDate AND :endDate")
    Double findTotalEarningsByTeacherIdAndDateRange(@Param("teacherId") Long teacherId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT te FROM TeacherEarnings te WHERE te.teacher.id = :teacherId AND te.sessionDate BETWEEN :startDate AND :endDate ORDER BY te.sessionDate DESC")
    List<TeacherEarnings> findEarningsByTeacherIdAndDateRange(@Param("teacherId") Long teacherId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

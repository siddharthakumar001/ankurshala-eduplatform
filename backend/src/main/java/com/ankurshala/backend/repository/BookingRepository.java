package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Page<Booking> findByStudentOrderByStartTimeDesc(User student, Pageable pageable);
    
    Page<Booking> findByStudentAndStatusInOrderByStartTimeDesc(User student, List<Booking.BookingStatus> statuses, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.startTime >= :from AND b.startTime <= :to ORDER BY b.startTime ASC")
    List<Booking> findByStudentAndTimeRange(@Param("student") User student, 
                                           @Param("from") LocalDateTime from, 
                                           @Param("to") LocalDateTime to);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.startTime > :now ORDER BY b.startTime ASC")
    List<Booking> findUpcomingByStudent(@Param("student") User student, @Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.startTime <= :now ORDER BY b.startTime DESC")
    List<Booking> findHistoryByStudent(@Param("student") User student, @Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.status IN :statuses " +
           "AND ((b.startTime <= :startTime AND b.endTime > :startTime) OR " +
           "(b.startTime < :endTime AND b.endTime >= :endTime) OR " +
           "(b.startTime >= :startTime AND b.endTime <= :endTime))")
    List<Booking> findConflictingBookings(@Param("student") User student,
                                         @Param("statuses") List<Booking.BookingStatus> statuses,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    Optional<Booking> findByAcceptanceToken(String acceptanceToken);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.student = :student AND b.status = :status")
    long countByStudentAndStatus(@Param("student") User student, @Param("status") Booking.BookingStatus status);
}

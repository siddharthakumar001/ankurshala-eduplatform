package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingState;
import com.ankurshala.backend.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find bookings by student ID
     */
    List<Booking> findByStudentIdOrderByStartTsDesc(Long studentId);

    /**
     * Find bookings by teacher ID
     */
    List<Booking> findByTeacherIdOrderByStartTsDesc(Long teacherId);

    /**
     * Find pending bookings for teachers
     */
    @Query("SELECT b FROM Booking b WHERE b.state = 'REQUESTED' ORDER BY b.startTs ASC")
    List<Booking> findPendingBookings();

    /**
     * Find conflicting bookings within time range
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "(b.startTs < :endTime AND b.endTs > :startTime) AND " +
           "b.state IN ('REQUESTED', 'ACCEPTED')")
    List<Booking> findConflictingBookings(@Param("startTime") ZonedDateTime startTime, 
                                        @Param("endTime") ZonedDateTime endTime);

    /**
     * Find conflicting bookings for a specific teacher within time range
     */
    @Query("SELECT b FROM Booking b WHERE b.teacherId = :teacherId AND " +
           "(b.startTs < :endTime AND b.endTs > :startTime) AND " +
           "b.state IN ('REQUESTED', 'ACCEPTED')")
    List<Booking> findConflictingBookingsForTeacher(@Param("teacherId") Long teacherId,
                                                     @Param("startTime") ZonedDateTime startTime, 
                                                     @Param("endTime") ZonedDateTime endTime);

    /**
     * Find bookings by state and time range
     */
    @Query("SELECT b FROM Booking b WHERE b.state = :state AND b.startTs >= :fromTime AND b.startTs <= :toTime ORDER BY b.startTs ASC")
    List<Booking> findByStateAndTimeRange(@Param("state") String state, 
                                        @Param("fromTime") ZonedDateTime fromTime, 
                                        @Param("toTime") ZonedDateTime toTime);

    /**
     * Find upcoming bookings for student
     */
    @Query("SELECT b FROM Booking b WHERE b.studentId = :studentId AND b.startTs > :now AND b.state IN ('ACCEPTED', 'REQUESTED') ORDER BY b.startTs ASC")
    List<Booking> findUpcomingBookingsForStudent(@Param("studentId") Long studentId, 
                                               @Param("now") ZonedDateTime now);

    /**
     * Find upcoming bookings for teacher
     */
    @Query("SELECT b FROM Booking b WHERE b.teacherId = :teacherId AND b.startTs > :now AND b.state IN ('ACCEPTED', 'REQUESTED') ORDER BY b.startTs ASC")
    List<Booking> findUpcomingBookingsForTeacher(@Param("teacherId") Long teacherId, 
                                               @Param("now") ZonedDateTime now);

    /**
     * Find completed bookings for student
     */
    @Query("SELECT b FROM Booking b WHERE b.studentId = :studentId AND b.state = 'COMPLETED' ORDER BY b.startTs DESC")
    List<Booking> findCompletedBookingsForStudent(@Param("studentId") Long studentId);

    /**
     * Find completed bookings for teacher
     */
    @Query("SELECT b FROM Booking b WHERE b.teacherId = :teacherId AND b.state = 'COMPLETED' ORDER BY b.startTs DESC")
    List<Booking> findCompletedBookingsForTeacher(@Param("teacherId") Long teacherId);

    /**
     * Atomic update to accept booking (first-accept wins)
     * Uses optimistic locking with version check for concurrency control
     */
    @Modifying
    @Query("UPDATE Booking b SET b.state = 'ACCEPTED', b.teacherId = :teacherId, b.acceptedAt = CURRENT_TIMESTAMP, b.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE b.id = :bookingId AND b.state = 'REQUESTED' AND b.teacherId IS NULL")
    int acceptBooking(@Param("bookingId") Long bookingId, @Param("teacherId") Long teacherId);

    /**
     * Find booking by ID with pessimistic write lock for atomic operations
     * Used during booking acceptance to prevent race conditions
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithLock(@Param("bookingId") Long bookingId);

    /**
     * Update booking state
     */
    @Modifying
    @Query("UPDATE Booking b SET b.state = :state, b.updatedAt = CURRENT_TIMESTAMP WHERE b.id = :bookingId")
    int updateBookingState(@Param("bookingId") Long bookingId, @Param("state") String state);

    /**
     * Find bookings by topic ID
     */
    List<Booking> findByTopicIdOrderByStartTsDesc(Long topicId);

    /**
     * Count bookings by state
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.state = :state")
    long countByState(@Param("state") String state);

    /**
     * Count bookings by student and state
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.studentId = :studentId AND b.state = :state")
    long countByStudentIdAndState(@Param("studentId") Long studentId, @Param("state") String state);

    /**
     * Count bookings by teacher and state
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.teacherId = :teacherId AND b.state = :state")
    long countByTeacherIdAndState(@Param("teacherId") Long teacherId, @Param("state") String state);

    /**
     * Find bookings for calendar view
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "((b.studentId = :userId AND b.state IN ('ACCEPTED', 'REQUESTED', 'COMPLETED')) OR " +
           "(b.teacherId = :userId AND b.state IN ('ACCEPTED', 'REQUESTED', 'COMPLETED'))) AND " +
           "b.startTs >= :fromTime AND b.startTs <= :toTime " +
           "ORDER BY b.startTs ASC")
    List<Booking> findCalendarBookings(@Param("userId") Long userId, 
                                     @Param("fromTime") ZonedDateTime fromTime, 
                                     @Param("toTime") ZonedDateTime toTime);

    // Additional methods needed by services
    Page<Booking> findByStudentOrderByStartTsDesc(User student, PageRequest pageRequest);
    Page<Booking> findByStudentOrderByStartTsDesc(User student, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.startTs >= :startTime AND b.startTs <= :endTime")
    List<Booking> findByStudentAndTimeRange(@Param("student") User student, 
                                            @Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);
    
    List<Booking> findByStudentIdAndStateOrderByStartTsAsc(Long studentId, BookingState state);
    List<Booking> findByTeacherIdAndStateOrderByStartTsAsc(Long teacherId, BookingState state);
    
    Optional<Booking> findByAcceptanceToken(String acceptanceToken);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.startTs >= :now ORDER BY b.startTs ASC")
    List<Booking> findUpcomingByStudent(@Param("student") User student, @Param("now") ZonedDateTime now);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.startTs < :now ORDER BY b.startTs DESC")
    List<Booking> findHistoryByStudent(@Param("student") User student, @Param("now") ZonedDateTime now);
    
    @Query("SELECT b FROM Booking b WHERE b.student = :student AND b.startTs < :now")
    Page<Booking> findHistoryByStudent(@Param("student") User student, @Param("now") ZonedDateTime now, Pageable pageable);
    
    /**
     * Find bookings by status (BookingStatus enum)
     */
    List<Booking> findByStatus(com.ankurshala.backend.entity.BookingStatus status);
    
    /**
     * Find bookings by teacher ID and status
     */
    List<Booking> findByTeacherIdAndStatus(Long teacherId, com.ankurshala.backend.entity.BookingStatus status);
}
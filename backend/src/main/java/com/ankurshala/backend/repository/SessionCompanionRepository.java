package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.SessionCompanion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionCompanionRepository extends JpaRepository<SessionCompanion, Long> {

    /**
     * Find companion by booking ID
     */
    Optional<SessionCompanion> findByBookingId(Long bookingId);

    /**
     * Find companion by booking ID and student ID (ownership check)
     */
    Optional<SessionCompanion> findByBookingIdAndStudentId(Long bookingId, Long studentId);

    /**
     * Find all companions for a student
     */
    List<SessionCompanion> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    /**
     * Find companions by status
     */
    List<SessionCompanion> findByStudentIdAndStatus(Long studentId, SessionCompanion.CompanionStatus status);

    /**
     * Find companions needing post-session generation (status = LIVE and booking completed)
     */
    @Query("SELECT sc FROM SessionCompanion sc JOIN Booking b ON sc.bookingId = b.id " +
           "WHERE sc.status = 'LIVE' AND b.status = 'COMPLETED'")
    List<SessionCompanion> findCompanionsNeedingPostGeneration();

    /**
     * Find recent companions for a student
     */
    @Query("SELECT sc FROM SessionCompanion sc WHERE sc.studentId = :studentId " +
           "AND sc.createdAt >= :since ORDER BY sc.createdAt DESC")
    List<SessionCompanion> findRecentByStudentId(@Param("studentId") Long studentId, 
                                                  @Param("since") LocalDateTime since);

    /**
     * Check if companion exists for booking
     */
    boolean existsByBookingId(Long bookingId);

    /**
     * Find companions with upcoming sessions (for notification triggers)
     */
    @Query("SELECT sc FROM SessionCompanion sc JOIN Booking b ON sc.bookingId = b.id " +
           "WHERE sc.studentId = :studentId AND b.status = 'CONFIRMED' " +
           "AND b.startTs >= :now ORDER BY b.startTs ASC")
    List<SessionCompanion> findUpcomingByStudentId(@Param("studentId") Long studentId, 
                                                    @Param("now") LocalDateTime now);
}


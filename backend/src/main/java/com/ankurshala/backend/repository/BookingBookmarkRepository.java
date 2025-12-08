package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.BookingBookmark;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingBookmarkRepository extends JpaRepository<BookingBookmark, Long> {
    List<BookingBookmark> findByBookingIdOrderByTsSecondsAsc(Long bookingId);
    
    boolean existsByBookingIdAndStudentId(Long bookingId, Long studentId);
    void deleteByBookingIdAndStudentId(Long bookingId, Long studentId);
}
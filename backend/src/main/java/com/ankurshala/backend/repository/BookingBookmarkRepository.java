package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.BookingBookmark;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingBookmarkRepository extends JpaRepository<BookingBookmark, Long> {
    
    Optional<BookingBookmark> findByBookingAndStudent(Booking booking, User student);
    
    boolean existsByBookingAndStudent(Booking booking, User student);
    
    void deleteByBookingAndStudent(Booking booking, User student);
}

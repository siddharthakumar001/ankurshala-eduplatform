package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.BookingNote;
import com.ankurshala.backend.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingNoteRepository extends JpaRepository<BookingNote, Long> {
    
    List<BookingNote> findByBookingOrderByCreatedAtDesc(Booking booking);
    
    Page<BookingNote> findByBookingOrderByCreatedAtDesc(Booking booking, Pageable pageable);
}

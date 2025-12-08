package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.BookingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingNoteRepository extends JpaRepository<BookingNote, Long> {
    List<BookingNote> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
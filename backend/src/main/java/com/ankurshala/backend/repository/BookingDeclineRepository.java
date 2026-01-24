package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.BookingDecline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingDeclineRepository extends JpaRepository<BookingDecline, Long> {
    boolean existsByBookingIdAndTeacherId(Long bookingId, Long teacherId);
    long countByBookingId(Long bookingId);
    long countByBookingIdAndTeacherIdIn(Long bookingId, List<Long> teacherIds);
    void deleteByBookingId(Long bookingId);
}

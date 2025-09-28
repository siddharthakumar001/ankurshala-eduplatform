package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherAvailabilitySlot;
import com.ankurshala.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TeacherAvailabilitySlotRepository extends JpaRepository<TeacherAvailabilitySlot, Long> {
    
    @Query("SELECT s FROM TeacherAvailabilitySlot s WHERE s.teacher = :teacher AND s.isAvailable = true " +
           "AND s.startTime <= :startTime AND s.endTime >= :endTime")
    List<TeacherAvailabilitySlot> findAvailableSlotsForTeacher(@Param("teacher") User teacher,
                                                               @Param("startTime") LocalDateTime startTime,
                                                               @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT s FROM TeacherAvailabilitySlot s WHERE s.teacher = :teacher " +
           "AND s.startTime >= :from AND s.startTime <= :to ORDER BY s.startTime ASC")
    List<TeacherAvailabilitySlot> findByTeacherAndTimeRange(@Param("teacher") User teacher,
                                                           @Param("from") LocalDateTime from,
                                                           @Param("to") LocalDateTime to);
}

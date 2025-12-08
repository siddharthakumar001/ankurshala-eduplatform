package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherWeeklyAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherWeeklyAvailabilityRepository extends JpaRepository<TeacherWeeklyAvailability, Long> {
    
    List<TeacherWeeklyAvailability> findByTeacherId(Long teacherId);
    
    List<TeacherWeeklyAvailability> findByTeacherIdAndIsAvailableTrue(Long teacherId);
    
    List<TeacherWeeklyAvailability> findByTeacherIdAndDayOfWeek(Long teacherId, Integer dayOfWeek);
    
    Optional<TeacherWeeklyAvailability> findByTeacherIdAndDayOfWeekAndStartTimeAndEndTime(
        Long teacherId, Integer dayOfWeek, LocalTime startTime, LocalTime endTime);
    
    @Query("SELECT twa FROM TeacherWeeklyAvailability twa WHERE twa.teacher.id = :teacherId AND twa.dayOfWeek = :dayOfWeek AND twa.isAvailable = true")
    List<TeacherWeeklyAvailability> findAvailableSlotsByTeacherAndDay(@Param("teacherId") Long teacherId, @Param("dayOfWeek") Integer dayOfWeek);
    
    void deleteByTeacherId(Long teacherId);
}

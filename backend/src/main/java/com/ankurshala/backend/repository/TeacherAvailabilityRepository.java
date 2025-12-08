package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {

    /**
     * Find availability by teacher ID
     */
    List<TeacherAvailability> findByTeacher_IdOrderByWeekdayAscStartTimeAsc(Long teacherId);

    /**
     * Find active availability by teacher ID
     */
    List<TeacherAvailability> findByTeacher_IdAndActiveOrderByWeekdayAscStartTimeAsc(Long teacherId, Boolean active);

    /**
     * Find availability by teacher ID and weekday
     */
    List<TeacherAvailability> findByTeacher_IdAndWeekdayOrderByStartTimeAsc(Long teacherId, Integer weekday);

    /**
     * Find active availability by teacher ID and weekday
     */
    List<TeacherAvailability> findByTeacher_IdAndWeekdayAndActiveOrderByStartTimeAsc(Long teacherId, Integer weekday, Boolean active);

    /**
     * Delete all availability for a teacher
     */
    void deleteByTeacher_Id(Long teacherId);

    /**
     * Find availability by weekday and active status
     */
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.weekday = :weekday AND ta.active = true ORDER BY ta.startTime ASC")
    List<TeacherAvailability> findByWeekdayAndActive(@Param("weekday") Integer weekday);

    /**
     * Check if teacher has availability for specific weekday
     */
    @Query("SELECT COUNT(ta) > 0 FROM TeacherAvailability ta WHERE ta.teacher.id = :teacherId AND ta.weekday = :weekday AND ta.active = true")
    boolean hasAvailabilityForWeekday(@Param("teacherId") Long teacherId, @Param("weekday") Integer weekday);

    /**
     * Find overlapping availability slots
     */
    @Query("SELECT ta FROM TeacherAvailability ta WHERE " +
           "ta.teacher.id = :teacherId AND ta.weekday = :weekday AND ta.active = true AND " +
           "((ta.startTime < :endTime AND ta.endTime > :startTime))")
    List<TeacherAvailability> findOverlappingSlots(@Param("teacherId") Long teacherId, 
                                                  @Param("weekday") Integer weekday,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime") String endTime);

    /**
     * Find availability by teacher ID and active status
     */
    List<TeacherAvailability> findByTeacher_IdAndActive(Long teacherId, boolean active);
}
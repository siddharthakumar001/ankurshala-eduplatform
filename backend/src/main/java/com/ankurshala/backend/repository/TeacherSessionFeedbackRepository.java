package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherSessionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSessionFeedbackRepository extends JpaRepository<TeacherSessionFeedback, Long> {
    
    List<TeacherSessionFeedback> findByTeacherId(Long teacherId);
    
    List<TeacherSessionFeedback> findByStudentId(Long studentId);
    
    Optional<TeacherSessionFeedback> findByBookingId(Long bookingId);
    
    @Query("SELECT AVG(tsf.sessionRating) FROM TeacherSessionFeedback tsf WHERE tsf.teacher.id = :teacherId")
    Double findAverageRatingByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT COUNT(tsf) FROM TeacherSessionFeedback tsf WHERE tsf.teacher.id = :teacherId AND tsf.wouldRecommend = true")
    Long countRecommendationsByTeacherId(@Param("teacherId") Long teacherId);
}

package com.ankurshala.backend.repository;

import com.ankurshala.backend.entity.TeacherReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherReviewRepository extends JpaRepository<TeacherReview, Long> {

    List<TeacherReview> findByTeacherIdAndApprovedOrderByCreatedAtDesc(Long teacherId, Boolean approved, Pageable pageable);

    List<TeacherReview> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    Optional<TeacherReview> findByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM TeacherReview r WHERE r.teacherId = :teacherId AND r.approved = true")
    Double getAverageRating(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(r) FROM TeacherReview r WHERE r.teacherId = :teacherId AND r.approved = true")
    Long countApprovedReviews(@Param("teacherId") Long teacherId);

    boolean existsByBookingIdAndStudentId(Long bookingId, Long studentId);
}

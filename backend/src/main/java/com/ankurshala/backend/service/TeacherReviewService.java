package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.TeacherSearchResponse.TeacherReviewDto;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.TeacherProfile;
import com.ankurshala.backend.entity.TeacherReview;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import com.ankurshala.backend.repository.TeacherReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherReviewService {

    private final TeacherReviewRepository reviewRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final BookingRepository bookingRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Transactional
    public TeacherReview submitReview(Long studentId, Long teacherId, Long bookingId, Double rating, String comment) {
        log.info("Submitting review for teacher {} by student {}", teacherId, studentId);

        // Validate booking exists and belongs to student
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("Booking does not belong to student");
        }

        if (!booking.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("Booking does not belong to teacher");
        }

        // Check if review already exists
        if (reviewRepository.existsByBookingIdAndStudentId(bookingId, studentId)) {
            throw new IllegalArgumentException("Review already submitted for this booking");
        }

        // Validate rating
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }

        // Create review
        TeacherReview review = TeacherReview.builder()
                .teacherId(teacherId)
                .studentId(studentId)
                .bookingId(bookingId)
                .rating(rating)
                .comment(comment)
                .approved(true)
                .build();

        TeacherReview savedReview = reviewRepository.save(review);

        // Update teacher statistics
        updateTeacherStatistics(teacherId);

        log.info("Review submitted successfully - ReviewId: {}", savedReview.getId());
        return savedReview;
    }

    @Transactional
    protected void updateTeacherStatistics(Long teacherId) {
        Double avgRating = reviewRepository.getAverageRating(teacherId);
        Long totalReviews = reviewRepository.countApprovedReviews(teacherId);

        TeacherProfile teacher = teacherProfileRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        teacher.setRating(avgRating != null ? BigDecimal.valueOf(avgRating) : BigDecimal.ZERO);
        teacher.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);

        teacherProfileRepository.save(teacher);
        log.debug("Updated teacher {} statistics - Rating: {}, Reviews: {}", teacherId, avgRating, totalReviews);
    }

    public List<TeacherReviewDto> getRecentReviews(Long teacherId, int limit) {
        List<TeacherReview> reviews = reviewRepository.findByTeacherIdAndApprovedOrderByCreatedAtDesc(
                teacherId, 
                true, 
                PageRequest.of(0, limit)
        );

        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TeacherReviewDto> getStudentReviews(Long studentId) {
        List<TeacherReview> reviews = reviewRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TeacherReviewDto convertToDto(TeacherReview review) {
        String studentName = "Anonymous";
        try {
            StudentProfile student = studentProfileRepository.findById(review.getStudentId()).orElse(null);
            if (student != null) {
                studentName = (student.getFirstName() + " " + student.getLastName()).trim();
                if (studentName.isBlank()) {
                    studentName = "Student #" + review.getStudentId();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch student name for review {}: {}", review.getId(), e.getMessage());
        }

        return TeacherReviewDto.builder()
                .studentName(studentName)
                .rating(review.getRating())
                .comment(review.getComment())
                .date(review.getCreatedAt().format(DATE_FORMATTER))
                .build();
    }
}

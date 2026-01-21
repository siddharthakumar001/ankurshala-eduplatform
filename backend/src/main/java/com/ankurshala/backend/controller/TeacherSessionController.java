package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.BookingResponse;
import com.ankurshala.backend.dto.teacher.TeacherSessionFeedbackDto;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherSessionFeedback;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TeacherSessionFeedbackRepository;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.TeacherBookingManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/teacher/sessions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@PreAuthorize("hasRole('TEACHER')")
@RequiredArgsConstructor
public class TeacherSessionController {

    private final TeacherBookingManagementService bookingManagementService;
    private final TeacherSessionFeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;
    private final TeacherRepository teacherRepository;

    @PostMapping("/{bookingId}/start")
    public ResponseEntity<BookingResponse> startSession(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Booking booking = bookingManagementService.startSession(userPrincipal.getId(), bookingId);
        return ResponseEntity.ok(convertToBookingResponse(booking));
    }

    @PostMapping("/{bookingId}/end")
    public ResponseEntity<BookingResponse> endSession(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        String sessionNotes = payload != null ? payload.getOrDefault("sessionNotes", "") : "";
        Booking booking = bookingManagementService.endSession(userPrincipal.getId(), bookingId, sessionNotes);
        return ResponseEntity.ok(convertToBookingResponse(booking));
    }

    @PostMapping("/feedback")
    public ResponseEntity<TeacherSessionFeedbackDto> submitFeedback(
            @Valid @RequestBody TeacherSessionFeedbackDto feedbackDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Teacher teacher = teacherRepository.findByUserId(userPrincipal.getId())
            .orElseThrow(() -> new RuntimeException("Teacher profile not found"));

        Booking booking = bookingRepository.findById(feedbackDto.getBookingId())
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getTeacherId() == null || !booking.getTeacherId().equals(userPrincipal.getId())) {
            return ResponseEntity.status(403).build();
        }

        TeacherSessionFeedback feedback = feedbackRepository.findByBookingId(booking.getId())
            .orElseGet(() -> new TeacherSessionFeedback(booking, teacher, booking.getStudent()));

        feedback.setSessionRating(feedbackDto.getSessionRating());
        feedback.setStudentEngagement(feedbackDto.getStudentEngagement());
        feedback.setSessionNotes(feedbackDto.getSessionNotes());
        feedback.setImprovementSuggestions(feedbackDto.getImprovementSuggestions());
        feedback.setWouldRecommend(feedbackDto.getWouldRecommend());

        TeacherSessionFeedback saved = feedbackRepository.save(feedback);

        TeacherSessionFeedbackDto response = new TeacherSessionFeedbackDto();
        response.setId(saved.getId());
        response.setBookingId(booking.getId());
        response.setSessionRating(saved.getSessionRating());
        response.setStudentEngagement(saved.getStudentEngagement());
        response.setSessionNotes(saved.getSessionNotes());
        response.setImprovementSuggestions(saved.getImprovementSuggestions());
        response.setWouldRecommend(saved.getWouldRecommend());

        return ResponseEntity.ok(response);
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setStudentId(booking.getStudentId());
        response.setStudentName(booking.getStudent() != null ? booking.getStudent().getName() : null);
        response.setTeacherId(booking.getTeacherId());
        response.setTopicId(booking.getTopicId());
        response.setTopicTitle(booking.getTopic() != null ? booking.getTopic().getTitle() : "Unknown");
        response.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getName() : null);
        response.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        response.setDurationMinutes(booking.getDurationMinutes());
        response.setStudentNotes(booking.getStudentNotes());
        response.setTeacherNotes(booking.getTeacherNotes());
        response.setPriceMin(booking.getPriceMin());
        response.setPriceMax(booking.getPriceMax());
        response.setPriceCurrency(booking.getPriceCurrency());
        if (booking.getStartTime() != null) {
            response.setStartTime(booking.getStartTime().toLocalDateTime());
        }
        if (booking.getEndTime() != null) {
            response.setEndTime(booking.getEndTime().toLocalDateTime());
        }
        if (booking.getAcceptedAt() != null) {
            response.setAcceptedAt(booking.getAcceptedAt().toLocalDateTime());
        }
        if (booking.getCancelledAt() != null) {
            response.setCancelledAt(booking.getCancelledAt().toLocalDateTime());
        }
        response.setCancellationReason(booking.getCancellationReason());
        if (booking.getCreatedAt() != null) {
            response.setCreatedAt(booking.getCreatedAt().toLocalDateTime());
        }
        if (booking.getUpdatedAt() != null) {
            response.setUpdatedAt(booking.getUpdatedAt().toLocalDateTime());
        }
        return response;
    }
}

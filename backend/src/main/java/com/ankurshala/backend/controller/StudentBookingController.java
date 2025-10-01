package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class StudentBookingController {

    private final StudentBookingService bookingService;

    @PostMapping("/quote")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingQuoteResponse> getBookingQuote(
            @Valid @RequestBody BookingQuoteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting booking quote for student {}", userPrincipal.getId());
        
        BookingQuoteResponse response = bookingService.getBookingQuote(request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Creating booking for student {}", userPrincipal.getId());
        
        BookingResponse response = bookingService.createBooking(request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CalendarEventResponse>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting calendar events for student {} from {} to {}", userPrincipal.getId(), from, to);
        
        List<CalendarEventResponse> events = bookingService.getCalendarEvents(userPrincipal, from, to);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> rescheduleBooking(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleBookingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Rescheduling booking {} for student {}", id, userPrincipal.getId());
        
        BookingResponse response = bookingService.rescheduleBooking(id, request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @Valid @RequestBody CancelBookingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Cancelling booking {} for student {}", id, userPrincipal.getId());
        
        BookingResponse response = bookingService.cancelBooking(id, request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting upcoming bookings for student {}", userPrincipal.getId());
        
        List<BookingResponse> bookings = bookingService.getUpcomingBookings(userPrincipal);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<BookingResponse>> getBookingHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting booking history for student {}", userPrincipal.getId());
        
        List<BookingResponse> bookings = bookingService.getBookingHistory(userPrincipal);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting booking {} for student {}", id, userPrincipal.getId());
        
        BookingResponse response = bookingService.getBookingById(id, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/fee-preview")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<FeePreviewResponse> getFeePreview(
            @PathVariable Long id,
            @Valid @RequestBody FeePreviewRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting fee preview for booking {} action {} for student {}", id, request.getAction(), userPrincipal.getId());
        
        FeePreviewResponse response = bookingService.getFeePreview(id, request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingNoteResponse> addBookingNote(
            @PathVariable Long id,
            @Valid @RequestBody BookingNoteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Adding note to booking {} for student {}", id, userPrincipal.getId());
        
        BookingNoteResponse response = bookingService.addBookingNote(id, request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/bookmark")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> bookmarkBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Bookmarking booking {} for student {}", id, userPrincipal.getId());
        
        bookingService.bookmarkBooking(id, userPrincipal);
        return ResponseEntity.ok(Map.of("message", "Booking bookmarked successfully"));
    }

    @DeleteMapping("/{id}/bookmark")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> unbookmarkBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Unbookmarking booking {} for student {}", id, userPrincipal.getId());
        
        bookingService.unbookmarkBooking(id, userPrincipal);
        return ResponseEntity.ok(Map.of("message", "Booking unbookmarked successfully"));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<BookingResponse> addBookingFeedback(
            @PathVariable Long id,
            @Valid @RequestBody BookingFeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Adding feedback to booking {} for student {}", id, userPrincipal.getId());
        
        BookingResponse response = bookingService.addBookingFeedback(id, request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> joinSession(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Student {} joining session for booking {}", userPrincipal.getId(), id);
        
        // TODO: Implement session joining logic
        // This would typically redirect to a video session or provide session details
        
        return ResponseEntity.ok(Map.of(
            "message", "Session joined successfully",
            "sessionUrl", "https://meet.ankurshala.com/session/" + id,
            "sessionId", id.toString()
        ));
    }
}

package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.BookingResponse;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/teacher/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TEACHER')")
public class TeacherBookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketService;

    @PostMapping("/{bookingId}/accept")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> acceptBooking(
            @PathVariable Long bookingId,
            @RequestParam String acceptanceToken,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Teacher {} accepting booking {} with token {}", userPrincipal.getId(), bookingId, acceptanceToken);
        
        // Find booking by acceptance token
        Optional<Booking> bookingOpt = bookingRepository.findByAcceptanceToken(acceptanceToken);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid acceptance token"));
        }
        
        Booking booking = bookingOpt.get();
        
        // Validate booking ID matches
        if (!booking.getId().equals(bookingId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Booking ID mismatch"));
        }
        
        // Validate booking is still in REQUESTED status
        if (booking.getStatus() != Booking.BookingStatus.REQUESTED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Booking is no longer available for acceptance"));
        }
        
        // Set teacher and accept booking
        User teacher = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        booking.setTeacher(teacher);
        booking.setStatus(Booking.BookingStatus.ACCEPTED);
        booking.setAcceptedAt(LocalDateTime.now());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send WebSocket notification to student
        webSocketService.notifyBookingAccepted(savedBooking);
        
        log.info("Teacher {} accepted booking {}", userPrincipal.getId(), bookingId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Booking accepted successfully",
            "bookingId", savedBooking.getId(),
            "studentId", savedBooking.getStudent().getId(),
            "startTime", savedBooking.getStartTime(),
            "endTime", savedBooking.getEndTime()
        ));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<BookingResponse>> getPendingBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting pending bookings for teacher {}", userPrincipal.getId());
        
        // TODO: Implement logic to get pending bookings for this teacher
        // This would typically involve:
        // 1. Finding bookings where teacher is null (not yet assigned)
        // 2. Filtering by teacher's subject expertise
        // 3. Filtering by teacher's availability
        // 4. Filtering by teacher's preferred student levels
        
        List<BookingResponse> pendingBookings = List.of(); // Placeholder
        
        return ResponseEntity.ok(pendingBookings);
    }

    @GetMapping("/accepted")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<BookingResponse>> getAcceptedBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting accepted bookings for teacher {}", userPrincipal.getId());
        
        // TODO: Implement logic to get accepted bookings for this teacher
        List<BookingResponse> acceptedBookings = List.of(); // Placeholder
        
        return ResponseEntity.ok(acceptedBookings);
    }

    @PostMapping("/{bookingId}/notes")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> addTeacherNote(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Teacher {} adding note to booking {}", userPrincipal.getId(), bookingId);
        
        // TODO: Implement teacher note addition
        // This would involve:
        // 1. Validating the teacher owns this booking
        // 2. Adding a note to the booking
        
        return ResponseEntity.ok(Map.of("message", "Note added successfully"));
    }

    @PostMapping("/{bookingId}/feedback")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, String>> addTeacherFeedback(
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Teacher {} adding feedback to booking {}", userPrincipal.getId(), bookingId);
        
        // TODO: Implement teacher feedback addition
        // This would involve:
        // 1. Validating the teacher owns this booking
        // 2. Adding feedback to the booking
        
        return ResponseEntity.ok(Map.of("message", "Feedback added successfully"));
    }
}

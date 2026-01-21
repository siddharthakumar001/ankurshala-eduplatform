package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.BookingResponse;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TeacherSubjectExpertiseRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('TEACHER')")
public class TeacherBookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherSubjectExpertiseRepository teacherSubjectExpertiseRepository;
    private final WebSocketNotificationService webSocketService;

    @PostMapping("/{bookingId}/accept")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> acceptBooking(
            @PathVariable Long bookingId,
            @RequestParam String acceptanceToken,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Teacher {} accepting booking {} with token {}", userPrincipal.getId(), bookingId, acceptanceToken);
        
        // Find booking by acceptance token (mock implementation)
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid acceptance token"));
        }
        
        Booking booking = bookingOpt.get();
        
        // Validate booking ID matches
        if (!booking.getId().equals(bookingId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Booking ID mismatch"));
        }
        
        // Validate booking is still in PENDING status
        if (booking.getStatus() != BookingStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("error", "Booking is no longer available for acceptance"));
        }
        
        // Set teacher and accept booking
        User teacher = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        booking.setTeacher(teacher);
        booking.setTeacherId(teacher.getId());
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAcceptedAt(ZonedDateTime.now());
        
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
        
        // Get teacher entity from user ID
        Optional<Teacher> teacherOpt = teacherRepository.findByUserId(userPrincipal.getId());
        if (teacherOpt.isEmpty()) {
            log.warn("No teacher profile found for user {}", userPrincipal.getId());
            return ResponseEntity.ok(List.of());
        }
        Teacher teacher = teacherOpt.get();
        
        // Get all pending bookings (where teacher is null - not yet assigned)
        List<Booking> allPendingBookings = bookingRepository.findByStatus(BookingStatus.PENDING);
        
        // Filter by teacher's subject expertise
        List<BookingResponse> matchingBookings = allPendingBookings.stream()
            .filter(booking -> {
                // Check if teacher has expertise for this booking's topic
                if (booking.getTopic() == null) return false;
                
                Long subjectId = booking.getTopic().getSubjectId();
                Long gradeId = booking.getTopic().getGradeId();
                Long boardId = booking.getTopic().getBoardId();
                
                return teacherSubjectExpertiseRepository.hasExpertise(
                    teacher.getId(), subjectId, gradeId, boardId);
            })
            .map(this::convertToBookingResponse)
            .collect(Collectors.toList());
        
        log.info("Found {} matching pending bookings for teacher {}", matchingBookings.size(), teacher.getId());
        return ResponseEntity.ok(matchingBookings);
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<BookingResponse>> getTeacherBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Booking> bookings = bookingRepository.findByTeacherIdOrderByStartTsDesc(userPrincipal.getId());
        List<BookingResponse> responses = bookings.stream()
            .map(this::convertToBookingResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
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
        return response;
    }

    @GetMapping("/accepted")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<BookingResponse>> getAcceptedBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting accepted bookings for teacher {}", userPrincipal.getId());
        
        // Get bookings where this teacher is assigned
        List<Booking> acceptedBookings = bookingRepository.findByTeacherIdAndStatus(
            userPrincipal.getId(), BookingStatus.ACCEPTED);
        
        List<BookingResponse> responses = acceptedBookings.stream()
            .map(this::convertToBookingResponse)
            .collect(Collectors.toList());
        
        log.info("Found {} accepted bookings for teacher {}", responses.size(), userPrincipal.getId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/completed")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<BookingResponse>> getCompletedBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting completed bookings for teacher {}", userPrincipal.getId());

        List<Booking> completedBookings = bookingRepository.findByTeacherIdAndStatus(
            userPrincipal.getId(), BookingStatus.COMPLETED);

        List<BookingResponse> responses = completedBookings.stream()
            .map(this::convertToBookingResponse)
            .collect(Collectors.toList());

        log.info("Found {} completed bookings for teacher {}", responses.size(), userPrincipal.getId());
        return ResponseEntity.ok(responses);
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

    @PostMapping("/{bookingId}/decline")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> declineBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Teacher {} declining booking {}", userPrincipal.getId(), bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only pending bookings can be declined"));
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason("Declined by teacher");
        booking.setCancelledAt(ZonedDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);
        webSocketService.notifyBookingCancelled(savedBooking);

        return ResponseEntity.ok(Map.of(
            "message", "Booking declined",
            "bookingId", savedBooking.getId()
        ));
    }

    @PostMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> completeBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Teacher {} completing booking {}", userPrincipal.getId(), bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getTeacherId() == null || !booking.getTeacherId().equals(userPrincipal.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED && booking.getStatus() != BookingStatus.IN_PROGRESS) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only accepted bookings can be completed"));
        }

        booking.setStatus(BookingStatus.COMPLETED);

        Booking savedBooking = bookingRepository.save(booking);
        webSocketService.notifyBookingCompleted(savedBooking);

        return ResponseEntity.ok(Map.of(
            "message", "Booking completed",
            "bookingId", savedBooking.getId()
        ));
    }
}

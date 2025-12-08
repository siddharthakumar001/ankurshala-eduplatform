package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.BookingResponse;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.WebSocketNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherBookingControllerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebSocketNotificationService webSocketService;

    @InjectMocks
    private TeacherBookingController controller;

    private UserPrincipal userPrincipal;
    private User teacher;
    private User student;
    private Booking booking;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "teacher@example.com", "Teacher Name", "password", Role.TEACHER, true);

        teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@example.com");
        teacher.setName("Teacher Name");
        teacher.setRole(Role.TEACHER);

        student = new User();
        student.setId(2L);
        student.setEmail("student@example.com");
        student.setName("Student Name");
        student.setRole(Role.STUDENT);

        booking = new Booking();
        booking.setId(1L);
        booking.setStudent(student);
        booking.setState("REQUESTED");
        booking.setAcceptanceToken("test-token");
        booking.setStartTs(ZonedDateTime.now().plusDays(1));
        booking.setEndTs(ZonedDateTime.now().plusDays(1).plusHours(1));

        bookingResponse = new BookingResponse();
        bookingResponse.setId(1L);
        bookingResponse.setStudentId(2L);
        bookingResponse.setTeacherId(1L);
        bookingResponse.setStatus("ACCEPTED");
    }

    @Test
    void testAcceptBooking_Success() {
        // Setup
        String acceptanceToken = "test-token";
        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setStudent(student);
        savedBooking.setTeacher(teacher);
        savedBooking.setState("ACCEPTED");
        savedBooking.setAcceptedAt(ZonedDateTime.now());
        savedBooking.setStartTs(ZonedDateTime.now().plusDays(1));
        savedBooking.setEndTs(ZonedDateTime.now().plusDays(1).plusHours(1));

        // Mock repositories
        when(bookingRepository.findByAcceptanceToken(acceptanceToken)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        // Execute
        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking accepted successfully", response.getBody().get("message"));
        assertEquals(1L, response.getBody().get("bookingId"));
        assertEquals(2L, response.getBody().get("studentId"));

        // Verify service calls
        verify(bookingRepository).findByAcceptanceToken(acceptanceToken);
        verify(userRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
        verify(webSocketService).notifyBookingAccepted(savedBooking);
    }

    @Test
    void testAcceptBooking_InvalidToken() {
        // Setup
        String acceptanceToken = "invalid-token";

        // Mock repository
        when(bookingRepository.findByAcceptanceToken(acceptanceToken)).thenReturn(Optional.empty());

        // Execute
        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid acceptance token", response.getBody().get("error"));

        // Verify no other operations were performed
        verify(bookingRepository).findByAcceptanceToken(acceptanceToken);
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(webSocketService, never()).notifyBookingAccepted(any(Booking.class));
    }

    @Test
    void testAcceptBooking_BookingIdMismatch() {
        // Setup
        String acceptanceToken = "test-token";
        booking.setId(2L); // Different ID

        // Mock repository
        when(bookingRepository.findByAcceptanceToken(acceptanceToken)).thenReturn(Optional.of(booking));

        // Execute
        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking ID mismatch", response.getBody().get("error"));

        // Verify no other operations were performed
        verify(bookingRepository).findByAcceptanceToken(acceptanceToken);
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(webSocketService, never()).notifyBookingAccepted(any(Booking.class));
    }

    @Test
    void testAcceptBooking_BookingNotRequested() {
        // Setup
        String acceptanceToken = "test-token";
        booking.setState("ACCEPTED"); // Already accepted

        // Mock repository
        when(bookingRepository.findByAcceptanceToken(acceptanceToken)).thenReturn(Optional.of(booking));

        // Execute
        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking is no longer available for acceptance", response.getBody().get("error"));

        // Verify no other operations were performed
        verify(bookingRepository).findByAcceptanceToken(acceptanceToken);
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(webSocketService, never()).notifyBookingAccepted(any(Booking.class));
    }

    @Test
    void testAcceptBooking_TeacherNotFound() {
        // Setup
        String acceptanceToken = "test-token";

        // Mock repositories
        when(bookingRepository.findByAcceptanceToken(acceptanceToken)).thenReturn(Optional.of(booking));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Execute and verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            controller.acceptBooking(1L, acceptanceToken, userPrincipal);
        });

        // Verify service calls
        verify(bookingRepository).findByAcceptanceToken(acceptanceToken);
        verify(userRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(webSocketService, never()).notifyBookingAccepted(any(Booking.class));
    }

    @Test
    void testGetPendingBookings_Success() {
        // Setup
        List<BookingResponse> pendingBookings = List.of(bookingResponse);

        // Execute
        ResponseEntity<List<BookingResponse>> response = controller.getPendingBookings(userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty()); // Currently returns empty list
    }

    @Test
    void testGetAcceptedBookings_Success() {
        // Setup
        List<BookingResponse> acceptedBookings = List.of(bookingResponse);

        // Execute
        ResponseEntity<List<BookingResponse>> response = controller.getAcceptedBookings(userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty()); // Currently returns empty list
    }

    @Test
    void testAddTeacherNote_Success() {
        // Setup
        Map<String, String> request = Map.of("note", "Test note");

        // Execute
        ResponseEntity<Map<String, String>> response = controller.addTeacherNote(1L, request, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Note added successfully", response.getBody().get("message"));
    }

    @Test
    void testAddTeacherFeedback_Success() {
        // Setup
        Map<String, Object> request = Map.of("rating", 5, "comment", "Great student!");

        // Execute
        ResponseEntity<Map<String, String>> response = controller.addTeacherFeedback(1L, request, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Feedback added successfully", response.getBody().get("message"));
    }
}

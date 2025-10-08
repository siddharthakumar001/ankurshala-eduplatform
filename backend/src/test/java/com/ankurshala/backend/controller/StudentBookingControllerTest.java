package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentBookingControllerTest {

    @Mock
    private StudentBookingService bookingService;

    @InjectMocks
    private StudentBookingController controller;

    private UserPrincipal userPrincipal;
    private BookingQuoteRequest quoteRequest;
    private BookingQuoteResponse quoteResponse;
    private CreateBookingRequest createRequest;
    private BookingResponse bookingResponse;
    private RescheduleBookingRequest rescheduleRequest;
    private CancelBookingRequest cancelRequest;
    private CalendarEventResponse calendarEvent;
    private FeePreviewRequest feePreviewRequest;
    private FeePreviewResponse feePreviewResponse;
    private BookingNoteRequest noteRequest;
    private BookingNoteResponse noteResponse;
    private BookingFeedbackRequest feedbackRequest;

    @BeforeEach
    void setUp() {
        userPrincipal = new UserPrincipal(1L, "student@example.com", "Student Name", "password", Role.STUDENT, true);

        quoteRequest = new BookingQuoteRequest();
        quoteRequest.setTopicId(1L);
        quoteRequest.setStartTime(LocalDateTime.now().plusDays(1));
        quoteRequest.setDurationMinutes(60);
        quoteRequest.setTimezone("Asia/Kolkata");

        quoteResponse = new BookingQuoteResponse();
        quoteResponse.setExpectedMinutes(60);
        quoteResponse.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        quoteResponse.setBufferOk(true);
        quoteResponse.setPrice(new BookingQuoteResponse.PriceInfo("INR", java.math.BigDecimal.valueOf(500), java.math.BigDecimal.valueOf(600), 1L));

        createRequest = new CreateBookingRequest();
        createRequest.setTopicId(1L);
        createRequest.setStartTime(LocalDateTime.now().plusDays(1));
        createRequest.setDurationMinutes(60);
        createRequest.setStudentNotes("Please focus on algebra");
        createRequest.setTimezone("Asia/Kolkata");

        bookingResponse = new BookingResponse();
        bookingResponse.setId(1L);
        bookingResponse.setStudentId(1L);
        bookingResponse.setTeacherId(1L);
        bookingResponse.setStatus("CONFIRMED");

        rescheduleRequest = new RescheduleBookingRequest();
        rescheduleRequest.setNewStartTime(LocalDateTime.now().plusDays(2));
        rescheduleRequest.setNewDurationMinutes(60);
        rescheduleRequest.setReason("Schedule conflict");
        rescheduleRequest.setTimezone("Asia/Kolkata");

        cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Schedule conflict");

        calendarEvent = new CalendarEventResponse();
        calendarEvent.setId(1L);
        calendarEvent.setTitle("Math Session");

        feePreviewRequest = new FeePreviewRequest();
        feePreviewRequest.setAction("CANCEL");

        feePreviewResponse = new FeePreviewResponse(
            java.math.BigDecimal.valueOf(50.0), 
            "INR", 
            "Cancellation fee", 
            false
        );

        noteRequest = new BookingNoteRequest();
        noteRequest.setContent("Please focus on algebra");
        noteRequest.setNoteType("GENERAL");

        noteResponse = new BookingNoteResponse();
        noteResponse.setId(1L);
        noteResponse.setAuthorId(1L);
        noteResponse.setAuthorName("Student Name");
        noteResponse.setContent("Please focus on algebra");
        noteResponse.setNoteType("GENERAL");
        noteResponse.setCreatedAt(LocalDateTime.now());

        feedbackRequest = new BookingFeedbackRequest();
        feedbackRequest.setRating(5);
        feedbackRequest.setFeedback("Great session!");
    }

    @Test
    void testGetBookingQuote_Success() {
        // Mock service
        when(bookingService.getBookingQuote(any(BookingQuoteRequest.class), any(UserPrincipal.class)))
                .thenReturn(quoteResponse);

        // Execute
        ResponseEntity<BookingQuoteResponse> response = controller.getBookingQuote(quoteRequest, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(quoteResponse, response.getBody());
        verify(bookingService).getBookingQuote(quoteRequest, userPrincipal);
    }

    @Test
    void testCreateBooking_Success() {
        // Mock service
        when(bookingService.createBooking(any(CreateBookingRequest.class), any(UserPrincipal.class)))
                .thenReturn(bookingResponse);

        // Execute
        ResponseEntity<BookingResponse> response = controller.createBooking(createRequest, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookingResponse, response.getBody());
        verify(bookingService).createBooking(createRequest, userPrincipal);
    }

    @Test
    void testGetCalendarEvents_Success() {
        // Setup
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(7);
        List<CalendarEventResponse> events = List.of(calendarEvent);

        // Mock service
        when(bookingService.getCalendarEvents(any(UserPrincipal.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(events);

        // Execute
        ResponseEntity<List<CalendarEventResponse>> response = controller.getCalendarEvents(from, to, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
        verify(bookingService).getCalendarEvents(userPrincipal, from, to);
    }

    @Test
    void testRescheduleBooking_Success() {
        // Mock service
        when(bookingService.rescheduleBooking(anyLong(), any(RescheduleBookingRequest.class), any(UserPrincipal.class)))
                .thenReturn(bookingResponse);

        // Execute
        ResponseEntity<BookingResponse> response = controller.rescheduleBooking(1L, rescheduleRequest, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookingResponse, response.getBody());
        verify(bookingService).rescheduleBooking(1L, rescheduleRequest, userPrincipal);
    }

    @Test
    void testCancelBooking_Success() {
        // Mock service
        when(bookingService.cancelBooking(anyLong(), any(CancelBookingRequest.class), any(UserPrincipal.class)))
                .thenReturn(bookingResponse);

        // Execute
        ResponseEntity<BookingResponse> response = controller.cancelBooking(1L, cancelRequest, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookingResponse, response.getBody());
        verify(bookingService).cancelBooking(1L, cancelRequest, userPrincipal);
    }

    @Test
    void testGetUpcomingBookings_Success() {
        // Setup
        List<BookingResponse> bookings = List.of(bookingResponse);

        // Mock service
        when(bookingService.getUpcomingBookings(any(UserPrincipal.class)))
                .thenReturn(bookings);

        // Execute
        ResponseEntity<List<BookingResponse>> response = controller.getUpcomingBookings(userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookings, response.getBody());
        verify(bookingService).getUpcomingBookings(userPrincipal);
    }

    @Test
    void testGetBookingHistory_Success() {
        // Setup
        List<BookingResponse> bookings = List.of(bookingResponse);

        // Mock service
        when(bookingService.getBookingHistory(any(UserPrincipal.class)))
                .thenReturn(bookings);

        // Execute
        ResponseEntity<List<BookingResponse>> response = controller.getBookingHistory(userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookings, response.getBody());
        verify(bookingService).getBookingHistory(userPrincipal);
    }

    @Test
    void testGetBookingById_Success() {
        // Mock service
        when(bookingService.getBookingById(anyLong(), any(UserPrincipal.class)))
                .thenReturn(bookingResponse);

        // Execute
        ResponseEntity<BookingResponse> response = controller.getBookingById(1L, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookingResponse, response.getBody());
        verify(bookingService).getBookingById(1L, userPrincipal);
    }

    @Test
    void testGetFeePreview_Success() {
        // Mock service
        when(bookingService.getFeePreview(anyLong(), any(FeePreviewRequest.class), any(UserPrincipal.class)))
                .thenReturn(feePreviewResponse);

        // Execute
        ResponseEntity<FeePreviewResponse> response = controller.getFeePreview(1L, feePreviewRequest, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(feePreviewResponse, response.getBody());
        verify(bookingService).getFeePreview(1L, feePreviewRequest, userPrincipal);
    }

    @Test
    void testAddBookingNote_Success() {
        // Mock service
        when(bookingService.addBookingNote(anyLong(), any(BookingNoteRequest.class), any(UserPrincipal.class)))
                .thenReturn(noteResponse);

        // Execute
        ResponseEntity<BookingNoteResponse> response = controller.addBookingNote(1L, noteRequest, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(noteResponse, response.getBody());
        verify(bookingService).addBookingNote(1L, noteRequest, userPrincipal);
    }

    @Test
    void testBookmarkBooking_Success() {
        // Mock service
        doNothing().when(bookingService).bookmarkBooking(anyLong(), any(UserPrincipal.class));

        // Execute
        ResponseEntity<Map<String, String>> response = controller.bookmarkBooking(1L, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking bookmarked successfully", response.getBody().get("message"));
        verify(bookingService).bookmarkBooking(1L, userPrincipal);
    }

    @Test
    void testUnbookmarkBooking_Success() {
        // Mock service
        doNothing().when(bookingService).unbookmarkBooking(anyLong(), any(UserPrincipal.class));

        // Execute
        ResponseEntity<Map<String, String>> response = controller.unbookmarkBooking(1L, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking unbookmarked successfully", response.getBody().get("message"));
        verify(bookingService).unbookmarkBooking(1L, userPrincipal);
    }

    @Test
    void testAddBookingFeedback_Success() {
        // Mock service
        when(bookingService.addBookingFeedback(anyLong(), any(BookingFeedbackRequest.class), any(UserPrincipal.class)))
                .thenReturn(bookingResponse);

        // Execute
        ResponseEntity<BookingResponse> response = controller.addBookingFeedback(1L, feedbackRequest, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookingResponse, response.getBody());
        verify(bookingService).addBookingFeedback(1L, feedbackRequest, userPrincipal);
    }

    @Test
    void testJoinSession_Success() {
        // Execute
        ResponseEntity<Map<String, String>> response = controller.joinSession(1L, userPrincipal);

        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Session joined successfully", response.getBody().get("message"));
        assertEquals("https://meet.ankurshala.com/session/1", response.getBody().get("sessionUrl"));
        assertEquals("1", response.getBody().get("sessionId"));
    }
}

package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.BookingResponse;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BookingDeclineRepository;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.TeacherAvailabilitySlotRepository;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TeacherSubjectExpertiseRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.DistributedLockService;
import com.ankurshala.backend.service.WebSocketNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeacherBookingControllerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingDeclineRepository bookingDeclineRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private TeacherSubjectExpertiseRepository teacherSubjectExpertiseRepository;

    @Mock
    private TeacherAvailabilitySlotRepository teacherAvailabilitySlotRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    private WebSocketNotificationService webSocketService;

    @InjectMocks
    private TeacherBookingController controller;

    private UserPrincipal userPrincipal;
    private User teacher;
    private Teacher teacherProfile;
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

        teacherProfile = new Teacher();
        teacherProfile.setId(10L);
        teacherProfile.setUser(teacher);
        teacherProfile.setName("Teacher Name");
        teacherProfile.setEmail("teacher@example.com");

        student = new User();
        student.setId(2L);
        student.setEmail("student@example.com");
        student.setName("Student Name");
        student.setRole(Role.STUDENT);

        Topic topic = new Topic();
        topic.setId(101L);
        topic.setTitle("Algebra");
        topic.setSubjectId(201L);
        topic.setGradeId(301L);
        topic.setBoardId(401L);

        booking = new Booking();
        booking.setId(1L);
        booking.setStudentId(student.getId());
        booking.setStudent(student);
        booking.setTopicId(topic.getId());
        booking.setTopic(topic);
        booking.setSubjectId(topic.getSubjectId());
        booking.setBoard("CBSE");
        booking.setGrade("GRADE_8");
        booking.setCategory("Academic");
        booking.setDurationMinutes(60);
        booking.setPriceMinCents(1000);
        booking.setPriceMaxCents(2000);
        booking.setState("REQUESTED");
        booking.setAcceptanceToken("test-token");
        booking.setStartTs(ZonedDateTime.now().plusDays(1));
        booking.setEndTs(ZonedDateTime.now().plusDays(1).plusHours(1));
        booking.setStatus(BookingStatus.PENDING);

        bookingResponse = new BookingResponse();
        bookingResponse.setId(1L);
        bookingResponse.setStudentId(2L);
        bookingResponse.setTeacherId(1L);
        bookingResponse.setStatus("ACCEPTED");

        when(distributedLockService.executeWithLock(anyString(), anyLong(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(2);
                    return supplier.get();
                });
    }

    @Test
    void testAcceptBooking_Success() {
        String acceptanceToken = "test-token";
        Booking savedBooking = new Booking();
        savedBooking.setId(1L);
        savedBooking.setStudent(student);
        savedBooking.setStudentId(student.getId());
        savedBooking.setTeacher(teacher);
        savedBooking.setTeacherId(teacher.getId());
        savedBooking.setTopic(booking.getTopic());
        savedBooking.setTopicId(booking.getTopicId());
        savedBooking.setSubjectId(booking.getSubjectId());
        savedBooking.setBoard(booking.getBoard());
        savedBooking.setGrade(booking.getGrade());
        savedBooking.setCategory(booking.getCategory());
        savedBooking.setDurationMinutes(booking.getDurationMinutes());
        savedBooking.setPriceMinCents(booking.getPriceMinCents());
        savedBooking.setPriceMaxCents(booking.getPriceMaxCents());
        savedBooking.setState("ACCEPTED");
        savedBooking.setAcceptedAt(ZonedDateTime.now());
        savedBooking.setStartTs(ZonedDateTime.now().plusDays(1));
        savedBooking.setEndTs(ZonedDateTime.now().plusDays(1).plusHours(1));
        savedBooking.setStatus(BookingStatus.ACCEPTED);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking))
                .thenReturn(Optional.of(savedBooking));
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacherProfile));
        when(teacherSubjectExpertiseRepository.hasExpertise(eq(teacherProfile.getId()), anyLong(), anyLong(), anyLong()))
                .thenReturn(true);
        when(teacherAvailabilitySlotRepository.countByTeacher(teacher)).thenReturn(0L);
        when(bookingDeclineRepository.existsByBookingIdAndTeacherId(1L, teacherProfile.getId())).thenReturn(false);
        when(bookingRepository.acceptBookingWithConflictCheck(
                eq(1L), eq(1L), any(ZonedDateTime.class), any(ZonedDateTime.class), any(ZonedDateTime.class), any(ZonedDateTime.class)))
                .thenReturn(1);

        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking accepted successfully", response.getBody().get("message"));
        assertEquals(1L, response.getBody().get("bookingId"));
        assertEquals(2L, response.getBody().get("studentId"));

        verify(userRepository).findById(1L);
        verify(teacherRepository).findByUserId(1L);
        verify(bookingRepository, times(2)).findById(1L);
        verify(bookingRepository).acceptBookingWithConflictCheck(
                eq(1L), eq(1L), any(ZonedDateTime.class), any(ZonedDateTime.class), any(ZonedDateTime.class), any(ZonedDateTime.class));
    }

    @Test
    void testAcceptBooking_InvalidToken() {
        String acceptanceToken = "invalid-token";

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacherProfile));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid acceptance token", response.getBody().get("error"));

        verify(userRepository).findById(1L);
        verify(teacherRepository).findByUserId(1L);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).acceptBookingWithConflictCheck(
                anyLong(), anyLong(), any(ZonedDateTime.class), any(ZonedDateTime.class), any(ZonedDateTime.class), any(ZonedDateTime.class));
    }

    @Test
    void testAcceptBooking_BookingNotFound() {
        String acceptanceToken = "test-token";

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacherProfile));
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking not found", response.getBody().get("error"));

        verify(userRepository).findById(1L);
        verify(teacherRepository).findByUserId(1L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void testAcceptBooking_BookingAlreadyAccepted() {
        String acceptanceToken = "test-token";
        booking.setStatus(BookingStatus.ACCEPTED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacherProfile));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        ResponseEntity<Map<String, Object>> response = controller.acceptBooking(1L, acceptanceToken, userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Booking has already been accepted", response.getBody().get("error"));

        verify(userRepository).findById(1L);
        verify(teacherRepository).findByUserId(1L);
        verify(bookingRepository).findById(1L);
    }

    @Test
    void testAcceptBooking_TeacherNotFound() {
        String acceptanceToken = "test-token";

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            controller.acceptBooking(1L, acceptanceToken, userPrincipal);
        });

        verify(userRepository).findById(1L);
        verify(teacherRepository, never()).findByUserId(anyLong());
        verify(bookingRepository, never()).findById(anyLong());
    }

    @Test
    void testGetPendingBookings_Success() {
        when(teacherRepository.findByUserId(1L)).thenReturn(Optional.of(teacherProfile));
        when(bookingRepository.findByStatus(BookingStatus.PENDING)).thenReturn(List.of(booking));
        when(teacherSubjectExpertiseRepository.hasExpertise(eq(teacherProfile.getId()), anyLong(), anyLong(), anyLong()))
                .thenReturn(true);
        when(bookingDeclineRepository.existsByBookingIdAndTeacherId(1L, teacherProfile.getId()))
                .thenReturn(false);

        ResponseEntity<List<BookingResponse>> response = controller.getPendingBookings(userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAcceptedBookings_Success() {
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setTeacherId(teacher.getId());
        booking.setTeacher(teacher);
        when(bookingRepository.findByTeacherIdAndStatus(1L, BookingStatus.ACCEPTED))
                .thenReturn(List.of(booking));

        ResponseEntity<List<BookingResponse>> response = controller.getAcceptedBookings(userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testAddTeacherNote_Success() {
        Map<String, String> request = Map.of("note", "Test note");

        ResponseEntity<Map<String, String>> response = controller.addTeacherNote(1L, request, userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Note added successfully", response.getBody().get("message"));
    }

    @Test
    void testAddTeacherFeedback_Success() {
        Map<String, Object> request = Map.of("rating", 5, "comment", "Great student!");

        ResponseEntity<Map<String, String>> response = controller.addTeacherFeedback(1L, request, userPrincipal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Feedback added successfully", response.getBody().get("message"));
    }
}

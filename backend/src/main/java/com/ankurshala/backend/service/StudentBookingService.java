package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.dto.admin.PricingRuleDto;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentBookingService {

    private final BookingRepository bookingRepository;
    private final BookingBookmarkRepository bookingBookmarkRepository;
    private final BookingNoteRepository bookingNoteRepository;
    private final TeacherAvailabilitySlotRepository teacherAvailabilitySlotRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final FeeWaiverRepository feeWaiverRepository;
    
    @Autowired
    private AdminPricingService pricingService;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private WebSocketNotificationService webSocketService;

    private static final int BUFFER_MINUTES = 15;
    private static final int MIN_SESSION_MINUTES = 30;
    private static final int MAX_SESSION_MINUTES = 120;

    public BookingQuoteResponse getBookingQuote(BookingQuoteRequest request, UserPrincipal userPrincipal) {
        log.info("Getting booking quote for topic {} at {}", request.getTopicId(), request.getStartTime());
        
        // Validate topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        // Convert timezone if provided
        LocalDateTime startTime = convertToUTC(request.getStartTime(), request.getTimezone());
        LocalDateTime endTime = startTime.plusMinutes(request.getDurationMinutes());
        
        // Check for conflicts
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<Booking.BookingStatus> conflictingStatuses = Arrays.asList(
                Booking.BookingStatus.REQUESTED,
                Booking.BookingStatus.ACCEPTED,
                Booking.BookingStatus.RESCHEDULED
        );
        
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                student, conflictingStatuses, startTime, endTime);
        
        boolean bufferOk = conflictingBookings.isEmpty();
        
        // Get pricing
        PricingRuleDto pricingRule = pricingService.resolvePricingRule(
                topic.getBoardId(), null, topic.getSubjectId(), 
                topic.getChapter().getId(), topic.getId());
        
        final BigDecimal priceMin;
        final BigDecimal priceMax;
        final Long ruleId;
        
        if (pricingRule != null) {
            BigDecimal hourlyRate = pricingRule.getHourlyRate();
            BigDecimal sessionHours = BigDecimal.valueOf(request.getDurationMinutes()).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            priceMin = hourlyRate.multiply(sessionHours);
            priceMax = priceMin; // For now, min and max are the same
            ruleId = pricingRule.getId();
        } else {
            priceMin = BigDecimal.ZERO;
            priceMax = BigDecimal.ZERO;
            ruleId = null;
        }
        
        return new BookingQuoteResponse() {{
            setExpectedMinutes(topic.getExpectedTimeMins());
            setEndTime(endTime);
            setBufferOk(bufferOk);
            setPrice(new BookingQuoteResponse.PriceInfo("INR", priceMin, priceMax, ruleId));
        }};
    }

    public BookingResponse createBooking(CreateBookingRequest request, UserPrincipal userPrincipal) {
        log.info("Creating booking for topic {} at {}", request.getTopicId(), request.getStartTime());
        
        // Validate topic exists
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        
        // Convert timezone if provided
        LocalDateTime startTime = convertToUTC(request.getStartTime(), request.getTimezone());
        LocalDateTime endTime = startTime.plusMinutes(request.getDurationMinutes());
        
        // Validate session duration
        if (request.getDurationMinutes() < MIN_SESSION_MINUTES || request.getDurationMinutes() > MAX_SESSION_MINUTES) {
            throw new IllegalArgumentException("Session duration must be between 30 and 120 minutes");
        }
        
        // Check for conflicts
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<Booking.BookingStatus> conflictingStatuses = Arrays.asList(
                Booking.BookingStatus.REQUESTED,
                Booking.BookingStatus.ACCEPTED,
                Booking.BookingStatus.RESCHEDULED
        );
        
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                student, conflictingStatuses, startTime, endTime);
        
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("Time slot conflicts with existing booking");
        }
        
        // Get pricing
        PricingRuleDto pricingRule = pricingService.resolvePricingRule(
                topic.getBoardId(), null, topic.getSubjectId(), 
                topic.getChapter().getId(), topic.getId());
        
        BigDecimal priceMin = BigDecimal.ZERO;
        BigDecimal priceMax = BigDecimal.ZERO;
        PricingRule pricingRuleEntity = null;
        
        if (pricingRule != null) {
            BigDecimal hourlyRate = pricingRule.getHourlyRate();
            BigDecimal sessionHours = BigDecimal.valueOf(request.getDurationMinutes()).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            priceMin = hourlyRate.multiply(sessionHours);
            priceMax = priceMin;
            pricingRuleEntity = new PricingRule();
            pricingRuleEntity.setId(pricingRule.getId());
        }
        
        // Create booking
        Booking booking = new Booking(student, topic, startTime, endTime, request.getDurationMinutes(), priceMin, priceMax);
        booking.setPricingRule(pricingRuleEntity);
        booking.setStudentNotes(request.getStudentNotes());
        booking.setAcceptanceToken(UUID.randomUUID().toString());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Publish Kafka event
        publishBookingRequestedEvent(savedBooking);
        
        // Broadcast to eligible teachers via WebSocket
        webSocketService.broadcastBookingRequest(savedBooking);
        
        log.info("Created booking {} for student {}", savedBooking.getId(), student.getId());
        
        return convertToBookingResponse(savedBooking);
    }

    public List<CalendarEventResponse> getCalendarEvents(UserPrincipal userPrincipal, LocalDateTime from, LocalDateTime to) {
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<Booking> bookings = bookingRepository.findByStudentAndTimeRange(student, from, to);
        
        return bookings.stream()
                .map(this::convertToCalendarEvent)
                .collect(Collectors.toList());
    }

    public BookingResponse rescheduleBooking(Long bookingId, RescheduleBookingRequest request, UserPrincipal userPrincipal) {
        log.info("Rescheduling booking {} to {}", bookingId, request.getNewStartTime());
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        // Validate status
        if (booking.getStatus() != Booking.BookingStatus.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted bookings can be rescheduled");
        }
        
        // Convert timezone if provided
        LocalDateTime newStartTime = convertToUTC(request.getNewStartTime(), request.getTimezone());
        LocalDateTime newEndTime = newStartTime.plusMinutes(request.getNewDurationMinutes());
        
        // Check for conflicts
        User student = booking.getStudent();
        List<Booking.BookingStatus> conflictingStatuses = Arrays.asList(
                Booking.BookingStatus.REQUESTED,
                Booking.BookingStatus.ACCEPTED,
                Booking.BookingStatus.RESCHEDULED
        );
        
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                student, conflictingStatuses, newStartTime, newEndTime);
        
        // Remove current booking from conflicts
        conflictingBookings.removeIf(b -> b.getId().equals(bookingId));
        
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("New time slot conflicts with existing booking");
        }
        
        // Update booking
        booking.setStartTime(newStartTime);
        booking.setEndTime(newEndTime);
        booking.setDurationMinutes(request.getNewDurationMinutes());
        booking.setStatus(Booking.BookingStatus.RESCHEDULED);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        log.info("Rescheduled booking {} for student {}", savedBooking.getId(), student.getId());
        
        return convertToBookingResponse(savedBooking);
    }

    public BookingResponse cancelBooking(Long bookingId, CancelBookingRequest request, UserPrincipal userPrincipal) {
        log.info("Cancelling booking {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        // Validate status
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }
        
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel completed booking");
        }
        
        // Update booking
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(request.getReason());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        log.info("Cancelled booking {} for student {}", savedBooking.getId(), booking.getStudent().getId());
        
        return convertToBookingResponse(savedBooking);
    }

    public List<BookingResponse> getUpcomingBookings(UserPrincipal userPrincipal) {
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<Booking> bookings = bookingRepository.findUpcomingByStudent(student, LocalDateTime.now());
        
        return bookings.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingHistory(UserPrincipal userPrincipal) {
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<Booking> bookings = bookingRepository.findHistoryByStudent(student, LocalDateTime.now());
        
        return bookings.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long bookingId, UserPrincipal userPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        return convertToBookingResponse(booking);
    }

    public FeePreviewResponse getFeePreview(Long bookingId, FeePreviewRequest request, UserPrincipal userPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        BigDecimal fee = BigDecimal.ZERO;
        String reason = "";
        boolean waived = false;
        
        if ("CANCEL".equals(request.getAction())) {
            fee = booking.getCancellationFee();
            reason = "Cancellation fee";
        } else if ("RESCHEDULE".equals(request.getAction())) {
            fee = booking.getRescheduleFee();
            reason = "Reschedule fee";
        }
        
        // Check for fee waivers
        if (booking.getFeeWaiver() != null) {
            fee = BigDecimal.ZERO;
            waived = true;
            reason = "Fee waived: " + booking.getFeeWaiver().getReason();
        }
        
        return new FeePreviewResponse(fee, "INR", reason, waived);
    }

    public BookingNoteResponse addBookingNote(Long bookingId, BookingNoteRequest request, UserPrincipal userPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        User author = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        BookingNote.NoteType noteType = BookingNote.NoteType.valueOf(request.getNoteType());
        
        BookingNote note = new BookingNote(booking, author, request.getContent(), noteType);
        BookingNote savedNote = bookingNoteRepository.save(note);
        
        return convertToBookingNoteResponse(savedNote);
    }

    public void bookmarkBooking(Long bookingId, UserPrincipal userPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        if (!bookingBookmarkRepository.existsByBookingAndStudent(booking, student)) {
            BookingBookmark bookmark = new BookingBookmark(booking, student);
            bookingBookmarkRepository.save(bookmark);
        }
    }

    public void unbookmarkBooking(Long bookingId, UserPrincipal userPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        bookingBookmarkRepository.deleteByBookingAndStudent(booking, student);
    }

    public BookingResponse addBookingFeedback(Long bookingId, BookingFeedbackRequest request, UserPrincipal userPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        // Validate status
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Feedback can only be added for completed bookings");
        }
        
        booking.setRating(request.getRating());
        booking.setStudentFeedback(request.getFeedback());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        return convertToBookingResponse(savedBooking);
    }

    // Helper methods
    private LocalDateTime convertToUTC(LocalDateTime localDateTime, String timezone) {
        if (timezone != null && !timezone.isEmpty()) {
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
            return zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        }
        return localDateTime;
    }

    private void publishBookingRequestedEvent(Booking booking) {
        Map<String, Object> event = new HashMap<>();
        event.put("bookingId", booking.getId());
        event.put("studentId", booking.getStudent().getId());
        event.put("topicId", booking.getTopic().getId());
        event.put("startTime", booking.getStartTime());
        event.put("endTime", booking.getEndTime());
        event.put("durationMinutes", booking.getDurationMinutes());
        event.put("acceptanceToken", booking.getAcceptanceToken());
        
        kafkaTemplate.send("booking.requested", event);
        log.info("Published booking.requested event for booking {}", booking.getId());
    }


    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setStudentId(booking.getStudent().getId());
        response.setTeacherId(booking.getTeacher() != null ? booking.getTeacher().getId() : null);
        response.setTopicId(booking.getTopic().getId());
        response.setTopicTitle(booking.getTopic().getTitle());
        response.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getName() : null);
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setDurationMinutes(booking.getDurationMinutes());
        response.setStatus(booking.getStatus().toString());
        response.setAcceptedAt(booking.getAcceptedAt());
        response.setCancelledAt(booking.getCancelledAt());
        response.setCancellationReason(booking.getCancellationReason());
        response.setPriceMin(booking.getPriceMin());
        response.setPriceMax(booking.getPriceMax());
        response.setPriceCurrency(booking.getPriceCurrency());
        response.setCancellationFee(booking.getCancellationFee());
        response.setRescheduleFee(booking.getRescheduleFee());
        response.setStudentNotes(booking.getStudentNotes());
        response.setTeacherNotes(booking.getTeacherNotes());
        response.setStudentFeedback(booking.getStudentFeedback());
        response.setTeacherFeedback(booking.getTeacherFeedback());
        response.setRating(booking.getRating());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        // Convert notes
        List<BookingNoteResponse> noteResponses = booking.getNotes().stream()
                .map(this::convertToBookingNoteResponse)
                .collect(Collectors.toList());
        response.setNotes(noteResponses);
        
        // Check if bookmarked
        boolean bookmarked = bookingBookmarkRepository.existsByBookingAndStudent(booking, booking.getStudent());
        response.setBookmarked(bookmarked);
        
        return response;
    }

    private BookingNoteResponse convertToBookingNoteResponse(BookingNote note) {
        BookingNoteResponse response = new BookingNoteResponse();
        response.setId(note.getId());
        response.setAuthorId(note.getAuthor().getId());
        response.setAuthorName(note.getAuthor().getName());
        response.setContent(note.getContent());
        response.setNoteType(note.getNoteType().toString());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        return response;
    }

    private CalendarEventResponse convertToCalendarEvent(Booking booking) {
        CalendarEventResponse response = new CalendarEventResponse();
        response.setId(booking.getId());
        response.setTitle(booking.getTopic().getTitle());
        response.setStart(booking.getStartTime());
        response.setEnd(booking.getEndTime());
        response.setStatus(booking.getStatus().toString());
        response.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getName() : "TBD");
        response.setTopicTitle(booking.getTopic().getTitle());
        
        // Set color based on status
        switch (booking.getStatus()) {
            case REQUESTED:
                response.setColor("#FACC15"); // Gold
                break;
            case ACCEPTED:
                response.setColor("#10B981"); // Emerald
                break;
            case RESCHEDULED:
                response.setColor("#F59E0B"); // Amber
                break;
            case CANCELLED:
                response.setColor("#EF4444"); // Red
                break;
            case COMPLETED:
                response.setColor("#6B7280"); // Gray
                break;
            default:
                response.setColor("#1E3A8A"); // Royal Blue
        }
        
        // Set action flags
        LocalDateTime now = LocalDateTime.now();
        response.setCanReschedule(booking.getStatus() == Booking.BookingStatus.ACCEPTED && booking.getStartTime().isAfter(now.plusHours(2)));
        response.setCanCancel(booking.getStatus() != Booking.BookingStatus.CANCELLED && booking.getStatus() != Booking.BookingStatus.COMPLETED);
        response.setCanJoin(booking.getStatus() == Booking.BookingStatus.ACCEPTED && 
                           booking.getStartTime().isBefore(now.plusMinutes(5)) && 
                           booking.getStartTime().isAfter(now.minusMinutes(30)));
        
        return response;
    }
}

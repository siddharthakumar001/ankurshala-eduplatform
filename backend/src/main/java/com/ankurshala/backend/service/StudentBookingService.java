package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.dto.admin.PricingRuleDto;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
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
    private final BookingDeclineRepository bookingDeclineRepository;
    private final TeacherAvailabilitySlotRepository teacherAvailabilitySlotRepository;
    private final TeacherSubjectExpertiseRepository teacherSubjectExpertiseRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final FeeWaiverRepository feeWaiverRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    
    @Autowired
    private AdminPricingService pricingService;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final int BUFFER_MINUTES = 15;
    private static final int MIN_SESSION_MINUTES = 30;
    private static final int MAX_SESSION_MINUTES = 120;
    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("500");
    private static final BigDecimal RANGE_LOWER_MULTIPLIER = new BigDecimal("0.90");
    private static final BigDecimal RANGE_UPPER_MULTIPLIER = new BigDecimal("1.15");

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

        ensureNoOutstandingDues(student);
        ensureTeachersAvailable(startTime, endTime, topic);

        List<BookingStatus> conflictingStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.ACCEPTED,
                BookingStatus.CONFIRMED,
                BookingStatus.IN_PROGRESS
        );

        List<Booking> conflictingBookings = bookingRepository.findByStudentAndTimeRange(student, startTime, endTime)
                .stream()
                .filter(booking -> conflictingStatuses.contains(booking.getStatus()))
                .collect(Collectors.toList());

        boolean bufferOk = conflictingBookings.isEmpty();

        PriceQuote priceQuote = calculatePrice(topic, request.getDurationMinutes(), startTime);
        
        return new BookingQuoteResponse() {{
            setExpectedMinutes(topic.getExpectedTimeMins());
            setEndTime(endTime);
            setBufferOk(bufferOk);
            setPrice(new BookingQuoteResponse.PriceInfo("INR", priceQuote.min, priceQuote.max, priceQuote.ruleId));
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

        ensureNoOutstandingDues(student);
        ensureTeachersAvailable(startTime, endTime, topic);
        
        List<BookingStatus> conflictingStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.ACCEPTED,
                BookingStatus.CONFIRMED,
                BookingStatus.IN_PROGRESS
        );

        List<Booking> conflictingBookings = bookingRepository.findByStudentAndTimeRange(student, startTime, endTime)
                .stream()
                .filter(booking -> conflictingStatuses.contains(booking.getStatus()))
                .collect(Collectors.toList());

        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("Time slot conflicts with existing booking");
        }

        PriceQuote priceQuote = calculatePrice(topic, request.getDurationMinutes(), startTime);
        
        // Create booking
        Booking booking = new Booking(student, topic,
                startTime.atZone(ZoneId.of("UTC")),
                endTime.atZone(ZoneId.of("UTC")),
                request.getDurationMinutes(), priceQuote.min, priceQuote.max);
        booking.setPricingRule(priceQuote.ruleEntity);
        booking.setStudentNotes(request.getStudentNotes());
        booking.setAcceptanceToken(UUID.randomUUID().toString());
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Publish Kafka event
        publishBookingRequestedEvent(savedBooking);
        
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
        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted bookings can be rescheduled");
        }
        
        // Convert timezone if provided
        LocalDateTime newStartTime = convertToUTC(request.getNewStartTime(), request.getTimezone());
        LocalDateTime newEndTime = newStartTime.plusMinutes(request.getNewDurationMinutes());
        
        // Check for conflicts
        User student = booking.getStudent();
        List<BookingStatus> conflictingStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.ACCEPTED,
                BookingStatus.CONFIRMED,
                BookingStatus.IN_PROGRESS
        );

        List<Booking> conflictingBookings = bookingRepository.findByStudentAndTimeRange(student, newStartTime, newEndTime)
                .stream()
                .filter(conflict -> conflictingStatuses.contains(conflict.getStatus()))
                .collect(Collectors.toList());
        
        // Remove current booking from conflicts
        conflictingBookings.removeIf(b -> b.getId().equals(bookingId));
        
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("New time slot conflicts with existing booking");
        }
        
        // Update booking and re-open for matching
        ensureTeachersAvailable(newStartTime, newEndTime, booking.getTopic());

        booking.setStartTs(newStartTime.atZone(ZoneId.of("UTC")));
        booking.setEndTs(newEndTime.atZone(ZoneId.of("UTC")));
        booking.setDurationMinutes(request.getNewDurationMinutes());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTeacherId(null);
        booking.setTeacher(null);
        booking.setAcceptedAt(null);

        bookingDeclineRepository.deleteByBookingId(bookingId);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        publishBookingRequestedEvent(savedBooking);

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
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }
        
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel completed booking");
        }
        
        // Update booking
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(ZonedDateTime.now());
        booking.setCancellationReason(request.getReason());
        
        Booking savedBooking = bookingRepository.save(booking);

        publishBookingCancelledEvent(savedBooking);
        
        log.info("Cancelled booking {} for student {}", savedBooking.getId(), booking.getStudent().getId());
        
        return convertToBookingResponse(savedBooking);
    }

    public List<BookingResponse> getUpcomingBookings(UserPrincipal userPrincipal) {
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<Booking> bookings = bookingRepository.findUpcomingByStudent(student, ZonedDateTime.now());
        
        return bookings.stream()
                .map(this::convertToBookingResponse)
                .collect(Collectors.toList());
    }

    public Page<BookingResponse> getBookingHistory(UserPrincipal userPrincipal, Pageable pageable) {
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Page<Booking> bookings = bookingRepository.findHistoryByStudent(student, ZonedDateTime.now(), pageable);
        
        return bookings.map(this::convertToBookingResponse);
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

    public List<AvailableSlotResponse> getNextAvailableSlots(LocalDateTime startTime,
                                                             int durationMinutes,
                                                             String timezone,
                                                             int limit) {
        LocalDateTime normalizedStart = convertToUTC(startTime, timezone);
        int pageSize = Math.max(limit * 3, 10);

        List<TeacherAvailabilitySlot> slots = teacherAvailabilitySlotRepository
                .findNextAvailableSlots(normalizedStart, PageRequest.of(0, pageSize));

        return slots.stream()
                .filter(slot -> Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes() >= durationMinutes)
                .map(slot -> new AvailableSlotResponse(slot.getStartTime(), slot.getEndTime()))
                .limit(limit)
                .collect(Collectors.toList());
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
        
        if ("CANCEL".equalsIgnoreCase(request.getAction())) {
            FeeResult feeResult = calculateCancellationFee(booking);
            fee = feeResult.fee;
            reason = feeResult.reason;
        } else if ("RESCHEDULE".equalsIgnoreCase(request.getAction())) {
            FeeResult feeResult = calculateRescheduleFee(booking);
            fee = feeResult.fee;
            reason = feeResult.reason;
        }
        
        // Check for fee waivers (not implemented yet)
        // if (booking.getFeeWaiver() != null) {
        //     fee = BigDecimal.ZERO;
        //     waived = true;
        //     reason = "Fee waived: " + booking.getFeeWaiver().getReason();
        // }
        
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
        
        String noteType = request.getNoteType();
        
        BookingNote note = new BookingNote();
        note.setBookingId(booking.getId());
        note.setAuthorId(author.getId());
        note.setContent(request.getContent());
        note.setNoteType(noteType);
        note.setAuthorRole(author.getRole());
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
        
        if (!bookingBookmarkRepository.existsByBookingIdAndStudentId(booking.getId(), student.getId())) {
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
        
        bookingBookmarkRepository.deleteByBookingIdAndStudentId(booking.getId(), student.getId());
    }

    public BookingResponse addBookingFeedback(Long bookingId, BookingFeedbackRequest request, UserPrincipal userPrincipal) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Validate ownership
        if (!booking.getStudent().getId().equals(userPrincipal.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        // Validate status
        if (booking.getStatus() != BookingStatus.COMPLETED) {
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
        response.setStudentName(booking.getStudent() != null ? booking.getStudent().getName() : null);
        response.setTeacherId(booking.getTeacher() != null ? booking.getTeacher().getId() : null);
        response.setTopicId(booking.getTopic().getId());
        response.setTopicTitle(booking.getTopic().getTitle());
        response.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getName() : null);
        if (booking.getStartTs() != null) {
            response.setStartTime(booking.getStartTs().toLocalDateTime());
        }
        if (booking.getEndTs() != null) {
            response.setEndTime(booking.getEndTs().toLocalDateTime());
        }
        response.setDurationMinutes(booking.getDurationMinutes());
        response.setStatus(booking.getStatus().toString());
        if (booking.getAcceptedAt() != null) {
            response.setAcceptedAt(booking.getAcceptedAt().toLocalDateTime());
        }
        if (booking.getCancelledAt() != null) {
            response.setCancelledAt(booking.getCancelledAt().toLocalDateTime());
        }
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
        if (booking.getCreatedAt() != null) {
            response.setCreatedAt(booking.getCreatedAt().toLocalDateTime());
        }
        if (booking.getUpdatedAt() != null) {
            response.setUpdatedAt(booking.getUpdatedAt().toLocalDateTime());
        }
        
        // Convert notes (not implemented yet)
        // List<BookingNoteResponse> noteResponses = booking.getNotes().stream()
        //         .map(this::convertToBookingNoteResponse)
        //         .collect(Collectors.toList());
        // response.setNotes(noteResponses);
        
        // Check if bookmarked
        boolean bookmarked = bookingBookmarkRepository.existsByBookingIdAndStudentId(booking.getId(), booking.getStudent().getId());
        response.setBookmarked(bookmarked);
        
        return response;
    }

    private BookingNoteResponse convertToBookingNoteResponse(BookingNote note) {
        BookingNoteResponse response = new BookingNoteResponse();
        response.setId(note.getId());
        // response.setAuthorId(note.getAuthor().getId());
        // response.setAuthorName(note.getAuthor().getName());
        response.setContent(note.getContent());
        response.setNoteType(note.getNoteType());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        return response;
    }

    private CalendarEventResponse convertToCalendarEvent(Booking booking) {
        CalendarEventResponse response = new CalendarEventResponse();
        response.setId(booking.getId());
        response.setTitle(booking.getTopic().getTitle());
        response.setStart(booking.getStartTs().toLocalDateTime());
        response.setEnd(booking.getEndTs().toLocalDateTime());
        response.setStatus(booking.getStatus().toString());
        response.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getName() : "TBD");
        response.setTopicTitle(booking.getTopic().getTitle());
        
        // Set color based on status
        switch (booking.getStatus()) {
            case PENDING:
                response.setColor("#FACC15"); // Gold
                break;
            case ACCEPTED:
                response.setColor("#10B981"); // Emerald
                break;
            case CONFIRMED:
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
        ZonedDateTime now = ZonedDateTime.now();
        response.setCanReschedule(booking.getStatus() == BookingStatus.ACCEPTED && booking.getStartTs().isAfter(now.plusHours(2)));
        response.setCanCancel(booking.getStatus() != BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.COMPLETED);
        response.setCanJoin(booking.getStatus() == BookingStatus.ACCEPTED && 
                           booking.getStartTs().isBefore(now.plusMinutes(5)) && 
                           booking.getStartTs().isAfter(now.minusMinutes(30)));
        
        return response;
    }

    private void ensureTeachersAvailable(LocalDateTime startTime, LocalDateTime endTime, Topic topic) {
        if (topic == null) {
            return;
        }

        List<Long> eligibleTeacherUserIds = teacherSubjectExpertiseRepository.findEligibleTeacherUserIds(
                topic.getSubjectId(), topic.getGradeId(), topic.getBoardId());
        if (eligibleTeacherUserIds.isEmpty()) {
            throw new IllegalArgumentException("No teachers are available for this subject right now.");
        }

        long totalSlots = teacherAvailabilitySlotRepository.count();
        if (totalSlots == 0) {
            return;
        }

        long availableSlots = teacherAvailabilitySlotRepository.countAvailableSlotsForTeachers(
                eligibleTeacherUserIds, startTime, endTime);
        if (availableSlots == 0) {
            throw new IllegalArgumentException("No teachers are available for the selected time. Please try another slot.");
        }
    }

    private void publishBookingCancelledEvent(Booking booking) {
        Map<String, Object> event = new HashMap<>();
        event.put("bookingId", booking.getId());
        event.put("studentId", booking.getStudentId());
        if (booking.getTeacherId() != null) {
            event.put("teacherId", booking.getTeacherId());
        }
        event.put("reason", booking.getCancellationReason());

        kafkaTemplate.send("booking.cancelled", booking.getId().toString(), event);
        log.info("Published booking.cancelled event for booking {}", booking.getId());
    }

    private PriceQuote calculatePrice(Topic topic, int durationMinutes, LocalDateTime startTime) {
        PricingRuleDto pricingRule = pricingService.resolvePricingRule(
                topic.getBoardId(), null, topic.getSubjectId(),
                topic.getChapter().getId(), topic.getId());

        BigDecimal hourlyRate = DEFAULT_HOURLY_RATE;
        Long ruleId = null;
        PricingRule pricingRuleEntity = null;
        if (pricingRule != null && pricingRule.getHourlyRate() != null) {
            hourlyRate = pricingRule.getHourlyRate();
            ruleId = pricingRule.getId();
            pricingRuleEntity = new PricingRule();
            pricingRuleEntity.setId(ruleId);
        }

        BigDecimal sessionHours = BigDecimal.valueOf(durationMinutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal basePrice = hourlyRate.multiply(sessionHours);
        BigDecimal surgeMultiplier = resolveUrgencyMultiplier(startTime);
        BigDecimal adjustedPrice = basePrice.multiply(surgeMultiplier);

        BigDecimal priceMin = adjustedPrice.multiply(RANGE_LOWER_MULTIPLIER).setScale(0, RoundingMode.HALF_UP);
        BigDecimal priceMax = adjustedPrice.multiply(RANGE_UPPER_MULTIPLIER).setScale(0, RoundingMode.HALF_UP);
        if (priceMin.compareTo(BigDecimal.ZERO) < 0) {
            priceMin = BigDecimal.ZERO;
        }
        if (priceMax.compareTo(priceMin) < 0) {
            priceMax = priceMin;
        }

        return new PriceQuote(priceMin, priceMax, ruleId, pricingRuleEntity);
    }

    private BigDecimal resolveUrgencyMultiplier(LocalDateTime startTime) {
        long minutesUntil = Duration.between(LocalDateTime.now(ZoneId.of("UTC")), startTime).toMinutes();
        if (minutesUntil <= 120) {
            return new BigDecimal("1.30");
        }
        if (minutesUntil <= 360) {
            return new BigDecimal("1.20");
        }
        if (minutesUntil <= 1440) {
            return new BigDecimal("1.10");
        }
        return BigDecimal.ONE;
    }

    private static final class PriceQuote {
        private final BigDecimal min;
        private final BigDecimal max;
        private final Long ruleId;
        private final PricingRule ruleEntity;

        private PriceQuote(BigDecimal min, BigDecimal max, Long ruleId, PricingRule ruleEntity) {
            this.min = min;
            this.max = max;
            this.ruleId = ruleId;
            this.ruleEntity = ruleEntity;
        }
    }

    private FeeResult calculateCancellationFee(Booking booking) {
        ZonedDateTime now = ZonedDateTime.now();
        long minutesUntil = Duration.between(now, booking.getStartTs()).toMinutes();
        BigDecimal basePrice = Optional.ofNullable(booking.getPriceMin()).orElse(BigDecimal.ZERO);

        if (minutesUntil <= 120) {
            BigDecimal fee = maxFee(basePrice, new BigDecimal("0.20"), new BigDecimal("50"));
            return new FeeResult(fee, "Late cancellation within 2 hours");
        }
        if (minutesUntil <= 1440) {
            BigDecimal fee = maxFee(basePrice, new BigDecimal("0.10"), new BigDecimal("25"));
            return new FeeResult(fee, "Cancellation within 24 hours");
        }
        return new FeeResult(BigDecimal.ZERO, "No cancellation fee");
    }

    private FeeResult calculateRescheduleFee(Booking booking) {
        ZonedDateTime now = ZonedDateTime.now();
        long minutesUntil = Duration.between(now, booking.getStartTs()).toMinutes();
        BigDecimal basePrice = Optional.ofNullable(booking.getPriceMin()).orElse(BigDecimal.ZERO);

        if (minutesUntil <= 120) {
            BigDecimal fee = maxFee(basePrice, new BigDecimal("0.15"), new BigDecimal("100"));
            return new FeeResult(fee, "Late reschedule within 2 hours");
        }
        if (minutesUntil <= 1440) {
            BigDecimal fee = maxFee(basePrice, new BigDecimal("0.05"), new BigDecimal("50"));
            return new FeeResult(fee, "Reschedule within 24 hours");
        }
        return new FeeResult(BigDecimal.ZERO, "No reschedule fee");
    }

    private BigDecimal maxFee(BigDecimal basePrice, BigDecimal percent, BigDecimal minimum) {
        BigDecimal fee = basePrice.multiply(percent).setScale(0, RoundingMode.HALF_UP);
        return fee.compareTo(minimum) < 0 ? minimum : fee;
    }

    private static final class FeeResult {
        private final BigDecimal fee;
        private final String reason;

        private FeeResult(BigDecimal fee, String reason) {
            this.fee = fee;
            this.reason = reason;
        }
    }

    private void ensureNoOutstandingDues(User student) {
        List<Booking> completedBookings = bookingRepository.findCompletedBookingsForStudent(student.getId());
        for (Booking booking : completedBookings) {
            Integer amountCents = booking.getPriceMinCents();
            if (amountCents != null && amountCents <= 0) {
                continue;
            }
            if (!isBookingPaid(booking, student.getId())) {
                throw new IllegalArgumentException("Please clear your outstanding dues before booking a new class");
            }
        }
    }

    private boolean isBookingPaid(Booking booking, Long studentId) {
        List<PaymentIntent> intents = paymentIntentRepository
                .findByBookingIdOrderByCreatedAtDesc(booking.getId());
        for (PaymentIntent intent : intents) {
            if (intent.getUserId() != null && !intent.getUserId().equals(studentId)) {
                continue;
            }
            if (intent.getStatus() == PaymentIntentStatus.COMPLETED) {
                return true;
            }
        }

        return !walletTransactionRepository
                .findByOwnerTypeAndOwnerIdAndBookingIdAndSourceAndType(
                        WalletOwnerType.STUDENT,
                        studentId,
                        booking.getId(),
                        WalletTransactionSource.BOOKING_PAYMENT,
                        WalletTransactionType.DEBIT
                )
                .isEmpty();
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.booking.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Booking Service
 * Implements Uber-style booking system with real-time teacher matching
 * Handles booking lifecycle, pricing, and availability validation
 */
@Slf4j
@Service
@Transactional
public class EnhancedBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private TeacherAvailabilityRepository teacherAvailabilityRepository;
    @Autowired
    private TeacherWeeklyAvailabilityRepository teacherWeeklyAvailabilityRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.booking.buffer-minutes:15}")
    private int bufferMinutes;

    @Value("${app.features.wallet.enabled:true}")
    private boolean walletEnabled;

    /**
     * Get booking quote with pricing and availability validation
     */
    public BookingQuoteResponse getBookingQuote(BookingQuoteRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting booking quote - TraceId: {}, TopicId: {}, StartTime: {}", 
                traceId, request.getTopicId(), request.getStartTimeISO());

        try {
            // Get topic details
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new BusinessException("Topic not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "TOPIC_NOT_FOUND"));

            // Calculate end time based on expected minutes
            ZonedDateTime endTime = request.getStartTimeISO().plusMinutes(topic.getExpectedMinutes());

            // Validate buffer time (15 minutes between classes)
            boolean bufferOk = validateBufferTime(request.getStartTimeISO(), endTime);

            // Get pricing
            PricingRule pricingRule = getPricingRule(request.getSubjectId(), request.getChapterId(), 
                    request.getTopicId(), request.getTeacherCategory());
            
            BookingQuoteResponse.PriceInfo priceInfo = new BookingQuoteResponse.PriceInfo();
            priceInfo.setCurrency("INR");
            priceInfo.setMin(pricingRule.getHourlyRate().multiply(BigDecimal.valueOf(topic.getExpectedMinutes()).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP)).intValue());
            priceInfo.setMax(priceInfo.getMin() + 150); // Add 150 INR buffer
            priceInfo.setRuleId(pricingRule.getId());

            BookingQuoteResponse response = new BookingQuoteResponse();
            response.setExpectedMinutes(topic.getExpectedMinutes());
            response.setEndTimeISO(endTime);
            response.setBufferOk(bufferOk);
            response.setPrice(priceInfo);

            log.info("Booking quote generated - TraceId: {}, ExpectedMinutes: {}, BufferOk: {}, Price: {}-{}", 
                    traceId, topic.getExpectedMinutes(), bufferOk, priceInfo.getMin(), priceInfo.getMax());

            return response;

        } catch (BusinessException e) {
            log.error("Booking quote failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Booking quote failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to generate booking quote", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "QUOTE_FAILED");
        }
    }

    /**
     * Create booking request (Uber-style matching)
     */
    public BookingResponse createBooking(Long studentId, CreateBookingRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating booking - TraceId: {}, StudentId: {}, TopicId: {}, StartTime: {}", 
                traceId, studentId, request.getTopicId(), request.getStartTimeISO());

        try {
            // Get student profile
            StudentProfile student = studentProfileRepository.findByUserId(studentId)
                    .orElseThrow(() -> new BusinessException("Student profile not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND"));

            // Get topic details
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new BusinessException("Topic not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "TOPIC_NOT_FOUND"));

            // Calculate end time
            ZonedDateTime endTime = request.getStartTimeISO().plusMinutes(topic.getExpectedMinutes());

            // Validate buffer time
            if (!validateBufferTime(request.getStartTimeISO(), endTime)) {
                throw new BusinessException("Booking conflicts with existing classes (15-minute buffer required)", 
                        org.springframework.http.HttpStatus.BAD_REQUEST, "BUFFER_CONFLICT");
            }

            // Get pricing
            PricingRule pricingRule = getPricingRule(request.getSubjectId(), request.getChapterId(), 
                    request.getTopicId(), request.getTeacherCategory());

            // Create booking
            Booking booking = new Booking();
            booking.setStudentId(studentId);
            booking.setBoard(student.getEducationalBoard().name());
            booking.setGrade(student.getClassLevel().name());
            booking.setSubjectId(request.getSubjectId());
            booking.setChapterId(request.getChapterId());
            booking.setTopicId(request.getTopicId());
            booking.setStartTs(request.getStartTimeISO());
            booking.setEndTs(endTime);
            booking.setCategory(request.getTeacherCategory());
            booking.setPriceMinCents(pricingRule.getHourlyRate().multiply(BigDecimal.valueOf(topic.getExpectedMinutes()).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP)).intValue());
            booking.setPriceMaxCents(booking.getPriceMinCents() + 150);
            booking.setAppliedRuleId(pricingRule.getId());
            booking.setStatus(BookingStatus.PENDING);
            booking.setNotes(request.getNotes());

            Booking savedBooking = bookingRepository.save(booking);

            // Broadcast to matching teachers via Kafka
            broadcastBookingRequest(savedBooking);

            // Send notification to student
            notificationService.createNotification(
                studentId,
                com.ankurshala.backend.entity.NotificationType.BOOKING_REQUESTED,
                "Booking Request Sent",
                "Your booking request has been sent to available teachers",
                java.util.Map.of("bookingId", savedBooking.getId(), "topic", topic.getTitle())
            );

            // Convert to response
            BookingResponse response = convertToBookingResponse(savedBooking);

            log.info("Booking created successfully - TraceId: {}, BookingId: {}, State: {}", 
                    traceId, savedBooking.getId(), savedBooking.getState());

            return response;

        } catch (BusinessException e) {
            log.error("Booking creation failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Booking creation failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to create booking", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "BOOKING_FAILED");
        }
    }

    /**
     * Teacher accepts booking (first-accept wins)
     */
    public BookingAcceptResponse acceptBooking(Long teacherId, Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Teacher accepting booking - TraceId: {}, TeacherId: {}, BookingId: {}", 
                traceId, teacherId, bookingId);

        try {
            // Get booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BusinessException("Booking not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

            // Check if booking is still available
            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new BusinessException("Booking is no longer available", 
                        org.springframework.http.HttpStatus.CONFLICT, "BOOKING_UNAVAILABLE");
            }

            // Validate teacher availability
            if (!isTeacherAvailable(teacherId, booking.getStartTs(), booking.getEndTs())) {
                throw new BusinessException("Teacher is not available for this time slot", 
                        org.springframework.http.HttpStatus.BAD_REQUEST, "TEACHER_UNAVAILABLE");
            }

            // Atomic update (first-accept wins)
            int updated = bookingRepository.acceptBooking(bookingId, teacherId);
            if (updated == 0) {
                throw new BusinessException("Booking was already accepted by another teacher", 
                        org.springframework.http.HttpStatus.CONFLICT, "ALREADY_ACCEPTED");
            }

            // Get updated booking
            booking = bookingRepository.findById(bookingId).orElseThrow();

            // Broadcast acceptance via Kafka
            broadcastBookingAccepted(booking);

            // Send notifications
            notificationService.sendBookingNotification(
                booking.getStudentId(),
                booking.getTeacherId(),
                com.ankurshala.backend.entity.NotificationType.BOOKING_ACCEPTED,
                "Booking Accepted!",
                "Your booking has been accepted by a teacher",
                java.util.Map.of("bookingId", booking.getId(), "teacherId", teacherId)
            );

            // Create response
            BookingAcceptResponse response = new BookingAcceptResponse();
            response.setState(booking.getState());
            response.setStartTime(booking.getStartTs());
            response.setEndTime(booking.getEndTs());
            response.setZoomHostLink("https://zoom.us/j/" + bookingId + "?pwd=stub"); // Stub for now

            log.info("Booking accepted successfully - TraceId: {}, BookingId: {}, TeacherId: {}", 
                    traceId, bookingId, teacherId);

            return response;

        } catch (BusinessException e) {
            log.error("Booking acceptance failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Booking acceptance failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to accept booking", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "ACCEPT_FAILED");
        }
    }

    /**
     * Teacher declines booking
     */
    public void declineBooking(Long teacherId, Long bookingId, String reason) {
        String traceId = TraceUtil.getTraceId();
        log.info("Teacher declining booking - TraceId: {}, TeacherId: {}, BookingId: {}", 
                traceId, teacherId, bookingId);

        try {
            // Get booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BusinessException("Booking not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

            // Check if booking is still available
            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new BusinessException("Booking is no longer available", 
                        org.springframework.http.HttpStatus.CONFLICT, "BOOKING_UNAVAILABLE");
            }

            // Broadcast decline via Kafka
            broadcastBookingDeclined(booking, reason);

            log.info("Booking declined - TraceId: {}, BookingId: {}, TeacherId: {}, Reason: {}", 
                    traceId, bookingId, teacherId, reason);

        } catch (BusinessException e) {
            log.error("Booking decline failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Booking decline failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to decline booking", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "DECLINE_FAILED");
        }
    }

    /**
     * Get fee preview for reschedule/cancel
     */
    public FeePreviewResponse getFeePreview(Long bookingId, FeePreviewRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting fee preview - TraceId: {}, BookingId: {}, Action: {}", 
                traceId, bookingId, request.getAction());

        try {
            // Get booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BusinessException("Booking not found", 
                            org.springframework.http.HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

            // Calculate fee based on action and timing
            int fee = calculateFee(booking, request.getAction(), request.getNewStart());

            FeePreviewResponse response = new FeePreviewResponse();
            response.setCurrency("INR");
            response.setFee(fee);
            response.setReason(getFeeReason(booking, request.getAction(), request.getNewStart()));
            response.setWaived(false); // TODO: Check for active waivers

            log.info("Fee preview generated - TraceId: {}, BookingId: {}, Action: {}, Fee: {}", 
                    traceId, bookingId, request.getAction(), fee);

            return response;

        } catch (BusinessException e) {
            log.error("Fee preview failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Fee preview failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to generate fee preview", 
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "FEE_PREVIEW_FAILED");
        }
    }

    // Helper methods

    private boolean validateBufferTime(ZonedDateTime startTime, ZonedDateTime endTime) {
        // Check for conflicts with existing bookings (15-minute buffer)
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                startTime.minusMinutes(bufferMinutes), 
                endTime.plusMinutes(bufferMinutes)
        );
        return conflictingBookings.isEmpty();
    }

    private PricingRule getPricingRule(Long subjectId, Long chapterId, Long topicId, String category) {
        // Try to find most specific pricing rule
        Optional<PricingRule> rule = pricingRuleRepository.findByTopicId(topicId);
        if (rule.isPresent()) return rule.get();

        rule = pricingRuleRepository.findByChapterId(chapterId);
        if (rule.isPresent()) return rule.get();

        rule = pricingRuleRepository.findBySubjectId(subjectId);
        if (rule.isPresent()) return rule.get();

        // Default pricing
        PricingRule defaultRule = new PricingRule();
        defaultRule.setHourlyRate(java.math.BigDecimal.valueOf(500)); // Default INR 500/hour
        return defaultRule;
    }

    private boolean isTeacherAvailable(Long teacherId, ZonedDateTime startTime, ZonedDateTime endTime) {
        int dayOfWeek = startTime.getDayOfWeek().getValue() % 7;
        List<TeacherWeeklyAvailability> weeklyAvailability = teacherWeeklyAvailabilityRepository
                .findByTeacherIdAndIsAvailableTrue(teacherId);

        if (!weeklyAvailability.isEmpty()) {
            for (TeacherWeeklyAvailability slot : weeklyAvailability) {
                if (slot.getDayOfWeek() == dayOfWeek &&
                        isTimeInSlot(startTime.toLocalTime(), endTime.toLocalTime(),
                                slot.getStartTime(), slot.getEndTime())) {
                    return true;
                }
            }
            return false;
        }

        List<TeacherAvailability> legacyAvailability = teacherAvailabilityRepository
                .findByTeacher_IdAndActive(teacherId, true);

        for (TeacherAvailability slot : legacyAvailability) {
            if (slot.getWeekday() == dayOfWeek &&
                    isTimeInSlot(startTime.toLocalTime(), endTime.toLocalTime(),
                            slot.getStartTime(), slot.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTimeInSlot(java.time.LocalTime start, java.time.LocalTime end, 
                                java.time.LocalTime slotStart, java.time.LocalTime slotEnd) {
        return !start.isBefore(slotStart) && !end.isAfter(slotEnd);
    }

    private int calculateFee(Booking booking, String action, ZonedDateTime newStart) {
        ZonedDateTime now = ZonedDateTime.now();
        long hoursUntilStart = ChronoUnit.HOURS.between(now, booking.getStartTs());

        if ("CANCEL".equals(action)) {
            if (hoursUntilStart < 2) return 50; // INR 50 cancellation fee within 2 hours
            if (hoursUntilStart < 24) return 25; // INR 25 cancellation fee within 24 hours
            return 0; // No fee if cancelled more than 24 hours in advance
        } else if ("RESCHEDULE".equals(action)) {
            if (hoursUntilStart < 2) return 100; // INR 100 reschedule fee within 2 hours
            if (hoursUntilStart < 24) return 50; // INR 50 reschedule fee within 24 hours
            return 0; // No fee if rescheduled more than 24 hours in advance
        }

        return 0;
    }

    private String getFeeReason(Booking booking, String action, ZonedDateTime newStart) {
        ZonedDateTime now = ZonedDateTime.now();
        long hoursUntilStart = ChronoUnit.HOURS.between(now, booking.getStartTs());

        if ("CANCEL".equals(action)) {
            if (hoursUntilStart < 2) return "Within 2h window";
            if (hoursUntilStart < 24) return "Within 24h window";
            return "No fee";
        } else if ("RESCHEDULE".equals(action)) {
            if (hoursUntilStart < 2) return "Within 2h window";
            if (hoursUntilStart < 24) return "Within 24h window";
            return "No fee";
        }

        return "No fee";
    }

    private void broadcastBookingRequest(Booking booking) {
        try {
            String message = String.format("{\"type\":\"BOOKING_REQUESTED\",\"bookingId\":%d,\"topicId\":%d,\"startTime\":\"%s\",\"category\":\"%s\"}", 
                    booking.getId(), booking.getTopicId(), booking.getStartTs(), booking.getCategory());
            kafkaTemplate.send("booking-events", message);
        } catch (Exception e) {
            log.error("Failed to broadcast booking request: {}", e.getMessage());
        }
    }

    private void broadcastBookingAccepted(Booking booking) {
        try {
            String message = String.format("{\"type\":\"BOOKING_ACCEPTED\",\"bookingId\":%d,\"studentId\":%d,\"teacherId\":%d}", 
                    booking.getId(), booking.getStudentId(), booking.getTeacherId());
            kafkaTemplate.send("booking-events", message);
        } catch (Exception e) {
            log.error("Failed to broadcast booking acceptance: {}", e.getMessage());
        }
    }

    private void broadcastBookingDeclined(Booking booking, String reason) {
        try {
            String message = String.format("{\"type\":\"BOOKING_DECLINED\",\"bookingId\":%d,\"reason\":\"%s\"}", 
                    booking.getId(), reason);
            kafkaTemplate.send("booking-events", message);
        } catch (Exception e) {
            log.error("Failed to broadcast booking decline: {}", e.getMessage());
        }
    }

    private BookingResponse convertToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setStudentId(booking.getStudentId());
        response.setTeacherId(booking.getTeacherId());
        response.setBoard(booking.getBoard());
        response.setGrade(booking.getGrade());
        response.setSubjectId(booking.getSubjectId());
        response.setChapterId(booking.getChapterId());
        response.setTopicId(booking.getTopicId());
        response.setStartTime(booking.getStartTs());
        response.setEndTime(booking.getEndTs());
        response.setCategory(booking.getCategory());
        response.setPriceMinCents(booking.getPriceMinCents());
        response.setPriceMaxCents(booking.getPriceMaxCents());
        response.setAppliedRuleId(booking.getAppliedRuleId());
        response.setState(booking.getState());
        response.setNotes(booking.getNotes());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        return response;
    }
}

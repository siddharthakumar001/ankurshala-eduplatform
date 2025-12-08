package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.waiver.CreateWaiverRequest;
import com.ankurshala.backend.dto.waiver.FeeWaiverDto;
import com.ankurshala.backend.dto.waiver.RequestWaiverRequest;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class FeeWaiverService {

    @Autowired
    private FeeWaiverRepository feeWaiverRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

    public FeeWaiver requestWaiver(Long studentId, RequestWaiverRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Student requesting fee waiver - TraceId: {}, StudentId: {}, BookingId: {}", 
                traceId, studentId, request.getBookingId());

        // Verify booking exists and belongs to student
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

        if (!booking.getStudentId().equals(studentId)) {
            throw new BusinessException("Access denied to this booking", HttpStatus.FORBIDDEN, "ACCESS_DENIED");
        }

        // Check if booking is eligible for waiver (not completed/cancelled)
        if (!"REQUESTED".equals(booking.getState()) && !"ACCEPTED".equals(booking.getState())) {
            throw new BusinessException("Booking is not eligible for fee waiver", HttpStatus.BAD_REQUEST, "BOOKING_NOT_ELIGIBLE");
        }

        // Check if waiver already exists for this user (mock implementation)
        if (feeWaiverRepository.existsByUser_IdAndStatus(studentId, FeeWaiver.WaiverStatus.APPROVED)) {
            throw new BusinessException("Fee waiver already exists for this user", HttpStatus.CONFLICT, "WAIVER_EXISTS");
        }

        // Create waiver request
        FeeWaiver waiver = new FeeWaiver();
        waiver.setUser(userRepository.findById(studentId).orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND")));
        waiver.setWaiverType(FeeWaiver.WaiverType.PERCENTAGE); // Default to percentage
        waiver.setWaiverPercentage(request.getPercentWaiver());
        waiver.setWaiverAmountCents(request.getFlatWaiverCents() != null ? request.getFlatWaiverCents().longValue() : 0L);
        waiver.setReason(request.getReason());
        waiver.setStatus(FeeWaiver.WaiverStatus.PENDING); // Initially pending, needs admin approval

        FeeWaiver savedWaiver = feeWaiverRepository.save(waiver);
        log.info("Fee waiver request created with ID {} - TraceId: {}", savedWaiver.getId(), traceId);

        // Notify admins (in real implementation, you'd have an admin notification system)
        notificationService.createNotification(
            null, // Admin notification would go to admin users
            NotificationType.SYSTEM_ANNOUNCEMENT,
            "Fee Waiver Request",
            "Student " + studentId + " has requested a fee waiver for booking " + request.getBookingId(),
            Map.of("waiverId", savedWaiver.getId(), "studentId", studentId, "bookingId", request.getBookingId())
        );

        return savedWaiver;
    }

    public FeeWaiver createWaiver(CreateWaiverRequest request) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating fee waiver - TraceId: {}, StudentId: {}, BookingId: {}", 
                traceId, request.getStudentId(), request.getBookingId());

        // Verify student exists
        userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("Student not found", HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND"));

        // If booking-specific waiver, verify booking exists
        if (request.getBookingId() != null) {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new BusinessException("Booking not found", HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND"));

            if (!booking.getStudentId().equals(request.getStudentId())) {
                throw new BusinessException("Booking does not belong to the specified student", HttpStatus.BAD_REQUEST, "BOOKING_MISMATCH");
            }
        }

        FeeWaiver waiver = new FeeWaiver();
        waiver.setUser(userRepository.findById(request.getStudentId()).orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND")));
        waiver.setWaiverType(FeeWaiver.WaiverType.PERCENTAGE); // Default to percentage
        waiver.setWaiverPercentage(request.getPercent());
        waiver.setWaiverAmountCents(request.getFlatCents() != null ? request.getFlatCents().longValue() : 0L);
        waiver.setReason(request.getReason());
        waiver.setStatus(FeeWaiver.WaiverStatus.APPROVED); // Admin created waiver is approved
        waiver.setExpiresAt(request.getExpiresAt());

        FeeWaiver savedWaiver = feeWaiverRepository.save(waiver);
        log.info("Fee waiver created with ID {} - TraceId: {}", savedWaiver.getId(), traceId);

        // Notify student
        notificationService.createNotification(
            request.getStudentId(),
            NotificationType.FEE_WAIVER_APPROVED,
            "Fee Waiver Approved",
            "Your fee waiver request has been approved",
            Map.of("waiverId", savedWaiver.getId(), "bookingId", request.getBookingId())
        );

        return savedWaiver;
    }

    public List<FeeWaiverDto> getStudentWaivers(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting student waivers - TraceId: {}, StudentId: {}", traceId, studentId);

        List<FeeWaiver> waivers = feeWaiverRepository.findByUser_IdOrderByCreatedAtDesc(studentId);
        return waivers.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public FeeWaiverDto getBookingWaiver(Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting booking waiver - TraceId: {}, BookingId: {}", traceId, bookingId);

        // Note: Booking-based waivers not implemented in current entity
        List<FeeWaiver> waivers = feeWaiverRepository.findAll();
        if (waivers.isEmpty()) {
            return null;
        }

        return convertToDto(waivers.get(0));
    }

    public Integer calculateWaivedAmount(Long bookingId, Integer originalAmountCents) {
        String traceId = TraceUtil.getTraceId();
        log.debug("Calculating waived amount - TraceId: {}, BookingId: {}, Original: {}", 
                traceId, bookingId, originalAmountCents);

        // Note: Booking-based waivers not implemented in current entity
        List<FeeWaiver> waivers = feeWaiverRepository.findAll();
        if (waivers.isEmpty()) {
            return 0;
        }

        FeeWaiver waiver = waivers.get(0);
        int waivedAmount = 0;

        // Apply percentage waiver
        if (waiver.getWaiverPercentage() != null) {
            waivedAmount += (int) (originalAmountCents * waiver.getWaiverPercentage().doubleValue() / 100.0);
        }

        // Apply flat waiver
        if (waiver.getWaiverAmountCents() != null) {
            waivedAmount += waiver.getWaiverAmountCents().intValue();
        }

        // Don't exceed original amount
        return Math.min(waivedAmount, originalAmountCents);
    }

    public void deactivateWaiver(Long waiverId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Deactivating fee waiver - TraceId: {}, WaiverId: {}", traceId, waiverId);

        FeeWaiver waiver = feeWaiverRepository.findById(waiverId)
                .orElseThrow(() -> new BusinessException("Fee waiver not found", HttpStatus.NOT_FOUND, "WAIVER_NOT_FOUND"));

        waiver.setStatus(FeeWaiver.WaiverStatus.REJECTED);
        feeWaiverRepository.save(waiver);

        log.info("Fee waiver deactivated - TraceId: {}, WaiverId: {}", traceId, waiverId);
    }

    private FeeWaiverDto convertToDto(FeeWaiver waiver) {
        FeeWaiverDto dto = new FeeWaiverDto();
        dto.setId(waiver.getId());
        dto.setStudentId(waiver.getUser() != null ? waiver.getUser().getId() : null);
        dto.setBookingId(null); // Not available in current entity
        dto.setPercent(waiver.getWaiverPercentage());
        dto.setFlatCents(waiver.getWaiverAmountCents() != null ? waiver.getWaiverAmountCents().intValue() : null);
        dto.setActive(waiver.getStatus() == FeeWaiver.WaiverStatus.APPROVED);
        dto.setReason(waiver.getReason());
        dto.setCreatedAt(waiver.getCreatedAt());
        dto.setExpiresAt(waiver.getExpiresAt());
        return dto;
    }
}

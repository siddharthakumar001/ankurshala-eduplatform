package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final BookingRepository bookingRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @KafkaListener(topics = "booking.requested", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingRequested(Map<String, Object> payload) {
        Long bookingId = extractBookingId(payload);
        if (bookingId == null) {
            log.warn("booking.requested missing bookingId");
            return;
        }

        bookingRepository.findById(bookingId).ifPresentOrElse(
                webSocketNotificationService::broadcastBookingRequest,
                () -> log.warn("booking.requested booking {} not found", bookingId)
        );
    }

    @KafkaListener(topics = "booking.accepted", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingAccepted(Map<String, Object> payload) {
        Long bookingId = extractBookingId(payload);
        if (bookingId == null) {
            log.warn("booking.accepted missing bookingId");
            return;
        }

        bookingRepository.findById(bookingId).ifPresentOrElse(booking -> {
            webSocketNotificationService.notifyBookingAccepted(booking);
            webSocketNotificationService.notifyBookingNoLongerAvailable(booking);
        }, () -> log.warn("booking.accepted booking {} not found", bookingId));
    }

    @KafkaListener(topics = "booking.declined", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingDeclined(Map<String, Object> payload) {
        Long bookingId = extractBookingId(payload);
        if (bookingId == null) {
            log.warn("booking.declined missing bookingId");
            return;
        }

        log.info("Received booking.declined for booking {}", bookingId);
    }

    @KafkaListener(topics = "booking.expired", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingExpired(Map<String, Object> payload) {
        Long bookingId = extractBookingId(payload);
        if (bookingId == null) {
            log.warn("booking.expired missing bookingId");
            return;
        }

        String reason = payload != null ? String.valueOf(payload.getOrDefault("reason", "No teachers were available for this slot.")) : null;

        bookingRepository.findById(bookingId).ifPresentOrElse(
                booking -> webSocketNotificationService.notifyBookingExpired(booking, reason),
                () -> log.warn("booking.expired booking {} not found", bookingId)
        );
    }

    @KafkaListener(topics = "booking.cancelled", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingCancelled(Map<String, Object> payload) {
        Long bookingId = extractBookingId(payload);
        if (bookingId == null) {
            log.warn("booking.cancelled missing bookingId");
            return;
        }

        bookingRepository.findById(bookingId).ifPresentOrElse(
                webSocketNotificationService::notifyBookingCancelled,
                () -> log.warn("booking.cancelled booking {} not found", bookingId)
        );
    }

    private Long extractBookingId(Map<String, Object> payload) {
        Object value = payload != null ? payload.get("bookingId") : null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}

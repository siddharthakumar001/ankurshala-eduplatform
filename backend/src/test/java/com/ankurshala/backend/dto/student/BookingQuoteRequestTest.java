package com.ankurshala.backend.dto.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingQuoteRequest Tests")
class BookingQuoteRequestTest {

    @Test
    @DisplayName("Should create BookingQuoteRequest with no-args constructor")
    void testNoArgsConstructor() {
        BookingQuoteRequest request = new BookingQuoteRequest();
        assertNotNull(request);
        assertNull(request.getTopicId());
        assertNull(request.getStartTime());
        assertNull(request.getDurationMinutes());
        assertNull(request.getTimezone());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        BookingQuoteRequest request = new BookingQuoteRequest();
        Long topicId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        Integer durationMinutes = 60;
        String timezone = "Asia/Kolkata";

        request.setTopicId(topicId);
        request.setStartTime(startTime);
        request.setDurationMinutes(durationMinutes);
        request.setTimezone(timezone);

        assertEquals(topicId, request.getTopicId());
        assertEquals(startTime, request.getStartTime());
        assertEquals(durationMinutes, request.getDurationMinutes());
        assertEquals(timezone, request.getTimezone());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        BookingQuoteRequest request = new BookingQuoteRequest();
        request.setTopicId(null);
        request.setStartTime(null);
        request.setDurationMinutes(null);
        request.setTimezone(null);

        assertNull(request.getTopicId());
        assertNull(request.getStartTime());
        assertNull(request.getDurationMinutes());
        assertNull(request.getTimezone());
    }
}

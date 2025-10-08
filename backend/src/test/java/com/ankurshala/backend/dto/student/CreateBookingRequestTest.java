package com.ankurshala.backend.dto.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateBookingRequest Tests")
class CreateBookingRequestTest {

    @Test
    @DisplayName("Should create CreateBookingRequest with no-args constructor")
    void testNoArgsConstructor() {
        CreateBookingRequest request = new CreateBookingRequest();
        assertNotNull(request);
        assertNull(request.getTopicId());
        assertNull(request.getStartTime());
        assertNull(request.getDurationMinutes());
        assertNull(request.getStudentNotes());
        assertNull(request.getTimezone());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        CreateBookingRequest request = new CreateBookingRequest();
        Long topicId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        Integer durationMinutes = 60;
        String studentNotes = "Need help with algebra";
        String timezone = "Asia/Kolkata";

        request.setTopicId(topicId);
        request.setStartTime(startTime);
        request.setDurationMinutes(durationMinutes);
        request.setStudentNotes(studentNotes);
        request.setTimezone(timezone);

        assertEquals(topicId, request.getTopicId());
        assertEquals(startTime, request.getStartTime());
        assertEquals(durationMinutes, request.getDurationMinutes());
        assertEquals(studentNotes, request.getStudentNotes());
        assertEquals(timezone, request.getTimezone());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setTopicId(null);
        request.setStartTime(null);
        request.setDurationMinutes(null);
        request.setStudentNotes(null);
        request.setTimezone(null);

        assertNull(request.getTopicId());
        assertNull(request.getStartTime());
        assertNull(request.getDurationMinutes());
        assertNull(request.getStudentNotes());
        assertNull(request.getTimezone());
    }
}

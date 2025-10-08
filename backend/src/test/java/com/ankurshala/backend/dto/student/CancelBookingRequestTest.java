package com.ankurshala.backend.dto.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancelBookingRequest Tests")
class CancelBookingRequestTest {

    @Test
    @DisplayName("Should create CancelBookingRequest with no-args constructor")
    void testNoArgsConstructor() {
        CancelBookingRequest request = new CancelBookingRequest();
        assertNotNull(request);
        assertNull(request.getReason());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void testSettersAndGetters() {
        CancelBookingRequest request = new CancelBookingRequest();
        String reason = "Schedule conflict";

        request.setReason(reason);

        assertEquals(reason, request.getReason());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        CancelBookingRequest request = new CancelBookingRequest();
        request.setReason(null);

        assertNull(request.getReason());
    }
}

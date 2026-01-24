package com.ankurshala.backend.test;

import com.ankurshala.backend.validation.IndianMobileNumberValidator;
import com.ankurshala.backend.validation.IndianPincodeValidator;
import com.ankurshala.backend.validation.FutureDateTime;
import com.ankurshala.backend.validation.FutureDateTimeValidator;
import com.ankurshala.backend.validation.ValidBookingDuration;
import com.ankurshala.backend.validation.ValidBookingDurationValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationTest {

    @Mock
    private ConstraintValidatorContext context;

    private IndianMobileNumberValidator mobileValidator;
    private IndianPincodeValidator pincodeValidator;
    private FutureDateTimeValidator futureDateTimeValidator;
    private ValidBookingDurationValidator durationValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mobileValidator = new IndianMobileNumberValidator();
        pincodeValidator = new IndianPincodeValidator();
        futureDateTimeValidator = new FutureDateTimeValidator();
        durationValidator = new ValidBookingDurationValidator();

        FutureDateTime defaultFutureDateTime = mock(FutureDateTime.class);
        when(defaultFutureDateTime.bufferMinutes()).thenReturn(0);
        futureDateTimeValidator.initialize(defaultFutureDateTime);

        ValidBookingDuration defaultDuration = mock(ValidBookingDuration.class);
        when(defaultDuration.minMinutes()).thenReturn(30);
        when(defaultDuration.maxMinutes()).thenReturn(180);
        durationValidator.initialize(defaultDuration);
    }

    @Test
    void testIndianMobileNumberValid() {
        // Valid mobile numbers
        assertTrue(mobileValidator.isValid("9876543210", context));
        assertTrue(mobileValidator.isValid("9123456789", context));
        assertTrue(mobileValidator.isValid("8765432109", context));
        assertTrue(mobileValidator.isValid("6123456789", context));
        
        // Valid with spaces and special characters
        assertTrue(mobileValidator.isValid("9876 543 210", context));
        assertTrue(mobileValidator.isValid("9876-543-210", context));
        assertTrue(mobileValidator.isValid("(9876) 543 210", context));
    }

    @Test
    void testIndianMobileNumberInvalid() {
        // Invalid mobile numbers
        assertFalse(mobileValidator.isValid("1234567890", context)); // Starts with 1
        assertFalse(mobileValidator.isValid("5123456789", context)); // Starts with 5
        assertFalse(mobileValidator.isValid("987654321", context)); // 9 digits
        assertFalse(mobileValidator.isValid("98765432100", context)); // 11 digits
        assertFalse(mobileValidator.isValid("", context)); // Empty
        assertFalse(mobileValidator.isValid(null, context)); // Null
        assertFalse(mobileValidator.isValid("abc1234567", context)); // Contains letters
    }

    @Test
    void testIndianPincodeValid() {
        // Valid pincodes
        assertTrue(pincodeValidator.isValid("110001", context));
        assertTrue(pincodeValidator.isValid("400001", context));
        assertTrue(pincodeValidator.isValid("560001", context));
        assertTrue(pincodeValidator.isValid("700001", context));
        assertTrue(pincodeValidator.isValid("800001", context));
        assertTrue(pincodeValidator.isValid("900001", context));
    }

    @Test
    void testIndianPincodeInvalid() {
        // Invalid pincodes
        assertFalse(pincodeValidator.isValid("010001", context)); // Starts with 0
        assertFalse(pincodeValidator.isValid("11001", context)); // 5 digits
        assertFalse(pincodeValidator.isValid("1100010", context)); // 7 digits
        assertFalse(pincodeValidator.isValid("", context)); // Empty
        assertFalse(pincodeValidator.isValid(null, context)); // Null
        assertFalse(pincodeValidator.isValid("abc123", context)); // Contains letters
    }

    @Test
    void testFutureDateTimeValid() {
        // Valid future dates
        LocalDateTime futureTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusHours(1);
        assertTrue(futureDateTimeValidator.isValid(futureTime, context));
        
        LocalDateTime futureTimeWithBuffer = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(30);
        assertTrue(futureDateTimeValidator.isValid(futureTimeWithBuffer, context));
    }

    @Test
    void testFutureDateTimeInvalid() {
        // Invalid past dates
        LocalDateTime pastTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).minusHours(1);
        assertFalse(futureDateTimeValidator.isValid(pastTime, context));
        
        LocalDateTime currentTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        assertFalse(futureDateTimeValidator.isValid(currentTime, context));
        
        assertFalse(futureDateTimeValidator.isValid(null, context));
    }

    @Test
    void testValidBookingDurationValid() {
        // Valid durations
        assertTrue(durationValidator.isValid(30, context)); // Minimum
        assertTrue(durationValidator.isValid(60, context)); // Middle
        assertTrue(durationValidator.isValid(180, context)); // Maximum
    }

    @Test
    void testValidBookingDurationInvalid() {
        // Invalid durations
        assertFalse(durationValidator.isValid(29, context)); // Below minimum
        assertFalse(durationValidator.isValid(181, context)); // Above maximum
        assertFalse(durationValidator.isValid(0, context)); // Zero
        assertFalse(durationValidator.isValid(-10, context)); // Negative
        assertFalse(durationValidator.isValid(null, context)); // Null
    }

    @Test
    void testFutureDateTimeWithBuffer() {
        // Test with custom buffer
        FutureDateTime futureDateTime = mock(FutureDateTime.class);
        when(futureDateTime.bufferMinutes()).thenReturn(15);
        futureDateTimeValidator.initialize(futureDateTime);
        
        LocalDateTime futureTimeWithSmallBuffer = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(10);
        assertFalse(futureDateTimeValidator.isValid(futureTimeWithSmallBuffer, context));
        
        LocalDateTime futureTimeWithLargeBuffer = LocalDateTime.now(ZoneId.of("Asia/Kolkata")).plusMinutes(20);
        assertTrue(futureDateTimeValidator.isValid(futureTimeWithLargeBuffer, context));
    }

    @Test
    void testValidBookingDurationWithCustomLimits() {
        // Test with custom limits
        ValidBookingDuration bookingDuration = mock(ValidBookingDuration.class);
        when(bookingDuration.minMinutes()).thenReturn(45);
        when(bookingDuration.maxMinutes()).thenReturn(120);
        durationValidator.initialize(bookingDuration);
        
        assertFalse(durationValidator.isValid(30, context)); // Below custom minimum
        assertTrue(durationValidator.isValid(60, context)); // Within custom range
        assertFalse(durationValidator.isValid(150, context)); // Above custom maximum
    }
}

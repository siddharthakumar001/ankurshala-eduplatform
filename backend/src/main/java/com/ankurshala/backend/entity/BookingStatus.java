package com.ankurshala.backend.entity;

/**
 * Booking status enum for tracking the lifecycle of a booking
 */
public enum BookingStatus {
    PENDING,        // Booking created, waiting for teacher acceptance
    ACCEPTED,        // Teacher has accepted the booking
    CONFIRMED,       // Booking confirmed and payment processed
    IN_PROGRESS,     // Class is currently in progress
    COMPLETED,       // Class has been completed successfully
    CANCELLED,       // Booking was cancelled
    EXPIRED,         // Booking expired without acceptance
    REFUNDED,        // Booking was refunded
    NO_SHOW_STUDENT, // Student didn't show up
    NO_SHOW_TEACHER  // Teacher didn't show up
}

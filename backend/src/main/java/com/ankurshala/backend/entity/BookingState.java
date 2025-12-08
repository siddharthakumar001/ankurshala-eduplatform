package com.ankurshala.backend.entity;

/**
 * Booking state enum for internal state management
 */
public enum BookingState {
    CREATED,        // Initial state when booking is created
    PENDING_ACCEPTANCE, // Waiting for teacher to accept
    REQUESTED,      // Booking requested by student
    ACCEPTED,       // Teacher accepted, waiting for confirmation
    CONFIRMED,      // Booking confirmed and ready
    ACTIVE,         // Class is active/in progress
    FINISHED,       // Class completed
    COMPLETED,      // Class completed successfully
    CANCELLED,      // Booking cancelled
    EXPIRED         // Booking expired
}

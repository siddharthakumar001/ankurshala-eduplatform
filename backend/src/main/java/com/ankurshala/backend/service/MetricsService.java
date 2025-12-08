package com.ankurshala.backend.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for tracking business-level metrics.
 * Provides comprehensive metrics for bookings, payments, sessions, and user activities.
 */
@Slf4j
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Booking metrics
    private final Counter bookingCreatedCounter;
    private final Counter bookingConfirmedCounter;
    private final Counter bookingCancelledCounter;
    private final Counter bookingCompletedCounter;
    private final Timer bookingCreationTimer;

    // Payment metrics
    private final Counter paymentInitiatedCounter;
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;
    private final Timer paymentProcessingTimer;

    // Session metrics
    private final Counter sessionStartedCounter;
    private final Counter sessionCompletedCounter;
    private final Counter sessionCancelledCounter;
    private final Timer sessionDurationTimer;

    // User activity metrics
    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    private final Counter userLoginFailureCounter;
    private final Timer teacherSearchTimer;

    // Content metrics
    private final Counter contentUploadCounter;
    private final Counter contentViewCounter;
    private final Timer contentUploadTimer;

    // Error metrics
    private final Counter apiErrorCounter;
    private final Counter databaseErrorCounter;
    private final Counter externalServiceErrorCounter;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize booking metrics
        this.bookingCreatedCounter = Counter.builder("booking.created")
                .description("Number of bookings created")
                .tag("type", "booking")
                .register(meterRegistry);

        this.bookingConfirmedCounter = Counter.builder("booking.confirmed")
                .description("Number of bookings confirmed")
                .tag("type", "booking")
                .register(meterRegistry);

        this.bookingCancelledCounter = Counter.builder("booking.cancelled")
                .description("Number of bookings cancelled")
                .tag("type", "booking")
                .register(meterRegistry);

        this.bookingCompletedCounter = Counter.builder("booking.completed")
                .description("Number of bookings completed")
                .tag("type", "booking")
                .register(meterRegistry);

        this.bookingCreationTimer = Timer.builder("booking.creation.time")
                .description("Time taken to create a booking")
                .register(meterRegistry);

        // Initialize payment metrics
        this.paymentInitiatedCounter = Counter.builder("payment.initiated")
                .description("Number of payments initiated")
                .tag("type", "payment")
                .register(meterRegistry);

        this.paymentSuccessCounter = Counter.builder("payment.success")
                .description("Number of successful payments")
                .tag("type", "payment")
                .register(meterRegistry);

        this.paymentFailureCounter = Counter.builder("payment.failure")
                .description("Number of failed payments")
                .tag("type", "payment")
                .register(meterRegistry);

        this.paymentProcessingTimer = Timer.builder("payment.processing.time")
                .description("Time taken to process a payment")
                .register(meterRegistry);

        // Initialize session metrics
        this.sessionStartedCounter = Counter.builder("session.started")
                .description("Number of sessions started")
                .tag("type", "session")
                .register(meterRegistry);

        this.sessionCompletedCounter = Counter.builder("session.completed")
                .description("Number of sessions completed")
                .tag("type", "session")
                .register(meterRegistry);

        this.sessionCancelledCounter = Counter.builder("session.cancelled")
                .description("Number of sessions cancelled")
                .tag("type", "session")
                .register(meterRegistry);

        this.sessionDurationTimer = Timer.builder("session.duration")
                .description("Duration of completed sessions")
                .register(meterRegistry);

        // Initialize user activity metrics
        this.userRegistrationCounter = Counter.builder("user.registration")
                .description("Number of user registrations")
                .tag("type", "user")
                .register(meterRegistry);

        this.userLoginCounter = Counter.builder("user.login")
                .description("Number of successful logins")
                .tag("type", "user")
                .register(meterRegistry);

        this.userLoginFailureCounter = Counter.builder("user.login.failure")
                .description("Number of failed login attempts")
                .tag("type", "user")
                .register(meterRegistry);

        this.teacherSearchTimer = Timer.builder("teacher.search.time")
                .description("Time taken to search for teachers")
                .register(meterRegistry);

        // Initialize content metrics
        this.contentUploadCounter = Counter.builder("content.upload")
                .description("Number of content uploads")
                .tag("type", "content")
                .register(meterRegistry);

        this.contentViewCounter = Counter.builder("content.view")
                .description("Number of content views")
                .tag("type", "content")
                .register(meterRegistry);

        this.contentUploadTimer = Timer.builder("content.upload.time")
                .description("Time taken to upload content")
                .register(meterRegistry);

        // Initialize error metrics
        this.apiErrorCounter = Counter.builder("error.api")
                .description("Number of API errors")
                .tag("type", "error")
                .register(meterRegistry);

        this.databaseErrorCounter = Counter.builder("error.database")
                .description("Number of database errors")
                .tag("type", "error")
                .register(meterRegistry);

        this.externalServiceErrorCounter = Counter.builder("error.external")
                .description("Number of external service errors")
                .tag("type", "error")
                .register(meterRegistry);
    }

    // Booking metrics methods
    public void recordBookingCreated() {
        bookingCreatedCounter.increment();
        log.debug("Booking created metric recorded");
    }

    public void recordBookingConfirmed() {
        bookingConfirmedCounter.increment();
        log.debug("Booking confirmed metric recorded");
    }

    public void recordBookingCancelled(String reason) {
        Counter.builder("booking.cancelled")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        bookingCancelledCounter.increment();
        log.debug("Booking cancelled metric recorded. Reason: {}", reason);
    }

    public void recordBookingCompleted() {
        bookingCompletedCounter.increment();
        log.debug("Booking completed metric recorded");
    }

    public void recordBookingCreationTime(long milliseconds) {
        bookingCreationTimer.record(milliseconds, TimeUnit.MILLISECONDS);
        log.debug("Booking creation time recorded: {}ms", milliseconds);
    }

    // Payment metrics methods
    public void recordPaymentInitiated(String amount, String currency) {
        Counter.builder("payment.initiated")
                .tag("currency", currency)
                .register(meterRegistry)
                .increment();
        paymentInitiatedCounter.increment();
        log.debug("Payment initiated metric recorded. Amount: {} {}", amount, currency);
    }

    public void recordPaymentSuccess(String amount, String currency, String paymentMethod) {
        Counter.builder("payment.success")
                .tag("currency", currency)
                .tag("method", paymentMethod)
                .register(meterRegistry)
                .increment();
        paymentSuccessCounter.increment();
        
        // Track revenue
        meterRegistry.gauge("revenue.total", Double.parseDouble(amount));
        
        log.debug("Payment success metric recorded. Amount: {} {}, Method: {}", amount, currency, paymentMethod);
    }

    public void recordPaymentFailure(String reason) {
        Counter.builder("payment.failure")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        paymentFailureCounter.increment();
        log.debug("Payment failure metric recorded. Reason: {}", reason);
    }

    public void recordPaymentProcessingTime(long milliseconds) {
        paymentProcessingTimer.record(milliseconds, TimeUnit.MILLISECONDS);
        log.debug("Payment processing time recorded: {}ms", milliseconds);
    }

    // Session metrics methods
    public void recordSessionStarted(String subjectName) {
        Counter.builder("session.started")
                .tag("subject", subjectName)
                .register(meterRegistry)
                .increment();
        sessionStartedCounter.increment();
        log.debug("Session started metric recorded. Subject: {}", subjectName);
    }

    public void recordSessionCompleted(String subjectName) {
        Counter.builder("session.completed")
                .tag("subject", subjectName)
                .register(meterRegistry)
                .increment();
        sessionCompletedCounter.increment();
        log.debug("Session completed metric recorded. Subject: {}", subjectName);
    }

    public void recordSessionCancelled(String reason) {
        Counter.builder("session.cancelled")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        sessionCancelledCounter.increment();
        log.debug("Session cancelled metric recorded. Reason: {}", reason);
    }

    public void recordSessionDuration(long minutes) {
        sessionDurationTimer.record(minutes, TimeUnit.MINUTES);
        log.debug("Session duration recorded: {} minutes", minutes);
    }

    // User activity metrics methods
    public void recordUserRegistration(String role) {
        Counter.builder("user.registration")
                .tag("role", role)
                .register(meterRegistry)
                .increment();
        userRegistrationCounter.increment();
        log.debug("User registration metric recorded. Role: {}", role);
    }

    public void recordUserLogin(String role) {
        Counter.builder("user.login")
                .tag("role", role)
                .register(meterRegistry)
                .increment();
        userLoginCounter.increment();
        log.debug("User login metric recorded. Role: {}", role);
    }

    public void recordUserLoginFailure(String reason) {
        Counter.builder("user.login.failure")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        userLoginFailureCounter.increment();
        log.debug("User login failure metric recorded. Reason: {}", reason);
    }

    public void recordTeacherSearchTime(long milliseconds, int resultCount) {
        teacherSearchTimer.record(milliseconds, TimeUnit.MILLISECONDS);
        meterRegistry.gauge("teacher.search.results", resultCount);
        log.debug("Teacher search time recorded: {}ms, Results: {}", milliseconds, resultCount);
    }

    // Content metrics methods
    public void recordContentUpload(String contentType) {
        Counter.builder("content.upload")
                .tag("content_type", contentType)
                .register(meterRegistry)
                .increment();
        contentUploadCounter.increment();
        log.debug("Content upload metric recorded. Type: {}", contentType);
    }

    public void recordContentView(String contentType) {
        Counter.builder("content.view")
                .tag("content_type", contentType)
                .register(meterRegistry)
                .increment();
        contentViewCounter.increment();
        log.debug("Content view metric recorded. Type: {}", contentType);
    }

    public void recordContentUploadTime(long milliseconds, long sizeInBytes) {
        contentUploadTimer.record(milliseconds, TimeUnit.MILLISECONDS);
        meterRegistry.gauge("content.upload.size", sizeInBytes);
        log.debug("Content upload time recorded: {}ms, Size: {} bytes", milliseconds, sizeInBytes);
    }

    // Error metrics methods
    public void recordApiError(String endpoint, String errorType) {
        Counter.builder("error.api")
                .tag("endpoint", endpoint)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
        apiErrorCounter.increment();
        log.warn("API error metric recorded. Endpoint: {}, Type: {}", endpoint, errorType);
    }

    public void recordDatabaseError(String operation, String errorType) {
        Counter.builder("error.database")
                .tag("operation", operation)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
        databaseErrorCounter.increment();
        log.error("Database error metric recorded. Operation: {}, Type: {}", operation, errorType);
    }

    public void recordExternalServiceError(String service, String errorType) {
        Counter.builder("error.external")
                .tag("service", service)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
        externalServiceErrorCounter.increment();
        log.error("External service error metric recorded. Service: {}, Type: {}", service, errorType);
    }

    // Custom gauge for active sessions
    public void setActiveSessions(int count) {
        meterRegistry.gauge("session.active", count);
        log.debug("Active sessions gauge updated: {}", count);
    }

    // Custom gauge for active users
    public void setActiveUsers(int count) {
        meterRegistry.gauge("user.active", count);
        log.debug("Active users gauge updated: {}", count);
    }

    // Rate limit metrics methods
    public void recordRateLimitExceeded(String endpoint, String identifier) {
        Counter.builder("rate.limit.exceeded")
                .tag("endpoint", endpoint)
                .tag("identifier_type", identifier.startsWith("user:") ? "user" : "ip")
                .register(meterRegistry)
                .increment();
        log.debug("Rate limit exceeded metric recorded. Endpoint: {}, Identifier: {}", endpoint, identifier);
    }

    public void recordRateLimitBlock(String identifier) {
        Counter.builder("rate.limit.block")
                .tag("identifier_type", identifier.startsWith("user:") ? "user" : "ip")
                .register(meterRegistry)
                .increment();
        log.warn("Rate limit block metric recorded. Identifier: {}", identifier);
    }
}

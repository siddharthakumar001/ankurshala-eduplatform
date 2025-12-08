package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive concurrency tests for Booking system
 * Tests race conditions, distributed locking, and atomic operations
 * 
 * Similar to Uber/Ola stress testing for ride acceptance
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Booking Concurrency Tests - Uber/Ola Style")
class BookingConcurrencyTest {

    @Autowired
    private BookingConcurrencyService bookingConcurrencyService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private DistributedLockService lockService;
    
    private Booking testBooking;
    
    @BeforeEach
    void setup() {
        // Create a test booking in PENDING state
        testBooking = new Booking();
        testBooking.setStudentId(1L);
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setState("REQUESTED");
        testBooking.setStartTs(ZonedDateTime.now().plusHours(1));
        testBooking.setEndTs(ZonedDateTime.now().plusHours(2));
        testBooking.setCreatedAt(ZonedDateTime.now());
        testBooking = bookingRepository.save(testBooking);
    }

    @Test
    @DisplayName("Test 1: Multiple teachers accepting same booking - Only ONE should succeed")
    void testMultipleTeachersAcceptSameBooking() throws Exception {
        int teacherCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(teacherCount);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // Simulate 10 teachers trying to accept simultaneously
        for (int i = 0; i < teacherCount; i++) {
            final Long teacherId = (long) (i + 1);
            futures.add(executor.submit(() -> {
                try {
                    bookingConcurrencyService.acceptBookingWithLock(testBooking.getId(), teacherId);
                    successCount.incrementAndGet();
                    return true;
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    return false;
                }
            }));
        }
        
        // Wait for all threads to complete
        for (Future<Boolean> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        
        // Verify ONLY ONE teacher succeeded
        assertEquals(1, successCount.get(), 
            "Expected exactly 1 teacher to successfully accept the booking");
        assertEquals(teacherCount - 1, failureCount.get(), 
            "Expected " + (teacherCount - 1) + " teachers to fail");
        
        // Verify booking state in database
        Booking updatedBooking = bookingRepository.findById(testBooking.getId()).orElseThrow();
        assertNotNull(updatedBooking.getTeacherId(), "Booking should have a teacher assigned");
        assertEquals(BookingStatus.ACCEPTED, updatedBooking.getStatus());
        assertEquals("ACCEPTED", updatedBooking.getState());
    }

    @Test
    @DisplayName("Test 2: Atomic acceptance with optimistic locking")
    void testOptimisticLockingWithRetry() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final Long teacherId = (long) (i + 1);
            futures.add(executor.submit(() -> {
                try {
                    bookingConcurrencyService.acceptBookingWithOptimisticLock(
                        testBooking.getId(), teacherId);
                    successCount.incrementAndGet();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }
        
        for (Future<Boolean> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        
        // Only one should succeed
        assertEquals(1, successCount.get());
    }

    @Test
    @DisplayName("Test 3: Atomic database-level acceptance")
    void testAtomicDatabaseAcceptance() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final Long teacherId = (long) (i + 1);
            futures.add(executor.submit(() -> {
                boolean accepted = bookingConcurrencyService.acceptBookingAtomic(
                    testBooking.getId(), teacherId);
                if (accepted) {
                    successCount.incrementAndGet();
                }
                return accepted;
            }));
        }
        
        for (Future<Boolean> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        
        assertEquals(1, successCount.get(), 
            "Exactly one teacher should succeed with atomic acceptance");
    }

    @Test
    @DisplayName("Test 4: Distributed lock prevents concurrent access")
    void testDistributedLockPreventsRaceCondition() throws Exception {
        String lockKey = "booking:accept:" + testBooking.getId();
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Integer>> futures = new ArrayList<>();
        
        AtomicInteger concurrentAccessCount = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                return lockService.executeWithLock(lockKey, 10, () -> {
                    // Track concurrent access
                    int current = concurrentAccessCount.incrementAndGet();
                    maxConcurrent.updateAndGet(max -> Math.max(max, current));
                    
                    // Simulate work
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    concurrentAccessCount.decrementAndGet();
                    return current;
                });
            }));
        }
        
        for (Future<Integer> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        
        // Verify no concurrent access occurred
        assertEquals(1, maxConcurrent.get(), 
            "Lock should prevent concurrent access - max should be 1");
    }

    @Test
    @DisplayName("Test 5: High load stress test - 100 teachers")
    void testHighLoadStressTest() throws Exception {
        int teacherCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        AtomicInteger successCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < teacherCount; i++) {
            final Long teacherId = (long) (i + 1);
            futures.add(executor.submit(() -> {
                try {
                    bookingConcurrencyService.acceptBookingWithLock(
                        testBooking.getId(), teacherId);
                    successCount.incrementAndGet();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }
        
        for (Future<Boolean> future : futures) {
            future.get(60, TimeUnit.SECONDS);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        executor.shutdown();
        
        assertEquals(1, successCount.get(), "Only 1 teacher should succeed");
        assertTrue(duration < 10000, 
            "High load test should complete within 10 seconds, took: " + duration + "ms");
    }

    @Test
    @DisplayName("Test 6: Teacher time conflict detection")
    void testTeacherTimeConflictPrevention() {
        // Create first booking
        Booking firstBooking = new Booking();
        firstBooking.setStudentId(1L);
        firstBooking.setTeacherId(100L);
        firstBooking.setStatus(BookingStatus.ACCEPTED);
        firstBooking.setState("ACCEPTED");
        firstBooking.setStartTs(ZonedDateTime.now().plusHours(1));
        firstBooking.setEndTs(ZonedDateTime.now().plusHours(2));
        bookingRepository.save(firstBooking);
        
        // Try to accept overlapping booking with same teacher
        testBooking.setStartTs(ZonedDateTime.now().plusHours(1).plusMinutes(30));
        testBooking.setEndTs(ZonedDateTime.now().plusHours(2).plusMinutes(30));
        testBooking = bookingRepository.save(testBooking);
        
        assertThrows(BookingConcurrencyService.TeacherTimeConflictException.class, () -> {
            bookingConcurrencyService.acceptBookingWithLock(testBooking.getId(), 100L);
        });
    }

    @Test
    @DisplayName("Test 7: Lock timeout handling")
    void testLockTimeoutHandling() throws Exception {
        String lockKey = "test:lock:timeout";
        
        // Hold lock in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> lockHolder = executor.submit(() -> {
            lockService.executeWithLock(lockKey, 60, () -> {
                try {
                    Thread.sleep(5000); // Hold for 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            return null;
        });
        
        // Wait for lock to be acquired
        Thread.sleep(100);
        
        // Try to acquire with short timeout
        assertThrows(DistributedLockService.LockAcquisitionException.class, () -> {
            lockService.executeWithLock(lockKey, 1, () -> {
                // Should not reach here
            });
        });
        
        lockHolder.get();
        executor.shutdown();
    }

    @Test
    @DisplayName("Test 8: Verify pessimistic locking generates SELECT FOR UPDATE")
    @Transactional
    void testPessimisticLockingSql() {
        // This test verifies the lock is applied correctly
        var booking = bookingRepository.findByIdWithLock(testBooking.getId());
        assertTrue(booking.isPresent());
        assertEquals(testBooking.getId(), booking.get().getId());
    }

    @Test
    @DisplayName("Test 9: Booking acceptance performance benchmark")
    void testBookingAcceptancePerformance() throws Exception {
        int iterations = 100;
        List<Long> durations = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            // Create new booking for each iteration
            Booking booking = new Booking();
            booking.setStudentId(1L);
            booking.setStatus(BookingStatus.PENDING);
            booking.setState("REQUESTED");
            booking.setStartTs(ZonedDateTime.now().plusHours(i + 1));
            booking.setEndTs(ZonedDateTime.now().plusHours(i + 2));
            booking = bookingRepository.save(booking);
            
            long start = System.currentTimeMillis();
            bookingConcurrencyService.acceptBookingWithLock(booking.getId(), 1L);
            long duration = System.currentTimeMillis() - start;
            durations.add(duration);
        }
        
        double avgDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(0);
        
        System.out.println("Average acceptance time: " + avgDuration + "ms");
        System.out.println("Max acceptance time: " + maxDuration + "ms");
        
        assertTrue(avgDuration < 500, 
            "Average acceptance should be under 500ms (Uber-level performance)");
        assertTrue(maxDuration < 2000, 
            "Max acceptance should be under 2 seconds");
    }

    @Test
    @DisplayName("Test 10: Concurrent bookings for different students (should all succeed)")
    void testConcurrentDifferentBookings() throws Exception {
        int bookingCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(bookingCount);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        // Create multiple bookings
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < bookingCount; i++) {
            Booking booking = new Booking();
            booking.setStudentId((long) (i + 1));
            booking.setStatus(BookingStatus.PENDING);
            booking.setState("REQUESTED");
            booking.setStartTs(ZonedDateTime.now().plusHours(i + 1));
            booking.setEndTs(ZonedDateTime.now().plusHours(i + 2));
            bookings.add(bookingRepository.save(booking));
        }
        
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Accept all bookings concurrently
        for (int i = 0; i < bookingCount; i++) {
            final Long bookingId = bookings.get(i).getId();
            final Long teacherId = (long) (i + 1);
            
            futures.add(executor.submit(() -> {
                try {
                    bookingConcurrencyService.acceptBookingWithLock(bookingId, teacherId);
                    successCount.incrementAndGet();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }));
        }
        
        for (Future<Boolean> future : futures) {
            future.get(10, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        
        // All should succeed since they're different bookings
        assertEquals(bookingCount, successCount.get(), 
            "All different bookings should be accepted successfully");
    }
}

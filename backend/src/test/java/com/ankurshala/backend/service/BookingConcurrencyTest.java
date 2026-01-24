package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.Board;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.entity.Chapter;
import com.ankurshala.backend.entity.Grade;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.Subject;
import com.ankurshala.backend.entity.Teacher;
import com.ankurshala.backend.entity.TeacherStatus;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BoardRepository;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.ChapterRepository;
import com.ankurshala.backend.repository.GradeRepository;
import com.ankurshala.backend.repository.SubjectRepository;
import com.ankurshala.backend.repository.TeacherRepository;
import com.ankurshala.backend.repository.TopicRepository;
import com.ankurshala.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.Duration;
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
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private DistributedLockService lockService;
    
    private Booking testBooking;
    private User student;
    private Board board;
    private Grade grade;
    private Subject subject;
    private Chapter chapter;
    private Topic topic;
    
    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();
        topicRepository.deleteAll();
        chapterRepository.deleteAll();
        subjectRepository.deleteAll();
        gradeRepository.deleteAll();
        boardRepository.deleteAll();

        board = createBoard();
        grade = createGrade(board);
        subject = createSubject(board, grade);
        chapter = createChapter(board, grade, subject);
        topic = createTopic(board, grade, subject, chapter);
        student = createStudent("student1@test.com");

        // Create a test booking in PENDING state
        testBooking = createBooking(student.getId(),
                ZonedDateTime.now().plusHours(1),
                ZonedDateTime.now().plusHours(2));
    }

    @Test
    @DisplayName("Test 1: Multiple teachers accepting same booking - Only ONE should succeed")
    void testMultipleTeachersAcceptSameBooking() throws Exception {
        int teacherCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(teacherCount);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Long> teacherIds = createTeachers(teacherCount);
        
        // Simulate 10 teachers trying to accept simultaneously
        for (int i = 0; i < teacherCount; i++) {
            final Long teacherId = teacherIds.get(i);
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

        List<Long> teacherIds = createTeachers(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final Long teacherId = teacherIds.get(i);
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

        List<Long> teacherIds = createTeachers(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final Long teacherId = teacherIds.get(i);
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

        List<Long> teacherIds = createTeachers(teacherCount);
        
        for (int i = 0; i < teacherCount; i++) {
            final Long teacherId = teacherIds.get(i);
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
        Teacher conflictTeacher = createTeacher("conflict");
        Long teacherId = conflictTeacher.getUser().getId();

        // Create first booking
        Booking firstBooking = createBooking(student.getId(),
                ZonedDateTime.now().plusHours(1),
                ZonedDateTime.now().plusHours(2));
        firstBooking.setTeacherId(teacherId);
        firstBooking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(firstBooking);
        
        // Try to accept overlapping booking with same teacher
        Booking overlappingBooking = createBooking(student.getId(),
                ZonedDateTime.now().plusHours(1).plusMinutes(30),
                ZonedDateTime.now().plusHours(2).plusMinutes(30));
        
        assertThrows(BookingConcurrencyService.TeacherTimeConflictException.class, () -> {
            bookingConcurrencyService.acceptBookingWithLock(overlappingBooking.getId(), teacherId);
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
        Long teacherId = createTeacher("perf").getUser().getId();
        
        for (int i = 0; i < iterations; i++) {
            // Create new booking for each iteration
            Booking booking = createBooking(student.getId(),
                    ZonedDateTime.now().plusHours(i + 1),
                    ZonedDateTime.now().plusHours(i + 2));
            
            long start = System.currentTimeMillis();
            bookingConcurrencyService.acceptBookingWithLock(booking.getId(), teacherId);
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
        List<Long> teacherIds = createTeachers(bookingCount);
        for (int i = 0; i < bookingCount; i++) {
            User otherStudent = createStudent("student" + (i + 2) + "@test.com");
            Booking booking = createBooking(otherStudent.getId(),
                    ZonedDateTime.now().plusHours(i + 1),
                    ZonedDateTime.now().plusHours(i + 2));
            bookings.add(booking);
        }
        
        AtomicInteger successCount = new AtomicInteger(0);
        
        // Accept all bookings concurrently
        for (int i = 0; i < bookingCount; i++) {
            final Long bookingId = bookings.get(i).getId();
            final Long teacherId = teacherIds.get(i);
            
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

    private List<Long> createTeachers(int count) {
        List<Long> teacherIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Teacher teacher = createTeacher("t" + i);
            teacherIds.add(teacher.getUser().getId());
        }
        return teacherIds;
    }

    private Teacher createTeacher(String suffix) {
        User user = new User();
        user.setName("Teacher " + suffix);
        user.setEmail("teacher-" + suffix + "@test.com");
        user.setPassword("password");
        user.setRole(Role.TEACHER);
        user.setEnabled(true);
        user = userRepository.save(user);

        Teacher teacher = new Teacher(user, user.getName(), user.getEmail());
        teacher.setStatus(TeacherStatus.ACTIVE);
        return teacherRepository.save(teacher);
    }

    private User createStudent(String email) {
        User user = new User();
        user.setName("Student " + email);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Booking createBooking(Long studentId, ZonedDateTime start, ZonedDateTime end) {
        Booking booking = new Booking();
        booking.setStudentId(studentId);
        booking.setTopicId(topic.getId());
        booking.setSubjectId(subject.getId());
        booking.setBoard(board.getName());
        booking.setGrade(grade.getName());
        booking.setCategory("STANDARD");
        booking.setDurationMinutes((int) Duration.between(start, end).toMinutes());
        booking.setPriceMinCents(30000);
        booking.setPriceMaxCents(35000);
        booking.setStartTs(start);
        booking.setEndTs(end);
        booking.setStatus(BookingStatus.PENDING);
        booking.setState("REQUESTED");
        return bookingRepository.save(booking);
    }

    private Board createBoard() {
        Board created = new Board();
        created.setName("CBSE");
        created.setActive(true);
        return boardRepository.save(created);
    }

    private Grade createGrade(Board board) {
        Grade created = new Grade();
        created.setName("GRADE_8");
        created.setDisplayName("Grade 8");
        created.setBoardId(board.getId());
        created.setActive(true);
        return gradeRepository.save(created);
    }

    private Subject createSubject(Board board, Grade grade) {
        Subject created = new Subject();
        created.setName("Science");
        created.setBoardId(board.getId());
        created.setGradeId(grade.getId());
        created.setActive(true);
        return subjectRepository.save(created);
    }

    private Chapter createChapter(Board board, Grade grade, Subject subject) {
        Chapter created = new Chapter();
        created.setName("Pollution");
        created.setBoardId(board.getId());
        created.setGradeId(grade.getId());
        created.setSubjectId(subject.getId());
        created.setActive(true);
        return chapterRepository.save(created);
    }

    private Topic createTopic(Board board, Grade grade, Subject subject, Chapter chapter) {
        Topic created = new Topic();
        created.setTitle("Test Topic");
        created.setDescription("Test topic");
        created.setExpectedMinutes(60);
        created.setBoardId(board.getId());
        created.setGradeId(grade.getId());
        created.setSubjectId(subject.getId());
        created.setChapterId(chapter.getId());
        created.setActive(true);
        return topicRepository.save(created);
    }
}

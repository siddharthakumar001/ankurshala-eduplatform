package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.booking.BookingQuoteRequest;
import com.ankurshala.backend.dto.booking.CreateBookingRequest;
import com.ankurshala.backend.entity.Board;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.ClassLevel;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.entity.Grade;
import com.ankurshala.backend.entity.PricingRule;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.entity.StudentProfile;
import com.ankurshala.backend.entity.Subject;
import com.ankurshala.backend.entity.Topic;
import com.ankurshala.backend.entity.Chapter;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BoardRepository;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.ChapterRepository;
import com.ankurshala.backend.repository.GradeRepository;
import com.ankurshala.backend.repository.PricingRuleRepository;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.SubjectRepository;
import com.ankurshala.backend.repository.TopicRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.service.EnhancedBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceIntegrationTest {

    @Autowired
    private EnhancedBookingService bookingService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

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
    private PricingRuleRepository pricingRuleRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    private User student;
    private User teacher;
    private StudentProfile studentProfile;
    private Board board;
    private Grade grade;
    private Subject subject;
    private Chapter chapter;
    private Topic topic;
    private PricingRule pricingRule;

    @BeforeEach
    void setUp() {
        // Clean up
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        studentProfileRepository.deleteAll();
        chapterRepository.deleteAll();
        subjectRepository.deleteAll();
        gradeRepository.deleteAll();
        boardRepository.deleteAll();
        
        // Create test data
        board = createTestBoard();
        grade = createTestGrade(board);
        subject = createTestSubject(board, grade);
        chapter = createTestChapter(board, grade, subject);
        student = createTestStudent();
        teacher = createTestTeacher();
        studentProfile = createTestStudentProfile(student, board, grade);
        topic = createTestTopic(board, grade, subject, chapter);
        pricingRule = createTestPricingRule(topic);
    }

    @Test
    void testGetBookingQuoteSuccess() {
        // Given
        BookingQuoteRequest request = new BookingQuoteRequest();
        request.setSubjectId(subject.getId());
        request.setChapterId(chapter.getId());
        request.setTopicId(topic.getId());
        request.setStartTimeISO(ZonedDateTime.now().plusHours(2));
        request.setTeacherCategory("STANDARD");

        // When
        var response = bookingService.getBookingQuote(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getDurationMinutes() > 0);
        assertNotNull(response.getEndTime());
        assertTrue(response.isBufferOk());
        assertNotNull(response.getPriceDetails());
        assertTrue(response.getPriceDetails().getMinCents() > 0);
    }

    @Test
    void testCreateBookingSuccess() {
        // Given
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSubjectId(subject.getId());
        request.setChapterId(chapter.getId());
        request.setTopicId(topic.getId());
        request.setTeacherId(teacher.getId());
        request.setStartTimeISO(ZonedDateTime.now().plusHours(2));
        request.setDurationMinutes(60);
        request.setTeacherCategory("STANDARD");
        request.setAppliedRuleId(pricingRule.getId());
        request.setPriceMinCents(50000);
        request.setPriceMaxCents(50000);
        request.setNotes("Test booking");

        // When
        var response = bookingService.createBooking(student.getId(), request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getBookingId());
        assertEquals("REQUESTED", response.getState());
        assertNotNull(response.getStartTime());
        assertNotNull(response.getEndTime());
        
        // Verify booking is saved
        assertTrue(bookingRepository.existsById(response.getBookingId()));
    }

    @Test
    void testCreateBookingInvalidTeacher() {
        // Given
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSubjectId(subject.getId());
        request.setChapterId(chapter.getId());
        request.setTopicId(topic.getId());
        request.setTeacherId(999L); // Non-existent teacher
        request.setStartTimeISO(ZonedDateTime.now().plusHours(2));
        request.setDurationMinutes(60);
        request.setTeacherCategory("STANDARD");
        request.setAppliedRuleId(pricingRule.getId());
        request.setPriceMinCents(50000);
        request.setPriceMaxCents(50000);

        // When & Then
        assertThrows(Exception.class, () -> 
            bookingService.createBooking(student.getId(), request));
    }

    @Test
    void testCreateBookingPastTime() {
        // Given
        CreateBookingRequest request = new CreateBookingRequest();
        request.setSubjectId(subject.getId());
        request.setChapterId(chapter.getId());
        request.setTopicId(topic.getId());
        request.setTeacherId(teacher.getId());
        request.setStartTimeISO(ZonedDateTime.now().minusHours(1)); // Past time
        request.setDurationMinutes(60);
        request.setTeacherCategory("STANDARD");
        request.setAppliedRuleId(pricingRule.getId());
        request.setPriceMinCents(50000);
        request.setPriceMaxCents(50000);

        // When & Then
        assertThrows(Exception.class, () -> 
            bookingService.createBooking(student.getId(), request));
    }

    private User createTestStudent() {
        User user = new User();
        user.setName("Test Student");
        user.setEmail("student@test.com");
        user.setPassword("password");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private User createTestTeacher() {
        User user = new User();
        user.setName("Test Teacher");
        user.setEmail("teacher@test.com");
        user.setPassword("password");
        user.setRole(Role.TEACHER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private Board createTestBoard() {
        Board board = new Board();
        board.setName("CBSE");
        board.setActive(true);
        return boardRepository.save(board);
    }

    private Grade createTestGrade(Board board) {
        Grade grade = new Grade();
        grade.setName("GRADE_8");
        grade.setDisplayName("Grade 8");
        grade.setBoardId(board.getId());
        grade.setActive(true);
        return gradeRepository.save(grade);
    }

    private Subject createTestSubject(Board board, Grade grade) {
        Subject subject = new Subject();
        subject.setName("Science");
        subject.setBoardId(board.getId());
        subject.setGradeId(grade.getId());
        subject.setActive(true);
        return subjectRepository.save(subject);
    }

    private Chapter createTestChapter(Board board, Grade grade, Subject subject) {
        Chapter chapter = new Chapter();
        chapter.setName("Photosynthesis");
        chapter.setBoardId(board.getId());
        chapter.setGradeId(grade.getId());
        chapter.setSubjectId(subject.getId());
        chapter.setActive(true);
        return chapterRepository.save(chapter);
    }

    private StudentProfile createTestStudentProfile(User user, Board board, Grade grade) {
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        profile.setFirstName("Test");
        profile.setLastName("Student");
        profile.setEducationalBoard(EducationalBoard.CBSE);
        profile.setClassLevel(ClassLevel.GRADE_8);
        profile.setBoardId(board.getId());
        profile.setGradeId(grade.getId());
        profile.setIsComplete(true);
        return studentProfileRepository.save(profile);
    }

    private Topic createTestTopic(Board board, Grade grade, Subject subject, Chapter chapter) {
        Topic topic = new Topic();
        topic.setTitle("Test Topic");
        topic.setDescription("Test Description");
        topic.setExpectedMinutes(60);
        topic.setBoardId(board.getId());
        topic.setGradeId(grade.getId());
        topic.setSubjectId(subject.getId());
        topic.setChapterId(chapter.getId());
        topic.setActive(true);
        return topicRepository.save(topic);
    }

    private PricingRule createTestPricingRule(Topic topic) {
        PricingRule rule = new PricingRule();
        rule.setHourlyRate(new BigDecimal("500.00"));
        rule.setTopic(topic);
        rule.setCategory("STANDARD");
        rule.setActive(true);
        return pricingRuleRepository.save(rule);
    }
}

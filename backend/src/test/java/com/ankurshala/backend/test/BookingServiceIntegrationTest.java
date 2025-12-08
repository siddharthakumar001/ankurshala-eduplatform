package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.booking.BookingQuoteRequest;
import com.ankurshala.backend.dto.booking.CreateBookingRequest;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
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
    private TopicRepository topicRepository;
    
    @Autowired
    private PricingRuleRepository pricingRuleRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    private User student;
    private User teacher;
    private Topic topic;
    private PricingRule pricingRule;

    @BeforeEach
    void setUp() {
        // Clean up
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test data
        student = createTestStudent();
        teacher = createTestTeacher();
        topic = createTestTopic();
        pricingRule = createTestPricingRule();
    }

    @Test
    void testGetBookingQuoteSuccess() {
        // Given
        BookingQuoteRequest request = new BookingQuoteRequest();
        request.setSubjectId(topic.getSubject().getId());
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
        request.setSubjectId(topic.getSubject().getId());
        request.setTopicId(topic.getId());
        request.setTeacherId(teacher.getId());
        request.setStartTimeISO(ZonedDateTime.now().plusHours(2));
        request.setDurationMinutes(60);
        request.setCategory("STANDARD");
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
        request.setSubjectId(topic.getSubject().getId());
        request.setTopicId(topic.getId());
        request.setTeacherId(999L); // Non-existent teacher
        request.setStartTimeISO(ZonedDateTime.now().plusHours(2));
        request.setDurationMinutes(60);
        request.setCategory("STANDARD");
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
        request.setSubjectId(topic.getSubject().getId());
        request.setTopicId(topic.getId());
        request.setTeacherId(teacher.getId());
        request.setStartTimeISO(ZonedDateTime.now().minusHours(1)); // Past time
        request.setDurationMinutes(60);
        request.setCategory("STANDARD");
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

    private Topic createTestTopic() {
        Topic topic = new Topic();
        topic.setTitle("Test Topic");
        topic.setDescription("Test Description");
        topic.setExpectedMinutes(60);
        // Set other required fields
        return topicRepository.save(topic);
    }

    private PricingRule createTestPricingRule() {
        PricingRule rule = new PricingRule();
        rule.setHourlyRate(new BigDecimal("500.00"));
        rule.setCategory("STANDARD");
        rule.setActive(true);
        // Set other required fields
        return pricingRuleRepository.save(rule);
    }
}

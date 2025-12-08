package com.ankurshala.backend.test.util;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class TestDataFactory {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private PricingRuleRepository pricingRuleRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    public User createTestStudent() {
        User user = new User();
        user.setName("Test Student");
        user.setEmail("teststudent@example.com");
        user.setPassword("password");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User createTestTeacher() {
        User user = new User();
        user.setName("Test Teacher");
        user.setEmail("testteacher@example.com");
        user.setPassword("password");
        user.setRole(Role.TEACHER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User createTestAdmin() {
        User user = new User();
        user.setName("Test Admin");
        user.setEmail("testadmin@example.com");
        user.setPassword("password");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public Topic createTestTopic() {
        Topic topic = new Topic();
        topic.setTitle("Test Topic");
        topic.setDescription("Test Description");
        topic.setExpectedMinutes(60);
        // Note: These fields are not available in the current Topic entity
        // Note: Tags field is not available in the current Topic entity
        return topicRepository.save(topic);
    }

    public PricingRule createTestPricingRule() {
        PricingRule rule = new PricingRule();
        rule.setHourlyRate(new BigDecimal("500.00"));
        rule.setCategory("STANDARD");
        rule.setActive(true);
        // Note: Description field is not available in the current PricingRule entity
        return pricingRuleRepository.save(rule);
    }

    public Booking createTestBooking(User student, User teacher, Topic topic) {
        Booking booking = new Booking();
        booking.setStudentId(student.getId());
        booking.setTeacherId(teacher.getId());
        booking.setBoard("CBSE");
        booking.setGrade("10");
        booking.setSubjectId(1L);
        booking.setTopicId(topic.getId());
        booking.setStartTs(ZonedDateTime.now().plusHours(1));
        booking.setEndTs(ZonedDateTime.now().plusHours(2));
        booking.setCategory("STANDARD");
        booking.setPriceMinCents(50000);
        booking.setPriceMaxCents(50000);
        booking.setState("REQUESTED");
        booking.setNotes("Test booking");
        return bookingRepository.save(booking);
    }

    public StudentProfile createTestStudentProfile(User user) {
        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        profile.setFirstName("Test");
        profile.setLastName("Student");
        profile.setMobileNumber("9876543210");
        profile.setDateOfBirth(LocalDate.of(2005, 5, 15));
        profile.setEducationalBoard(EducationalBoard.CBSE);
        profile.setClassLevel(ClassLevel.GRADE_10);
        profile.setGradeLevel("10");
        profile.setSchoolName("Test School");
        profile.setEmergencyContact("9876543210");
        profile.setGuardianName("Guardian Name");
        // Note: Other fields are not available in the current StudentProfile entity
        return profile;
    }

    public Teacher createTestTeacherProfile(User user) {
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setName("Test Teacher");
        teacher.setEmail("testteacher@example.com");
        teacher.setBio("Experienced mathematics teacher");
        teacher.setYearsExperience(5);
        teacher.setLanguages("[\"English\", \"Hindi\"]");
        teacher.setCategories("[\"STANDARD\"]");
        teacher.setHourlyRate(new BigDecimal("500.00"));
        teacher.setStatus(TeacherStatus.ACTIVE);
        // Note: setActive method is not available in the current Teacher entity
        return teacher;
    }

    public TeacherAvailability createTestTeacherAvailability(User teacher) {
        TeacherAvailability availability = new TeacherAvailability();
        // Note: Teacher entity is needed but not directly accessible from User
        // This test factory might need refactoring if Teacher is needed
        // For now, skip setting teacher to allow compilation
        availability.setWeekday(1); // Monday
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));
        availability.setTimezone("Asia/Kolkata");
        availability.setActive(true);
        return availability;
    }

    public List<User> createMultipleTestUsers(int count, Role role) {
        return Arrays.stream(new int[count])
                .mapToObj(i -> {
                    User user = new User();
                    user.setName("Test User " + i);
                    user.setEmail("testuser" + i + "@example.com");
                    user.setPassword("password");
                    user.setRole(role);
                    user.setEnabled(true);
                    return userRepository.save(user);
                })
                .toList();
    }

    public void cleanupTestData() {
        bookingRepository.deleteAll();
        topicRepository.deleteAll();
        pricingRuleRepository.deleteAll();
        userRepository.deleteAll();
    }
}

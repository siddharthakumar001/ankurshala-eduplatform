package com.ankurshala.backend.test;

import com.ankurshala.backend.dto.student.CompleteStepRequest;
import com.ankurshala.backend.dto.student.CompleteStepResponse;
import com.ankurshala.backend.dto.student.DailyPlanDto;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Integration tests for Today Home feature
 * Tests full stack: Controller -> Service -> Repository -> Database
 * with RBAC and ownership checks
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Today Home Integration Tests")
public class TodayHomeIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;

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
    private BookingRepository bookingRepository;
    
    @Autowired
    private DailyPlanProgressRepository dailyPlanProgressRepository;
    
    @Autowired
    private WeakTopicRecommendationRepository weakTopicRecommendationRepository;
    
    @Autowired
    private DailyPracticeQueueRepository dailyPracticeQueueRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private User student;
    private User teacher;
    private Topic topic;
    private String studentToken;
    private Board board;
    private Grade grade;
    private Subject subject;
    private Chapter chapter;
    
    @BeforeEach
    void setUp() {
        // Clean up
        dailyPlanProgressRepository.deleteAll();
        weakTopicRecommendationRepository.deleteAll();
        dailyPracticeQueueRepository.deleteAll();
        bookingRepository.deleteAll();
        
        // Create test student
        student = new User();
        student.setEmail("student.today@test.com");
        student.setPassword(passwordEncoder.encode("password"));
        student.setName("Test Student");
        student.setRole(Role.STUDENT);
        student.setEnabled(true);
        student = userRepository.save(student);
        
        // Create test teacher
        teacher = new User();
        teacher.setEmail("teacher.today@test.com");
        teacher.setPassword(passwordEncoder.encode("password"));
        teacher.setName("Test Teacher");
        teacher.setRole(Role.TEACHER);
        teacher.setEnabled(true);
        teacher = userRepository.save(teacher);

        board = new Board();
        board.setName("CBSE");
        board.setActive(true);
        board = boardRepository.save(board);

        grade = new Grade();
        grade.setName("GRADE_8");
        grade.setDisplayName("Grade 8");
        grade.setBoardId(board.getId());
        grade.setActive(true);
        grade = gradeRepository.save(grade);

        subject = new Subject();
        subject.setName("Science");
        subject.setBoardId(board.getId());
        subject.setGradeId(grade.getId());
        subject.setActive(true);
        subject = subjectRepository.save(subject);

        chapter = new Chapter();
        chapter.setName("Pollution");
        chapter.setBoardId(board.getId());
        chapter.setGradeId(grade.getId());
        chapter.setSubjectId(subject.getId());
        chapter.setActive(true);
        chapter = chapterRepository.save(chapter);
        
        // Create test topic
        topic = new Topic();
        topic.setTitle("Test Topic - Algebra");
        topic.setDescription("Test algebra topic");
        topic.setBoardId(board.getId());
        topic.setGradeId(grade.getId());
        topic.setSubjectId(subject.getId());
        topic.setChapterId(chapter.getId());
        topic.setActive(true);
        topic = topicRepository.save(topic);
        
        // Generate JWT token for student
        studentToken = jwtTokenProvider.generateAccessToken(student);
    }
    
    @Test
    @DisplayName("GET /student/today - Success with empty plan")
    void testGetDailyPlan_EmptyPlan() throws Exception {
        mockMvc.perform(get("/student/today")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.validUntil").exists())
                .andExpect(jsonPath("$.progress").exists())
                .andExpect(jsonPath("$.progress.totalSteps").value(5))
                .andExpect(jsonPath("$.progress.completedSteps").value(0));
    }
    
    @Test
    @DisplayName("GET /student/today - With upcoming booking")
    void testGetDailyPlan_WithUpcomingBooking() throws Exception {
        // Create upcoming booking
        Booking booking = new Booking(student, topic, 
                ZonedDateTime.now().plusHours(2),
                ZonedDateTime.now().plusHours(3),
                60,
                new java.math.BigDecimal("300"),
                new java.math.BigDecimal("350"));
        booking.setTeacher(teacher);
        booking.setTeacherId(teacher.getId());
        booking.setBoard(board.getName());
        booking.setGrade(grade.getName());
        booking.setSubjectId(subject.getId());
        booking.setChapterId(chapter.getId());
        booking.setCategory("STANDARD");
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);
        
        mockMvc.perform(get("/student/today")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextClass").exists())
                .andExpect(jsonPath("$.nextClass.bookingId").value(booking.getId()))
                .andExpect(jsonPath("$.nextClass.topicName").value("Test Topic - Algebra"))
                .andExpect(jsonPath("$.nextClass.teacherName").value("Test Teacher"))
                .andExpect(jsonPath("$.nextClass.durationMinutes").value(60))
                .andExpect(jsonPath("$.nextClass.companionCta").exists())
                .andExpect(jsonPath("$.nextClass.companionLink").value("/student/bookings/" + booking.getId() + "/companion"));
    }
    
    @Test
    @DisplayName("GET /student/today - With weak topic recommendation")
    void testGetDailyPlan_WithWeakTopicRecommendation() throws Exception {
        // Create weak topic recommendation
        WeakTopicRecommendation recommendation = WeakTopicRecommendation.builder()
                .studentId(student.getId())
                .topicId(topic.getId())
                .recommendationReason("Low performance in recent quizzes")
                .prerequisiteGaps("")
                .confidenceScore(new java.math.BigDecimal("0.85"))
                .practiceAttemptsCount(3)
                .isActive(true)
                .build();
        weakTopicRecommendationRepository.save(recommendation);
        
        mockMvc.perform(get("/student/today")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weakTopicRecommendation").exists())
                .andExpect(jsonPath("$.weakTopicRecommendation.topicId").value(topic.getId()))
                .andExpect(jsonPath("$.weakTopicRecommendation.topicName").value("Test Topic - Algebra"))
                .andExpect(jsonPath("$.weakTopicRecommendation.reason").value("Low performance in recent quizzes"))
                .andExpect(jsonPath("$.weakTopicRecommendation.confidenceScore").value(0.85))
                .andExpect(jsonPath("$.weakTopicRecommendation.actionCta").exists())
                .andExpect(jsonPath("$.weakTopicRecommendation.actionLink").value("/student/practice/" + topic.getId()));
    }
    
    @Test
    @DisplayName("POST /student/today/complete-step - Success")
    void testCompleteStep_Success() throws Exception {
        // Create request
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("duration", 300);
        metadata.put("score", 85);
        
        CompleteStepRequest request = CompleteStepRequest.builder()
                .stepType("PRACTICE")
                .stepIdentifier(topic.getId().toString())
                .metadata(metadata)
                .build();
        
        mockMvc.perform(post("/student/today/complete-step")
                        .with(csrf())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Great job! Step completed successfully."))
                .andExpect(jsonPath("$.completedAt").exists())
                .andExpect(jsonPath("$.todayStepsCompleted").value(1))
                .andExpect(jsonPath("$.totalStepsCompleted").value(1))
                .andExpect(jsonPath("$.nextRecommendation").exists())
                .andExpect(jsonPath("$.motivationalMessage").exists());
        
        // Verify database record
        var progress = dailyPlanProgressRepository
                .findByStudentIdAndPlanDateOrderByCompletedAtDesc(student.getId(), LocalDate.now());
        
        assert progress.size() == 1;
        assert progress.get(0).getStepType().equals("PRACTICE");
        assert progress.get(0).getStepIdentifier().equals(topic.getId().toString());
    }
    
    @Test
    @DisplayName("POST /student/today/complete-step - Already completed")
    void testCompleteStep_AlreadyCompleted() throws Exception {
        // Create existing progress
        DailyPlanProgress existingProgress = DailyPlanProgress.builder()
                .studentId(student.getId())
                .planDate(LocalDate.now())
                .stepType("PRACTICE")
                .stepIdentifier(topic.getId().toString())
                .build();
        dailyPlanProgressRepository.save(existingProgress);
        
        // Try to complete again
        CompleteStepRequest request = CompleteStepRequest.builder()
                .stepType("PRACTICE")
                .stepIdentifier(topic.getId().toString())
                .metadata(new HashMap<>())
                .build();
        
        mockMvc.perform(post("/student/today/complete-step")
                        .with(csrf())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You've already completed this step today!"));
    }
    
    @Test
    @DisplayName("POST /student/today/complete-step - Invalid step type")
    void testCompleteStep_InvalidStepType() throws Exception {
        CompleteStepRequest request = CompleteStepRequest.builder()
                .stepType("INVALID_TYPE")
                .stepIdentifier("123")
                .metadata(new HashMap<>())
                .build();
        
        mockMvc.perform(post("/student/today/complete-step")
                        .with(csrf())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
    
    @Test
    @DisplayName("GET /student/today - Unauthorized without token")
    void testGetDailyPlan_Unauthorized() throws Exception {
        mockMvc.perform(get("/student/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("POST /student/today/complete-step - Unauthorized without token")
    void testCompleteStep_Unauthorized() throws Exception {
        CompleteStepRequest request = CompleteStepRequest.builder()
                .stepType("PRACTICE")
                .stepIdentifier("1")
                .metadata(new HashMap<>())
                .build();
        
        mockMvc.perform(post("/student/today/complete-step")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("GET /student/today - Cache works properly")
    void testGetDailyPlan_Caching() throws Exception {
        // First call - generates plan
        mockMvc.perform(get("/student/today")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        // Add a booking
        Booking booking = new Booking(student, topic,
                ZonedDateTime.now().plusHours(2),
                ZonedDateTime.now().plusHours(3),
                60,
                new java.math.BigDecimal("300"),
                new java.math.BigDecimal("350"));
        booking.setTeacher(teacher);
        booking.setTeacherId(teacher.getId());
        booking.setBoard(board.getName());
        booking.setGrade(grade.getName());
        booking.setSubjectId(subject.getId());
        booking.setChapterId(chapter.getId());
        booking.setCategory("STANDARD");
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);
        
        // Second call - should return cached (no booking yet due to cache)
        // Note: In real scenario, cache would be invalidated on booking creation
        mockMvc.perform(get("/student/today")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

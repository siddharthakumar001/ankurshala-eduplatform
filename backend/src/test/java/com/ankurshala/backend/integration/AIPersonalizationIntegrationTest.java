package com.ankurshala.backend.integration;

import com.ankurshala.backend.dto.ai.MasteryDTO;
import com.ankurshala.backend.dto.ai.QuizDTO;
import com.ankurshala.backend.dto.ai.RecommendationDTO;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for AI Personalization features.
 * Tests the complete flow from API to database for mastery, quizzes, and recommendations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class AIPersonalizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private StudentTopicMasteryRepository masteryRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private User testStudent;
    private Topic testTopic;
    private Long quizId;
    private Long attemptId;

    @BeforeEach
    void setUp() {
        // Create test student user
        Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ERole.ROLE_STUDENT);
                    return roleRepository.save(role);
                });

        testStudent = userRepository.findByEmail("test.student.ai@ankurshala.com")
                .orElseGet(() -> {
                    User user = new User();
                    user.setName("AI Test Student");
                    user.setEmail("test.student.ai@ankurshala.com");
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setRoles(new HashSet<>(Collections.singletonList(studentRole)));
                    user.setActive(true);
                    return userRepository.save(user);
                });

        studentToken = jwtTokenProvider.generateToken(testStudent.getId(), testStudent.getEmail(), "STUDENT");

        // Create test board, grade, subject, chapter, topic
        Board board = boardRepository.findByCode("CBSE")
                .orElseGet(() -> {
                    Board b = new Board();
                    b.setName("CBSE");
                    b.setCode("CBSE");
                    b.setActive(true);
                    return boardRepository.save(b);
                });

        Grade grade = gradeRepository.findAll().stream()
                .filter(g -> g.getName().equals("Class 9"))
                .findFirst()
                .orElseGet(() -> {
                    Grade g = new Grade();
                    g.setName("Class 9");
                    g.setLevel(9);
                    g.setActive(true);
                    return gradeRepository.save(g);
                });

        Subject subject = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals("AI Test Mathematics"))
                .findFirst()
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setName("AI Test Mathematics");
                    s.setCode("AI_TEST_MATH");
                    s.setActive(true);
                    return subjectRepository.save(s);
                });

        Chapter chapter = chapterRepository.findAll().stream()
                .filter(c -> c.getTitle().equals("AI Test Algebra"))
                .findFirst()
                .orElseGet(() -> {
                    Chapter c = new Chapter();
                    c.setTitle("AI Test Algebra");
                    c.setSubjectId(subject.getId());
                    c.setBoardId(board.getId());
                    c.setGradeId(grade.getId());
                    c.setActive(true);
                    return chapterRepository.save(c);
                });

        testTopic = topicRepository.findAll().stream()
                .filter(t -> t.getTitle().equals("AI Test Linear Equations"))
                .findFirst()
                .orElseGet(() -> {
                    Topic t = new Topic();
                    t.setTitle("AI Test Linear Equations");
                    t.setChapterId(chapter.getId());
                    t.setSubjectId(subject.getId());
                    t.setBoardId(board.getId());
                    t.setGradeId(grade.getId());
                    t.setActive(true);
                    return topicRepository.save(t);
                });
    }

    @Test
    @Order(1)
    void getMasteryOverview_Authenticated_ReturnsOverview() throws Exception {
        mockMvc.perform(get("/student/mastery")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(testStudent.getId()));
    }

    @Test
    @Order(2)
    void getTopicMastery_ValidTopic_ReturnsMastery() throws Exception {
        mockMvc.perform(get("/student/mastery/topic/" + testTopic.getId())
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.topicId").value(testTopic.getId()))
                .andExpect(jsonPath("$.data.topicTitle").value(testTopic.getTitle()));
    }

    @Test
    @Order(3)
    void getWeakTopics_Authenticated_ReturnsWeakTopics() throws Exception {
        // First create a mastery record with low score
        StudentTopicMastery mastery = new StudentTopicMastery(testStudent.getId(), testTopic.getId());
        mastery.setMasteryScore(new BigDecimal("0.300"));
        masteryRepository.save(mastery);

        mockMvc.perform(get("/student/mastery/weak-topics")
                        .param("threshold", "0.65")
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(testStudent.getId()));
    }

    @Test
    @Order(4)
    void getPrerequisiteRecommendations_ValidTopic_ReturnsRecommendations() throws Exception {
        mockMvc.perform(get("/student/mastery/recommendations/topic/" + testTopic.getId())
                        .param("threshold", "0.65")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetTopicId").value(testTopic.getId()))
                .andExpect(jsonPath("$.data.targetTopicTitle").value(testTopic.getTitle()));
    }

    @Test
    @Order(5)
    void generateQuiz_ValidRequest_ReturnsQuiz() throws Exception {
        QuizDTO.GenerateRequest request = QuizDTO.GenerateRequest.builder()
                .topicId(testTopic.getId())
                .numQuestions(3)
                .difficulty("MEDIUM")
                .language("en")
                .build();

        MvcResult result = mockMvc.perform(post("/student/quizzes")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quizId").exists())
                .andExpect(jsonPath("$.data.topicId").value(testTopic.getId()))
                .andExpect(jsonPath("$.data.questions").isArray())
                .andExpect(jsonPath("$.data.questions", hasSize(3)))
                .andReturn();

        // Extract quiz ID for next tests
        String responseJson = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        quizId = ((Number) data.get("quizId")).longValue();
    }

    @Test
    @Order(6)
    void getQuiz_ValidId_ReturnsQuiz() throws Exception {
        // First generate a quiz
        QuizDTO.GenerateRequest request = QuizDTO.GenerateRequest.builder()
                .topicId(testTopic.getId())
                .numQuestions(2)
                .build();

        MvcResult createResult = mockMvc.perform(post("/student/quizzes")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        Long newQuizId = ((Number) data.get("quizId")).longValue();

        // Then get it
        mockMvc.perform(get("/student/quizzes/" + newQuizId)
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quizId").value(newQuizId));
    }

    @Test
    @Order(7)
    void startAttempt_ValidQuiz_ReturnsAttempt() throws Exception {
        // First generate a quiz
        QuizDTO.GenerateRequest request = QuizDTO.GenerateRequest.builder()
                .topicId(testTopic.getId())
                .numQuestions(2)
                .build();

        MvcResult createResult = mockMvc.perform(post("/student/quizzes")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        Long newQuizId = ((Number) data.get("quizId")).longValue();

        // Start attempt
        MvcResult attemptResult = mockMvc.perform(post("/student/quizzes/" + newQuizId + "/attempts")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attemptId").exists())
                .andExpect(jsonPath("$.data.quizId").value(newQuizId))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andReturn();

        // Extract attempt ID for next tests
        responseJson = attemptResult.getResponse().getContentAsString();
        response = objectMapper.readValue(responseJson, Map.class);
        data = (Map<String, Object>) response.get("data");
        attemptId = ((Number) data.get("attemptId")).longValue();
    }

    @Test
    @Order(8)
    void submitAttempt_ValidAnswers_ReturnsGradedResult() throws Exception {
        // First generate a quiz and start an attempt
        QuizDTO.GenerateRequest genRequest = QuizDTO.GenerateRequest.builder()
                .topicId(testTopic.getId())
                .numQuestions(2)
                .build();

        MvcResult createResult = mockMvc.perform(post("/student/quizzes")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(genRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        Long newQuizId = ((Number) data.get("quizId")).longValue();
        List<Map<String, Object>> questions = (List<Map<String, Object>>) data.get("questions");

        // Start attempt
        MvcResult attemptResult = mockMvc.perform(post("/student/quizzes/" + newQuizId + "/attempts")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        responseJson = attemptResult.getResponse().getContentAsString();
        response = objectMapper.readValue(responseJson, Map.class);
        data = (Map<String, Object>) response.get("data");
        Long newAttemptId = ((Number) data.get("attemptId")).longValue();

        // Prepare answers
        List<QuizDTO.SubmitAnswerRequest> answers = new ArrayList<>();
        for (Map<String, Object> question : questions) {
            Long questionId = ((Number) question.get("questionId")).longValue();
            answers.add(QuizDTO.SubmitAnswerRequest.builder()
                    .questionId(questionId)
                    .selectedOptionIds(Arrays.asList("0"))  // Select first option
                    .build());
        }

        QuizDTO.SubmitAttemptRequest submitRequest = QuizDTO.SubmitAttemptRequest.builder()
                .attemptId(newAttemptId)
                .answers(answers)
                .build();

        // Submit
        mockMvc.perform(post("/student/quizzes/" + newQuizId + "/attempts/" + newAttemptId + "/submit")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attemptId").value(newAttemptId))
                .andExpect(jsonPath("$.data.percentage").exists())
                .andExpect(jsonPath("$.data.correctCount").exists())
                .andExpect(jsonPath("$.data.totalQuestions").value(2))
                .andExpect(jsonPath("$.data.overallFeedback").exists());
    }

    @Test
    @Order(9)
    void getQuizHistory_Authenticated_ReturnsHistory() throws Exception {
        mockMvc.perform(get("/student/quizzes/history")
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(10)
    void generateStudyPlan_ValidRequest_ReturnsPlan() throws Exception {
        RecommendationDTO.GenerateStudyPlanRequest request = RecommendationDTO.GenerateStudyPlanRequest.builder()
                .targetTopicId(testTopic.getId())
                .availableDays(7)
                .minutesPerDay(60)
                .focusArea("BALANCED")
                .build();

        mockMvc.perform(post("/student/mastery/study-plan")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(testStudent.getId()))
                .andExpect(jsonPath("$.data.targetTopicId").value(testTopic.getId()))
                .andExpect(jsonPath("$.data.dailyPlan").isArray());
    }

    @Test
    void masteryEndpoints_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/student/mastery")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void quizEndpoints_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/student/quizzes/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}


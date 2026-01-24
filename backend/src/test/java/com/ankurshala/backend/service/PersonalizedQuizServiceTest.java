package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.ai.MasteryDTO;
import com.ankurshala.backend.dto.ai.QuizDTO;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.exception.ResourceNotFoundException;
import com.ankurshala.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalizedQuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository questionRepository;

    @Mock
    private QuizAttemptRepository attemptRepository;

    @Mock
    private QuizAnswerRepository answerRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private MasteryService masteryService;

    @Mock
    private AIInteractionRepository aiInteractionRepository;

    @InjectMocks
    private PersonalizedQuizService quizService;

    private Long studentId;
    private Long topicId;
    private Topic testTopic;

    @BeforeEach
    void setUp() {
        studentId = 1L;
        topicId = 100L;

        testTopic = new Topic();
        testTopic.setId(topicId);
        testTopic.setTitle("Test Topic");
        testTopic.setSubjectId(10L);
        testTopic.setGradeId(5L);
        testTopic.setBoardId(1L);
    }

    @Test
    void generateQuiz_ValidRequest_ReturnsQuiz() {
        // Given
        QuizDTO.GenerateRequest request = QuizDTO.GenerateRequest.builder()
                .topicId(topicId)
                .numQuestions(5)
                .difficulty("MEDIUM")
                .language("en")
                .build();

        Quiz savedQuiz = new Quiz();
        savedQuiz.setId(1L);
        savedQuiz.setTitle("Quiz: Test Topic");
        savedQuiz.setTopicId(topicId);
        savedQuiz.setQuestionCount(5);
        savedQuiz.setDifficulty(Quiz.QuizDifficulty.MEDIUM);
        savedQuiz.setStatus(Quiz.QuizStatus.ACTIVE);

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);
        when(questionRepository.save(any(QuizQuestion.class))).thenAnswer(invocation -> {
            QuizQuestion q = invocation.getArgument(0);
            q.setId((long) (Math.random() * 1000));
            return q;
        });
        when(aiInteractionRepository.save(any(AIInteraction.class))).thenReturn(new AIInteraction());

        // When
        QuizDTO.QuizResponse result = quizService.generateQuiz(studentId, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getQuizId());
        assertEquals("Quiz: Test Topic", result.getTitle());
        assertNotNull(result.getQuestions());
        assertEquals(5, result.getQuestions().size());
        verify(quizRepository).save(any(Quiz.class));
        verify(questionRepository, times(5)).save(any(QuizQuestion.class));
    }

    @Test
    void generateQuiz_TopicNotFound_ThrowsException() {
        // Given
        QuizDTO.GenerateRequest request = QuizDTO.GenerateRequest.builder()
                .topicId(999L)
                .numQuestions(5)
                .build();

        when(topicRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> 
                quizService.generateQuiz(studentId, request));
    }

    @Test
    void startAttempt_ValidQuiz_ReturnsAttempt() {
        // Given
        Long quizId = 1L;
        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Test Quiz");
        quiz.setStatus(Quiz.QuizStatus.ACTIVE);

        QuizAttempt savedAttempt = new QuizAttempt(quizId, studentId);
        savedAttempt.setId(100L);
        savedAttempt.setStartedAt(LocalDateTime.now());

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findByStudentIdAndStatus(studentId, QuizAttempt.AttemptStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());
        when(attemptRepository.save(any(QuizAttempt.class))).thenReturn(savedAttempt);

        // When
        QuizDTO.AttemptResponse result = quizService.startAttempt(studentId, quizId);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getAttemptId());
        assertEquals(quizId, result.getQuizId());
        assertEquals("IN_PROGRESS", result.getStatus());
        verify(attemptRepository).save(any(QuizAttempt.class));
    }

    @Test
    void startAttempt_ExistingInProgressAttempt_ReturnsExisting() {
        // Given
        Long quizId = 1L;
        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTitle("Test Quiz");
        quiz.setStatus(Quiz.QuizStatus.ACTIVE);

        QuizAttempt existingAttempt = new QuizAttempt(quizId, studentId);
        existingAttempt.setId(50L);
        existingAttempt.setStartedAt(LocalDateTime.now().minusMinutes(10));

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(attemptRepository.findByStudentIdAndStatus(studentId, QuizAttempt.AttemptStatus.IN_PROGRESS))
                .thenReturn(Collections.singletonList(existingAttempt));

        // When
        QuizDTO.AttemptResponse result = quizService.startAttempt(studentId, quizId);

        // Then
        assertNotNull(result);
        assertEquals(50L, result.getAttemptId());
        verify(attemptRepository, never()).save(any(QuizAttempt.class));
    }

    @Test
    void startAttempt_InactiveQuiz_ThrowsException() {
        // Given
        Long quizId = 1L;
        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setStatus(Quiz.QuizStatus.ARCHIVED);

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        // When/Then
        assertThrows(BusinessException.class, () -> 
                quizService.startAttempt(studentId, quizId));
    }

    @Test
    void submitAttempt_ValidSubmission_ReturnsGradedResult() {
        // Given
        Long attemptId = 100L;
        Long quizId = 1L;

        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setTopicId(topicId);
        quiz.setSubjectId(10L);
        
        // Add questions to quiz
        List<QuizQuestion> questions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            QuizQuestion q = new QuizQuestion();
            q.setId((long) (i + 1));
            q.setQuizId(quizId);
            q.setQuestionText("Question " + (i + 1));
            q.setQuestionType(QuizQuestion.QuestionType.MCQ);
            q.setPoints(1);
            
            List<Map<String, Object>> options = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                Map<String, Object> opt = new HashMap<>();
                opt.put("id", String.valueOf(j));
                opt.put("text", "Option " + j);
                opt.put("isCorrect", j == 0);
                options.add(opt);
            }
            q.setOptions(options);
            questions.add(q);
        }
        quiz.setQuestions(questions);

        QuizAttempt attempt = new QuizAttempt(quizId, studentId);
        attempt.setId(attemptId);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(5));
        attempt.setStatus(QuizAttempt.AttemptStatus.IN_PROGRESS);

        List<QuizDTO.SubmitAnswerRequest> answers = Arrays.asList(
                QuizDTO.SubmitAnswerRequest.builder()
                        .questionId(1L)
                        .selectedOptionIds(Arrays.asList("0"))  // Correct
                        .build(),
                QuizDTO.SubmitAnswerRequest.builder()
                        .questionId(2L)
                        .selectedOptionIds(Arrays.asList("1"))  // Incorrect
                        .build(),
                QuizDTO.SubmitAnswerRequest.builder()
                        .questionId(3L)
                        .selectedOptionIds(Arrays.asList("0"))  // Correct
                        .build()
        );

        QuizDTO.SubmitAttemptRequest request = QuizDTO.SubmitAttemptRequest.builder()
                .attemptId(attemptId)
                .answers(answers)
                .build();

        MasteryDTO.TopicMastery currentMastery = MasteryDTO.TopicMastery.builder()
                .topicId(topicId)
                .masteryScore(new BigDecimal("0.500"))
                .build();

        MasteryDTO.MasteryUpdateResponse masteryUpdate = MasteryDTO.MasteryUpdateResponse.builder()
                .topicId(topicId)
                .previousScore(new BigDecimal("0.500"))
                .newScore(new BigDecimal("0.600"))
                .delta(new BigDecimal("0.100"))
                .newLevel("DEVELOPING")
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(quizRepository.findByIdWithQuestions(quizId)).thenReturn(Optional.of(quiz));
        when(answerRepository.save(any(QuizAnswer.class))).thenAnswer(invocation -> {
            QuizAnswer a = invocation.getArgument(0);
            a.setId((long) (Math.random() * 1000));
            return a;
        });
        when(attemptRepository.save(any(QuizAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(masteryService.getTopicMastery(studentId, topicId)).thenReturn(currentMastery);
        when(masteryService.updateMastery(eq(studentId), any(MasteryDTO.MasteryUpdateRequest.class)))
                .thenReturn(masteryUpdate);
        when(aiInteractionRepository.save(any(AIInteraction.class))).thenReturn(new AIInteraction());

        // When
        QuizDTO.GradeAttemptResponse result = quizService.submitAttempt(studentId, request);

        // Then
        assertNotNull(result);
        assertEquals(attemptId, result.getAttemptId());
        assertNotNull(result.getPercentage());
        assertNotNull(result.getCorrectCount());
        assertEquals(3, result.getTotalQuestions());
        assertNotNull(result.getOverallFeedback());
        assertEquals(new BigDecimal("0.500"), result.getMasteryBefore());
        assertEquals(new BigDecimal("0.600"), result.getMasteryAfter());
    }

    @Test
    void submitAttempt_NotOwner_ThrowsException() {
        // Given
        Long attemptId = 100L;
        Long differentStudentId = 999L;

        QuizAttempt attempt = new QuizAttempt(1L, differentStudentId);
        attempt.setId(attemptId);
        attempt.setStatus(QuizAttempt.AttemptStatus.IN_PROGRESS);

        QuizDTO.SubmitAttemptRequest request = QuizDTO.SubmitAttemptRequest.builder()
                .attemptId(attemptId)
                .answers(Collections.emptyList())
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        // When/Then
        BusinessException exception = assertThrows(BusinessException.class, () -> 
                quizService.submitAttempt(studentId, request));
        assertEquals("NOT_OWNER", exception.getErrorCode());
    }

    @Test
    void submitAttempt_AlreadySubmitted_ThrowsException() {
        // Given
        Long attemptId = 100L;

        QuizAttempt attempt = new QuizAttempt(1L, studentId);
        attempt.setId(attemptId);
        attempt.setStatus(QuizAttempt.AttemptStatus.GRADED);

        QuizDTO.SubmitAttemptRequest request = QuizDTO.SubmitAttemptRequest.builder()
                .attemptId(attemptId)
                .answers(Collections.emptyList())
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        // When/Then
        BusinessException exception = assertThrows(BusinessException.class, () -> 
                quizService.submitAttempt(studentId, request));
        assertEquals("ALREADY_SUBMITTED", exception.getErrorCode());
    }

    @Test
    void getQuizHistory_ValidStudent_ReturnsHistory() {
        // Given
        QuizAttempt attempt1 = new QuizAttempt(1L, studentId);
        attempt1.setId(100L);
        attempt1.setSubmittedAt(LocalDateTime.now().minusDays(1));
        attempt1.setPercentage(new BigDecimal("80.00"));
        attempt1.setStatus(QuizAttempt.AttemptStatus.GRADED);

        QuizAttempt attempt2 = new QuizAttempt(2L, studentId);
        attempt2.setId(101L);
        attempt2.setSubmittedAt(LocalDateTime.now().minusDays(2));
        attempt2.setPercentage(new BigDecimal("65.00"));
        attempt2.setStatus(QuizAttempt.AttemptStatus.GRADED);

        Quiz quiz1 = new Quiz();
        quiz1.setId(1L);
        quiz1.setTitle("Quiz 1");
        quiz1.setTopicId(topicId);

        Quiz quiz2 = new Quiz();
        quiz2.setId(2L);
        quiz2.setTitle("Quiz 2");
        quiz2.setTopicId(101L);

        when(attemptRepository.findRecentGradedAttempts(eq(studentId), any()))
                .thenReturn(Arrays.asList(attempt1, attempt2));
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz1));
        when(quizRepository.findById(2L)).thenReturn(Optional.of(quiz2));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(testTopic));
        when(topicRepository.findById(101L)).thenReturn(Optional.empty());
        when(answerRepository.countCorrectAnswers(100L)).thenReturn(4L);
        when(answerRepository.countByAttemptId(100L)).thenReturn(5L);
        when(answerRepository.countCorrectAnswers(101L)).thenReturn(3L);
        when(answerRepository.countByAttemptId(101L)).thenReturn(5L);

        // When
        List<QuizDTO.QuizHistoryItem> result = quizService.getQuizHistory(studentId, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100L, result.get(0).getAttemptId());
        assertEquals(new BigDecimal("80.00"), result.get(0).getPercentage());
    }
}


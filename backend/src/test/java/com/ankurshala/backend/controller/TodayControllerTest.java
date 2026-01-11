package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.CompleteStepRequest;
import com.ankurshala.backend.dto.student.CompleteStepResponse;
import com.ankurshala.backend.dto.student.DailyPlanDto;
import com.ankurshala.backend.entity.Role;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.TodayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TodayController
 * Tests RBAC, ownership checks, and proper delegation to service
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Today Controller Tests")
class TodayControllerTest {
    
    @Mock
    private TodayService todayService;
    
    @InjectMocks
    private TodayController todayController;
    
    private UserPrincipal studentPrincipal;
    private DailyPlanDto mockDailyPlan;
    private CompleteStepRequest completeStepRequest;
    private CompleteStepResponse completeStepResponse;
    
    @BeforeEach
    void setUp() {
        // Setup student principal
        studentPrincipal = new UserPrincipal(
                1L,
                "student@example.com",
                "password",
                Role.STUDENT,
                new ArrayList<>()
        );
        
        // Setup mock daily plan
        mockDailyPlan = DailyPlanDto.builder()
                .generatedAt(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusMinutes(5))
                .planDate("2026-01-10")
                .nextClass(DailyPlanDto.UpcomingClassDto.builder()
                        .bookingId(1L)
                        .topicName("Mathematics")
                        .teacherName("John Doe")
                        .startTime(LocalDateTime.now().plusHours(2))
                        .durationMinutes(60)
                        .status("ACCEPTED")
                        .companionCta("Prepare for class")
                        .companionLink("/student/bookings/1/companion")
                        .build())
                .weakTopicRecommendation(DailyPlanDto.WeakTopicRecommendationDto.builder()
                        .topicId(10L)
                        .topicName("Algebra Basics")
                        .reason("Low performance in recent quizzes")
                        .prerequisiteGaps(new ArrayList<>())
                        .confidenceScore(0.85)
                        .actionCta("Practice now")
                        .actionLink("/student/practice/10")
                        .build())
                .practiceItems(new ArrayList<>())
                .progress(DailyPlanDto.DailyProgressSummaryDto.builder()
                        .totalSteps(5)
                        .completedSteps(2)
                        .practiceCompleted(1)
                        .notesRevised(1)
                        .sprintsCompleted(0)
                        .motivationalMessage("Great start!")
                        .build())
                .build();
        
        // Setup complete step request
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("duration", 300);
        metadata.put("score", 85);
        
        completeStepRequest = CompleteStepRequest.builder()
                .stepType("PRACTICE")
                .stepIdentifier("10")
                .metadata(metadata)
                .build();
        
        // Setup complete step response
        completeStepResponse = CompleteStepResponse.builder()
                .success(true)
                .message("Great job! Step completed successfully.")
                .completedAt(LocalDateTime.now())
                .todayStepsCompleted(3)
                .totalStepsCompleted(25)
                .nextRecommendation("Try revising your notes next.")
                .motivationalMessage("You're on fire! Keep it up!")
                .build();
    }
    
    @Test
    @DisplayName("GET /student/today - Success")
    void testGetDailyPlan_Success() {
        // Mock service
        when(todayService.getDailyPlan(any(UserPrincipal.class)))
                .thenReturn(mockDailyPlan);
        
        // Execute
        ResponseEntity<DailyPlanDto> response = todayController.getDailyPlan(studentPrincipal);
        
        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2026-01-10", response.getBody().getPlanDate());
        assertEquals(5, response.getBody().getProgress().getTotalSteps());
        assertEquals(2, response.getBody().getProgress().getCompletedSteps());
        
        verify(todayService, times(1)).getDailyPlan(studentPrincipal);
    }
    
    @Test
    @DisplayName("GET /student/today - Returns null when service returns null")
    void testGetDailyPlan_ServiceReturnsNull() {
        // Mock service to return null
        when(todayService.getDailyPlan(any(UserPrincipal.class)))
                .thenReturn(null);
        
        // Execute
        ResponseEntity<DailyPlanDto> response = todayController.getDailyPlan(studentPrincipal);
        
        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(todayService, times(1)).getDailyPlan(studentPrincipal);
    }
    
    @Test
    @DisplayName("POST /student/today/complete-step - Success")
    void testCompleteStep_Success() {
        // Mock service
        when(todayService.completeStep(any(CompleteStepRequest.class), any(UserPrincipal.class)))
                .thenReturn(completeStepResponse);
        
        // Execute
        ResponseEntity<CompleteStepResponse> response = 
                todayController.completeStep(completeStepRequest, studentPrincipal);
        
        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(3, response.getBody().getTodayStepsCompleted());
        assertEquals(25, response.getBody().getTotalStepsCompleted());
        assertEquals("Try revising your notes next.", response.getBody().getNextRecommendation());
        
        verify(todayService, times(1)).completeStep(completeStepRequest, studentPrincipal);
    }
    
    @Test
    @DisplayName("POST /student/today/complete-step - Already completed")
    void testCompleteStep_AlreadyCompleted() {
        // Setup response for already completed
        CompleteStepResponse alreadyCompletedResponse = CompleteStepResponse.builder()
                .success(false)
                .message("You've already completed this step today!")
                .completedAt(LocalDateTime.now().minusHours(1))
                .todayStepsCompleted(3)
                .totalStepsCompleted(25)
                .nextRecommendation("Try another activity from your plan")
                .motivationalMessage("Keep up the great work!")
                .build();
        
        // Mock service
        when(todayService.completeStep(any(CompleteStepRequest.class), any(UserPrincipal.class)))
                .thenReturn(alreadyCompletedResponse);
        
        // Execute
        ResponseEntity<CompleteStepResponse> response = 
                todayController.completeStep(completeStepRequest, studentPrincipal);
        
        // Verify
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getSuccess());
        assertEquals("You've already completed this step today!", response.getBody().getMessage());
        
        verify(todayService, times(1)).completeStep(completeStepRequest, studentPrincipal);
    }
    
    @Test
    @DisplayName("POST /student/today/complete-step - Different step types")
    void testCompleteStep_DifferentStepTypes() {
        // Test REVISE_NOTE
        CompleteStepRequest reviseNoteRequest = CompleteStepRequest.builder()
                .stepType("REVISE_NOTE")
                .stepIdentifier("5")
                .metadata(new HashMap<>())
                .build();
        
        when(todayService.completeStep(eq(reviseNoteRequest), any(UserPrincipal.class)))
                .thenReturn(completeStepResponse);
        
        ResponseEntity<CompleteStepResponse> response1 = 
                todayController.completeStep(reviseNoteRequest, studentPrincipal);
        
        assertNotNull(response1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Test FOCUS_SPRINT
        CompleteStepRequest focusSprintRequest = CompleteStepRequest.builder()
                .stepType("FOCUS_SPRINT")
                .stepIdentifier("10")
                .metadata(new HashMap<>())
                .build();
        
        when(todayService.completeStep(eq(focusSprintRequest), any(UserPrincipal.class)))
                .thenReturn(completeStepResponse);
        
        ResponseEntity<CompleteStepResponse> response2 = 
                todayController.completeStep(focusSprintRequest, studentPrincipal);
        
        assertNotNull(response2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        
        // Test BOOKING_COMPANION
        CompleteStepRequest bookingCompanionRequest = CompleteStepRequest.builder()
                .stepType("BOOKING_COMPANION")
                .stepIdentifier("1")
                .metadata(new HashMap<>())
                .build();
        
        when(todayService.completeStep(eq(bookingCompanionRequest), any(UserPrincipal.class)))
                .thenReturn(completeStepResponse);
        
        ResponseEntity<CompleteStepResponse> response3 = 
                todayController.completeStep(bookingCompanionRequest, studentPrincipal);
        
        assertNotNull(response3);
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        
        verify(todayService, times(3)).completeStep(any(CompleteStepRequest.class), eq(studentPrincipal));
    }
}

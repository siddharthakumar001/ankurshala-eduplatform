package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.CompleteStepRequest;
import com.ankurshala.backend.dto.student.CompleteStepResponse;
import com.ankurshala.backend.dto.student.DailyPlanDto;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Today Service - Generates personalized daily plans for students
 * with caching and intelligent recommendations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TodayService {
    
    private final DailyPlanProgressRepository dailyPlanProgressRepository;
    private final WeakTopicRecommendationRepository weakTopicRecommendationRepository;
    private final DailyPracticeQueueRepository dailyPracticeQueueRepository;
    private final BookingRepository bookingRepository;
    private final TopicRepository topicRepository;
    private final StudyListService studyListService;
    private final ObjectMapper objectMapper;
    
    private static final String DAILY_PLAN_CACHE = "dailyPlan";
    private static final int CACHE_TTL_MINUTES = 5;
    
    /**
     * Get the daily plan for a student
     * Cached for 5 minutes per student
     */
    @Cacheable(value = DAILY_PLAN_CACHE, key = "#userPrincipal.id", unless = "#result == null")
    public DailyPlanDto getDailyPlan(UserPrincipal userPrincipal) {
        log.info("Generating daily plan for student ID: {}", userPrincipal.getId());
        
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        
        // Build the daily plan
        DailyPlanDto dailyPlan = DailyPlanDto.builder()
                .generatedAt(now)
                .validUntil(now.plusMinutes(CACHE_TTL_MINUTES))
                .planDate(today.format(DateTimeFormatter.ISO_DATE))
                .nextClass(getNextClass(userPrincipal.getId()))
                .weakTopicRecommendation(getWeakTopicRecommendation(userPrincipal.getId()))
                .practiceItems(getPracticeItems(userPrincipal.getId(), today))
                .reviseNoteSuggestion(getReviseNoteSuggestion(userPrincipal.getId()))
                .focusSprint(getFocusSprint(userPrincipal.getId()))
                .progress(getDailyProgress(userPrincipal.getId(), today))
                .build();
        
        log.info("Daily plan generated successfully for student ID: {}", userPrincipal.getId());
        return dailyPlan;
    }
    
    /**
     * Complete a step in the daily plan
     * Invalidates cache to force refresh
     */
    @CacheEvict(value = DAILY_PLAN_CACHE, key = "#userPrincipal.id")
    @Transactional
    public CompleteStepResponse completeStep(CompleteStepRequest request, UserPrincipal userPrincipal) {
        log.info("Completing step {} for student ID: {}", request.getStepType(), userPrincipal.getId());
        
        LocalDate today = LocalDate.now();
        Long studentId = userPrincipal.getId();
        
        // Validate step type
        validateStepType(request.getStepType());
        
        // Check if step already completed today
        var existingProgress = dailyPlanProgressRepository
                .findByStudentIdAndPlanDateAndStepTypeAndStepIdentifier(
                        studentId, today, request.getStepType(), request.getStepIdentifier()
                );
        
        if (existingProgress.isPresent()) {
            log.warn("Step already completed today for student {}: {}", studentId, request.getStepType());
            return buildAlreadyCompletedResponse(existingProgress.get());
        }
        
        // Save progress
        DailyPlanProgress progress = DailyPlanProgress.builder()
                .studentId(studentId)
                .planDate(today)
                .stepType(request.getStepType())
                .stepIdentifier(request.getStepIdentifier())
                .completedAt(LocalDateTime.now())
                .completionMetadata(request.getMetadata())
                .build();
        
        dailyPlanProgressRepository.save(progress);
        
        // Mark practice queue item as completed if applicable
        if ("PRACTICE".equals(request.getStepType())) {
            markPracticeCompleted(studentId, request.getStepIdentifier(), today);
        }
        
        // Get updated counts
        Long todayCount = dailyPlanProgressRepository.countByStudentIdAndPlanDate(studentId, today);
        Long totalCount = dailyPlanProgressRepository.countByStudentId(studentId);
        
        log.info("Step completed successfully for student {}: {} steps today, {} total", 
                 studentId, todayCount, totalCount);
        
        return CompleteStepResponse.builder()
                .success(true)
                .message("Great job! Step completed successfully.")
                .completedAt(LocalDateTime.now())
                .todayStepsCompleted(todayCount.intValue())
                .totalStepsCompleted(totalCount.intValue())
                .nextRecommendation(getNextRecommendation(studentId, request.getStepType()))
                .motivationalMessage(getMotivationalMessage(todayCount.intValue()))
                .build();
    }
    
    /**
     * Get next upcoming class
     */
    private DailyPlanDto.UpcomingClassDto getNextClass(Long studentId) {
        try {
            User student = new User();
            student.setId(studentId);
            
            var upcomingBookings = bookingRepository.findUpcomingByStudent(
                    student, 
                    ZonedDateTime.now()
            );
            
            if (upcomingBookings.isEmpty()) {
                return null;
            }
            
            Booking booking = upcomingBookings.get(0);
            
            return DailyPlanDto.UpcomingClassDto.builder()
                    .bookingId(booking.getId())
                    .topicName(booking.getTopic() != null ? booking.getTopic().getTitle() : "Class")
                    .teacherName(booking.getTeacher() != null ? booking.getTeacher().getName() : "Teacher")
                    .startTime(booking.getStartTs().toLocalDateTime())
                    .durationMinutes(booking.getDurationMinutes())
                    .status(booking.getStatus().name())
                    .companionCta(getBookingCta(booking))
                    .companionLink("/student/bookings/" + booking.getId() + "/companion")
                    .build();
        } catch (Exception e) {
            log.error("Error getting next class for student {}: {}", studentId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get weak topic recommendation
     */
    private DailyPlanDto.WeakTopicRecommendationDto getWeakTopicRecommendation(Long studentId) {
        try {
            var recommendation = weakTopicRecommendationRepository
                    .findTopRecommendation(studentId, LocalDateTime.now())
                    .orElse(null);
            
            if (recommendation == null) {
                return null;
            }
            
            Topic topic = topicRepository.findById(recommendation.getTopicId()).orElse(null);
            if (topic == null) {
                return null;
            }
            
            List<String> prereqGaps = parsePrerequisiteGaps(recommendation.getPrerequisiteGaps());
            
            return DailyPlanDto.WeakTopicRecommendationDto.builder()
                    .topicId(recommendation.getTopicId())
                    .topicName(topic.getTitle())
                    .reason(recommendation.getRecommendationReason())
                    .prerequisiteGaps(prereqGaps)
                    .confidenceScore(recommendation.getConfidenceScore().doubleValue())
                    .actionCta(prereqGaps.isEmpty() ? "Practice now" : "Learn basics first")
                    .actionLink("/student/practice/" + recommendation.getTopicId())
                    .build();
        } catch (Exception e) {
            log.error("Error getting weak topic recommendation for student {}: {}", studentId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get practice items for today
     */
    private List<DailyPlanDto.PracticeItemDto> getPracticeItems(Long studentId, LocalDate today) {
        try {
            var queueItems = dailyPracticeQueueRepository
                    .findByStudentIdAndScheduledForDateOrderByCreatedAtAsc(studentId, today);
            
            return queueItems.stream()
                    .filter(item -> item.getStatus() == DailyPracticeQueue.PracticeStatus.PENDING)
                    .limit(3) // Show top 3 practice items
                    .map(item -> {
                        Topic topic = topicRepository.findById(item.getTopicId()).orElse(null);
                        if (topic == null) return null;
                        
                        // Determine source based on creation pattern (simplified)
                        String source = "ADAPTIVE";
                        boolean isDueForReview = false;
                        int priority = 5; // Medium priority by default
                        
                        return DailyPlanDto.PracticeItemDto.builder()
                                .topicId(item.getTopicId())
                                .topicName(topic.getTitle())
                                .source(source)
                                .isDueForReview(isDueForReview)
                                .priority(priority)
                                .difficulty(determineDifficulty(priority))
                                .actionCta("Practice now")
                                .actionLink("/student/practice/" + item.getTopicId())
                                .build();
                    })
                    .filter(item -> item != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting practice items for student {}: {}", studentId, e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get revise note suggestion
     */
    private DailyPlanDto.ReviseNoteSuggestionDto getReviseNoteSuggestion(Long studentId) {
        try {
            // This would integrate with StudyListService to get recent notes
            // For now, return null as notes system integration is a future enhancement
            return null;
        } catch (Exception e) {
            log.error("Error getting revise note suggestion for student {}: {}", studentId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get focus sprint suggestion
     */
    private DailyPlanDto.FocusSprintDto getFocusSprint(Long studentId) {
        try {
            // Get a topic from weak recommendations or practice queue
            var recommendation = weakTopicRecommendationRepository
                    .findTopRecommendation(studentId, LocalDateTime.now())
                    .orElse(null);
            
            if (recommendation != null) {
                Topic topic = topicRepository.findById(recommendation.getTopicId()).orElse(null);
                if (topic != null) {
                    return DailyPlanDto.FocusSprintDto.builder()
                            .topicId(topic.getId())
                            .topicName(topic.getTitle())
                            .defaultDurationMinutes(15)
                            .description("Focused 15-minute practice session on " + topic.getTitle())
                            .actionCta("Start 15-min sprint")
                            .actionLink("/student/sprint/" + topic.getId())
                            .build();
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error getting focus sprint for student {}: {}", studentId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get daily progress summary
     */
    private DailyPlanDto.DailyProgressSummaryDto getDailyProgress(Long studentId, LocalDate today) {
        try {
            var todayProgress = dailyPlanProgressRepository
                    .findByStudentIdAndPlanDateOrderByCompletedAtDesc(studentId, today);
            
            int total = 5; // Default total steps (practice, revise, sprint, etc.)
            int completed = todayProgress.size();
            
            // Count specific types
            long practiceCompleted = todayProgress.stream()
                    .filter(p -> "PRACTICE".equals(p.getStepType()))
                    .count();
            long notesRevised = todayProgress.stream()
                    .filter(p -> "REVISE_NOTE".equals(p.getStepType()))
                    .count();
            long sprintsCompleted = todayProgress.stream()
                    .filter(p -> "FOCUS_SPRINT".equals(p.getStepType()))
                    .count();
            
            return DailyPlanDto.DailyProgressSummaryDto.builder()
                    .totalSteps(total)
                    .completedSteps(completed)
                    .practiceCompleted((int) practiceCompleted)
                    .notesRevised((int) notesRevised)
                    .sprintsCompleted((int) sprintsCompleted)
                    .motivationalMessage(getMotivationalMessage(completed))
                    .build();
        } catch (Exception e) {
            log.error("Error getting daily progress for student {}: {}", studentId, e.getMessage());
            return DailyPlanDto.DailyProgressSummaryDto.builder()
                    .totalSteps(5)
                    .completedSteps(0)
                    .practiceCompleted(0)
                    .notesRevised(0)
                    .sprintsCompleted(0)
                    .motivationalMessage("Let's start your learning journey today!")
                    .build();
        }
    }
    
    // Helper methods
    
    private void validateStepType(String stepType) {
        List<String> validTypes = List.of("PRACTICE", "REVISE_NOTE", "FOCUS_SPRINT", "BOOKING_COMPANION");
        if (!validTypes.contains(stepType)) {
            throw new IllegalArgumentException("Invalid step type: " + stepType);
        }
    }
    
    private CompleteStepResponse buildAlreadyCompletedResponse(DailyPlanProgress existingProgress) {
        Long studentId = existingProgress.getStudentId();
        Long todayCount = dailyPlanProgressRepository.countByStudentIdAndPlanDate(
                studentId, existingProgress.getPlanDate()
        );
        Long totalCount = dailyPlanProgressRepository.countByStudentId(studentId);
        
        return CompleteStepResponse.builder()
                .success(false)
                .message("You've already completed this step today!")
                .completedAt(existingProgress.getCompletedAt())
                .todayStepsCompleted(todayCount.intValue())
                .totalStepsCompleted(totalCount.intValue())
                .nextRecommendation("Try another activity from your plan")
                .motivationalMessage("Keep up the great work!")
                .build();
    }
    
    private void markPracticeCompleted(Long studentId, String topicIdStr, LocalDate today) {
        try {
            Long topicId = Long.parseLong(topicIdStr);
            var queueItems = dailyPracticeQueueRepository
                    .findByStudentIdAndScheduledForDateOrderByCreatedAtAsc(studentId, today);
            
            for (DailyPracticeQueue item : queueItems) {
                if (item.getTopicId().equals(topicId) && 
                    item.getStatus() == DailyPracticeQueue.PracticeStatus.PENDING) {
                    item.setStatus(DailyPracticeQueue.PracticeStatus.COMPLETED);
                    item.setCompletedAt(LocalDateTime.now());
                    dailyPracticeQueueRepository.save(item);
                    log.info("Marked practice queue item as completed: {}", item.getId());
                    break;
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid topic ID format for marking practice completed: {}", topicIdStr);
        }
    }
    
    private String getNextRecommendation(Long studentId, String completedStepType) {
        // Simple recommendation logic
        switch (completedStepType) {
            case "PRACTICE":
                return "Great! Try revising your notes next.";
            case "REVISE_NOTE":
                return "Excellent! How about a quick focus sprint?";
            case "FOCUS_SPRINT":
                return "Amazing! Take a short break, then continue practicing.";
            case "BOOKING_COMPANION":
                return "Perfect! Keep preparing for your upcoming class.";
            default:
                return "Keep going! Check your daily plan for more activities.";
        }
    }
    
    private String getMotivationalMessage(int completedSteps) {
        if (completedSteps == 0) {
            return "Let's start your learning journey today!";
        } else if (completedSteps == 1) {
            return "Great start! Keep the momentum going!";
        } else if (completedSteps == 2) {
            return "You're on fire! Keep it up!";
        } else if (completedSteps >= 3) {
            return "Outstanding progress! You're crushing it today!";
        }
        return "Keep learning and growing!";
    }
    
    private String getBookingCta(Booking booking) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = booking.getStartTs().toLocalDateTime();
        
        if (startTime.minusMinutes(30).isBefore(now) && startTime.isAfter(now)) {
            return "Join soon";
        } else if (startTime.isBefore(now)) {
            return "Join now";
        } else {
            return "Prepare for class";
        }
    }
    
    private String determineDifficulty(Integer priority) {
        if (priority <= 3) return "HARD";
        if (priority <= 6) return "MEDIUM";
        return "EASY";
    }
    
    private List<String> parsePrerequisiteGaps(String prerequisiteGaps) {
        if (prerequisiteGaps == null || prerequisiteGaps.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Simple comma-separated parsing
        return List.of(prerequisiteGaps.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.entity.NotificationStatus;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentDashboardService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TeacherRepository teacherRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationRepository notificationRepository;
    private final FeeWaiverRepository feeWaiverRepository;

    public StudentDashboardDto getStudentDashboard(UserPrincipal userPrincipal) {
        log.info("Getting student dashboard for user ID: {}", userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        // Get dashboard components
        StudentDashboardStatsDto stats = getDashboardStats(student);
        List<StudentUpcomingBookingDto> upcomingBookings = getUpcomingBookings(student);
        List<StudentRecentSessionDto> recentSessions = getRecentSessions(student);
        List<StudentProgressDto> recentProgress = getRecentProgress(student);
        List<StudentNotificationDto> recentNotifications = getRecentNotifications(student);
        StudentLearningAnalyticsDto learningAnalytics = getLearningAnalytics(student);
        
        return new StudentDashboardDto() {{
            setStats(stats);
            setUpcomingBookings(upcomingBookings);
            setRecentSessions(recentSessions);
            setRecentProgress(recentProgress);
            setRecentNotifications(recentNotifications);
            setLearningAnalytics(learningAnalytics);
        }};
    }

    private StudentDashboardStatsDto getDashboardStats(User student) {
        // Get all bookings for the student
        List<Booking> allBookings = bookingRepository.findByStudentOrderByStartTsDesc(student, 
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        
        int totalBookings = allBookings.size();
        int upcomingBookings = (int) allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED && 
                           b.getStartTs().isAfter(ZonedDateTime.now()))
                .count();
        int completedSessions = (int) allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .count();
        int cancelledSessions = (int) allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();
        
        // Calculate total hours spent
        int totalHoursSpent = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .mapToInt(Booking::getDurationMinutes)
                .sum() / 60;
        
        // Calculate total amount spent (mock for now)
        BigDecimal totalAmountSpent = BigDecimal.valueOf(completedSessions * 500); // Mock calculation
        BigDecimal monthlySpending = BigDecimal.valueOf(
                allBookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.COMPLETED && 
                                   b.getStartTs().isAfter(ZonedDateTime.now().minusMonths(1)))
                        .count() * 500
        );
        
        // Get unique teachers
        Set<Long> uniqueTeachers = allBookings.stream()
                .map(b -> b.getTeacher().getId())
                .collect(Collectors.toSet());
        int totalTeachers = uniqueTeachers.size();
        int activeTeachers = (int) allBookings.stream()
                .filter(b -> b.getStartTs().isAfter(ZonedDateTime.now().minusMonths(3)))
                .map(b -> b.getTeacher().getId())
                .collect(Collectors.toSet()).size();
        
        // Get next class info
        Optional<Booking> nextClass = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.ACCEPTED && 
                           b.getStartTs().isAfter(ZonedDateTime.now()))
                .min(Comparator.comparing(Booking::getStartTs));
        
        LocalDateTime nextClassTime = nextClass.map(b -> b.getStartTs().toLocalDateTime()).orElse(null);
        String nextClassTopic = nextClass.map(b -> b.getTopic().getTitle()).orElse(null);
        String nextClassTeacher = nextClass.map(b -> b.getTeacher().getName()).orElse(null);
        boolean hasUpcomingClass = nextClass.isPresent();
        
        // Calculate completion rate (mock for now)
        int completedTopics = (int) allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(b -> b.getTopic().getId())
                .collect(Collectors.toSet()).size();
        int totalTopics = (int) allBookings.stream()
                .map(b -> b.getTopic().getId())
                .collect(Collectors.toSet()).size();
        double completionRate = totalTopics > 0 ? (double) completedTopics / totalTopics * 100 : 0;
        
        return new StudentDashboardStatsDto() {{
            setTotalBookings(totalBookings);
            setUpcomingBookings(upcomingBookings);
            setCompletedSessions(completedSessions);
            setCancelledSessions(cancelledSessions);
            setTotalHoursSpent(totalHoursSpent);
            setTotalAmountSpent(totalAmountSpent);
            setMonthlySpending(monthlySpending);
            setAverageRating(4.5); // Mock rating
            setTotalTeachers(totalTeachers);
            setActiveTeachers(activeTeachers);
            setCompletedTopics(completedTopics);
            setTotalTopics(totalTopics);
            setCompletionRate(completionRate);
            setNextClassTime(nextClassTime);
            setNextClassTopic(nextClassTopic);
            setNextClassTeacher(nextClassTeacher);
            setHasUpcomingClass(hasUpcomingClass);
        }};
    }

    private List<StudentUpcomingBookingDto> getUpcomingBookings(User student) {
        List<Booking> upcomingBookings = bookingRepository.findUpcomingByStudent(student, ZonedDateTime.now());
        
        return upcomingBookings.stream()
                .limit(10) // Limit to 10 upcoming bookings
                .map(this::convertToUpcomingBookingDto)
                .collect(Collectors.toList());
    }

    private List<StudentRecentSessionDto> getRecentSessions(User student) {
        List<Booking> recentSessions = bookingRepository.findHistoryByStudent(student, ZonedDateTime.now());
        
        return recentSessions.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .limit(10) // Limit to 10 recent sessions
                .map(this::convertToRecentSessionDto)
                .collect(Collectors.toList());
    }

    private List<StudentProgressDto> getRecentProgress(User student) {
        // Get recent bookings and calculate progress
        List<Booking> recentBookings = bookingRepository.findByStudentOrderByStartTsDesc(student, 
                org.springframework.data.domain.PageRequest.of(0, 50)).getContent();
        
        Map<Long, List<Booking>> topicBookings = recentBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getTopic().getId()));
        
        return topicBookings.entrySet().stream()
                .limit(10) // Limit to 10 topics
                .map(entry -> {
                    Long topicId = entry.getKey();
                    List<Booking> bookings = entry.getValue();
                    
                    Topic topic = topicRepository.findById(topicId).orElse(null);
                    if (topic == null) return null;
                    
                    int sessionsCompleted = (int) bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                            .count();
                    int totalSessions = bookings.size();
                    double completionPercentage = totalSessions > 0 ? (double) sessionsCompleted / totalSessions * 100 : 0;
                    
                    Optional<Booking> lastSession = bookings.stream()
                            .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                            .max(Comparator.comparing(Booking::getStartTs));
                    
                    String progressStatus = sessionsCompleted == 0 ? "NOT_STARTED" : 
                                          sessionsCompleted == totalSessions ? "COMPLETED" : "IN_PROGRESS";
                    
                    return new StudentProgressDto() {{
                        setId(topicId);
                        setTopicId(topicId);
                        setTopicTitle(topic.getTitle());
                        setSubjectName(topic.getChapter().getSubject().getName());
                        setChapterName(topic.getChapter().getName());
                        setSessionsCompleted(sessionsCompleted);
                        setTotalSessions(totalSessions);
                        setCompletionPercentage(completionPercentage);
                        setLastSessionDate(lastSession.map(b -> b.getStartTs().toLocalDateTime()).orElse(null));
                        setLastSessionTeacher(lastSession.map(b -> b.getTeacher().getName()).orElse(null));
                        setProgressStatus(progressStatus);
                        setCreatedAt(topic.getCreatedAt());
                        setUpdatedAt(LocalDateTime.now());
                    }};
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<StudentNotificationDto> getRecentNotifications(User student) {
        // Get recent notifications for the student
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(student.getId(), 
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        
        return notifications.stream()
                .map(this::convertToNotificationDto)
                .collect(Collectors.toList());
    }

    private StudentLearningAnalyticsDto getLearningAnalytics(User student) {
        // Get all completed bookings for analytics
        List<Booking> completedBookings = bookingRepository.findByStudentOrderByStartTsDesc(student, 
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent()
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());
        
        // Calculate overall progress
        Set<Long> uniqueTopics = completedBookings.stream()
                .map(b -> b.getTopic().getId())
                .collect(Collectors.toSet());
        double overallProgress = uniqueTopics.size() * 10.0; // Mock calculation
        
        // Calculate subject progress
        Map<String, Integer> subjectProgress = completedBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getTopic().getChapter().getSubject().getName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // Calculate monthly sessions
        Map<String, Integer> monthlySessions = completedBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getStartTs().getMonth().toString(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        
        // Mock learning streaks
        List<StudentStreakDto> learningStreaks = generateMockStreaks();
        
        // Mock performance trend
        StudentPerformanceTrendDto performanceTrend = generateMockPerformanceTrend();
        
        // Mock achievements
        List<StudentAchievementDto> recentAchievements = generateMockAchievements();
        
        return new StudentLearningAnalyticsDto() {{
            setOverallProgress(overallProgress);
            setTotalTopicsStudied(uniqueTopics.size());
            setTotalSessionsCompleted(completedBookings.size());
            setAverageSessionRating(4.5); // Mock rating
            setSubjectProgress(subjectProgress);
            setMonthlySessions(monthlySessions);
            setLearningStreaks(learningStreaks);
            setPerformanceTrend(performanceTrend);
            setRecentAchievements(recentAchievements);
        }};
    }

    // Conversion methods
    private StudentUpcomingBookingDto convertToUpcomingBookingDto(Booking booking) {
        return new StudentUpcomingBookingDto() {{
            setId(booking.getId());
            setTopicId(booking.getTopic().getId());
            setTopicTitle(booking.getTopic().getTitle());
            setSubjectName(booking.getTopic().getChapter().getSubject().getName());
            setChapterName(booking.getTopic().getChapter().getName());
            setTeacherId(booking.getTeacher().getId());
            setTeacherName(booking.getTeacher().getName());
            setTeacherEmail(booking.getTeacher().getEmail());
            setStartTime(booking.getStartTs().toLocalDateTime());
            setEndTime(booking.getEndTs().toLocalDateTime());
            setDurationMinutes(booking.getDurationMinutes());
            setStatus(booking.getStatus().toString());
            setPriceMin(BigDecimal.valueOf(500)); // Mock price
            setPriceMax(BigDecimal.valueOf(500)); // Mock price
            setStudentNotes(booking.getStudentNotes());
            setCanJoin(booking.getStatus() == BookingStatus.ACCEPTED && 
                      booking.getStartTs().isBefore(ZonedDateTime.now().plusMinutes(15)));
            setCanCancel(booking.getStatus() == BookingStatus.ACCEPTED && 
                        booking.getStartTs().isAfter(ZonedDateTime.now().plusHours(2)));
            setCanReschedule(booking.getStatus() == BookingStatus.ACCEPTED && 
                           booking.getStartTs().isAfter(ZonedDateTime.now().plusHours(24)));
            setCreatedAt(booking.getCreatedAt().toLocalDateTime());
        }};
    }

    private StudentRecentSessionDto convertToRecentSessionDto(Booking booking) {
        return new StudentRecentSessionDto() {{
            setId(booking.getId());
            setTopicId(booking.getTopic().getId());
            setTopicTitle(booking.getTopic().getTitle());
            setSubjectName(booking.getTopic().getChapter().getSubject().getName());
            setChapterName(booking.getTopic().getChapter().getName());
            setTeacherId(booking.getTeacher().getId());
            setTeacherName(booking.getTeacher().getName());
            setStartTime(booking.getStartTs().toLocalDateTime());
            setEndTime(booking.getEndTs().toLocalDateTime());
            setDurationMinutes(booking.getDurationMinutes());
            setStatus(booking.getStatus().toString());
            setAmountPaid(BigDecimal.valueOf(500)); // Mock amount
            setStudentRating(5); // Mock rating
            setStudentFeedback("Great session!"); // Mock feedback
            setTeacherFeedback("Student was engaged"); // Mock feedback
            setSessionNotes(booking.getTeacherNotes());
            setHasFeedback(true); // Mock
            setCompletedAt(booking.getEndTs().toLocalDateTime());
        }};
    }

    private StudentNotificationDto convertToNotificationDto(Notification notification) {
        return new StudentNotificationDto() {{
            setId(notification.getId());
            setTitle(notification.getTitle());
            setMessage(notification.getBody());
            setType(notification.getAudience().toString());
            setRead(notification.getStatus() == NotificationStatus.READ);
            setCreatedAt(notification.getCreatedAt());
            setActionUrl("/student/bookings");
            setActionText("View Details");
        }};
    }

    // Mock data generators
    private List<StudentStreakDto> generateMockStreaks() {
        List<StudentStreakDto> streaks = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            final int finalI = i;
            final LocalDate date = LocalDate.now().minusDays(i);
            streaks.add(new StudentStreakDto() {{
                setDate(date);
                setSessionsCompleted(finalI % 2 == 0 ? 1 : 0);
                setActive(finalI < 3);
            }});
        }
        return streaks;
    }

    private StudentPerformanceTrendDto generateMockPerformanceTrend() {
        List<StudentPerformanceDataPoint> weeklyTrend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            final int finalI = i;
            final LocalDate date = LocalDate.now().minusDays(i);
            weeklyTrend.add(new StudentPerformanceDataPoint() {{
                setDate(date);
                setAverageRating(4.0 + (finalI % 3) * 0.5);
                setSessionsCompleted(finalI % 2 == 0 ? 1 : 0);
                setCompletionRate(80.0 + (finalI % 3) * 10);
            }});
        }
        
        return new StudentPerformanceTrendDto() {{
            setWeeklyTrend(weeklyTrend);
            setMonthlyTrend(weeklyTrend); // Same for mock
            setImprovementRate(5.2);
            setTrendDirection("IMPROVING");
        }};
    }

    private List<StudentAchievementDto> generateMockAchievements() {
        return Arrays.asList(
                new StudentAchievementDto() {{
                    setId(1L);
                    setTitle("First Session Complete");
                    setDescription("Completed your first learning session");
                    setType("MILESTONE");
                    setIconUrl("/icons/first-session.png");
                    setEarnedAt(LocalDateTime.now().minusDays(5));
                    setNew(false);
                }},
                new StudentAchievementDto() {{
                    setId(2L);
                    setTitle("Week Streak");
                    setDescription("Completed sessions for 7 consecutive days");
                    setType("STREAK");
                    setIconUrl("/icons/week-streak.png");
                    setEarnedAt(LocalDateTime.now().minusDays(1));
                    setNew(true);
                }}
        );
    }
}

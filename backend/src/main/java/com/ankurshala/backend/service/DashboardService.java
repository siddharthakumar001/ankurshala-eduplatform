package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.dashboard.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class DashboardService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private StudentQuizResultRepository studentQuizResultRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private TeacherSubjectExpertiseRepository teacherSubjectExpertiseRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    public StudentDashboardStats getStudentDashboardStats(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting student dashboard stats - TraceId: {}, StudentId: {}", traceId, studentId);

        try {
            StudentDashboardStats stats = new StudentDashboardStats();

            // Get upcoming bookings count
            List<Booking> upcomingBookings = bookingRepository.findByStudentIdOrderByStartTsDesc(studentId).stream()
                    .filter(b -> b.getStartTs().isAfter(ZonedDateTime.now()))
                    .filter(b -> EnumSet.of(
                            BookingStatus.PENDING,
                            BookingStatus.ACCEPTED,
                            BookingStatus.CONFIRMED,
                            BookingStatus.IN_PROGRESS
                    ).contains(b.getStatus()))
                    .collect(Collectors.toList());
            stats.setUpcomingBookings((long) upcomingBookings.size());

            // Get completed bookings count
            List<Booking> completedBookings = bookingRepository.findByStudentIdAndStatus(studentId, BookingStatus.COMPLETED);
            stats.setCompletedBookings((long) completedBookings.size());

            // Calculate total hours spent
            long totalMinutes = completedBookings.stream()
                    .mapToLong(b -> java.time.Duration.between(b.getStartTs(), b.getEndTs()).toMinutes())
                    .sum();
            stats.setTotalHoursSpent(totalMinutes / 60);

            // Get subject mastery
            List<SubjectMastery> subjectMastery = getSubjectMastery(studentId);
            stats.setSubjectMastery(subjectMastery);

            // Get upcoming classes
            List<UpcomingClass> upcomingClasses = upcomingBookings.stream()
                    .map(this::convertToUpcomingClass)
                    .collect(Collectors.toList());
            stats.setUpcomingClasses(upcomingClasses);

            // Get AI recommendations (stub)
            List<TopicRecommendation> recommendations = getTopicRecommendations(studentId);
            stats.setRecommendations(recommendations);

            log.info("Student dashboard stats retrieved - TraceId: {}, Upcoming: {}, Completed: {}", 
                    traceId, stats.getUpcomingBookings(), stats.getCompletedBookings());
            return stats;

        } catch (Exception e) {
            log.error("Failed to get student dashboard stats - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to get student dashboard stats", HttpStatus.INTERNAL_SERVER_ERROR, "DASHBOARD_ERROR");
        }
    }

    public TeacherDashboardStats getTeacherDashboardStats(Long teacherId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting teacher dashboard stats - TraceId: {}, TeacherId: {}", traceId, teacherId);

        try {
            TeacherDashboardStats stats = new TeacherDashboardStats();

            Teacher teacher = teacherRepository.findByUserId(teacherId)
                    .orElseThrow(() -> new BusinessException("Teacher not found", HttpStatus.NOT_FOUND, "TEACHER_NOT_FOUND"));

            // Get pending requests count (only matching teacher expertise)
            List<Booking> pendingBookings = bookingRepository.findByStatus(BookingStatus.PENDING)
                    .stream()
                    .filter(booking -> {
                        if (booking.getTopic() == null) return false;
                        Long subjectId = booking.getTopic().getSubjectId();
                        Long gradeId = booking.getTopic().getGradeId();
                        Long boardId = booking.getTopic().getBoardId();
                        return teacherSubjectExpertiseRepository.hasExpertise(teacher.getId(), subjectId, gradeId, boardId);
                    })
                    .collect(Collectors.toList());
            stats.setPendingRequests((long) pendingBookings.size());

            // Get upcoming classes count
            List<Booking> upcomingBookings = bookingRepository.findByTeacherIdAndStatus(teacherId, BookingStatus.ACCEPTED);
            upcomingBookings = upcomingBookings.stream()
                    .filter(b -> b.getStartTs().isAfter(ZonedDateTime.now()))
                    .collect(Collectors.toList());
            stats.setUpcomingClasses((long) upcomingBookings.size());

            // Get earnings
            BigDecimal earningsMTD = getEarningsThisMonth(teacherId);
            stats.setEarningsMTD(earningsMTD);

            BigDecimal totalEarnings = getTotalEarnings(teacherId);
            stats.setTotalEarnings(totalEarnings);

            stats.setAverageRating(teacher.getRatingAvg() != null ? teacher.getRatingAvg().doubleValue() : 0.0);
            stats.setTotalRatings(teacher.getRatingCount() != null ? teacher.getRatingCount().longValue() : 0L);

            // Get pending bookings details
            List<PendingBooking> pendingBookingsList = pendingBookings.stream()
                    .map(this::convertToPendingBooking)
                    .collect(Collectors.toList());
            stats.setPendingBookings(pendingBookingsList);

            // Get upcoming classes details
            List<UpcomingClass> upcomingClassesList = upcomingBookings.stream()
                    .map(this::convertToTeacherUpcomingClass)
                    .collect(Collectors.toList());
            stats.setUpcomingClassesList(upcomingClassesList);

            // Get earnings breakdown
            EarningsBreakdown earningsBreakdown = getEarningsBreakdown(teacherId);
            stats.setEarningsBreakdown(earningsBreakdown);

            log.info("Teacher dashboard stats retrieved - TraceId: {}, Pending: {}, Upcoming: {}", 
                    traceId, stats.getPendingRequests(), stats.getUpcomingClasses());
            return stats;

        } catch (Exception e) {
            log.error("Failed to get teacher dashboard stats - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to get teacher dashboard stats", HttpStatus.INTERNAL_SERVER_ERROR, "DASHBOARD_ERROR");
        }
    }

    public TeacherEarningsResponse getTeacherEarnings(Long teacherId, LocalDateTime fromDate, LocalDateTime toDate) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting teacher earnings - TraceId: {}, TeacherId: {}, From: {}, To: {}", 
                traceId, teacherId, fromDate, toDate);

        try {
            TeacherEarningsResponse response = new TeacherEarningsResponse();

            // Get earnings transactions
            List<WalletTransaction> earnings = walletTransactionRepository
                    .findByOwnerTypeAndOwnerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                            WalletOwnerType.TEACHER, teacherId, fromDate, toDate);

            // Calculate totals
            BigDecimal totalEarnings = earnings.stream()
                    .filter(t -> t.getType() == WalletTransactionType.CREDIT && t.getSource() == WalletTransactionSource.EARNING)
                    .map(t -> BigDecimal.valueOf(t.getAmountCents()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.setTotalEarnings(totalEarnings);

            // Calculate this month earnings
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = LocalDateTime.now();
            BigDecimal thisMonthEarnings = getEarningsInPeriod(teacherId, startOfMonth, endOfMonth);
            response.setThisMonthEarnings(thisMonthEarnings);

            // Calculate last month earnings
            LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
            LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);
            BigDecimal lastMonthEarnings = getEarningsInPeriod(teacherId, startOfLastMonth, endOfLastMonth);
            response.setLastMonthEarnings(lastMonthEarnings);

            // Get earnings by period
            List<EarningsPeriod> earningsByPeriod = getEarningsByPeriod(teacherId, fromDate, toDate);
            response.setEarningsByPeriod(earningsByPeriod);

            // Get earnings by subject
            List<EarningsBySubject> earningsBySubject = getEarningsBySubject(teacherId, fromDate, toDate);
            response.setEarningsBySubject(earningsBySubject);

            // Calculate averages
            long totalClasses = earningsBySubject.stream().mapToLong(EarningsBySubject::getClassCount).sum();
            response.setTotalClasses(totalClasses);
            response.setAveragePerClass(totalClasses > 0 ? 
                    totalEarnings.divide(BigDecimal.valueOf(totalClasses), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

            log.info("Teacher earnings retrieved - TraceId: {}, Total: {}", traceId, totalEarnings);
            return response;

        } catch (Exception e) {
            log.error("Failed to get teacher earnings - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw new BusinessException("Failed to get teacher earnings", HttpStatus.INTERNAL_SERVER_ERROR, "EARNINGS_ERROR");
        }
    }

    // Helper methods
    private List<SubjectMastery> getSubjectMastery(Long studentId) {
        List<StudentQuizResult> quizResults = studentQuizResultRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        
        Map<Long, List<StudentQuizResult>> resultsBySubject = quizResults.stream()
                .collect(Collectors.groupingBy(StudentQuizResult::getSubjectId));

        return resultsBySubject.entrySet().stream()
                .map(entry -> {
                    Long subjectId = entry.getKey();
                    List<StudentQuizResult> results = entry.getValue();
                    
                    SubjectMastery mastery = new SubjectMastery();
                    mastery.setSubjectId(subjectId);
                    
                    // Get subject name
                    Subject subject = subjectRepository.findById(subjectId).orElse(null);
                    mastery.setSubjectName(subject != null ? subject.getName() : "Unknown Subject");
                    
                    // Calculate mastery
                    Double averageScore = studentQuizResultRepository.getAverageScoreByStudentAndSubject(studentId, subjectId);
                    mastery.setMasteryPercentage(averageScore != null ? averageScore : 0.0);
                    mastery.setTotalQuizzes((long) results.size());
                    mastery.setAverageScore(averageScore != null ? averageScore : 0.0);
                    
                    return mastery;
                })
                .collect(Collectors.toList());
    }

    private List<TopicRecommendation> getTopicRecommendations(Long studentId) {
        // Stub implementation - in real scenario, this would use AI service
        List<TopicRecommendation> recommendations = new ArrayList<>();
        
        TopicRecommendation rec1 = new TopicRecommendation();
        rec1.setTopicId(1L);
        rec1.setTitle("Advanced Mathematics");
        rec1.setDescription("Based on your performance, we recommend this topic");
        rec1.setConfidence(0.85);
        rec1.setReason("Strong performance in basic math concepts");
        recommendations.add(rec1);
        
        return recommendations;
    }

    private UpcomingClass convertToUpcomingClass(Booking booking) {
        UpcomingClass upcomingClass = new UpcomingClass();
        upcomingClass.setBookingId(booking.getId());
        
        Topic topic = topicRepository.findById(booking.getTopicId()).orElse(null);
        upcomingClass.setTopicTitle(topic != null ? topic.getTitle() : "Unknown Topic");
        
        User teacher = userRepository.findById(booking.getTeacherId()).orElse(null);
        upcomingClass.setTeacherName(teacher != null ? teacher.getName() : "TBD");
        
        upcomingClass.setStartTime(booking.getStartTs().toLocalDateTime());
        upcomingClass.setEndTime(booking.getEndTs().toLocalDateTime());
        upcomingClass.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        
        return upcomingClass;
    }

    private PendingBooking convertToPendingBooking(Booking booking) {
        PendingBooking pendingBooking = new PendingBooking();
        pendingBooking.setBookingId(booking.getId());
        
        User student = userRepository.findById(booking.getStudentId()).orElse(null);
        pendingBooking.setStudentName(student != null ? student.getName() : "Unknown Student");
        
        Topic topic = topicRepository.findById(booking.getTopicId()).orElse(null);
        pendingBooking.setTopicTitle(topic != null ? topic.getTitle() : "Unknown Topic");
        
        pendingBooking.setStartTime(booking.getStartTs().toLocalDateTime());
        pendingBooking.setEndTime(booking.getEndTs().toLocalDateTime());
        pendingBooking.setPriceCents(booking.getPriceMinCents());
        pendingBooking.setCategory(booking.getCategory());
        
        return pendingBooking;
    }

    private UpcomingClass convertToTeacherUpcomingClass(Booking booking) {
        UpcomingClass upcomingClass = new UpcomingClass();
        upcomingClass.setBookingId(booking.getId());
        
        User student = userRepository.findById(booking.getStudentId()).orElse(null);
        upcomingClass.setStudentName(student != null ? student.getName() : "Unknown Student");
        
        Topic topic = topicRepository.findById(booking.getTopicId()).orElse(null);
        upcomingClass.setTopicTitle(topic != null ? topic.getTitle() : "Unknown Topic");
        
        upcomingClass.setStartTime(booking.getStartTs().toLocalDateTime());
        upcomingClass.setEndTime(booking.getEndTs().toLocalDateTime());
        upcomingClass.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        
        return upcomingClass;
    }

    private BigDecimal getEarningsThisMonth(Long teacherId) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now();
        return getEarningsInPeriod(teacherId, startOfMonth, endOfMonth);
    }

    private BigDecimal getTotalEarnings(Long teacherId) {
        List<WalletTransaction> earnings = walletTransactionRepository
                .findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(WalletOwnerType.TEACHER, teacherId);
        
        return earnings.stream()
                .filter(t -> t.getType() == WalletTransactionType.CREDIT && t.getSource() == WalletTransactionSource.EARNING)
                .map(t -> BigDecimal.valueOf(t.getAmountCents()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getEarningsInPeriod(Long teacherId, LocalDateTime fromDate, LocalDateTime toDate) {
        List<WalletTransaction> earnings = walletTransactionRepository
                .findByOwnerTypeAndOwnerIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        WalletOwnerType.TEACHER, teacherId, fromDate, toDate);
        
        return earnings.stream()
                .filter(t -> t.getType() == WalletTransactionType.CREDIT && t.getSource() == WalletTransactionSource.EARNING)
                .map(t -> BigDecimal.valueOf(t.getAmountCents()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private EarningsBreakdown getEarningsBreakdown(Long teacherId) {
        EarningsBreakdown breakdown = new EarningsBreakdown();
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now();
        breakdown.setThisMonth(getEarningsInPeriod(teacherId, startOfMonth, endOfMonth));
        
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);
        breakdown.setLastMonth(getEarningsInPeriod(teacherId, startOfLastMonth, endOfLastMonth));
        
        LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
        breakdown.setThisYear(getEarningsInPeriod(teacherId, startOfYear, endOfMonth));
        
        // Calculate total classes and average
        List<Booking> completedBookings = bookingRepository.findByTeacherIdAndStatus(teacherId, BookingStatus.COMPLETED);
        breakdown.setTotalClasses((long) completedBookings.size());
        breakdown.setAveragePerClass(completedBookings.size() > 0 ? 
                breakdown.getThisYear().divide(BigDecimal.valueOf(completedBookings.size()), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        
        return breakdown;
    }

    private List<EarningsPeriod> getEarningsByPeriod(Long teacherId, LocalDateTime fromDate, LocalDateTime toDate) {
        // Stub implementation - group earnings by month
        List<EarningsPeriod> periods = new ArrayList<>();
        
        EarningsPeriod period1 = new EarningsPeriod();
        period1.setPeriod("2024-01");
        period1.setAmount(BigDecimal.valueOf(5000));
        period1.setClassCount(10L);
        periods.add(period1);
        
        EarningsPeriod period2 = new EarningsPeriod();
        period2.setPeriod("2024-02");
        period2.setAmount(BigDecimal.valueOf(7500));
        period2.setClassCount(15L);
        periods.add(period2);
        
        return periods;
    }

    private List<EarningsBySubject> getEarningsBySubject(Long teacherId, LocalDateTime fromDate, LocalDateTime toDate) {
        // Stub implementation - get earnings by subject
        List<EarningsBySubject> earningsBySubject = new ArrayList<>();
        
        EarningsBySubject mathEarnings = new EarningsBySubject();
        mathEarnings.setSubjectId(1L);
        mathEarnings.setSubjectName("Mathematics");
        mathEarnings.setAmount(BigDecimal.valueOf(8000));
        mathEarnings.setClassCount(20L);
        mathEarnings.setAveragePerClass(BigDecimal.valueOf(400));
        earningsBySubject.add(mathEarnings);
        
        EarningsBySubject physicsEarnings = new EarningsBySubject();
        physicsEarnings.setSubjectId(2L);
        physicsEarnings.setSubjectName("Physics");
        physicsEarnings.setAmount(BigDecimal.valueOf(4500));
        physicsEarnings.setClassCount(15L);
        physicsEarnings.setAveragePerClass(BigDecimal.valueOf(300));
        earningsBySubject.add(physicsEarnings);
        
        return earningsBySubject;
    }
}

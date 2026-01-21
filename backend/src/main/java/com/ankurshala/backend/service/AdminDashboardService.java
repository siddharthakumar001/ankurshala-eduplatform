package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.DashboardActivityDto;
import com.ankurshala.backend.dto.admin.DashboardMetricsDto;
import com.ankurshala.backend.dto.admin.DashboardSeriesDto;
import com.ankurshala.backend.dto.admin.SystemStatusDto;
import com.ankurshala.backend.entity.Booking;
import com.ankurshala.backend.entity.BookingStatus;
import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentIntentStatus;
import com.ankurshala.backend.entity.User;
import com.ankurshala.backend.repository.BookingRepository;
import com.ankurshala.backend.repository.StudentProfileRepository;
import com.ankurshala.backend.repository.TeacherProfileRepository;
import com.ankurshala.backend.repository.BoardRepository;
import com.ankurshala.backend.repository.GradeRepository;
import com.ankurshala.backend.repository.SubjectRepository;
import com.ankurshala.backend.repository.ChapterRepository;
import com.ankurshala.backend.repository.TopicRepository;
import com.ankurshala.backend.repository.ImportJobRepository;
import com.ankurshala.backend.repository.PaymentIntentRepository;
import com.ankurshala.backend.repository.UserRepository;
import com.ankurshala.backend.entity.ImportJobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AdminDashboardService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

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
    private ImportJobRepository importJobRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentIntentRepository paymentIntentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired(required = false)
    private PaymentHealthService paymentHealthService;

    // @Cacheable(value = "dashboardMetrics", unless = "#result == null")
    public DashboardMetricsDto getDashboardMetrics() {
        // Total counts
        long totalStudents = studentProfileRepository.countByUserEnabledTrue();
        long totalTeachers = teacherProfileRepository.countByUserEnabledTrue();
        
        // Active/Inactive counts
        long activeStudents = studentProfileRepository.countByUserEnabledTrue();
        long activeTeachers = teacherProfileRepository.countByUserEnabledTrue();
        long inactiveStudents = studentProfileRepository.countByUserEnabledFalse();
        long inactiveTeachers = teacherProfileRepository.countByUserEnabledFalse();

        // Registration trends
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        long newStudentsLast7Days = studentProfileRepository.countByUserCreatedAtBetween(sevenDaysAgo, LocalDateTime.now());
        long newStudentsLast30Days = studentProfileRepository.countByUserCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now());
        long newTeachersLast7Days = teacherProfileRepository.countByUserCreatedAtBetween(sevenDaysAgo, LocalDateTime.now());
        long newTeachersLast30Days = teacherProfileRepository.countByUserCreatedAtBetween(thirtyDaysAgo, LocalDateTime.now());

        // Content counts - using real data from repositories
        long totalBoards = boardRepository.countByActiveTrueAndSoftDeletedFalse();
        long totalGrades = gradeRepository.count(); // Total grades (active + inactive)
        long totalSubjects = subjectRepository.countByActiveTrueAndSoftDeletedFalse();
        long totalChapters = chapterRepository.countByActiveTrueAndSoftDeletedFalse();
        long totalTopics = topicRepository.countByActiveTrueAndSoftDeletedFalse();

        // Course counts (placeholders for now)
        long activeCourses = totalSubjects;
        long completedCourses = 0;

        // Revenue analytics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last30Start = now.minusDays(30);
        LocalDateTime prev30Start = now.minusDays(60);
        long totalRevenueCents = Optional.ofNullable(
            paymentIntentRepository.sumAmountCentsByStatus(PaymentIntentStatus.COMPLETED)
        ).orElse(0L);
        long revenueLast30DaysCents = Optional.ofNullable(
            paymentIntentRepository.sumAmountCentsByStatusAndCreatedAtBetween(
                PaymentIntentStatus.COMPLETED,
                last30Start,
                now
            )
        ).orElse(0L);
        long revenuePrevious30DaysCents = Optional.ofNullable(
            paymentIntentRepository.sumAmountCentsByStatusAndCreatedAtBetween(
                PaymentIntentStatus.COMPLETED,
                prev30Start,
                last30Start
            )
        ).orElse(0L);

        // Import analytics - using real data from repository
        long totalImports = importJobRepository.count();
        long successfulImports = importJobRepository.countByStatus(ImportJobStatus.SUCCEEDED);
        long failedImports = importJobRepository.countByStatus(ImportJobStatus.FAILED);
        long pendingImports = importJobRepository.countByStatus(ImportJobStatus.PENDING);
        long runningImports = importJobRepository.countByStatus(ImportJobStatus.RUNNING);

        return new DashboardMetricsDto(
                totalStudents,
                totalTeachers,
                activeStudents,
                activeTeachers,
                inactiveStudents,
                inactiveTeachers,
                newStudentsLast7Days,
                newStudentsLast30Days,
                newTeachersLast7Days,
                newTeachersLast30Days,
                totalBoards,
                totalGrades,
                totalSubjects,
                totalChapters,
                totalTopics,
                activeCourses,
                completedCourses,
                totalRevenueCents,
                revenueLast30DaysCents,
                revenuePrevious30DaysCents,
                totalImports,
                successfulImports,
                failedImports,
                pendingImports,
                runningImports
        );
    }

    // @Cacheable(value = "dashboardSeries", unless = "#result == null")
    public List<DashboardSeriesDto> getDashboardSeries() {
        List<DashboardSeriesDto> series = new ArrayList<>();
        
        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);
            
            // Get registrations for this day
            long students = studentProfileRepository.countByUserCreatedAtBetween(startOfDay, endOfDay);
            long teachers = teacherProfileRepository.countByUserCreatedAtBetween(startOfDay, endOfDay);
            
            series.add(new DashboardSeriesDto(
                date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                students,
                teachers
            ));
        }
        
        return series;
    }

    @Transactional(readOnly = true)
    public List<DashboardActivityDto> getRecentActivity(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        int perTypeLimit = Math.max(3, safeLimit);
        List<ActivityEntry> entries = new ArrayList<>();

        studentProfileRepository
            .findAll(PageRequest.of(0, perTypeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
            .forEach(profile -> {
                DashboardActivityDto dto = new DashboardActivityDto();
                dto.setId("student-" + profile.getId());
                dto.setType("success");
                String fullName = String.join(" ", profile.getFirstName(), profile.getLastName()).trim();
                dto.setTitle("New student registered");
                dto.setDescription(buildStudentDescription(profile.getEducationalBoard(), profile.getGradeLevel(), profile.getSchoolName()));
                dto.setUser(fullName.isBlank() ? "Student" : fullName);
                entries.add(new ActivityEntry(dto, profile.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
            });

        teacherProfileRepository
            .findAll(PageRequest.of(0, perTypeLimit, Sort.by(Sort.Direction.DESC, "updatedAt")))
            .forEach(profile -> {
                DashboardActivityDto dto = new DashboardActivityDto();
                dto.setId("teacher-" + profile.getId());
                boolean verified = Boolean.TRUE.equals(profile.getVerified());
                dto.setType(verified ? "success" : "info");
                String fullName = String.join(" ", nullToEmpty(profile.getFirstName()), nullToEmpty(profile.getLastName())).trim();
                dto.setTitle(verified ? "Teacher verified" : "Teacher onboarded");
                dto.setDescription(verified ? "Profile verified and ready for bookings" : "Profile created and pending verification");
                dto.setUser(fullName.isBlank() ? "Teacher" : fullName);
                LocalDateTime ts = profile.getUpdatedAt() != null ? profile.getUpdatedAt() : profile.getCreatedAt();
                entries.add(new ActivityEntry(dto, ts.atZone(ZoneId.systemDefault()).toInstant()));
            });

        bookingRepository
            .findAll(PageRequest.of(0, perTypeLimit, Sort.by(Sort.Direction.DESC, "createdAt")))
            .forEach(booking -> {
                DashboardActivityDto dto = new DashboardActivityDto();
                dto.setId("booking-" + booking.getId());
                dto.setType(resolveBookingActivityType(booking.getStatus()));
                dto.setTitle(resolveBookingTitle(booking.getStatus()));
                dto.setDescription(buildBookingDescription(booking));
                ActivityEntry entry = new ActivityEntry(dto, booking.getCreatedAt().toInstant());
                entry.userId = booking.getStudentId();
                entries.add(entry);
            });

        paymentIntentRepository
            .findTop10ByStatusOrderByCreatedAtDesc(PaymentIntentStatus.COMPLETED)
            .forEach(intent -> entries.add(buildPaymentActivity(intent, "success", "Payment completed")));

        paymentIntentRepository
            .findTop10ByStatusOrderByCreatedAtDesc(PaymentIntentStatus.FAILED)
            .forEach(intent -> entries.add(buildPaymentActivity(intent, "warning", "Payment failed")));

        Map<Long, String> userNames = loadUserNames(entries);
        entries.forEach(entry -> {
            if (entry.dto.getUser() == null && entry.userId != null) {
                entry.dto.setUser(userNames.get(entry.userId));
            }
            entry.dto.setTimestamp(formatTimestamp(entry.timestamp));
        });

        entries.sort(Comparator.comparing(ActivityEntry::timestamp).reversed());
        List<DashboardActivityDto> result = new ArrayList<>();
        for (ActivityEntry entry : entries) {
            if (result.size() >= safeLimit) {
                break;
            }
            result.add(entry.dto);
        }
        return result;
    }

    public List<SystemStatusDto> getSystemStatus() {
        List<SystemStatusDto> statuses = new ArrayList<>();

        SystemStatusDto backendStatus = new SystemStatusDto();
        backendStatus.setName("Backend API");
        backendStatus.setStatus("operational");
        backendStatus.setUptime(formatUptime(java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime()));
        statuses.add(backendStatus);

        statuses.add(checkDatabaseStatus());
        statuses.add(checkRedisStatus());
        statuses.add(checkPaymentStatus());

        return statuses;
    }

    private SystemStatusDto checkDatabaseStatus() {
        SystemStatusDto status = new SystemStatusDto();
        status.setName("Database");
        long start = System.currentTimeMillis();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long latency = System.currentTimeMillis() - start;
            status.setStatus("operational");
            status.setLatency(latency + "ms");
        } catch (Exception e) {
            status.setStatus("down");
            status.setLatency("unavailable");
        }
        return status;
    }

    private SystemStatusDto checkRedisStatus() {
        SystemStatusDto status = new SystemStatusDto();
        status.setName("Redis Cache");
        if (redisTemplate == null) {
            status.setStatus("degraded");
            status.setLatency("not configured");
            return status;
        }
        long start = System.currentTimeMillis();
        try {
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(5));
            String value = redisTemplate.opsForValue().get("health:check");
            redisTemplate.delete("health:check");
            long latency = System.currentTimeMillis() - start;
            status.setStatus("ok".equals(value) ? "operational" : "degraded");
            status.setLatency(latency + "ms");
        } catch (Exception e) {
            status.setStatus("down");
            status.setLatency("unavailable");
        }
        return status;
    }

    private SystemStatusDto checkPaymentStatus() {
        SystemStatusDto status = new SystemStatusDto();
        status.setName("Payment Gateway");
        if (paymentHealthService == null) {
            status.setStatus("degraded");
            status.setLatency("not configured");
            return status;
        }
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> health = paymentHealthService.getPaymentSystemHealth();
            boolean overall = Boolean.TRUE.equals(health.get("overall"));
            long latency = System.currentTimeMillis() - start;
            status.setStatus(overall ? "operational" : "degraded");
            status.setLatency(latency + "ms");
        } catch (Exception e) {
            status.setStatus("down");
            status.setLatency("unavailable");
        }
        return status;
    }

    private ActivityEntry buildPaymentActivity(PaymentIntent intent, String type, String title) {
        DashboardActivityDto dto = new DashboardActivityDto();
        dto.setId("payment-" + intent.getId());
        dto.setType(type);
        dto.setTitle(title);
        dto.setDescription(buildPaymentDescription(intent));
        ActivityEntry entry = new ActivityEntry(dto, intent.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        entry.userId = intent.getUserId();
        return entry;
    }

    private Map<Long, String> loadUserNames(List<ActivityEntry> entries) {
        Set<Long> userIds = new HashSet<>();
        for (ActivityEntry entry : entries) {
            if (entry.userId != null && entry.dto.getUser() == null) {
                userIds.add(entry.userId);
            }
        }
        if (userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> names = new HashMap<>();
        for (User user : userRepository.findAllById(userIds)) {
            names.put(user.getId(), user.getName());
        }
        return names;
    }

    private String buildStudentDescription(Object board, String gradeLevel, String schoolName) {
        List<String> parts = new ArrayList<>();
        if (board != null) {
            parts.add(board.toString());
        }
        if (gradeLevel != null && !gradeLevel.isBlank()) {
            parts.add("Grade " + gradeLevel);
        }
        if (schoolName != null && !schoolName.isBlank()) {
            parts.add(schoolName);
        }
        if (parts.isEmpty()) {
            return "Student onboarding completed";
        }
        return String.join(" | ", parts);
    }

    private String buildBookingDescription(Booking booking) {
        String board = booking.getBoard();
        String grade = booking.getGrade();
        String subject = booking.getSubjectId() != null ? "Subject " + booking.getSubjectId() : "Subject";
        List<String> parts = new ArrayList<>();
        if (board != null) {
            parts.add(board);
        }
        if (grade != null) {
            parts.add(grade);
        }
        parts.add(subject);
        return String.join(" | ", parts);
    }

    private String buildPaymentDescription(PaymentIntent intent) {
        long amount = intent.getAmountCents() != null ? intent.getAmountCents() : 0;
        String amountLabel = "INR " + formatRupees(amount);
        if (intent.getBookingId() != null) {
            return amountLabel + " for booking #" + intent.getBookingId();
        }
        return amountLabel + " wallet payment";
    }

    private String formatRupees(long cents) {
        long rupees = Math.round(cents / 100.0);
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        return formatter.format(rupees);
    }

    private String resolveBookingActivityType(BookingStatus status) {
        if (status == null) {
            return "info";
        }
        return switch (status) {
            case CONFIRMED, COMPLETED -> "success";
            case CANCELLED, REFUNDED, NO_SHOW_STUDENT, NO_SHOW_TEACHER -> "warning";
            case PENDING, ACCEPTED, IN_PROGRESS, EXPIRED -> "info";
        };
    }

    private String resolveBookingTitle(BookingStatus status) {
        if (status == null) {
            return "Booking updated";
        }
        return switch (status) {
            case PENDING -> "Booking requested";
            case ACCEPTED, CONFIRMED -> "Booking confirmed";
            case IN_PROGRESS -> "Class in progress";
            case COMPLETED -> "Class completed";
            case CANCELLED -> "Booking cancelled";
            case REFUNDED -> "Booking refunded";
            case EXPIRED -> "Booking expired";
            case NO_SHOW_STUDENT -> "Student no-show";
            case NO_SHOW_TEACHER -> "Teacher no-show";
        };
    }

    private String formatTimestamp(java.time.Instant instant) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(instant.atZone(ZoneId.systemDefault()));
    }

    private String formatUptime(long uptimeMs) {
        Duration duration = Duration.ofMillis(uptimeMs);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        if (days > 0) {
            return String.format("%dd %dh", days, hours);
        }
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class ActivityEntry {
        private final DashboardActivityDto dto;
        private final java.time.Instant timestamp;
        private Long userId;

        private ActivityEntry(DashboardActivityDto dto, java.time.Instant timestamp) {
            this.dto = dto;
            this.timestamp = timestamp;
        }

        private java.time.Instant timestamp() {
            return timestamp;
        }
    }
}

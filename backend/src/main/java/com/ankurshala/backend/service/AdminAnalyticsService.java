package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.admin.AnalyticsOverviewDto;
import com.ankurshala.backend.entity.EducationalBoard;
import com.ankurshala.backend.entity.ImportJobStatus;
import com.ankurshala.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private ImportJobRepository importJobRepository;

    public AnalyticsOverviewDto getAnalyticsOverview(String from, String to) {
        // Parse date range
        LocalDateTime fromDate = parseDate(from, LocalDateTime.now().minusMonths(1));
        LocalDateTime toDate = parseDate(to, LocalDateTime.now());

        // User analytics
        long totalStudents = studentProfileRepository.count();
        long totalTeachers = teacherProfileRepository.count();
        long activeStudents = studentProfileRepository.countByUserEnabledTrue();
        long activeTeachers = teacherProfileRepository.countByUserEnabledTrue();

        // Content analytics
        long totalBoards = boardRepository.count();
        long totalSubjects = subjectRepository.count();
        long totalChapters = chapterRepository.count();
        long totalTopics = topicRepository.count();

        // Import analytics
        long totalImports = importJobRepository.count();
        long successfulImports = importJobRepository.countByStatus(ImportJobStatus.SUCCEEDED);
        long failedImports = importJobRepository.countByStatus(ImportJobStatus.FAILED);

        // Registration trends
        long newStudents = studentProfileRepository.countByUserCreatedAtBetween(fromDate, toDate);
        long newTeachers = teacherProfileRepository.countByUserCreatedAtBetween(fromDate, toDate);

        return new AnalyticsOverviewDto(
                totalStudents, totalTeachers, activeStudents, activeTeachers,
                totalBoards, totalSubjects, totalChapters, totalTopics,
                totalImports, successfulImports, failedImports,
                newStudents, newTeachers
        );
    }

    public Map<String, Object> getUserAnalytics(String from, String to) {
        LocalDateTime fromDate = parseDate(from, LocalDateTime.now().minusMonths(1));
        LocalDateTime toDate = parseDate(to, LocalDateTime.now());

        Map<String, Object> analytics = new HashMap<>();
        
        // User counts by role
        analytics.put("totalStudents", studentProfileRepository.count());
        analytics.put("totalTeachers", teacherProfileRepository.count());
        analytics.put("activeStudents", studentProfileRepository.countByUserEnabledTrue());
        analytics.put("activeTeachers", teacherProfileRepository.countByUserEnabledTrue());

        // Registration trends
        analytics.put("newStudents", studentProfileRepository.countByUserCreatedAtBetween(fromDate, toDate));
        analytics.put("newTeachers", teacherProfileRepository.countByUserCreatedAtBetween(fromDate, toDate));

        // Board distribution
        Map<String, Long> boardDistribution = new HashMap<>();
        boardDistribution.put("CBSE", studentProfileRepository.countByEducationalBoard(EducationalBoard.CBSE));
        boardDistribution.put("ICSE", studentProfileRepository.countByEducationalBoard(EducationalBoard.ICSE));
        boardDistribution.put("STATE_BOARD", studentProfileRepository.countByEducationalBoard(EducationalBoard.STATE_BOARD));
        analytics.put("boardDistribution", boardDistribution);

        return analytics;
    }

    public Map<String, Object> getContentAnalytics(String from, String to) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Content counts
        analytics.put("totalBoards", boardRepository.count());
        analytics.put("totalSubjects", subjectRepository.count());
        analytics.put("totalChapters", chapterRepository.count());
        analytics.put("totalTopics", topicRepository.count());

        // Active content
        analytics.put("activeBoards", boardRepository.countByActiveTrue());
        analytics.put("activeSubjects", subjectRepository.countByActiveTrue());
        analytics.put("activeChapters", chapterRepository.countByActiveTrue());
        analytics.put("activeTopics", topicRepository.countByActiveTrue());

        return analytics;
    }

    public Map<String, Object> getImportAnalytics(String from, String to) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Import job statistics
        analytics.put("totalImports", importJobRepository.count());
        analytics.put("successfulImports", importJobRepository.countByStatus(ImportJobStatus.SUCCEEDED));
        analytics.put("failedImports", importJobRepository.countByStatus(ImportJobStatus.FAILED));
        analytics.put("pendingImports", importJobRepository.countByStatus(ImportJobStatus.PENDING));
        analytics.put("runningImports", importJobRepository.countByStatus(ImportJobStatus.RUNNING));

        return analytics;
    }

    private LocalDateTime parseDate(String dateStr, LocalDateTime defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(dateStr + "-01T00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

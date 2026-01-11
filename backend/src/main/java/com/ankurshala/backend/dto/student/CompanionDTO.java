package com.ankurshala.backend.dto.student;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for Session Companion (Live Class Companion) feature.
 */
public class CompanionDTO {

    // ==================== COMPANION RESPONSE ====================

    public static class CompanionResponse {
        private Long id;
        private Long bookingId;
        private Long studentId;
        private Long teacherId;
        private String teacherName;
        private Long topicId;
        private String topicName;
        private Long subjectId;
        private String subjectName;
        
        // Session details from booking
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime scheduledStartTime;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime scheduledEndTime;
        private String bookingStatus;
        
        // Pre-session
        private String preSessionPlanMd;
        private Long warmupQuizId;
        private Boolean warmupCompleted;
        
        // Live session
        private String liveNotesMd;
        private List<String> sessionHighlights;
        private List<String> questionsAsked;
        private List<CompanionNoteResponse> notes;
        
        // Post-session
        private String postSessionSummaryMd;
        private String homeworkPlanMd;
        private List<Long> recommendedTopics;
        private List<String> recommendedTopicNames;
        
        // Status
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime prepGeneratedAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime liveStartedAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime postGeneratedAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

        public Long getTopicId() { return topicId; }
        public void setTopicId(Long topicId) { this.topicId = topicId; }

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }

        public Long getSubjectId() { return subjectId; }
        public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
        public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

        public LocalDateTime getScheduledEndTime() { return scheduledEndTime; }
        public void setScheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }

        public String getBookingStatus() { return bookingStatus; }
        public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

        public String getPreSessionPlanMd() { return preSessionPlanMd; }
        public void setPreSessionPlanMd(String preSessionPlanMd) { this.preSessionPlanMd = preSessionPlanMd; }

        public Long getWarmupQuizId() { return warmupQuizId; }
        public void setWarmupQuizId(Long warmupQuizId) { this.warmupQuizId = warmupQuizId; }

        public Boolean getWarmupCompleted() { return warmupCompleted; }
        public void setWarmupCompleted(Boolean warmupCompleted) { this.warmupCompleted = warmupCompleted; }

        public String getLiveNotesMd() { return liveNotesMd; }
        public void setLiveNotesMd(String liveNotesMd) { this.liveNotesMd = liveNotesMd; }

        public List<String> getSessionHighlights() { return sessionHighlights; }
        public void setSessionHighlights(List<String> sessionHighlights) { this.sessionHighlights = sessionHighlights; }

        public List<String> getQuestionsAsked() { return questionsAsked; }
        public void setQuestionsAsked(List<String> questionsAsked) { this.questionsAsked = questionsAsked; }

        public List<CompanionNoteResponse> getNotes() { return notes; }
        public void setNotes(List<CompanionNoteResponse> notes) { this.notes = notes; }

        public String getPostSessionSummaryMd() { return postSessionSummaryMd; }
        public void setPostSessionSummaryMd(String postSessionSummaryMd) { this.postSessionSummaryMd = postSessionSummaryMd; }

        public String getHomeworkPlanMd() { return homeworkPlanMd; }
        public void setHomeworkPlanMd(String homeworkPlanMd) { this.homeworkPlanMd = homeworkPlanMd; }

        public List<Long> getRecommendedTopics() { return recommendedTopics; }
        public void setRecommendedTopics(List<Long> recommendedTopics) { this.recommendedTopics = recommendedTopics; }

        public List<String> getRecommendedTopicNames() { return recommendedTopicNames; }
        public void setRecommendedTopicNames(List<String> recommendedTopicNames) { this.recommendedTopicNames = recommendedTopicNames; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getPrepGeneratedAt() { return prepGeneratedAt; }
        public void setPrepGeneratedAt(LocalDateTime prepGeneratedAt) { this.prepGeneratedAt = prepGeneratedAt; }

        public LocalDateTime getLiveStartedAt() { return liveStartedAt; }
        public void setLiveStartedAt(LocalDateTime liveStartedAt) { this.liveStartedAt = liveStartedAt; }

        public LocalDateTime getPostGeneratedAt() { return postGeneratedAt; }
        public void setPostGeneratedAt(LocalDateTime postGeneratedAt) { this.postGeneratedAt = postGeneratedAt; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ==================== COMPANION NOTE ====================

    public static class CompanionNoteResponse {
        private Long id;
        private String noteType;
        private String content;
        private Integer timestampInSession;
        private String aiResponse;
        private Boolean isResolved;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNoteType() { return noteType; }
        public void setNoteType(String noteType) { this.noteType = noteType; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Integer getTimestampInSession() { return timestampInSession; }
        public void setTimestampInSession(Integer timestampInSession) { this.timestampInSession = timestampInSession; }

        public String getAiResponse() { return aiResponse; }
        public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }

        public Boolean getIsResolved() { return isResolved; }
        public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    // ==================== PREP REQUEST ====================

    public static class GeneratePrepRequest {
        private String language = "en";
        private Boolean generateWarmupQuiz = true;

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public Boolean getGenerateWarmupQuiz() { return generateWarmupQuiz; }
        public void setGenerateWarmupQuiz(Boolean generateWarmupQuiz) { this.generateWarmupQuiz = generateWarmupQuiz; }
    }

    // ==================== LIVE NOTES REQUEST ====================

    public static class AddLiveNoteRequest {
        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Note content must be less than 5000 characters")
        private String content;

        private String noteType = "NOTE"; // NOTE, QUESTION, HIGHLIGHT, ACTION_ITEM

        private Integer timestampInSession; // Seconds from session start

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getNoteType() { return noteType; }
        public void setNoteType(String noteType) { this.noteType = noteType; }

        public Integer getTimestampInSession() { return timestampInSession; }
        public void setTimestampInSession(Integer timestampInSession) { this.timestampInSession = timestampInSession; }
    }

    // ==================== POST-SESSION REQUEST ====================

    public static class GeneratePostRequest {
        private String language = "en";
        private Boolean generateHomework = true;
        private Boolean updateMastery = true;

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public Boolean getGenerateHomework() { return generateHomework; }
        public void setGenerateHomework(Boolean generateHomework) { this.generateHomework = generateHomework; }

        public Boolean getUpdateMastery() { return updateMastery; }
        public void setUpdateMastery(Boolean updateMastery) { this.updateMastery = updateMastery; }
    }

    // ==================== WARMUP QUIZ RESPONSE ====================

    public static class WarmupQuizResponse {
        private Long quizId;
        private String topicName;
        private Integer totalQuestions;
        private List<WarmupQuestionResponse> questions;

        public Long getQuizId() { return quizId; }
        public void setQuizId(Long quizId) { this.quizId = quizId; }

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }

        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

        public List<WarmupQuestionResponse> getQuestions() { return questions; }
        public void setQuestions(List<WarmupQuestionResponse> questions) { this.questions = questions; }
    }

    public static class WarmupQuestionResponse {
        private Long id;
        private String questionText;
        private List<String> options;
        private String questionType;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }
    }

    // ==================== COMPANION LIST ====================

    public static class CompanionListResponse {
        private List<CompanionSummary> companions;

        public List<CompanionSummary> getCompanions() { return companions; }
        public void setCompanions(List<CompanionSummary> companions) { this.companions = companions; }
    }

    public static class CompanionSummary {
        private Long id;
        private Long bookingId;
        private String topicName;
        private String subjectName;
        private String teacherName;
        private String status;
        private String bookingStatus;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime scheduledStartTime;
        private Boolean hasPrepPlan;
        private Boolean hasPostSummary;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

        public String getTopicName() { return topicName; }
        public void setTopicName(String topicName) { this.topicName = topicName; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getBookingStatus() { return bookingStatus; }
        public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

        public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
        public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

        public Boolean getHasPrepPlan() { return hasPrepPlan; }
        public void setHasPrepPlan(Boolean hasPrepPlan) { this.hasPrepPlan = hasPrepPlan; }

        public Boolean getHasPostSummary() { return hasPostSummary; }
        public void setHasPostSummary(Boolean hasPostSummary) { this.hasPostSummary = hasPostSummary; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}


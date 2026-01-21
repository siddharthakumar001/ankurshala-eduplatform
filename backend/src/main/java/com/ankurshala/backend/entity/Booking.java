package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;

    @Column(name = "teacher_id")
    private Long teacherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", insertable = false, updatable = false)
    private User teacher;

    @NotNull
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    private PricingRule pricingRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @NotNull
    @Min(1)
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "student_notes", columnDefinition = "TEXT")
    private String studentNotes;

    @Column(name = "acceptance_token")
    private String acceptanceToken;

    @Column(name = "final_price_cents")
    private Long finalPriceCents;

    @Column(name = "teacher_price_cents")
    private Long teacherPriceCents;

    @Column(name = "cancelled_at")
    private ZonedDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "accepted_at")
    private ZonedDateTime acceptedAt;

    @Column(name = "price_min", precision = 8, scale = 2)
    private BigDecimal priceMin;

    @Column(name = "price_max", precision = 8, scale = 2)
    private BigDecimal priceMax;

    @Column(name = "price_currency", length = 3)
    private String priceCurrency = "INR";

    @Column(name = "cancellation_fee", precision = 8, scale = 2)
    private BigDecimal cancellationFee;

    @Column(name = "reschedule_fee", precision = 8, scale = 2)
    private BigDecimal rescheduleFee;

    @Column(name = "teacher_notes", columnDefinition = "TEXT")
    private String teacherNotes;

    @Column(name = "student_feedback", columnDefinition = "TEXT")
    private String studentFeedback;

    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    @Column(name = "rating")
    private Integer rating;

    @NotBlank
    @Column(name = "board", nullable = false)
    private String board;

    @NotBlank
    @Column(name = "grade", nullable = false)
    private String grade;

    @NotNull
    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "chapter_id")
    private Long chapterId;

    @NotNull
    @Column(name = "start_ts", nullable = false)
    private ZonedDateTime startTs;

    @NotNull
    @Column(name = "end_ts", nullable = false)
    private ZonedDateTime endTs;

    @NotBlank
    @Column(name = "category", nullable = false)
    private String category;

    @NotNull
    @Min(0)
    @Column(name = "price_min_cents", nullable = false)
    private Integer priceMinCents;

    @NotNull
    @Min(0)
    @Column(name = "price_max_cents", nullable = false)
    private Integer priceMaxCents;

    @Column(name = "applied_rule_id")
    private Long appliedRuleId;

    @NotBlank
    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Constructors
    public Booking() {}

    public Booking(User student, Topic topic, ZonedDateTime startTime, ZonedDateTime endTime, 
                   Integer durationMinutes, BigDecimal priceMin, BigDecimal priceMax) {
        this.student = student;
        this.studentId = student.getId();
        this.topic = topic;
        this.topicId = topic.getId();
        this.startTs = startTime;
        this.endTs = endTime;
        this.durationMinutes = durationMinutes;
        setPriceMin(priceMin);
        setPriceMax(priceMax);
        this.status = BookingStatus.PENDING;
        this.state = "REQUESTED";
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public ZonedDateTime getStartTs() {
        return startTs;
    }

    public void setStartTs(ZonedDateTime startTs) {
        this.startTs = startTs;
    }

    public ZonedDateTime getEndTs() {
        return endTs;
    }

    public void setEndTs(ZonedDateTime endTs) {
        this.endTs = endTs;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPriceMinCents() {
        if (priceMinCents == null && priceMin != null) {
            return priceMin.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        }
        return priceMinCents;
    }

    public void setPriceMinCents(Integer priceMinCents) {
        this.priceMinCents = priceMinCents;
        if (priceMinCents != null) {
            this.priceMin = BigDecimal.valueOf(priceMinCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }

    public Integer getPriceMaxCents() {
        if (priceMaxCents == null && priceMax != null) {
            return priceMax.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        }
        return priceMaxCents;
    }

    public void setPriceMaxCents(Integer priceMaxCents) {
        this.priceMaxCents = priceMaxCents;
        if (priceMaxCents != null) {
            this.priceMax = BigDecimal.valueOf(priceMaxCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
    }

    public Long getAppliedRuleId() {
        return appliedRuleId;
    }

    public void setAppliedRuleId(Long appliedRuleId) {
        this.appliedRuleId = appliedRuleId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Additional getters and setters for new fields
    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public PricingRule getPricingRule() {
        return pricingRule;
    }

    public void setPricingRule(PricingRule pricingRule) {
        this.pricingRule = pricingRule;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
        if (status != null) {
            this.state = BookingStateMapper.toState(status);
        }
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStudentNotes() {
        return studentNotes;
    }

    public void setStudentNotes(String studentNotes) {
        this.studentNotes = studentNotes;
    }

    public String getAcceptanceToken() {
        return acceptanceToken;
    }

    public void setAcceptanceToken(String acceptanceToken) {
        this.acceptanceToken = acceptanceToken;
    }

    public Long getFinalPriceCents() {
        return finalPriceCents;
    }

    public void setFinalPriceCents(Long finalPriceCents) {
        this.finalPriceCents = finalPriceCents;
    }

    public Long getTeacherPriceCents() {
        return teacherPriceCents;
    }

    public void setTeacherPriceCents(Long teacherPriceCents) {
        this.teacherPriceCents = teacherPriceCents;
    }

    // Additional methods for compatibility
    public ZonedDateTime getStartTime() {
        return startTs;
    }

    public ZonedDateTime getEndTime() {
        return endTs;
    }

    // Additional methods for new fields
    public ZonedDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(ZonedDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public ZonedDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(ZonedDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public BigDecimal getPriceMin() {
        if (priceMin == null && priceMinCents != null) {
            return BigDecimal.valueOf(priceMinCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return priceMin;
    }

    public void setPriceMin(BigDecimal priceMin) {
        this.priceMin = priceMin;
        if (priceMin != null) {
            this.priceMinCents = priceMin.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        }
    }

    public BigDecimal getPriceMax() {
        if (priceMax == null && priceMaxCents != null) {
            return BigDecimal.valueOf(priceMaxCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return priceMax;
    }

    public void setPriceMax(BigDecimal priceMax) {
        this.priceMax = priceMax;
        if (priceMax != null) {
            this.priceMaxCents = priceMax.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        }
    }

    public String getPriceCurrency() {
        return priceCurrency;
    }

    public void setPriceCurrency(String priceCurrency) {
        this.priceCurrency = priceCurrency;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public BigDecimal getRescheduleFee() {
        return rescheduleFee;
    }

    public void setRescheduleFee(BigDecimal rescheduleFee) {
        this.rescheduleFee = rescheduleFee;
    }

    public String getTeacherNotes() {
        return teacherNotes;
    }

    public void setTeacherNotes(String teacherNotes) {
        this.teacherNotes = teacherNotes;
    }

    public String getStudentFeedback() {
        return studentFeedback;
    }

    public void setStudentFeedback(String studentFeedback) {
        this.studentFeedback = studentFeedback;
    }

    public String getTeacherFeedback() {
        return teacherFeedback;
    }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    // Additional compatibility methods
    public void setStartTime(ZonedDateTime startTime) {
        this.startTs = startTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTs = endTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

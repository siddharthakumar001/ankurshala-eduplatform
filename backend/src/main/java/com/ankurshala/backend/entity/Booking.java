package com.ankurshala.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Min(30)
    @Max(120)
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status = BookingStatus.REQUESTED;
    
    @Column(name = "acceptance_token", unique = true)
    private String acceptanceToken;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @DecimalMin("0.00")
    @Column(name = "price_min", nullable = false, precision = 8, scale = 2)
    private BigDecimal priceMin;
    
    @DecimalMin("0.00")
    @Column(name = "price_max", nullable = false, precision = 8, scale = 2)
    private BigDecimal priceMax;
    
    @Size(max = 3)
    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency = "INR";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pricing_rule_id")
    private PricingRule pricingRule;
    
    @DecimalMin("0.00")
    @Column(name = "cancellation_fee", precision = 8, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;
    
    @DecimalMin("0.00")
    @Column(name = "reschedule_fee", precision = 8, scale = 2)
    private BigDecimal rescheduleFee = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_waiver_id")
    private FeeWaiver feeWaiver;
    
    @Column(name = "student_notes", columnDefinition = "TEXT")
    private String studentNotes;
    
    @Column(name = "teacher_notes", columnDefinition = "TEXT")
    private String teacherNotes;
    
    @Column(name = "student_feedback", columnDefinition = "TEXT")
    private String studentFeedback;
    
    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;
    
    @Min(1)
    @Max(5)
    @Column(name = "rating")
    private Integer rating;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingBookmark> bookmarks = new ArrayList<>();
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingNote> notes = new ArrayList<>();
    
    public Booking(User student, Topic topic, LocalDateTime startTime, LocalDateTime endTime, 
                   Integer durationMinutes, BigDecimal priceMin, BigDecimal priceMax) {
        this.student = student;
        this.topic = topic;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.priceMin = priceMin;
        this.priceMax = priceMax;
    }
    
    public enum BookingStatus {
        REQUESTED, ACCEPTED, RESCHEDULED, CANCELLED, COMPLETED, NO_SHOW
    }
}

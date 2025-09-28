package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
    
    private Long id;
    private Long studentId;
    private Long teacherId;
    private Long topicId;
    private String topicTitle;
    private String teacherName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private String status;
    private LocalDateTime acceptedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private String priceCurrency;
    private BigDecimal cancellationFee;
    private BigDecimal rescheduleFee;
    private String studentNotes;
    private String teacherNotes;
    private String studentFeedback;
    private String teacherFeedback;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookingNoteResponse> notes;
    private Boolean bookmarked;
}

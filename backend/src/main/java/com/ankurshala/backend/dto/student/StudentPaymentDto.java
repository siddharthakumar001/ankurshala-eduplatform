package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentPaymentDto {
    private Long id;
    private Long bookingId;
    private String bookingTitle;
    private String teacherName;
    private LocalDateTime sessionDate;
    private Integer sessionDuration;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime dueDate;
    private String invoiceUrl;
    private String receiptUrl;
}

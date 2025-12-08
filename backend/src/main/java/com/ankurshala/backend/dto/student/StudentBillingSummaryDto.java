package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentBillingSummaryDto {
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private BigDecimal overdueAmount;
    private int totalInvoices;
    private int paidInvoices;
    private int pendingInvoices;
    private int overdueInvoices;
    private LocalDateTime lastPaymentDate;
    private BigDecimal monthlySpending;
    private BigDecimal yearlySpending;
    private StudentPaymentMethodDto defaultPaymentMethod;
}

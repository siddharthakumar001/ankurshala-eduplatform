package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherEarningsResponse {
    private BigDecimal totalEarnings;
    private BigDecimal thisMonthEarnings;
    private BigDecimal lastMonthEarnings;
    private List<EarningsPeriod> earningsByPeriod;
    private List<EarningsBySubject> earningsBySubject;
    private BigDecimal averagePerClass;
    private Long totalClasses;
}


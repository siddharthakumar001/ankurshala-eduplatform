package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EarningsBreakdown {
    private BigDecimal thisMonth;
    private BigDecimal lastMonth;
    private BigDecimal thisYear;
    private Long totalClasses;
    private BigDecimal averagePerClass;
}

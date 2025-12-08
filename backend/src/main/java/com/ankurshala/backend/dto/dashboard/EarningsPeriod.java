package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EarningsPeriod {
    private String period; // e.g., "2024-01", "2024-02"
    private BigDecimal amount;
    private Long classCount;
}

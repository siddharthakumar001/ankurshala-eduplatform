package com.ankurshala.backend.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EarningsBySubject {
    private Long subjectId;
    private String subjectName;
    private BigDecimal amount;
    private Long classCount;
    private BigDecimal averagePerClass;
}

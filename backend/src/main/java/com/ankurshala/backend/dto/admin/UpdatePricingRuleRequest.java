package com.ankurshala.backend.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class UpdatePricingRuleRequest {
    private Long boardId;
    private Long gradeId;
    private Long subjectId;
    private Long chapterId;
    private Long topicId;

    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    private BigDecimal hourlyRate;

    private Boolean active;
}

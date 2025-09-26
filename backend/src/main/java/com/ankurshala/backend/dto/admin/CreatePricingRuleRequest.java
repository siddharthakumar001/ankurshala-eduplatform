package com.ankurshala.backend.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CreatePricingRuleRequest {
    private Long boardId;
    private Long gradeId;
    private Long subjectId;
    private Long chapterId;
    private Long topicId;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    private BigDecimal hourlyRate;

    private Boolean active = true;
}

package com.ankurshala.backend.dto.fee;

import com.ankurshala.backend.entity.FeeWaiver;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeWaiverRequest {
    @NotNull(message = "Fee application ID is required")
    private Long feeApplicationId;

    @NotNull(message = "Waiver type is required")
    private FeeWaiver.WaiverType waiverType;

    @NotBlank(message = "Reason is required")
    private String reason;

    private Long waiverAmountCents;

    @DecimalMin(value = "0.00", message = "Waiver percentage must be at least 0.00")
    @DecimalMax(value = "100.00", message = "Waiver percentage must not exceed 100.00")
    private BigDecimal waiverPercentage;
}

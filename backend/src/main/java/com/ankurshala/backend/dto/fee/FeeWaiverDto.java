package com.ankurshala.backend.dto.fee;

import com.ankurshala.backend.entity.FeeWaiver;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeWaiverDto {
    private Long id;
    private Long userId;
    private Long feeApplicationId;
    private FeeWaiver.WaiverType waiverType;
    private Long waiverAmountCents;
    private BigDecimal waiverPercentage;
    private String reason;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private FeeWaiver.WaiverStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

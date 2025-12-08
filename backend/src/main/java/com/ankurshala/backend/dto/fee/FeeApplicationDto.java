package com.ankurshala.backend.dto.fee;

import com.ankurshala.backend.entity.FeeApplication;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeeApplicationDto {
    private Long id;
    private String entityType;
    private Long entityId;
    private Long amountCents;
    private Long feeAmountCents;
    private LocalDateTime calculatedAt;
    private LocalDateTime appliedAt;
    private FeeApplication.FeeApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

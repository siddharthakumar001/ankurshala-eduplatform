package com.ankurshala.backend.dto.policy;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PolicyAcceptanceRequest {
    @NotNull(message = "Policy ID is required")
    private Long policyId;
}

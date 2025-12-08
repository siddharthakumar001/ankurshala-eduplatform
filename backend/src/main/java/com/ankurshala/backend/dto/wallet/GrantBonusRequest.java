package com.ankurshala.backend.dto.wallet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class GrantBonusRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount in cents is required")
    @Min(value = 1, message = "Amount must be positive")
    private Long amountCents;

    @NotNull(message = "Owner type is required")
    private String ownerType; // STUDENT or TEACHER

    private String reason;
}

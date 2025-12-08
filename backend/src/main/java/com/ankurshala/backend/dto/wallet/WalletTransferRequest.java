package com.ankurshala.backend.dto.wallet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class WalletTransferRequest {
    @NotNull(message = "From user ID is required")
    private Long fromUserId;

    @NotNull(message = "To user ID is required")
    private Long toUserId;

    @NotNull(message = "Amount in cents is required")
    @Min(value = 1, message = "Amount must be at least 1 cent")
    private Long amountCents;

    @NotBlank(message = "From user type is required")
    private String fromUserType;

    @NotBlank(message = "To user type is required")
    private String toUserType;

    @NotBlank(message = "Description is required")
    private String description;
}

package com.ankurshala.backend.dto.wallet;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WalletBalanceDto {
    private String ownerType;
    private Long ownerId;
    private Long balanceCents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.ankurshala.backend.dto.wallet;

import com.ankurshala.backend.entity.WalletTransaction;
import com.ankurshala.backend.entity.WalletOwnerType;
import com.ankurshala.backend.entity.WalletTransactionType;
import com.ankurshala.backend.entity.WalletTransactionSource;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class WalletTransactionDto {
    private Long id;
    private WalletOwnerType ownerType;
    private Long ownerId;
    private Long bookingId;
    private WalletTransactionType type;
    private Long amountCents;
    private WalletTransactionSource source;
    private Map<String, Object> meta;
    private LocalDateTime createdAt;
}
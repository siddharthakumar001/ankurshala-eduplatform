package com.ankurshala.backend.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FeeWaiverDto {
    private Long id;
    private Long bookingId;
    private Long userId;
    private String userEmail;
    private String reason;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public FeeWaiverDto(Long id, Long bookingId, Long userId, String userEmail, 
                       String reason, BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.reason = reason;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}

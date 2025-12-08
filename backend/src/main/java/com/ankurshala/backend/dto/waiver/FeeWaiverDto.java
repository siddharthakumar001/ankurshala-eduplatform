package com.ankurshala.backend.dto.waiver;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeWaiverDto {
    private Long id;
    private Long studentId;
    private Long bookingId;
    private BigDecimal percent;
    private Integer flatCents;
    private Boolean active;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}

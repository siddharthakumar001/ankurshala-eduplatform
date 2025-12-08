package com.ankurshala.backend.dto.payment;

import com.ankurshala.backend.entity.PaymentMethodType;
import com.ankurshala.backend.entity.PaymentProvider;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentMethodDto {
    private Long id;
    private Long userId;
    private PaymentMethodType methodType;
    private PaymentProvider provider;
    private String providerId;
    private String maskedDetails;
    private Boolean isDefault;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

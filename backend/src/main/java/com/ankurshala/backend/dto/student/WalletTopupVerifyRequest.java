package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WalletTopupVerifyRequest {
    @NotBlank
    private String orderId;
    @NotBlank
    private String paymentId;
    @NotBlank
    private String signature;
}

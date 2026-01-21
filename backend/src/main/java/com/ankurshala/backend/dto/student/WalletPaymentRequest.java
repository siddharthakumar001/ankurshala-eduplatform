package com.ankurshala.backend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletPaymentRequest {
    @NotNull
    private Long bookingId;
    private String notes;
}

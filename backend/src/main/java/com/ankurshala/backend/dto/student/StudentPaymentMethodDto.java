package com.ankurshala.backend.dto.student;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentPaymentMethodDto {
    private Long id;
    private String type; // CARD, UPI, NET_BANKING, WALLET
    private String lastFourDigits;
    private String cardBrand;
    private String upiId;
    private String bankName;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

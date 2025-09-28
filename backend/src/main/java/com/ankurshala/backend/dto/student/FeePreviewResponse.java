package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeePreviewResponse {
    
    private BigDecimal fee;
    private String currency;
    private String reason;
    private Boolean waived;
    
    public FeePreviewResponse(BigDecimal fee, String currency, String reason, Boolean waived) {
        this.fee = fee;
        this.currency = currency;
        this.reason = reason;
        this.waived = waived;
    }
}

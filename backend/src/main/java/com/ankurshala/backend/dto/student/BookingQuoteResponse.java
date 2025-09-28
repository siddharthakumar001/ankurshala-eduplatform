package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingQuoteResponse {
    
    private Integer expectedMinutes;
    private LocalDateTime endTime;
    private Boolean bufferOk;
    private PriceInfo price;
    
    @Data
    public static class PriceInfo {
        private String currency;
        private BigDecimal min;
        private BigDecimal max;
        private Long ruleId;
        
        public PriceInfo(String currency, BigDecimal min, BigDecimal max, Long ruleId) {
            this.currency = currency;
            this.min = min;
            this.max = max;
            this.ruleId = ruleId;
        }
    }
}

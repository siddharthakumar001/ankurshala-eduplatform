package com.ankurshala.backend.dto.student;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class WalletTopupOrderResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private String receipt;
    private String status;
    private String message;
    private Map<String, Object> orderDetails;
}

package com.ankurshala.backend.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for payment order creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentOrderResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String keyId; // Razorpay key_id for frontend
    private String receipt;
    private String status;
    private Map<String, Object> orderDetails;
    private Long bookingId;
    private String message;
}

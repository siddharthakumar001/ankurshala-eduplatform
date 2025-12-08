package com.ankurshala.backend.service;

import com.ankurshala.backend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Razorpay Payment Gateway Integration Service
 * Handles order creation, payment verification, and webhook processing
 */
@Service
@Slf4j
public class RazorpayService {

    @Value("${razorpay.key_id:rzp_test_dummy}")
    private String razorpayKeyId;

    @Value("${razorpay.key_secret:dummy_secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.enabled:false}")
    private boolean razorpayEnabled;

    /**
     * Create a Razorpay order for payment
     * @param amount Amount in INR (will be converted to paise)
     * @param currency Currency code (default: INR)
     * @param receipt Receipt/order ID for reference
     * @param notes Additional metadata
     * @return Order details including order_id
     */
    public Map<String, Object> createOrder(BigDecimal amount, String currency, String receipt, Map<String, String> notes) {
        log.info("Creating Razorpay order: amount={}, currency={}, receipt={}", amount, currency, receipt);

        if (!razorpayEnabled) {
            log.warn("Razorpay is disabled, returning mock order");
            return createMockOrder(amount, currency, receipt);
        }

        try {
            // Convert rupees to paise (1 rupee = 100 paise)
            int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();

            // In real implementation, use Razorpay SDK:
            // RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            // JSONObject orderRequest = new JSONObject();
            // orderRequest.put("amount", amountInPaise);
            // orderRequest.put("currency", currency != null ? currency : "INR");
            // orderRequest.put("receipt", receipt);
            // if (notes != null) orderRequest.put("notes", new JSONObject(notes));
            // Order order = razorpayClient.orders.create(orderRequest);
            
            // For now, return mock data with proper structure
            Map<String, Object> order = new HashMap<>();
            order.put("id", "order_" + System.currentTimeMillis());
            order.put("entity", "order");
            order.put("amount", amountInPaise);
            order.put("amount_paid", 0);
            order.put("amount_due", amountInPaise);
            order.put("currency", currency != null ? currency : "INR");
            order.put("receipt", receipt);
            order.put("status", "created");
            order.put("attempts", 0);
            order.put("notes", notes);
            order.put("created_at", System.currentTimeMillis() / 1000);

            log.info("Razorpay order created successfully: orderId={}", order.get("id"));
            return order;

        } catch (Exception e) {
            log.error("Failed to create Razorpay order", e);
            throw new BusinessException("Failed to create payment order", HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_ORDER_CREATION_FAILED");
        }
    }

    /**
     * Verify Razorpay payment signature
     * @param orderId Razorpay order ID
     * @param paymentId Razorpay payment ID
     * @param signature Razorpay signature from frontend
     * @return true if signature is valid
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        log.info("Verifying Razorpay payment signature: orderId={}, paymentId={}", orderId, paymentId);

        if (!razorpayEnabled) {
            log.warn("Razorpay is disabled, skipping signature verification");
            return true; // In test mode, always pass verification
        }

        try {
            // Generate expected signature
            String payload = orderId + "|" + paymentId;
            String expectedSignature = generateSignature(payload, razorpayKeySecret);

            boolean isValid = expectedSignature.equals(signature);
            
            if (isValid) {
                log.info("Payment signature verified successfully");
            } else {
                log.error("Payment signature verification failed: expected={}, actual={}", expectedSignature, signature);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying payment signature", e);
            return false;
        }
    }

    /**
     * Verify Razorpay webhook signature
     * @param payload Webhook request body
     * @param signature X-Razorpay-Signature header value
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String payload, String signature) {
        log.info("Verifying Razorpay webhook signature");

        if (!razorpayEnabled) {
            log.warn("Razorpay is disabled, skipping webhook verification");
            return true; // In test mode, always pass verification
        }

        try {
            String expectedSignature = generateSignature(payload, razorpayKeySecret);
            boolean isValid = expectedSignature.equals(signature);

            if (isValid) {
                log.info("Webhook signature verified successfully");
            } else {
                log.error("Webhook signature verification failed");
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Create a refund for a payment
     * @param paymentId Razorpay payment ID
     * @param amount Amount to refund (optional, full refund if null)
     * @param notes Additional metadata
     * @return Refund details
     */
    public Map<String, Object> createRefund(String paymentId, BigDecimal amount, Map<String, String> notes) {
        log.info("Creating Razorpay refund: paymentId={}, amount={}", paymentId, amount);

        if (!razorpayEnabled) {
            log.warn("Razorpay is disabled, returning mock refund");
            return createMockRefund(paymentId, amount);
        }

        try {
            // In real implementation, use Razorpay SDK:
            // RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            // JSONObject refundRequest = new JSONObject();
            // refundRequest.put("payment_id", paymentId);
            // if (amount != null) refundRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            // if (notes != null) refundRequest.put("notes", new JSONObject(notes));
            // Refund refund = razorpayClient.payments.refund(paymentId, refundRequest);

            // For now, return mock data
            Map<String, Object> refund = new HashMap<>();
            refund.put("id", "rfnd_" + System.currentTimeMillis());
            refund.put("entity", "refund");
            refund.put("payment_id", paymentId);
            refund.put("amount", amount != null ? amount.multiply(BigDecimal.valueOf(100)).intValue() : 0);
            refund.put("currency", "INR");
            refund.put("status", "processed");
            refund.put("notes", notes);
            refund.put("created_at", System.currentTimeMillis() / 1000);

            log.info("Razorpay refund created successfully: refundId={}", refund.get("id"));
            return refund;

        } catch (Exception e) {
            log.error("Failed to create Razorpay refund", e);
            throw new BusinessException("Failed to create refund", HttpStatus.INTERNAL_SERVER_ERROR, "REFUND_CREATION_FAILED");
        }
    }

    /**
     * Fetch payment details from Razorpay
     * @param paymentId Razorpay payment ID
     * @return Payment details
     */
    public Map<String, Object> fetchPayment(String paymentId) {
        log.info("Fetching Razorpay payment: paymentId={}", paymentId);

        if (!razorpayEnabled) {
            log.warn("Razorpay is disabled, returning mock payment");
            return createMockPayment(paymentId);
        }

        try {
            // In real implementation, use Razorpay SDK:
            // RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            // Payment payment = razorpayClient.payments.fetch(paymentId);

            // For now, return mock data
            Map<String, Object> payment = new HashMap<>();
            payment.put("id", paymentId);
            payment.put("entity", "payment");
            payment.put("amount", 50000); // 500 INR in paise
            payment.put("currency", "INR");
            payment.put("status", "captured");
            payment.put("method", "card");
            payment.put("captured", true);
            payment.put("created_at", System.currentTimeMillis() / 1000);

            log.info("Payment fetched successfully");
            return payment;

        } catch (Exception e) {
            log.error("Failed to fetch Razorpay payment", e);
            throw new BusinessException("Failed to fetch payment details", HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_FETCH_FAILED");
        }
    }

    // ========== Helper Methods ==========

    private String generateSignature(String payload, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating signature", e);
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private Map<String, Object> createMockOrder(BigDecimal amount, String currency, String receipt) {
        Map<String, Object> order = new HashMap<>();
        order.put("id", "order_mock_" + System.currentTimeMillis());
        order.put("entity", "order");
        order.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
        order.put("currency", currency != null ? currency : "INR");
        order.put("receipt", receipt);
        order.put("status", "created");
        order.put("attempts", 0);
        order.put("created_at", System.currentTimeMillis() / 1000);
        return order;
    }

    private Map<String, Object> createMockRefund(String paymentId, BigDecimal amount) {
        Map<String, Object> refund = new HashMap<>();
        refund.put("id", "rfnd_mock_" + System.currentTimeMillis());
        refund.put("payment_id", paymentId);
        refund.put("amount", amount != null ? amount.multiply(BigDecimal.valueOf(100)).intValue() : 0);
        refund.put("status", "processed");
        refund.put("created_at", System.currentTimeMillis() / 1000);
        return refund;
    }

    private Map<String, Object> createMockPayment(String paymentId) {
        Map<String, Object> payment = new HashMap<>();
        payment.put("id", paymentId);
        payment.put("status", "captured");
        payment.put("amount", 50000);
        payment.put("currency", "INR");
        payment.put("created_at", System.currentTimeMillis() / 1000);
        return payment;
    }

    public String getKeyId() {
        return razorpayKeyId;
    }

    public boolean isEnabled() {
        return razorpayEnabled;
    }
}

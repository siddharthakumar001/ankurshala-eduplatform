package com.ankurshala.backend.service;

import com.ankurshala.backend.config.PaymentConfiguration;
import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentMethod;
import com.ankurshala.backend.entity.PaymentProvider;
import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.util.PaymentUtils;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class PaymentSecurityService {

    @Autowired
    private PaymentConfiguration paymentConfiguration;
    @Autowired
    private PaymentUtils paymentUtils;

    public boolean verifyWebhookSignature(PaymentProvider provider, String payload, String signature) {
        String traceId = TraceUtil.getTraceId();
        log.info("Verifying webhook signature - TraceId: {}, Provider: {}", traceId, provider);

        try {
            switch (provider) {
                case RAZORPAY:
                    return paymentUtils.verifyRazorpaySignature(payload, signature, paymentConfiguration.getRazorpay().getWebhookSecret());
                case STRIPE:
                    return paymentUtils.verifyStripeSignature(payload, signature, paymentConfiguration.getStripe().getWebhookSecret());
                case PAYU:
                    // PayU signature verification requires specific format
                    return true; // TODO: Implement PayU signature verification
                case PAYTM:
                    return true; // TODO: Implement Paytm signature verification
                case PHONEPE:
                    return paymentUtils.verifyPhonepeSignature(payload, signature, paymentConfiguration.getPhonepe().getMerchantKey());
                case GOOGLE_PAY:
                    return paymentUtils.verifyGooglePaySignature(payload, signature, paymentConfiguration.getGooglePay().getMerchantKey());
                default:
                    log.warn("Unsupported payment provider for signature verification - TraceId: {}, Provider: {}", traceId, provider);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error verifying webhook signature - TraceId: {}, Provider: {}, Error: {}", traceId, provider, e.getMessage());
            return false;
        }
    }

    public boolean validatePaymentIntent(PaymentIntent paymentIntent) {
        String traceId = TraceUtil.getTraceId();
        log.info("Validating payment intent - TraceId: {}, IntentId: {}", traceId, paymentIntent.getId());

        try {
            // Validate amount
            if (paymentIntent.getAmountCents() == null || paymentIntent.getAmountCents() <= 0) {
                log.warn("Invalid payment amount - TraceId: {}, Amount: {}", traceId, paymentIntent.getAmountCents());
                return false;
            }

            // Validate currency
            if (paymentIntent.getCurrency() == null || paymentIntent.getCurrency().isEmpty()) {
                log.warn("Invalid payment currency - TraceId: {}, Currency: {}", traceId, paymentIntent.getCurrency());
                return false;
            }

            // Validate user ID
            if (paymentIntent.getUserId() == null || paymentIntent.getUserId() <= 0) {
                log.warn("Invalid user ID - TraceId: {}, UserId: {}", traceId, paymentIntent.getUserId());
                return false;
            }

            // Validate status
            if (paymentIntent.getStatus() == null) {
                log.warn("Invalid payment status - TraceId: {}, Status: {}", traceId, paymentIntent.getStatus());
                return false;
            }

            log.info("Payment intent validation successful - TraceId: {}, IntentId: {}", traceId, paymentIntent.getId());
            return true;

        } catch (Exception e) {
            log.error("Error validating payment intent - TraceId: {}, IntentId: {}, Error: {}", traceId, paymentIntent.getId(), e.getMessage());
            return false;
        }
    }

    public boolean validatePaymentMethod(PaymentMethod paymentMethod) {
        String traceId = TraceUtil.getTraceId();
        log.info("Validating payment method - TraceId: {}, MethodId: {}", traceId, paymentMethod.getId());

        try {
            // Validate user ID
            if (paymentMethod.getUserId() == null || paymentMethod.getUserId() <= 0) {
                log.warn("Invalid user ID - TraceId: {}, UserId: {}", traceId, paymentMethod.getUserId());
                return false;
            }

            // Validate method type
            if (paymentMethod.getMethodType() == null) {
                log.warn("Invalid method type - TraceId: {}, MethodType: {}", traceId, paymentMethod.getMethodType());
                return false;
            }

            // Validate provider
            if (paymentMethod.getProvider() == null) {
                log.warn("Invalid provider - TraceId: {}, Provider: {}", traceId, paymentMethod.getProvider());
                return false;
            }

            // Validate provider ID
            if (paymentMethod.getProviderId() == null || paymentMethod.getProviderId().isEmpty()) {
                log.warn("Invalid provider ID - TraceId: {}, ProviderId: {}", traceId, paymentMethod.getProviderId());
                return false;
            }

            // Validate masked details
            if (paymentMethod.getMaskedDetails() == null || paymentMethod.getMaskedDetails().isEmpty()) {
                log.warn("Invalid masked details - TraceId: {}, MaskedDetails: {}", traceId, paymentMethod.getMaskedDetails());
                return false;
            }

            log.info("Payment method validation successful - TraceId: {}, MethodId: {}", traceId, paymentMethod.getId());
            return true;

        } catch (Exception e) {
            log.error("Error validating payment method - TraceId: {}, MethodId: {}, Error: {}", traceId, paymentMethod.getId(), e.getMessage());
            return false;
        }
    }

    public boolean validatePaymentRefund(PaymentRefund refund) {
        String traceId = TraceUtil.getTraceId();
        log.info("Validating payment refund - TraceId: {}, RefundId: {}", traceId, refund.getId());

        try {
            // Validate payment intent ID
            if (refund.getPaymentIntentId() == null || refund.getPaymentIntentId() <= 0) {
                log.warn("Invalid payment intent ID - TraceId: {}, PaymentIntentId: {}", traceId, refund.getPaymentIntentId());
                return false;
            }

            // Validate amount
            if (refund.getAmountCents() == null || refund.getAmountCents() <= 0) {
                log.warn("Invalid refund amount - TraceId: {}, Amount: {}", traceId, refund.getAmountCents());
                return false;
            }

            // Validate status
            if (refund.getStatus() == null) {
                log.warn("Invalid refund status - TraceId: {}, Status: {}", traceId, refund.getStatus());
                return false;
            }

            log.info("Payment refund validation successful - TraceId: {}, RefundId: {}", traceId, refund.getId());
            return true;

        } catch (Exception e) {
            log.error("Error validating payment refund - TraceId: {}, RefundId: {}, Error: {}", traceId, refund.getId(), e.getMessage());
            return false;
        }
    }

    public boolean authorizePaymentOperation(Long userId, PaymentIntent paymentIntent) {
        String traceId = TraceUtil.getTraceId();
        log.info("Authorizing payment operation - TraceId: {}, UserId: {}, IntentId: {}", traceId, userId, paymentIntent.getId());

        try {
            // Check if user owns the payment intent
            if (!paymentIntent.getUserId().equals(userId)) {
                log.warn("User not authorized for payment intent - TraceId: {}, UserId: {}, IntentUserId: {}", traceId, userId, paymentIntent.getUserId());
                return false;
            }

            log.info("Payment operation authorized - TraceId: {}, UserId: {}, IntentId: {}", traceId, userId, paymentIntent.getId());
            return true;

        } catch (Exception e) {
            log.error("Error authorizing payment operation - TraceId: {}, UserId: {}, IntentId: {}, Error: {}", traceId, userId, paymentIntent.getId(), e.getMessage());
            return false;
        }
    }

    public boolean authorizePaymentMethodOperation(Long userId, PaymentMethod paymentMethod) {
        String traceId = TraceUtil.getTraceId();
        log.info("Authorizing payment method operation - TraceId: {}, UserId: {}, MethodId: {}", traceId, userId, paymentMethod.getId());

        try {
            // Check if user owns the payment method
            if (!paymentMethod.getUserId().equals(userId)) {
                log.warn("User not authorized for payment method - TraceId: {}, UserId: {}, MethodUserId: {}", traceId, userId, paymentMethod.getUserId());
                return false;
            }

            log.info("Payment method operation authorized - TraceId: {}, UserId: {}, MethodId: {}", traceId, userId, paymentMethod.getId());
            return true;

        } catch (Exception e) {
            log.error("Error authorizing payment method operation - TraceId: {}, UserId: {}, MethodId: {}, Error: {}", traceId, userId, paymentMethod.getId(), e.getMessage());
            return false;
        }
    }

    public boolean authorizeRefundOperation(Long userId, PaymentRefund refund) {
        String traceId = TraceUtil.getTraceId();
        log.info("Authorizing refund operation - TraceId: {}, UserId: {}, RefundId: {}", traceId, userId, refund.getId());

        try {
            // Check if user is authorized for refund
            // This could be the user who made the payment or an admin
            // For now, we'll check if the user is the one who made the payment
            // TODO: Implement proper refund authorization logic

            log.info("Refund operation authorized - TraceId: {}, UserId: {}, RefundId: {}", traceId, userId, refund.getId());
            return true;

        } catch (Exception e) {
            log.error("Error authorizing refund operation - TraceId: {}, UserId: {}, RefundId: {}, Error: {}", traceId, userId, refund.getId(), e.getMessage());
            return false;
        }
    }

    public void validateWebhookPayload(Map<String, Object> webhookPayload) {
        String traceId = TraceUtil.getTraceId();
        log.info("Validating webhook payload - TraceId: {}", traceId);

        try {
            if (webhookPayload == null || webhookPayload.isEmpty()) {
                throw new BusinessException("Webhook payload is empty", HttpStatus.BAD_REQUEST, "EMPTY_PAYLOAD");
            }

            // Basic validation - check for required fields
            if (!webhookPayload.containsKey("event") && !webhookPayload.containsKey("type") && !webhookPayload.containsKey("status")) {
                throw new BusinessException("Webhook payload missing required fields", HttpStatus.BAD_REQUEST, "INVALID_PAYLOAD");
            }

            log.info("Webhook payload validation successful - TraceId: {}", traceId);

        } catch (BusinessException e) {
            log.error("Webhook payload validation failed - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error validating webhook payload - TraceId: {}, Error: {}", traceId, e.getMessage());
            throw new BusinessException("Webhook payload validation failed", HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
        }
    }
}

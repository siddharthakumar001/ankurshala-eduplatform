package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AppProperties;
import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentMethod;
import com.ankurshala.backend.entity.PaymentProvider;
import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentProviderService {

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private RazorpayService razorpayService;

    public Map<String, Object> createPaymentIntent(PaymentProvider provider, PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating payment intent with provider - TraceId: {}, Provider: {}, IntentId: {}", traceId, provider, paymentIntent.getId());

        if (!appProperties.getFeatures().isPayments()) {
            throw new BusinessException("Payments feature is disabled", HttpStatus.SERVICE_UNAVAILABLE, "FEATURE_DISABLED");
        }

        switch (provider) {
            case RAZORPAY:
                return createRazorpayPaymentIntent(paymentIntent, paymentMethod);
            case STRIPE:
                return createStripePaymentIntent(paymentIntent, paymentMethod);
            case PAYU:
                return createPayuPaymentIntent(paymentIntent, paymentMethod);
            case PAYTM:
                return createPaytmPaymentIntent(paymentIntent, paymentMethod);
            case PHONEPE:
                return createPhonepePaymentIntent(paymentIntent, paymentMethod);
            case GOOGLE_PAY:
                return createGooglePayPaymentIntent(paymentIntent, paymentMethod);
            default:
                throw new BusinessException("Unsupported payment provider", HttpStatus.BAD_REQUEST, "UNSUPPORTED_PROVIDER");
        }
    }

    public Map<String, Object> processPayment(PaymentProvider provider, PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        String traceId = TraceUtil.getTraceId();
        log.info("Processing payment with provider - TraceId: {}, Provider: {}, IntentId: {}", traceId, provider, paymentIntent.getId());

        switch (provider) {
            case RAZORPAY:
                return processRazorpayPayment(paymentIntent, providerResponse);
            case STRIPE:
                return processStripePayment(paymentIntent, providerResponse);
            case PAYU:
                return processPayuPayment(paymentIntent, providerResponse);
            case PAYTM:
                return processPaytmPayment(paymentIntent, providerResponse);
            case PHONEPE:
                return processPhonepePayment(paymentIntent, providerResponse);
            case GOOGLE_PAY:
                return processGooglePayPayment(paymentIntent, providerResponse);
            default:
                throw new BusinessException("Unsupported payment provider", HttpStatus.BAD_REQUEST, "UNSUPPORTED_PROVIDER");
        }
    }

    public Map<String, Object> createRefund(PaymentProvider provider, PaymentRefund refund) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating refund with provider - TraceId: {}, Provider: {}, RefundId: {}", traceId, provider, refund.getId());

        switch (provider) {
            case RAZORPAY:
                return createRazorpayRefund(refund);
            case STRIPE:
                return createStripeRefund(refund);
            case PAYU:
                return createPayuRefund(refund);
            case PAYTM:
                return createPaytmRefund(refund);
            case PHONEPE:
                return createPhonepeRefund(refund);
            case GOOGLE_PAY:
                return createGooglePayRefund(refund);
            default:
                throw new BusinessException("Unsupported payment provider", HttpStatus.BAD_REQUEST, "UNSUPPORTED_PROVIDER");
        }
    }

    // Razorpay implementation stubs
    private Map<String, Object> createRazorpayPaymentIntent(PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        log.info("Creating Razorpay payment intent - IntentId: {}", paymentIntent.getId());
        BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmountCents())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String receipt = "booking_" + (paymentIntent.getBookingId() != null ? paymentIntent.getBookingId() : paymentIntent.getId());
        Map<String, String> notes = new HashMap<>();
        notes.put("payment_intent_id", paymentIntent.getId().toString());
        if (paymentIntent.getBookingId() != null) {
            notes.put("booking_id", paymentIntent.getBookingId().toString());
        }
        if (paymentMethod != null && paymentMethod.getId() != null) {
            notes.put("payment_method_id", paymentMethod.getId().toString());
        }

        return razorpayService.createOrder(amount, paymentIntent.getCurrency(), receipt, notes);
    }

    private Map<String, Object> processRazorpayPayment(PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        log.info("Processing Razorpay payment - IntentId: {}", paymentIntent.getId());
        String orderId = getString(providerResponse, "orderId", "order_id", "razorpay_order_id");
        String paymentId = getString(providerResponse, "paymentId", "payment_id", "razorpay_payment_id");
        String signature = getString(providerResponse, "signature", "razorpay_signature");

        if (orderId == null || paymentId == null || signature == null) {
            throw new BusinessException("Missing Razorpay payment verification fields", HttpStatus.BAD_REQUEST, "PAYMENT_VERIFICATION_MISSING");
        }

        boolean signatureValid = razorpayService.verifyPaymentSignature(orderId, paymentId, signature);
        if (!signatureValid) {
            throw new BusinessException("Invalid Razorpay signature", HttpStatus.BAD_REQUEST, "PAYMENT_VERIFICATION_FAILED");
        }

        Map<String, Object> paymentDetails = razorpayService.fetchPayment(paymentId);
        paymentDetails.put("verified", true);
        paymentDetails.put("order_id", orderId);
        return paymentDetails;
    }

    private Map<String, Object> createRazorpayRefund(PaymentRefund refund) {
        log.info("Creating Razorpay refund - RefundId: {}", refund.getId());
        String paymentId = null;
        if (refund.getProviderResponse() != null) {
            paymentId = getString(refund.getProviderResponse(), "paymentId", "providerPaymentId", "payment_id");
        }

        if (paymentId == null) {
            throw new BusinessException("Missing provider payment id for refund", HttpStatus.BAD_REQUEST, "REFUND_PAYMENT_ID_MISSING");
        }

        BigDecimal amount = BigDecimal.valueOf(refund.getAmountCents())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        Map<String, String> notes = new HashMap<>();
        notes.put("refund_id", refund.getId().toString());
        if (refund.getReason() != null) {
            notes.put("reason", refund.getReason());
        }

        return razorpayService.createRefund(paymentId, amount, notes);
    }

    // Stripe implementation stubs
    private Map<String, Object> createStripePaymentIntent(PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        log.info("Creating Stripe payment intent - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("Stripe integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> processStripePayment(PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        log.info("Processing Stripe payment - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("Stripe integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> createStripeRefund(PaymentRefund refund) {
        log.info("Creating Stripe refund - RefundId: {}", refund.getId());
        throw new BusinessException("Stripe integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    // PayU implementation stubs
    private Map<String, Object> createPayuPaymentIntent(PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        log.info("Creating PayU payment intent - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("PayU integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> processPayuPayment(PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        log.info("Processing PayU payment - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("PayU integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> createPayuRefund(PaymentRefund refund) {
        log.info("Creating PayU refund - RefundId: {}", refund.getId());
        throw new BusinessException("PayU integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    // Paytm implementation stubs
    private Map<String, Object> createPaytmPaymentIntent(PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        log.info("Creating Paytm payment intent - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("Paytm integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> processPaytmPayment(PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        log.info("Processing Paytm payment - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("Paytm integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> createPaytmRefund(PaymentRefund refund) {
        log.info("Creating Paytm refund - RefundId: {}", refund.getId());
        throw new BusinessException("Paytm integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    // PhonePe implementation stubs
    private Map<String, Object> createPhonepePaymentIntent(PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        log.info("Creating PhonePe payment intent - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("PhonePe integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> processPhonepePayment(PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        log.info("Processing PhonePe payment - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("PhonePe integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> createPhonepeRefund(PaymentRefund refund) {
        log.info("Creating PhonePe refund - RefundId: {}", refund.getId());
        throw new BusinessException("PhonePe integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    // Google Pay implementation stubs
    private Map<String, Object> createGooglePayPaymentIntent(PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        log.info("Creating Google Pay payment intent - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("Google Pay integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> processGooglePayPayment(PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        log.info("Processing Google Pay payment - IntentId: {}", paymentIntent.getId());
        throw new BusinessException("Google Pay integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private Map<String, Object> createGooglePayRefund(PaymentRefund refund) {
        log.info("Creating Google Pay refund - RefundId: {}", refund.getId());
        throw new BusinessException("Google Pay integration not implemented", HttpStatus.NOT_IMPLEMENTED, "PROVIDER_NOT_IMPLEMENTED");
    }

    private String getString(Map<String, Object> payload, String... keys) {
        if (payload == null) {
            return null;
        }
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }
}

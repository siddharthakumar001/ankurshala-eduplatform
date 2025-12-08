package com.ankurshala.backend.service;

import com.ankurshala.backend.config.PaymentConfiguration;
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

import java.util.Map;

@Service
@Slf4j
public class PaymentIntegrationService {

    @Autowired
    private PaymentConfiguration paymentConfiguration;
    @Autowired
    private PaymentProviderService paymentProviderService;
    @Autowired
    private PaymentWebhookService paymentWebhookService;

    public Map<String, Object> createPaymentIntent(PaymentProvider provider, PaymentIntent paymentIntent, PaymentMethod paymentMethod) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating payment intent with integration - TraceId: {}, Provider: {}, IntentId: {}", traceId, provider, paymentIntent.getId());

        // Check if provider is enabled
        if (!isProviderEnabled(provider)) {
            throw new BusinessException("Payment provider is not enabled", HttpStatus.SERVICE_UNAVAILABLE, "PROVIDER_DISABLED");
        }

        // Create payment intent with provider
        Map<String, Object> providerResponse = paymentProviderService.createPaymentIntent(provider, paymentIntent, paymentMethod);

        // Update payment intent with provider response
        paymentIntent.setProviderResponse(providerResponse);
        paymentIntent.setStatus(com.ankurshala.backend.entity.PaymentIntentStatus.PROCESSING);

        log.info("Payment intent created with provider - TraceId: {}, IntentId: {}, ProviderResponse: {}", traceId, paymentIntent.getId(), providerResponse);
        return providerResponse;
    }

    public Map<String, Object> processPayment(PaymentProvider provider, PaymentIntent paymentIntent, Map<String, Object> providerResponse) {
        String traceId = TraceUtil.getTraceId();
        log.info("Processing payment with integration - TraceId: {}, Provider: {}, IntentId: {}", traceId, provider, paymentIntent.getId());

        // Process payment with provider
        Map<String, Object> result = paymentProviderService.processPayment(provider, paymentIntent, providerResponse);

        // Update payment intent with result
        paymentIntent.setProviderResponse(result);

        log.info("Payment processed with provider - TraceId: {}, IntentId: {}, Result: {}", traceId, paymentIntent.getId(), result);
        return result;
    }

    public Map<String, Object> createRefund(PaymentProvider provider, PaymentRefund refund) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating refund with integration - TraceId: {}, Provider: {}, RefundId: {}", traceId, provider, refund.getId());

        // Check if provider is enabled
        if (!isProviderEnabled(provider)) {
            throw new BusinessException("Payment provider is not enabled", HttpStatus.SERVICE_UNAVAILABLE, "PROVIDER_DISABLED");
        }

        // Create refund with provider
        Map<String, Object> providerResponse = paymentProviderService.createRefund(provider, refund);

        // Update refund with provider response
        refund.setProviderResponse(providerResponse);
        refund.setStatus(com.ankurshala.backend.entity.PaymentRefundStatus.PROCESSING);

        log.info("Refund created with provider - TraceId: {}, RefundId: {}, ProviderResponse: {}", traceId, refund.getId(), providerResponse);
        return providerResponse;
    }

    public void handleWebhook(PaymentProvider provider, Map<String, Object> webhookPayload, String signature) {
        String traceId = TraceUtil.getTraceId();
        log.info("Handling webhook with integration - TraceId: {}, Provider: {}", traceId, provider);

        // Check if provider is enabled
        if (!isProviderEnabled(provider)) {
            throw new BusinessException("Payment provider is not enabled", HttpStatus.SERVICE_UNAVAILABLE, "PROVIDER_DISABLED");
        }

        // Handle webhook based on provider
        switch (provider) {
            case RAZORPAY:
                paymentWebhookService.handleRazorpayWebhook(webhookPayload, signature);
                break;
            case STRIPE:
                paymentWebhookService.handleStripeWebhook(webhookPayload, signature);
                break;
            case PAYU:
                paymentWebhookService.handlePayuWebhook(webhookPayload, signature);
                break;
            case PAYTM:
                // TODO: Implement Paytm webhook handling
                log.warn("Paytm webhook handling not implemented - TraceId: {}", traceId);
                break;
            case PHONEPE:
                // TODO: Implement PhonePe webhook handling
                log.warn("PhonePe webhook handling not implemented - TraceId: {}", traceId);
                break;
            case GOOGLE_PAY:
                // TODO: Implement Google Pay webhook handling
                log.warn("Google Pay webhook handling not implemented - TraceId: {}", traceId);
                break;
            default:
                throw new BusinessException("Unsupported payment provider", HttpStatus.BAD_REQUEST, "UNSUPPORTED_PROVIDER");
        }

        log.info("Webhook handled successfully - TraceId: {}, Provider: {}", traceId, provider);
    }

    private boolean isProviderEnabled(PaymentProvider provider) {
        switch (provider) {
            case RAZORPAY:
                return paymentConfiguration.getRazorpay().getEnabled();
            case STRIPE:
                return paymentConfiguration.getStripe().getEnabled();
            case PAYU:
                return paymentConfiguration.getPayu().getEnabled();
            case PAYTM:
                return paymentConfiguration.getPaytm().getEnabled();
            case PHONEPE:
                return paymentConfiguration.getPhonepe().getEnabled();
            case GOOGLE_PAY:
                return paymentConfiguration.getGooglePay().getEnabled();
            default:
                return false;
        }
    }

    public PaymentConfiguration.RazorpayConfig getRazorpayConfig() {
        return paymentConfiguration.getRazorpay();
    }

    public PaymentConfiguration.StripeConfig getStripeConfig() {
        return paymentConfiguration.getStripe();
    }

    public PaymentConfiguration.PayuConfig getPayuConfig() {
        return paymentConfiguration.getPayu();
    }

    public PaymentConfiguration.PaytmConfig getPaytmConfig() {
        return paymentConfiguration.getPaytm();
    }

    public PaymentConfiguration.PhonepeConfig getPhonepeConfig() {
        return paymentConfiguration.getPhonepe();
    }

    public PaymentConfiguration.GooglePayConfig getGooglePayConfig() {
        return paymentConfiguration.getGooglePay();
    }
}

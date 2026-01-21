package com.ankurshala.backend.service;

import com.ankurshala.backend.config.AppProperties;
import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentMethod;
import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.entity.PaymentIntentStatus;
import com.ankurshala.backend.entity.PaymentRefundStatus;
import com.ankurshala.backend.entity.PaymentMethodType;
import com.ankurshala.backend.entity.PaymentProvider;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.PaymentIntentRepository;
import com.ankurshala.backend.repository.PaymentMethodRepository;
import com.ankurshala.backend.repository.PaymentRefundRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentIntentRepository paymentIntentRepository;
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    @Autowired
    private PaymentRefundRepository paymentRefundRepository;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private PaymentProviderService paymentProviderService;

    public PaymentIntent createPaymentIntent(Long userId, Long bookingId, Integer amountCents, String currency, Long paymentMethodId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating payment intent - TraceId: {}, UserId: {}, BookingId: {}, Amount: {}", traceId, userId, bookingId, amountCents);

        if (!appProperties.getFeatures().isPayments()) {
            throw new BusinessException("Payments feature is disabled", HttpStatus.SERVICE_UNAVAILABLE, "FEATURE_DISABLED");
        }

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setUserId(userId);
        paymentIntent.setBookingId(bookingId);
        paymentIntent.setAmountCents(amountCents);
        paymentIntent.setCurrency(currency != null ? currency : "INR");
        paymentIntent.setStatus(PaymentIntentStatus.CREATED);
        paymentIntent.setPaymentMethodId(paymentMethodId);

        PaymentIntent savedIntent = paymentIntentRepository.save(paymentIntent);
        log.info("Payment intent created - TraceId: {}, IntentId: {}", traceId, savedIntent.getId());

        return savedIntent;
    }

    public PaymentIntent processPayment(Long intentId, Map<String, Object> providerResponse) {
        String traceId = TraceUtil.getTraceId();
        log.info("Processing payment - TraceId: {}, IntentId: {}", traceId, intentId);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND, "PAYMENT_INTENT_NOT_FOUND"));

        if (paymentIntent.getStatus() != PaymentIntentStatus.CREATED) {
            throw new BusinessException("Payment intent is not in CREATED status", HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS");
        }

        paymentIntent.setStatus(PaymentIntentStatus.PROCESSING);
        paymentIntent.setProviderResponse(providerResponse);
        paymentIntent.setUpdatedAt(LocalDateTime.now());

        PaymentIntent savedIntent = paymentIntentRepository.save(paymentIntent);
        log.info("Payment processing started - TraceId: {}, IntentId: {}", traceId, intentId);

        return savedIntent;
    }

    public PaymentIntent completePayment(Long intentId, String providerPaymentId, String providerOrderId, Map<String, Object> providerResponse) {
        String traceId = TraceUtil.getTraceId();
        log.info("Completing payment - TraceId: {}, IntentId: {}, ProviderPaymentId: {}", traceId, intentId, providerPaymentId);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND, "PAYMENT_INTENT_NOT_FOUND"));

        if (paymentIntent.getStatus() != PaymentIntentStatus.PROCESSING) {
            throw new BusinessException("Payment intent is not in PROCESSING status", HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS");
        }

        paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
        paymentIntent.setProviderPaymentId(providerPaymentId);
        paymentIntent.setProviderOrderId(providerOrderId);
        paymentIntent.setProviderResponse(providerResponse);
        paymentIntent.setUpdatedAt(LocalDateTime.now());

        PaymentIntent savedIntent = paymentIntentRepository.save(paymentIntent);
        log.info("Payment completed - TraceId: {}, IntentId: {}", traceId, intentId);

        return savedIntent;
    }

    public PaymentIntent failPayment(Long intentId, String failureReason, Map<String, Object> providerResponse) {
        String traceId = TraceUtil.getTraceId();
        log.info("Failing payment - TraceId: {}, IntentId: {}, Reason: {}", traceId, intentId, failureReason);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND, "PAYMENT_INTENT_NOT_FOUND"));

        if (paymentIntent.getStatus() != PaymentIntentStatus.PROCESSING) {
            throw new BusinessException("Payment intent is not in PROCESSING status", HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS");
        }

        paymentIntent.setStatus(PaymentIntentStatus.FAILED);
        paymentIntent.setFailureReason(failureReason);
        paymentIntent.setProviderResponse(providerResponse);
        paymentIntent.setUpdatedAt(LocalDateTime.now());

        PaymentIntent savedIntent = paymentIntentRepository.save(paymentIntent);
        log.info("Payment failed - TraceId: {}, IntentId: {}", traceId, intentId);

        return savedIntent;
    }

    public PaymentRefund createRefund(Long paymentIntentId, Integer amountCents, String reason, Long processedBy) {
        String traceId = TraceUtil.getTraceId();
        log.info("Creating refund - TraceId: {}, PaymentIntentId: {}, Amount: {}", traceId, paymentIntentId, amountCents);

        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new BusinessException("Payment intent not found", HttpStatus.NOT_FOUND, "PAYMENT_INTENT_NOT_FOUND"));

        if (paymentIntent.getStatus() != PaymentIntentStatus.COMPLETED) {
            throw new BusinessException("Payment must be completed before refund", HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS");
        }

        PaymentRefund refund = new PaymentRefund();
        refund.setPaymentIntentId(paymentIntentId);
        refund.setAmountCents(amountCents);
        refund.setReason(reason);
        refund.setStatus(PaymentRefundStatus.PENDING);
        refund.setProcessedBy(processedBy);

        PaymentRefund savedRefund = paymentRefundRepository.save(refund);

        if (paymentIntent.getProviderPaymentId() == null || paymentIntent.getProviderPaymentId().isBlank()) {
            throw new BusinessException("Missing provider payment reference for refund", HttpStatus.BAD_REQUEST, "REFUND_MISSING_PROVIDER_PAYMENT_ID");
        }

        PaymentProvider provider = PaymentProvider.RAZORPAY;
        if (paymentIntent.getPaymentMethodId() != null) {
            provider = paymentMethodRepository.findById(paymentIntent.getPaymentMethodId())
                    .map(PaymentMethod::getProvider)
                    .orElse(PaymentProvider.RAZORPAY);
        }

        refund.setProviderResponse(Map.of("paymentId", paymentIntent.getProviderPaymentId()));
        Map<String, Object> providerResponse = paymentProviderService.createRefund(provider, refund);
        refund.setProviderResponse(providerResponse);
        Object refundId = providerResponse.get("id");
        if (refundId == null) {
            refundId = providerResponse.get("refund_id");
        }
        if (refundId != null) {
            refund.setProviderRefundId(refundId.toString());
        }
        refund.setStatus(PaymentRefundStatus.PROCESSING);

        savedRefund = paymentRefundRepository.save(refund);
        log.info("Refund created - TraceId: {}, RefundId: {}", traceId, savedRefund.getId());

        return savedRefund;
    }

    public List<PaymentIntent> getPaymentIntentsByUser(Long userId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching payment intents for user - TraceId: {}, UserId: {}", traceId, userId);

        return paymentIntentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<PaymentMethod> getPaymentMethodsByUser(Long userId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching payment methods for user - TraceId: {}, UserId: {}", traceId, userId);

        return paymentMethodRepository.findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId);
    }

    public PaymentMethod addPaymentMethod(Long userId, PaymentMethodType methodType, PaymentProvider provider, String providerId, String maskedDetails) {
        String traceId = TraceUtil.getTraceId();
        log.info("Adding payment method - TraceId: {}, UserId: {}, Type: {}, Provider: {}", traceId, userId, methodType, provider);

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setUserId(userId);
        paymentMethod.setMethodType(methodType);
        paymentMethod.setProvider(provider);
        paymentMethod.setProviderId(providerId);
        paymentMethod.setMaskedDetails(maskedDetails);
        paymentMethod.setIsDefault(false); // Will be set to true if it's the first method
        paymentMethod.setIsActive(true);

        // If this is the first payment method, make it default
        List<PaymentMethod> existingMethods = paymentMethodRepository.findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId);
        if (existingMethods.isEmpty()) {
            paymentMethod.setIsDefault(true);
        }

        PaymentMethod savedMethod = paymentMethodRepository.save(paymentMethod);
        log.info("Payment method added - TraceId: {}, MethodId: {}", traceId, savedMethod.getId());

        return savedMethod;
    }

    public void removePaymentMethod(Long methodId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Removing payment method - TraceId: {}, MethodId: {}", traceId, methodId);

        PaymentMethod paymentMethod = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new BusinessException("Payment method not found", HttpStatus.NOT_FOUND, "PAYMENT_METHOD_NOT_FOUND"));

        if (paymentMethod.getIsDefault()) {
            // If removing default method, make another method default
            List<PaymentMethod> otherMethods = paymentMethodRepository.findByUserIdAndIsActiveTrueAndIdNotOrderByCreatedAtAsc(paymentMethod.getUserId(), methodId);
            if (!otherMethods.isEmpty()) {
                PaymentMethod newDefault = otherMethods.get(0);
                newDefault.setIsDefault(true);
                paymentMethodRepository.save(newDefault);
            }
        }

        paymentMethod.setIsActive(false);
        paymentMethodRepository.save(paymentMethod);
        log.info("Payment method removed - TraceId: {}, MethodId: {}", traceId, methodId);
    }

    public Optional<PaymentIntent> getPaymentIntentById(Long intentId) {
        return paymentIntentRepository.findById(intentId);
    }

    public Optional<PaymentMethod> getPaymentMethodById(Long methodId) {
        return paymentMethodRepository.findById(methodId);
    }

    public Optional<PaymentRefund> getRefundById(Long refundId) {
        return paymentRefundRepository.findById(refundId);
    }
}

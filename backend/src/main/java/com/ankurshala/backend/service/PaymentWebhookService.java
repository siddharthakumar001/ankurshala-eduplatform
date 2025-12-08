package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentIntentStatus;
import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.entity.PaymentRefundStatus;
import com.ankurshala.backend.repository.PaymentIntentRepository;
import com.ankurshala.backend.repository.PaymentRefundRepository;
import com.ankurshala.backend.util.PaymentUtils;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class PaymentWebhookService {

    @Autowired
    private PaymentIntentRepository paymentIntentRepository;
    @Autowired
    private PaymentRefundRepository paymentRefundRepository;
    @Autowired
    private PaymentUtils paymentUtils;

    public void handleRazorpayWebhook(Map<String, Object> webhookPayload, String signature) {
        String traceId = TraceUtil.getTraceId();
        log.info("Handling Razorpay webhook - TraceId: {}, Event: {}", traceId, webhookPayload.get("event"));

        try {
            // TODO: Verify webhook signature using PaymentUtils.verifyRazorpaySignature()
            // TODO: Process different webhook events

            String event = (String) webhookPayload.get("event");
            Map<String, Object> entity = (Map<String, Object>) webhookPayload.get("entity");

            switch (event) {
                case "payment.captured":
                    handlePaymentCaptured(entity);
                    break;
                case "payment.failed":
                    handlePaymentFailed(entity);
                    break;
                case "refund.created":
                    handleRefundCreated(entity);
                    break;
                case "refund.processed":
                    handleRefundProcessed(entity);
                    break;
                default:
                    log.warn("Unhandled Razorpay webhook event - TraceId: {}, Event: {}", traceId, event);
            }

        } catch (Exception e) {
            log.error("Error handling Razorpay webhook - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw e;
        }
    }

    public void handleStripeWebhook(Map<String, Object> webhookPayload, String signature) {
        String traceId = TraceUtil.getTraceId();
        log.info("Handling Stripe webhook - TraceId: {}, Type: {}", traceId, webhookPayload.get("type"));

        try {
            // TODO: Verify webhook signature using PaymentUtils.verifyStripeSignature()
            // TODO: Process different webhook events

            String type = (String) webhookPayload.get("type");
            Map<String, Object> data = (Map<String, Object>) webhookPayload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");

            switch (type) {
                case "payment_intent.succeeded":
                    handlePaymentSucceeded(object);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentFailed(object);
                    break;
                case "charge.dispute.created":
                    handleChargeDispute(object);
                    break;
                default:
                    log.warn("Unhandled Stripe webhook event - TraceId: {}, Type: {}", traceId, type);
            }

        } catch (Exception e) {
            log.error("Error handling Stripe webhook - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw e;
        }
    }

    public void handlePayuWebhook(Map<String, Object> webhookPayload, String signature) {
        String traceId = TraceUtil.getTraceId();
        log.info("Handling PayU webhook - TraceId: {}, Status: {}", traceId, webhookPayload.get("status"));

        try {
            // TODO: Verify webhook signature using PaymentUtils.verifyPayuSignature()
            // TODO: Process different webhook events

            String status = (String) webhookPayload.get("status");
            String txnid = (String) webhookPayload.get("txnid");

            switch (status) {
                case "success":
                    handlePaymentSuccess(txnid, webhookPayload);
                    break;
                case "failure":
                    handlePaymentFailure(txnid, webhookPayload);
                    break;
                default:
                    log.warn("Unhandled PayU webhook status - TraceId: {}, Status: {}", traceId, status);
            }

        } catch (Exception e) {
            log.error("Error handling PayU webhook - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
            throw e;
        }
    }

    private void handlePaymentCaptured(Map<String, Object> entity) {
        String paymentId = (String) entity.get("id");
        log.info("Handling payment captured - PaymentId: {}", paymentId);

        // Find payment intent by provider payment ID
        Optional<PaymentIntent> paymentIntentOpt = paymentIntentRepository.findByProviderPaymentId(paymentId);
        if (paymentIntentOpt.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOpt.get();
            paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
            paymentIntent.setUpdatedAt(LocalDateTime.now());
            paymentIntentRepository.save(paymentIntent);
            log.info("Payment intent completed - IntentId: {}", paymentIntent.getId());
        } else {
            log.warn("Payment intent not found for payment ID: {}", paymentId);
        }
    }

    private void handlePaymentFailed(Map<String, Object> entity) {
        String paymentId = (String) entity.get("id");
        String failureReason = (String) entity.get("error_description");
        log.info("Handling payment failed - PaymentId: {}, Reason: {}", paymentId, failureReason);

        // Find payment intent by provider payment ID
        Optional<PaymentIntent> paymentIntentOpt = paymentIntentRepository.findByProviderPaymentId(paymentId);
        if (paymentIntentOpt.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOpt.get();
            paymentIntent.setStatus(PaymentIntentStatus.FAILED);
            paymentIntent.setFailureReason(failureReason);
            paymentIntent.setUpdatedAt(LocalDateTime.now());
            paymentIntentRepository.save(paymentIntent);
            log.info("Payment intent failed - IntentId: {}", paymentIntent.getId());
        } else {
            log.warn("Payment intent not found for payment ID: {}", paymentId);
        }
    }

    private void handleRefundCreated(Map<String, Object> entity) {
        String refundId = (String) entity.get("id");
        log.info("Handling refund created - RefundId: {}", refundId);

        // Find refund by provider refund ID
        Optional<PaymentRefund> refundOpt = paymentRefundRepository.findByProviderRefundId(refundId);
        if (refundOpt.isPresent()) {
            PaymentRefund refund = refundOpt.get();
            refund.setStatus(PaymentRefundStatus.PROCESSING);
            refund.setUpdatedAt(LocalDateTime.now());
            paymentRefundRepository.save(refund);
            log.info("Refund processing - RefundId: {}", refund.getId());
        } else {
            log.warn("Refund not found for provider refund ID: {}", refundId);
        }
    }

    private void handleRefundProcessed(Map<String, Object> entity) {
        String refundId = (String) entity.get("id");
        log.info("Handling refund processed - RefundId: {}", refundId);

        // Find refund by provider refund ID
        Optional<PaymentRefund> refundOpt = paymentRefundRepository.findByProviderRefundId(refundId);
        if (refundOpt.isPresent()) {
            PaymentRefund refund = refundOpt.get();
            refund.setStatus(PaymentRefundStatus.COMPLETED);
            refund.setProcessedAt(LocalDateTime.now());
            refund.setUpdatedAt(LocalDateTime.now());
            paymentRefundRepository.save(refund);
            log.info("Refund completed - RefundId: {}", refund.getId());
        } else {
            log.warn("Refund not found for provider refund ID: {}", refundId);
        }
    }

    private void handlePaymentSucceeded(Map<String, Object> object) {
        String paymentIntentId = (String) object.get("id");
        log.info("Handling payment succeeded - PaymentIntentId: {}", paymentIntentId);

        // Find payment intent by provider payment ID
        Optional<PaymentIntent> paymentIntentOpt = paymentIntentRepository.findByProviderPaymentId(paymentIntentId);
        if (paymentIntentOpt.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOpt.get();
            paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
            paymentIntent.setUpdatedAt(LocalDateTime.now());
            paymentIntentRepository.save(paymentIntent);
            log.info("Payment intent completed - IntentId: {}", paymentIntent.getId());
        } else {
            log.warn("Payment intent not found for payment ID: {}", paymentIntentId);
        }
    }

    private void handleChargeDispute(Map<String, Object> object) {
        String chargeId = (String) object.get("id");
        log.info("Handling charge dispute - ChargeId: {}", chargeId);

        // TODO: Handle charge dispute
        // TODO: Update payment intent status
        // TODO: Send notification to user
    }

    private void handlePaymentSuccess(String txnid, Map<String, Object> webhookPayload) {
        log.info("Handling payment success - TxnId: {}", txnid);

        // Find payment intent by provider order ID
        Optional<PaymentIntent> paymentIntentOpt = paymentIntentRepository.findByProviderOrderId(txnid);
        if (paymentIntentOpt.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOpt.get();
            paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
            paymentIntent.setUpdatedAt(LocalDateTime.now());
            paymentIntentRepository.save(paymentIntent);
            log.info("Payment intent completed - IntentId: {}", paymentIntent.getId());
        } else {
            log.warn("Payment intent not found for order ID: {}", txnid);
        }
    }

    private void handlePaymentFailure(String txnid, Map<String, Object> webhookPayload) {
        String failureReason = (String) webhookPayload.get("error_Message");
        log.info("Handling payment failure - TxnId: {}, Reason: {}", txnid, failureReason);

        // Find payment intent by provider order ID
        Optional<PaymentIntent> paymentIntentOpt = paymentIntentRepository.findByProviderOrderId(txnid);
        if (paymentIntentOpt.isPresent()) {
            PaymentIntent paymentIntent = paymentIntentOpt.get();
            paymentIntent.setStatus(PaymentIntentStatus.FAILED);
            paymentIntent.setFailureReason(failureReason);
            paymentIntent.setUpdatedAt(LocalDateTime.now());
            paymentIntentRepository.save(paymentIntent);
            log.info("Payment intent failed - IntentId: {}", paymentIntent.getId());
        } else {
            log.warn("Payment intent not found for order ID: {}", txnid);
        }
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentMethod;
import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class PaymentNotificationService {

    @Autowired
    private NotificationService notificationService;

    public void sendPaymentIntentCreatedNotification(PaymentIntent paymentIntent) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending payment intent created notification - TraceId: {}, IntentId: {}, UserId: {}", traceId, paymentIntent.getId(), paymentIntent.getUserId());

        try {
            notificationService.createNotification(
                    paymentIntent.getUserId(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Payment Intent Created",
                    "Your payment intent has been created successfully. Amount: ₹" + (paymentIntent.getAmountCents() / 100.0),
                    Map.of(
                            "paymentIntentId", paymentIntent.getId(),
                            "amount", paymentIntent.getAmountCents(),
                            "currency", paymentIntent.getCurrency(),
                            "status", paymentIntent.getStatus().name()
                    )
            );

            log.info("Payment intent created notification sent - TraceId: {}, IntentId: {}", traceId, paymentIntent.getId());
        } catch (Exception e) {
            log.error("Error sending payment intent created notification - TraceId: {}, IntentId: {}, Error: {}", traceId, paymentIntent.getId(), e.getMessage());
        }
    }

    public void sendPaymentIntentStatusNotification(PaymentIntent paymentIntent, String oldStatus, String newStatus) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending payment intent status notification - TraceId: {}, IntentId: {}, OldStatus: {}, NewStatus: {}", traceId, paymentIntent.getId(), oldStatus, newStatus);

        try {
            String title = "Payment Status Updated";
            String message = "Your payment status has been updated from " + oldStatus + " to " + newStatus;

            notificationService.createNotification(
                    paymentIntent.getUserId(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    title,
                    message,
                    Map.of(
                            "paymentIntentId", paymentIntent.getId(),
                            "oldStatus", oldStatus,
                            "newStatus", newStatus,
                            "amount", paymentIntent.getAmountCents(),
                            "currency", paymentIntent.getCurrency()
                    )
            );

            log.info("Payment intent status notification sent - TraceId: {}, IntentId: {}", traceId, paymentIntent.getId());
        } catch (Exception e) {
            log.error("Error sending payment intent status notification - TraceId: {}, IntentId: {}, Error: {}", traceId, paymentIntent.getId(), e.getMessage());
        }
    }

    public void sendPaymentMethodAddedNotification(PaymentMethod paymentMethod) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending payment method added notification - TraceId: {}, MethodId: {}, UserId: {}", traceId, paymentMethod.getId(), paymentMethod.getUserId());

        try {
            notificationService.createNotification(
                    paymentMethod.getUserId(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Payment Method Added",
                    "Your payment method has been added successfully. Method: " + paymentMethod.getMethodType() + " (" + paymentMethod.getProvider() + ")",
                    Map.of(
                            "paymentMethodId", paymentMethod.getId(),
                            "methodType", paymentMethod.getMethodType().name(),
                            "provider", paymentMethod.getProvider().name(),
                            "maskedDetails", paymentMethod.getMaskedDetails()
                    )
            );

            log.info("Payment method added notification sent - TraceId: {}, MethodId: {}", traceId, paymentMethod.getId());
        } catch (Exception e) {
            log.error("Error sending payment method added notification - TraceId: {}, MethodId: {}, Error: {}", traceId, paymentMethod.getId(), e.getMessage());
        }
    }

    public void sendPaymentMethodRemovedNotification(PaymentMethod paymentMethod) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending payment method removed notification - TraceId: {}, MethodId: {}, UserId: {}", traceId, paymentMethod.getId(), paymentMethod.getUserId());

        try {
            notificationService.createNotification(
                    paymentMethod.getUserId(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Payment Method Removed",
                    "Your payment method has been removed successfully. Method: " + paymentMethod.getMethodType() + " (" + paymentMethod.getProvider() + ")",
                    Map.of(
                            "paymentMethodId", paymentMethod.getId(),
                            "methodType", paymentMethod.getMethodType().name(),
                            "provider", paymentMethod.getProvider().name(),
                            "maskedDetails", paymentMethod.getMaskedDetails()
                    )
            );

            log.info("Payment method removed notification sent - TraceId: {}, MethodId: {}", traceId, paymentMethod.getId());
        } catch (Exception e) {
            log.error("Error sending payment method removed notification - TraceId: {}, MethodId: {}, Error: {}", traceId, paymentMethod.getId(), e.getMessage());
        }
    }

    public void sendRefundCreatedNotification(PaymentRefund refund) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending refund created notification - TraceId: {}, RefundId: {}, PaymentIntentId: {}", traceId, refund.getId(), refund.getPaymentIntentId());

        try {
            notificationService.createNotification(
                    refund.getProcessedBy(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Refund Created",
                    "Your refund has been created successfully. Amount: ₹" + (refund.getAmountCents() / 100.0) + ", Reason: " + refund.getReason(),
                    Map.of(
                            "refundId", refund.getId(),
                            "paymentIntentId", refund.getPaymentIntentId(),
                            "amount", refund.getAmountCents(),
                            "reason", refund.getReason(),
                            "status", refund.getStatus().name()
                    )
            );

            log.info("Refund created notification sent - TraceId: {}, RefundId: {}", traceId, refund.getId());
        } catch (Exception e) {
            log.error("Error sending refund created notification - TraceId: {}, RefundId: {}, Error: {}", traceId, refund.getId(), e.getMessage());
        }
    }

    public void sendRefundStatusNotification(PaymentRefund refund, String oldStatus, String newStatus) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending refund status notification - TraceId: {}, RefundId: {}, OldStatus: {}, NewStatus: {}", traceId, refund.getId(), oldStatus, newStatus);

        try {
            String title = "Refund Status Updated";
            String message = "Your refund status has been updated from " + oldStatus + " to " + newStatus;

            notificationService.createNotification(
                    refund.getProcessedBy(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    title,
                    message,
                    Map.of(
                            "refundId", refund.getId(),
                            "paymentIntentId", refund.getPaymentIntentId(),
                            "oldStatus", oldStatus,
                            "newStatus", newStatus,
                            "amount", refund.getAmountCents(),
                            "reason", refund.getReason()
                    )
            );

            log.info("Refund status notification sent - TraceId: {}, RefundId: {}", traceId, refund.getId());
        } catch (Exception e) {
            log.error("Error sending refund status notification - TraceId: {}, RefundId: {}, Error: {}", traceId, refund.getId(), e.getMessage());
        }
    }

    public void sendPaymentFailureNotification(PaymentIntent paymentIntent, String failureReason) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending payment failure notification - TraceId: {}, IntentId: {}, FailureReason: {}", traceId, paymentIntent.getId(), failureReason);

        try {
            notificationService.createNotification(
                    paymentIntent.getUserId(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Payment Failed",
                    "Your payment has failed. Reason: " + failureReason + ". Amount: ₹" + (paymentIntent.getAmountCents() / 100.0),
                    Map.of(
                            "paymentIntentId", paymentIntent.getId(),
                            "amount", paymentIntent.getAmountCents(),
                            "currency", paymentIntent.getCurrency(),
                            "failureReason", failureReason,
                            "status", paymentIntent.getStatus().name()
                    )
            );

            log.info("Payment failure notification sent - TraceId: {}, IntentId: {}", traceId, paymentIntent.getId());
        } catch (Exception e) {
            log.error("Error sending payment failure notification - TraceId: {}, IntentId: {}, Error: {}", traceId, paymentIntent.getId(), e.getMessage());
        }
    }

    public void sendPaymentSuccessNotification(PaymentIntent paymentIntent) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending payment success notification - TraceId: {}, IntentId: {}", traceId, paymentIntent.getId());

        try {
            notificationService.createNotification(
                    paymentIntent.getUserId(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Payment Successful",
                    "Your payment has been processed successfully. Amount: ₹" + (paymentIntent.getAmountCents() / 100.0),
                    Map.of(
                            "paymentIntentId", paymentIntent.getId(),
                            "amount", paymentIntent.getAmountCents(),
                            "currency", paymentIntent.getCurrency(),
                            "status", paymentIntent.getStatus().name()
                    )
            );

            log.info("Payment success notification sent - TraceId: {}, IntentId: {}", traceId, paymentIntent.getId());
        } catch (Exception e) {
            log.error("Error sending payment success notification - TraceId: {}, IntentId: {}, Error: {}", traceId, paymentIntent.getId(), e.getMessage());
        }
    }

    public void sendRefundFailureNotification(PaymentRefund refund, String failureReason) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending refund failure notification - TraceId: {}, RefundId: {}, FailureReason: {}", traceId, refund.getId(), failureReason);

        try {
            notificationService.createNotification(
                    refund.getProcessedBy(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Refund Failed",
                    "Your refund has failed. Reason: " + failureReason + ". Amount: ₹" + (refund.getAmountCents() / 100.0),
                    Map.of(
                            "refundId", refund.getId(),
                            "paymentIntentId", refund.getPaymentIntentId(),
                            "amount", refund.getAmountCents(),
                            "failureReason", failureReason,
                            "status", refund.getStatus().name()
                    )
            );

            log.info("Refund failure notification sent - TraceId: {}, RefundId: {}", traceId, refund.getId());
        } catch (Exception e) {
            log.error("Error sending refund failure notification - TraceId: {}, RefundId: {}, Error: {}", traceId, refund.getId(), e.getMessage());
        }
    }

    public void sendRefundSuccessNotification(PaymentRefund refund) {
        String traceId = TraceUtil.getTraceId();
        log.info("Sending refund success notification - TraceId: {}, RefundId: {}", traceId, refund.getId());

        try {
            notificationService.createNotification(
                    refund.getProcessedBy(),
                    com.ankurshala.backend.entity.NotificationType.GENERAL_ANNOUNCEMENT,
                    "Refund Successful",
                    "Your refund has been processed successfully. Amount: ₹" + (refund.getAmountCents() / 100.0),
                    Map.of(
                            "refundId", refund.getId(),
                            "paymentIntentId", refund.getPaymentIntentId(),
                            "amount", refund.getAmountCents(),
                            "reason", refund.getReason(),
                            "status", refund.getStatus().name()
                    )
            );

            log.info("Refund success notification sent - TraceId: {}, RefundId: {}", traceId, refund.getId());
        } catch (Exception e) {
            log.error("Error sending refund success notification - TraceId: {}, RefundId: {}, Error: {}", traceId, refund.getId(), e.getMessage());
        }
    }
}

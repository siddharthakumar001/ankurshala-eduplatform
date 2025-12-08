package com.ankurshala.backend.service;

import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class PaymentErrorHandler {

    @Autowired
    private PaymentAuditService paymentAuditService;
    @Autowired
    private PaymentMetricsService paymentMetricsService;
    @Autowired
    private PaymentNotificationService paymentNotificationService;

    public void handlePaymentError(String errorType, String errorMessage, String stackTrace, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        log.error("Handling payment error - TraceId: {}, ErrorType: {}, ErrorMessage: {}", traceId, errorType, errorMessage);

        try {
            // Log error details
            log.error("Payment error details - TraceId: {}, Context: {}, StackTrace: {}", traceId, context, stackTrace);

            // Record error metrics
            paymentMetricsService.recordErrorMetrics(errorType, errorMessage, stackTrace);

            // Record error audit
            paymentAuditService.auditPaymentSecurityEvent("PAYMENT_ERROR", null, errorMessage);

            // Send error notification if critical
            if (isCriticalError(errorType)) {
                sendCriticalErrorNotification(errorType, errorMessage, context);
            }

            log.info("Payment error handled - TraceId: {}, ErrorType: {}", traceId, errorType);
        } catch (Exception e) {
            log.error("Error handling payment error - TraceId: {}, ErrorType: {}, Error: {}", traceId, errorType, e.getMessage());
        }
    }

    public void handleWebhookError(String provider, String eventType, String errorMessage, String stackTrace, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        log.error("Handling webhook error - TraceId: {}, Provider: {}, EventType: {}, ErrorMessage: {}", traceId, provider, eventType, errorMessage);

        try {
            // Log error details
            log.error("Webhook error details - TraceId: {}, Context: {}, StackTrace: {}", traceId, context, stackTrace);

            // Record error metrics
            paymentMetricsService.recordErrorMetrics("WEBHOOK_ERROR", errorMessage, stackTrace);

            // Record error audit
            paymentAuditService.auditWebhookProcessing(provider, eventType, false, errorMessage);

            // Send error notification if critical
            if (isCriticalWebhookError(provider, eventType)) {
                sendCriticalWebhookErrorNotification(provider, eventType, errorMessage, context);
            }

            log.info("Webhook error handled - TraceId: {}, Provider: {}, EventType: {}", traceId, provider, eventType);
        } catch (Exception e) {
            log.error("Error handling webhook error - TraceId: {}, Provider: {}, EventType: {}, Error: {}", traceId, provider, eventType, e.getMessage());
        }
    }

    public void handleProviderError(String provider, String operation, String errorMessage, String stackTrace, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        log.error("Handling provider error - TraceId: {}, Provider: {}, Operation: {}, ErrorMessage: {}", traceId, provider, operation, errorMessage);

        try {
            // Log error details
            log.error("Provider error details - TraceId: {}, Context: {}, StackTrace: {}", traceId, context, stackTrace);

            // Record error metrics
            paymentMetricsService.recordErrorMetrics("PROVIDER_ERROR", errorMessage, stackTrace);

            // Record error audit
            paymentAuditService.auditPaymentProviderCall(provider, operation, false, errorMessage);

            // Send error notification if critical
            if (isCriticalProviderError(provider, operation)) {
                sendCriticalProviderErrorNotification(provider, operation, errorMessage, context);
            }

            log.info("Provider error handled - TraceId: {}, Provider: {}, Operation: {}", traceId, provider, operation);
        } catch (Exception e) {
            log.error("Error handling provider error - TraceId: {}, Provider: {}, Operation: {}, Error: {}", traceId, provider, operation, e.getMessage());
        }
    }

    public void handleSecurityError(String eventType, String userId, String errorMessage, String stackTrace, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        log.error("Handling security error - TraceId: {}, EventType: {}, UserId: {}, ErrorMessage: {}", traceId, eventType, userId, errorMessage);

        try {
            // Log error details
            log.error("Security error details - TraceId: {}, Context: {}, StackTrace: {}", traceId, context, stackTrace);

            // Record error metrics
            paymentMetricsService.recordErrorMetrics("SECURITY_ERROR", errorMessage, stackTrace);

            // Record error audit
            paymentAuditService.auditPaymentSecurityEvent(eventType, userId, errorMessage);

            // Send security alert notification
            sendSecurityAlertNotification(eventType, userId, errorMessage, context);

            log.info("Security error handled - TraceId: {}, EventType: {}, UserId: {}", traceId, eventType, userId);
        } catch (Exception e) {
            log.error("Error handling security error - TraceId: {}, EventType: {}, UserId: {}, Error: {}", traceId, eventType, userId, e.getMessage());
        }
    }

    public void handleComplianceError(String eventType, String userId, String errorMessage, String stackTrace, Map<String, Object> context) {
        String traceId = TraceUtil.getTraceId();
        log.error("Handling compliance error - TraceId: {}, EventType: {}, UserId: {}, ErrorMessage: {}", traceId, eventType, userId, errorMessage);

        try {
            // Log error details
            log.error("Compliance error details - TraceId: {}, Context: {}, StackTrace: {}", traceId, context, stackTrace);

            // Record error metrics
            paymentMetricsService.recordErrorMetrics("COMPLIANCE_ERROR", errorMessage, stackTrace);

            // Record error audit
            // paymentAuditService.auditComplianceEvent(eventType, userId, errorMessage);

            // Send compliance alert notification
            sendComplianceAlertNotification(eventType, userId, errorMessage, context);

            log.info("Compliance error handled - TraceId: {}, EventType: {}, UserId: {}", traceId, eventType, userId);
        } catch (Exception e) {
            log.error("Error handling compliance error - TraceId: {}, EventType: {}, UserId: {}, Error: {}", traceId, eventType, userId, e.getMessage());
        }
    }

    private boolean isCriticalError(String errorType) {
        return "PAYMENT_PROCESSING_ERROR".equals(errorType) ||
               "PAYMENT_INTENT_CREATION_ERROR".equals(errorType) ||
               "PAYMENT_INTENT_UPDATE_ERROR".equals(errorType) ||
               "PAYMENT_REFUND_ERROR".equals(errorType);
    }

    private boolean isCriticalWebhookError(String provider, String eventType) {
        return "payment.captured".equals(eventType) ||
               "payment.failed".equals(eventType) ||
               "payment_intent.succeeded".equals(eventType) ||
               "payment_intent.payment_failed".equals(eventType);
    }

    private boolean isCriticalProviderError(String provider, String operation) {
        return "create_payment_intent".equals(operation) ||
               "process_payment".equals(operation) ||
               "create_refund".equals(operation) ||
               "process_refund".equals(operation);
    }

    private void sendCriticalErrorNotification(String errorType, String errorMessage, Map<String, Object> context) {
        try {
            // TODO: Send critical error notification to admin
            log.warn("Critical payment error occurred - ErrorType: {}, ErrorMessage: {}, Context: {}", errorType, errorMessage, context);
        } catch (Exception e) {
            log.error("Error sending critical error notification: {}", e.getMessage());
        }
    }

    private void sendCriticalWebhookErrorNotification(String provider, String eventType, String errorMessage, Map<String, Object> context) {
        try {
            // TODO: Send critical webhook error notification to admin
            log.warn("Critical webhook error occurred - Provider: {}, EventType: {}, ErrorMessage: {}, Context: {}", provider, eventType, errorMessage, context);
        } catch (Exception e) {
            log.error("Error sending critical webhook error notification: {}", e.getMessage());
        }
    }

    private void sendCriticalProviderErrorNotification(String provider, String operation, String errorMessage, Map<String, Object> context) {
        try {
            // TODO: Send critical provider error notification to admin
            log.warn("Critical provider error occurred - Provider: {}, Operation: {}, ErrorMessage: {}, Context: {}", provider, operation, errorMessage, context);
        } catch (Exception e) {
            log.error("Error sending critical provider error notification: {}", e.getMessage());
        }
    }

    private void sendSecurityAlertNotification(String eventType, String userId, String errorMessage, Map<String, Object> context) {
        try {
            // TODO: Send security alert notification to admin
            log.warn("Security alert - EventType: {}, UserId: {}, ErrorMessage: {}, Context: {}", eventType, userId, errorMessage, context);
        } catch (Exception e) {
            log.error("Error sending security alert notification: {}", e.getMessage());
        }
    }

    private void sendComplianceAlertNotification(String eventType, String userId, String errorMessage, Map<String, Object> context) {
        try {
            // TODO: Send compliance alert notification to admin
            log.warn("Compliance alert - EventType: {}, UserId: {}, ErrorMessage: {}, Context: {}", eventType, userId, errorMessage, context);
        } catch (Exception e) {
            log.error("Error sending compliance alert notification: {}", e.getMessage());
        }
    }
}

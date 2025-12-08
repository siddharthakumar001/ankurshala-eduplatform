package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentMethod;
import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.entity.PaymentMetric;
import com.ankurshala.backend.repository.PaymentMetricRepository;
import com.ankurshala.backend.util.TraceUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentMetricsService {

    private final PaymentMetricRepository metricRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async
    public void recordPaymentIntentMetrics(PaymentIntent paymentIntent) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording payment intent metrics - TraceId: {}, IntentId: {}, Amount: {}, Currency: {}", 
                traceId, paymentIntent.getId(), paymentIntent.getAmountCents(), paymentIntent.getCurrency());

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("PAYMENT_INTENT")
                    .provider("RAZORPAY")  // Fixed: use hardcoded provider
                    .userId(paymentIntent.getUserId().toString())  // Fixed: use userId
                    .timestamp(LocalDateTime.now())
                    .success(paymentIntent.getStatus() != null)
                    .amountCents(paymentIntent.getAmountCents() != null ? paymentIntent.getAmountCents().longValue() : 0L)  // Fixed: convert to Long
                    .currency(paymentIntent.getCurrency())
                    .traceId(traceId)
                    .details("Payment intent created for booking: " + paymentIntent.getBookingId())
                    .metadata(serializeMetadata(Map.of(
                            "intentId", paymentIntent.getId(),
                            "bookingId", paymentIntent.getBookingId(),
                            "status", paymentIntent.getStatus()
                    )))
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "payment-intent", Map.of(
                    "metricId", metric.getId(),
                    "intentId", paymentIntent.getId(),
                    "amount", paymentIntent.getAmountCents(),
                    "currency", paymentIntent.getCurrency(),
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Payment intent metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record payment intent metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordPaymentMethodMetrics(PaymentMethod paymentMethod) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording payment method metrics - TraceId: {}, MethodId: {}, Provider: {}, MethodType: {}", 
                traceId, paymentMethod.getId(), paymentMethod.getProvider(), paymentMethod.getMethodType());

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("PAYMENT_METHOD")
                    .provider(paymentMethod.getProvider().name())
                    .userId(paymentMethod.getUserId().toString())
                    .timestamp(LocalDateTime.now())
                    .success(paymentMethod.getIsActive())
                    .traceId(traceId)
                    .details("Payment method type: " + paymentMethod.getMethodType())
                    .metadata(serializeMetadata(Map.of(
                            "methodId", paymentMethod.getId(),
                            "methodType", paymentMethod.getMethodType(),
                            "isActive", paymentMethod.getIsActive(),
                            "isDefault", paymentMethod.getIsDefault()
                    )))
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "payment-method", Map.of(
                    "metricId", metric.getId(),
                    "methodId", paymentMethod.getId(),
                    "provider", paymentMethod.getProvider().name(),
                    "methodType", paymentMethod.getMethodType().name(),
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Payment method metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record payment method metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordPaymentRefundMetrics(PaymentRefund refund) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording payment refund metrics - TraceId: {}, RefundId: {}, Amount: {}, Reason: {}", 
                traceId, refund.getId(), refund.getAmountCents(), refund.getReason());

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("PAYMENT_REFUND")
                    .provider("RAZORPAY")  // Fixed: use hardcoded provider
                    .userId(String.valueOf(refund.getProcessedBy() != null ? refund.getProcessedBy() : 0L))  // Fixed: use processedBy
                    .timestamp(LocalDateTime.now())
                    .success(refund.getStatus() != null)
                    .amountCents(refund.getAmountCents() != null ? refund.getAmountCents().longValue() : 0L)  // Fixed: convert to Long
                    .currency("INR")  // Fixed: hardcoded currency
                    .traceId(traceId)
                    .details("Refund reason: " + refund.getReason())
                    .metadata(serializeMetadata(Map.of(
                            "refundId", refund.getId(),
                            "paymentIntentId", refund.getPaymentIntentId(),
                            "status", refund.getStatus(),
                            "reason", refund.getReason() != null ? refund.getReason() : "N/A"
                    )))
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "payment-refund", Map.of(
                    "metricId", metric.getId(),
                    "refundId", refund.getId(),
                    "amount", refund.getAmountCents(),
                    "currency", "INR",
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Payment refund metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record payment refund metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordWebhookMetrics(String provider, String eventType, boolean success, long processingTime) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording webhook metrics - TraceId: {}, Provider: {}, EventType: {}, Success: {}, ProcessingTime: {}", 
                traceId, provider, eventType, success, processingTime);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("WEBHOOK")
                    .provider(provider)
                    .eventType(eventType)
                    .timestamp(LocalDateTime.now())
                    .success(success)
                    .executionTime(processingTime)
                    .traceId(traceId)
                    .details("Webhook event processed")
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "webhook", Map.of(
                    "metricId", metric.getId(),
                    "provider", provider,
                    "eventType", eventType,
                    "success", success,
                    "processingTime", processingTime,
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Webhook metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record webhook metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordProviderCallMetrics(String provider, String operation, boolean success, long responseTime) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording provider call metrics - TraceId: {}, Provider: {}, Operation: {}, Success: {}, ResponseTime: {}", 
                traceId, provider, operation, success, responseTime);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("PROVIDER_CALL")
                    .provider(provider)
                    .operation(operation)
                    .timestamp(LocalDateTime.now())
                    .success(success)
                    .executionTime(responseTime)
                    .traceId(traceId)
                    .details("API call to payment provider")
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "provider-call", Map.of(
                    "metricId", metric.getId(),
                    "provider", provider,
                    "operation", operation,
                    "success", success,
                    "responseTime", responseTime,
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Provider call metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record provider call metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordSecurityMetrics(String eventType, String userId, boolean success, String details) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording security metrics - TraceId: {}, EventType: {}, UserId: {}, Success: {}, Details: {}", 
                traceId, eventType, userId, success, details);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("SECURITY")
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .success(success)
                    .traceId(traceId)
                    .details(details)
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "security", Map.of(
                    "metricId", metric.getId(),
                    "eventType", eventType,
                    "userId", userId,
                    "success", success,
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Security metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record security metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordComplianceMetrics(String eventType, String userId, boolean success, String details) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording compliance metrics - TraceId: {}, EventType: {}, UserId: {}, Success: {}, Details: {}", 
                traceId, eventType, userId, success, details);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("COMPLIANCE")
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .success(success)
                    .traceId(traceId)
                    .details(details)
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "compliance", Map.of(
                    "metricId", metric.getId(),
                    "eventType", eventType,
                    "userId", userId,
                    "success", success,
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Compliance metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record compliance metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordAuditMetrics(String eventType, String userId, boolean success, String details) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording audit metrics - TraceId: {}, EventType: {}, UserId: {}, Success: {}, Details: {}", 
                traceId, eventType, userId, success, details);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("AUDIT")
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .success(success)
                    .traceId(traceId)
                    .details(details)
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "audit", Map.of(
                    "metricId", metric.getId(),
                    "eventType", eventType,
                    "userId", userId,
                    "success", success,
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Audit metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record audit metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordErrorMetrics(String errorType, String errorMessage, String stackTrace) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording error metrics - TraceId: {}, ErrorType: {}, ErrorMessage: {}", traceId, errorType, errorMessage);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("ERROR")
                    .eventType(errorType)
                    .timestamp(LocalDateTime.now())
                    .success(false)
                    .traceId(traceId)
                    .errorMessage(errorMessage)
                    .stackTrace(stackTrace)
                    .details("Error occurred during payment processing")
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "error", Map.of(
                    "metricId", metric.getId(),
                    "errorType", errorType,
                    "errorMessage", errorMessage,
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Error metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record error metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordPerformanceMetrics(String operation, long executionTime, String details) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording performance metrics - TraceId: {}, Operation: {}, ExecutionTime: {}", traceId, operation, executionTime);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("PERFORMANCE")
                    .operation(operation)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .executionTime(executionTime)
                    .traceId(traceId)
                    .details(details)
                    .build();

            metricRepository.save(metric);

            kafkaTemplate.send("payment-metrics", "performance", Map.of(
                    "metricId", metric.getId(),
                    "operation", operation,
                    "executionTime", executionTime,
                    "timestamp", metric.getTimestamp()
            ));

            log.debug("Performance metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record performance metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    @Async
    public void recordBusinessMetrics(String eventType, String userId, Map<String, Object> details) {
        String traceId = TraceUtil.getTraceId();
        log.info("Recording business metrics - TraceId: {}, EventType: {}, UserId: {}", traceId, eventType, userId);

        try {
            PaymentMetric metric = PaymentMetric.builder()
                    .metricType("BUSINESS")
                    .eventType(eventType)
                    .userId(userId)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .traceId(traceId)
                    .details("Business event tracked")
                    .metadata(serializeMetadata(details))
                    .build();

            metricRepository.save(metric);

            Map<String, Object> kafkaPayload = new HashMap<>(details);
            kafkaPayload.put("metricId", metric.getId());
            kafkaPayload.put("eventType", eventType);
            kafkaPayload.put("userId", userId);
            kafkaPayload.put("timestamp", metric.getTimestamp());

            kafkaTemplate.send("payment-metrics", "business", kafkaPayload);

            log.debug("Business metrics recorded successfully - MetricId: {}", metric.getId());
        } catch (Exception e) {
            log.error("Failed to record business metrics - TraceId: {}, Error: {}", traceId, e.getMessage(), e);
        }
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize metadata: {}", e.getMessage());
            return "{}";
        }
    }
}

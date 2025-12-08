package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.payment.PaymentIntentDto;
import com.ankurshala.backend.dto.payment.PaymentMethodDto;
import com.ankurshala.backend.dto.payment.PaymentRefundDto;
import com.ankurshala.backend.dto.payment.CreatePaymentIntentRequest;
import com.ankurshala.backend.dto.payment.AddPaymentMethodRequest;
import com.ankurshala.backend.dto.payment.CreateRefundRequest;
import com.ankurshala.backend.entity.PaymentIntent;
import com.ankurshala.backend.entity.PaymentMethod;
import com.ankurshala.backend.entity.PaymentRefund;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.service.PaymentService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private LoggingService loggingService;

    @PostMapping("/intent")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentIntentDto>> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", request.getBookingId());
        context.put("amountCents", request.getAmountCents());
        loggingService.logBusinessOperationStart("CREATE_PAYMENT_INTENT", userId.toString(), context);

        try {
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(
                    userId,
                    request.getBookingId(),
                    request.getAmountCents(),
                    request.getCurrency(),
                    request.getPaymentMethodId()
            );
            PaymentIntentDto paymentIntentDto = convertToIntentDto(paymentIntent);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_PAYMENT_INTENT", userId.toString(), true, executionTime);

            ApiResponse<PaymentIntentDto> apiResponse = ApiResponse.success(paymentIntentDto, "Payment intent created successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_PAYMENT_INTENT", userId.toString(), false, executionTime);
            loggingService.logError("CREATE_PAYMENT_INTENT", e, context);
            throw e;
        }
    }

    @PostMapping("/intent/{intentId}/process")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentIntentDto>> processPayment(
            @PathVariable Long intentId,
            @RequestBody Map<String, Object> providerResponse,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("intentId", intentId);
        loggingService.logBusinessOperationStart("PROCESS_PAYMENT", userId.toString(), context);

        try {
            PaymentIntent paymentIntent = paymentService.processPayment(intentId, providerResponse);
            PaymentIntentDto paymentIntentDto = convertToIntentDto(paymentIntent);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("PROCESS_PAYMENT", userId.toString(), true, executionTime);

            ApiResponse<PaymentIntentDto> apiResponse = ApiResponse.success(paymentIntentDto, "Payment processing started");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("PROCESS_PAYMENT", userId.toString(), false, executionTime);
            loggingService.logError("PROCESS_PAYMENT", e, context);
            throw e;
        }
    }

    @PostMapping("/intent/{intentId}/complete")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentIntentDto>> completePayment(
            @PathVariable Long intentId,
            @RequestParam String providerPaymentId,
            @RequestParam String providerOrderId,
            @RequestBody Map<String, Object> providerResponse,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("intentId", intentId);
        context.put("providerPaymentId", providerPaymentId);
        loggingService.logBusinessOperationStart("COMPLETE_PAYMENT", userId.toString(), context);

        try {
            PaymentIntent paymentIntent = paymentService.completePayment(intentId, providerPaymentId, providerOrderId, providerResponse);
            PaymentIntentDto paymentIntentDto = convertToIntentDto(paymentIntent);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("COMPLETE_PAYMENT", userId.toString(), true, executionTime);

            ApiResponse<PaymentIntentDto> apiResponse = ApiResponse.success(paymentIntentDto, "Payment completed successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("COMPLETE_PAYMENT", userId.toString(), false, executionTime);
            loggingService.logError("COMPLETE_PAYMENT", e, context);
            throw e;
        }
    }

    @PostMapping("/intent/{intentId}/fail")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentIntentDto>> failPayment(
            @PathVariable Long intentId,
            @RequestParam String failureReason,
            @RequestBody Map<String, Object> providerResponse,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("intentId", intentId);
        context.put("failureReason", failureReason);
        loggingService.logBusinessOperationStart("FAIL_PAYMENT", userId.toString(), context);

        try {
            PaymentIntent paymentIntent = paymentService.failPayment(intentId, failureReason, providerResponse);
            PaymentIntentDto paymentIntentDto = convertToIntentDto(paymentIntent);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("FAIL_PAYMENT", userId.toString(), true, executionTime);

            ApiResponse<PaymentIntentDto> apiResponse = ApiResponse.success(paymentIntentDto, "Payment failed");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("FAIL_PAYMENT", userId.toString(), false, executionTime);
            loggingService.logError("FAIL_PAYMENT", e, context);
            throw e;
        }
    }

    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentRefundDto>> createRefund(
            @Valid @RequestBody CreateRefundRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("paymentIntentId", request.getPaymentIntentId());
        context.put("amountCents", request.getAmountCents());
        loggingService.logBusinessOperationStart("CREATE_REFUND", userId.toString(), context);

        try {
            PaymentRefund refund = paymentService.createRefund(
                    request.getPaymentIntentId(),
                    request.getAmountCents(),
                    request.getReason(),
                    userId
            );
            PaymentRefundDto refundDto = convertToRefundDto(refund);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_REFUND", userId.toString(), true, executionTime);

            ApiResponse<PaymentRefundDto> apiResponse = ApiResponse.success(refundDto, "Refund created successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CREATE_REFUND", userId.toString(), false, executionTime);
            loggingService.logError("CREATE_REFUND", e, context);
            throw e;
        }
    }

    @GetMapping("/intents")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentIntentDto>>> getPaymentIntents(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_PAYMENT_INTENTS", userId.toString(), context);

        try {
            List<PaymentIntent> paymentIntents = paymentService.getPaymentIntentsByUser(userId);
            List<PaymentIntentDto> paymentIntentDtos = paymentIntents.stream()
                    .map(this::convertToIntentDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_INTENTS", userId.toString(), true, executionTime);

            ApiResponse<List<PaymentIntentDto>> apiResponse = ApiResponse.success(paymentIntentDtos, "Payment intents fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_INTENTS", userId.toString(), false, executionTime);
            loggingService.logError("GET_PAYMENT_INTENTS", e, context);
            throw e;
        }
    }

    @GetMapping("/methods")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentMethodDto>>> getPaymentMethods(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_PAYMENT_METHODS", userId.toString(), context);

        try {
            List<PaymentMethod> paymentMethods = paymentService.getPaymentMethodsByUser(userId);
            List<PaymentMethodDto> paymentMethodDtos = paymentMethods.stream()
                    .map(this::convertToMethodDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_METHODS", userId.toString(), true, executionTime);

            ApiResponse<List<PaymentMethodDto>> apiResponse = ApiResponse.success(paymentMethodDtos, "Payment methods fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_METHODS", userId.toString(), false, executionTime);
            loggingService.logError("GET_PAYMENT_METHODS", e, context);
            throw e;
        }
    }

    @PostMapping("/methods")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMethodDto>> addPaymentMethod(
            @Valid @RequestBody AddPaymentMethodRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("methodType", request.getMethodType());
        context.put("provider", request.getProvider());
        loggingService.logBusinessOperationStart("ADD_PAYMENT_METHOD", userId.toString(), context);

        try {
            PaymentMethod paymentMethod = paymentService.addPaymentMethod(
                    userId,
                    request.getMethodType(),
                    request.getProvider(),
                    request.getProviderId(),
                    request.getMaskedDetails()
            );
            PaymentMethodDto paymentMethodDto = convertToMethodDto(paymentMethod);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ADD_PAYMENT_METHOD", userId.toString(), true, executionTime);

            ApiResponse<PaymentMethodDto> apiResponse = ApiResponse.success(paymentMethodDto, "Payment method added successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ADD_PAYMENT_METHOD", userId.toString(), false, executionTime);
            loggingService.logError("ADD_PAYMENT_METHOD", e, context);
            throw e;
        }
    }

    @DeleteMapping("/methods/{methodId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removePaymentMethod(
            @PathVariable Long methodId,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("methodId", methodId);
        loggingService.logBusinessOperationStart("REMOVE_PAYMENT_METHOD", userId.toString(), context);

        try {
            paymentService.removePaymentMethod(methodId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REMOVE_PAYMENT_METHOD", userId.toString(), true, executionTime);

            ApiResponse<Void> apiResponse = ApiResponse.success(null, "Payment method removed successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REMOVE_PAYMENT_METHOD", userId.toString(), false, executionTime);
            loggingService.logError("REMOVE_PAYMENT_METHOD", e, context);
            throw e;
        }
    }

    private PaymentIntentDto convertToIntentDto(PaymentIntent paymentIntent) {
        PaymentIntentDto dto = new PaymentIntentDto();
        dto.setId(paymentIntent.getId());
        dto.setUserId(paymentIntent.getUserId());
        dto.setBookingId(paymentIntent.getBookingId());
        dto.setAmountCents(paymentIntent.getAmountCents());
        dto.setCurrency(paymentIntent.getCurrency());
        dto.setStatus(paymentIntent.getStatus());
        dto.setPaymentMethodId(paymentIntent.getPaymentMethodId());
        dto.setProviderPaymentId(paymentIntent.getProviderPaymentId());
        dto.setProviderOrderId(paymentIntent.getProviderOrderId());
        dto.setProviderResponse(paymentIntent.getProviderResponse());
        dto.setFailureReason(paymentIntent.getFailureReason());
        dto.setCreatedAt(paymentIntent.getCreatedAt());
        dto.setUpdatedAt(paymentIntent.getUpdatedAt());
        return dto;
    }

    private PaymentMethodDto convertToMethodDto(PaymentMethod paymentMethod) {
        PaymentMethodDto dto = new PaymentMethodDto();
        dto.setId(paymentMethod.getId());
        dto.setUserId(paymentMethod.getUserId());
        dto.setMethodType(paymentMethod.getMethodType());
        dto.setProvider(paymentMethod.getProvider());
        dto.setProviderId(paymentMethod.getProviderId());
        dto.setMaskedDetails(paymentMethod.getMaskedDetails());
        dto.setIsDefault(paymentMethod.getIsDefault());
        dto.setIsActive(paymentMethod.getIsActive());
        dto.setExpiresAt(paymentMethod.getExpiresAt());
        dto.setCreatedAt(paymentMethod.getCreatedAt());
        dto.setUpdatedAt(paymentMethod.getUpdatedAt());
        return dto;
    }

    private PaymentRefundDto convertToRefundDto(PaymentRefund refund) {
        PaymentRefundDto dto = new PaymentRefundDto();
        dto.setId(refund.getId());
        dto.setPaymentIntentId(refund.getPaymentIntentId());
        dto.setAmountCents(refund.getAmountCents());
        dto.setReason(refund.getReason());
        dto.setStatus(refund.getStatus());
        dto.setProviderRefundId(refund.getProviderRefundId());
        dto.setProviderResponse(refund.getProviderResponse());
        dto.setFailureReason(refund.getFailureReason());
        dto.setProcessedBy(refund.getProcessedBy());
        dto.setProcessedAt(refund.getProcessedAt());
        dto.setCreatedAt(refund.getCreatedAt());
        dto.setUpdatedAt(refund.getUpdatedAt());
        return dto;
    }
}

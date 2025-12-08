package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.service.PaymentHealthService;
import com.ankurshala.backend.util.TraceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "https://ankurshala.com", "https://www.ankurshala.com"}, maxAge = 3600)
@RestController
@RequestMapping("/api/payments/health")
public class PaymentHealthController {

    @Autowired
    private PaymentHealthService paymentHealthService;
    @Autowired
    private LoggingService loggingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentSystemHealth(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("GET_PAYMENT_SYSTEM_HEALTH", null, context);

        try {
            Map<String, Object> health = paymentHealthService.getPaymentSystemHealth();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_SYSTEM_HEALTH", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(health, "Payment system health retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_SYSTEM_HEALTH", null, false, executionTime);
            loggingService.logError("GET_PAYMENT_SYSTEM_HEALTH", e, context);
            throw e;
        }
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentSystemMetrics(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("GET_PAYMENT_SYSTEM_METRICS", null, context);

        try {
            Map<String, Object> metrics = paymentHealthService.getPaymentSystemMetrics();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_SYSTEM_METRICS", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(metrics, "Payment system metrics retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_SYSTEM_METRICS", null, false, executionTime);
            loggingService.logError("GET_PAYMENT_SYSTEM_METRICS", e, context);
            throw e;
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentSystemStatus(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("GET_PAYMENT_SYSTEM_STATUS", null, context);

        try {
            Map<String, Object> status = paymentHealthService.getPaymentSystemStatus();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_SYSTEM_STATUS", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(status, "Payment system status retrieved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PAYMENT_SYSTEM_STATUS", null, false, executionTime);
            loggingService.logError("GET_PAYMENT_SYSTEM_STATUS", e, context);
            throw e;
        }
    }
}

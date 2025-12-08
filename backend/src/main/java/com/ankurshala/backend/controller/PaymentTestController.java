package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.service.PaymentTestService;
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
@RequestMapping("/api/payments/test")
public class PaymentTestController {

    @Autowired
    private PaymentTestService paymentTestService;
    @Autowired
    private LoggingService loggingService;

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testPaymentSystem(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("TEST_PAYMENT_SYSTEM", null, context);

        try {
            Map<String, Object> testResults = paymentTestService.runPaymentSystemTests();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_SYSTEM", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(testResults, "Payment system test completed");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_SYSTEM", null, false, executionTime);
            loggingService.logError("TEST_PAYMENT_SYSTEM", e, context);
            throw e;
        }
    }

    @GetMapping("/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testPaymentProviders(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("TEST_PAYMENT_PROVIDERS", null, context);

        try {
            Map<String, Object> testResults = paymentTestService.runPaymentProviderTests();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_PROVIDERS", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(testResults, "Payment providers test completed");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_PROVIDERS", null, false, executionTime);
            loggingService.logError("TEST_PAYMENT_PROVIDERS", e, context);
            throw e;
        }
    }

    @GetMapping("/webhooks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testPaymentWebhooks(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("TEST_PAYMENT_WEBHOOKS", null, context);

        try {
            Map<String, Object> testResults = paymentTestService.runPaymentWebhookTests();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_WEBHOOKS", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(testResults, "Payment webhooks test completed");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_WEBHOOKS", null, false, executionTime);
            loggingService.logError("TEST_PAYMENT_WEBHOOKS", e, context);
            throw e;
        }
    }

    @GetMapping("/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testPaymentSecurity(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("TEST_PAYMENT_SECURITY", null, context);

        try {
            Map<String, Object> testResults = paymentTestService.runPaymentSecurityTests();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_SECURITY", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(testResults, "Payment security test completed");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_SECURITY", null, false, executionTime);
            loggingService.logError("TEST_PAYMENT_SECURITY", e, context);
            throw e;
        }
    }

    @GetMapping("/compliance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testPaymentCompliance(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("TEST_PAYMENT_COMPLIANCE", null, context);

        try {
            Map<String, Object> testResults = paymentTestService.runPaymentComplianceTests();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_COMPLIANCE", null, true, executionTime);

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(testResults, "Payment compliance test completed");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("TEST_PAYMENT_COMPLIANCE", null, false, executionTime);
            loggingService.logError("TEST_PAYMENT_COMPLIANCE", e, context);
            throw e;
        }
    }
}
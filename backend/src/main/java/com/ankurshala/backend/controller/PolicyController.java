package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.policy.PolicyAcceptanceRequest;
import com.ankurshala.backend.dto.policy.PolicyDto;
import com.ankurshala.backend.entity.Policy;
import com.ankurshala.backend.entity.PolicyAcceptance;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.service.PolicyService;
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
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;
    @Autowired
    private LoggingService loggingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PolicyDto>>> getActivePolicies(HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        loggingService.logBusinessOperationStart("GET_ACTIVE_POLICIES", null, context);

        try {
            List<Policy> policies = policyService.getActivePolicies();
            List<PolicyDto> policyDtos = policies.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_ACTIVE_POLICIES", null, true, executionTime);

            ApiResponse<List<PolicyDto>> apiResponse = ApiResponse.success(policyDtos, "Active policies fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_ACTIVE_POLICIES", null, false, executionTime);
            loggingService.logError("GET_ACTIVE_POLICIES", e, context);
            throw e;
        }
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<ApiResponse<List<PolicyDto>>> getPoliciesByCategory(
            @PathVariable String categoryName,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        context.put("categoryName", categoryName);
        loggingService.logBusinessOperationStart("GET_POLICIES_BY_CATEGORY", null, context);

        try {
            List<Policy> policies = policyService.getPoliciesByCategory(categoryName);
            List<PolicyDto> policyDtos = policies.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_POLICIES_BY_CATEGORY", null, true, executionTime);

            ApiResponse<List<PolicyDto>> apiResponse = ApiResponse.success(policyDtos, "Policies fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_POLICIES_BY_CATEGORY", null, false, executionTime);
            loggingService.logError("GET_POLICIES_BY_CATEGORY", e, context);
            throw e;
        }
    }

    @GetMapping("/category/{categoryName}/latest")
    public ResponseEntity<ApiResponse<PolicyDto>> getLatestPolicyByCategory(
            @PathVariable String categoryName,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        Map<String, Object> context = new HashMap<>();
        context.put("categoryName", categoryName);
        loggingService.logBusinessOperationStart("GET_LATEST_POLICY", null, context);

        try {
            Policy policy = policyService.getLatestPolicyByCategory(categoryName);
            PolicyDto policyDto = convertToDto(policy);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_LATEST_POLICY", null, true, executionTime);

            ApiResponse<PolicyDto> apiResponse = ApiResponse.success(policyDto, "Latest policy fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_LATEST_POLICY", null, false, executionTime);
            loggingService.logError("GET_LATEST_POLICY", e, context);
            throw e;
        }
    }

    @PostMapping("/accept")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<PolicyAcceptance>> acceptPolicy(
            @Valid @RequestBody PolicyAcceptanceRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("policyId", request.getPolicyId());
        loggingService.logBusinessOperationStart("ACCEPT_POLICY", userId.toString(), context);

        try {
            PolicyAcceptance acceptance = policyService.acceptPolicy(
                    userId,
                    request.getPolicyId(),
                    getClientIpAddress(httpRequest),
                    httpRequest.getHeader("User-Agent")
            );

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ACCEPT_POLICY", userId.toString(), true, executionTime);

            ApiResponse<PolicyAcceptance> apiResponse = ApiResponse.success(acceptance, "Policy accepted successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("ACCEPT_POLICY", userId.toString(), false, executionTime);
            loggingService.logError("ACCEPT_POLICY", e, context);
            throw e;
        }
    }

    @GetMapping("/acceptance-status/{policyId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> hasUserAcceptedPolicy(
            @PathVariable Long policyId,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("policyId", policyId);
        loggingService.logBusinessOperationStart("CHECK_POLICY_ACCEPTANCE", userId.toString(), context);

        try {
            boolean hasAccepted = policyService.hasUserAcceptedPolicy(userId, policyId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CHECK_POLICY_ACCEPTANCE", userId.toString(), true, executionTime);

            ApiResponse<Boolean> apiResponse = ApiResponse.success(hasAccepted, "Policy acceptance status checked");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CHECK_POLICY_ACCEPTANCE", userId.toString(), false, executionTime);
            loggingService.logError("CHECK_POLICY_ACCEPTANCE", e, context);
            throw e;
        }
    }

    @GetMapping("/acceptance-status/category/{categoryName}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> hasUserAcceptedLatestPolicy(
            @PathVariable String categoryName,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("categoryName", categoryName);
        loggingService.logBusinessOperationStart("CHECK_LATEST_POLICY_ACCEPTANCE", userId.toString(), context);

        try {
            boolean hasAccepted = policyService.hasUserAcceptedLatestPolicy(userId, categoryName);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CHECK_LATEST_POLICY_ACCEPTANCE", userId.toString(), true, executionTime);

            ApiResponse<Boolean> apiResponse = ApiResponse.success(hasAccepted, "Latest policy acceptance status checked");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CHECK_LATEST_POLICY_ACCEPTANCE", userId.toString(), false, executionTime);
            loggingService.logError("CHECK_LATEST_POLICY_ACCEPTANCE", e, context);
            throw e;
        }
    }

    @GetMapping("/my-acceptances")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PolicyAcceptance>>> getUserPolicyAcceptances(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_USER_POLICY_ACCEPTANCES", userId.toString(), context);

        try {
            List<PolicyAcceptance> acceptances = policyService.getUserPolicyAcceptances(userId);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_USER_POLICY_ACCEPTANCES", userId.toString(), true, executionTime);

            ApiResponse<List<PolicyAcceptance>> apiResponse = ApiResponse.success(acceptances, "User policy acceptances fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_USER_POLICY_ACCEPTANCES", userId.toString(), false, executionTime);
            loggingService.logError("GET_USER_POLICY_ACCEPTANCES", e, context);
            throw e;
        }
    }

    private PolicyDto convertToDto(Policy policy) {
        PolicyDto dto = new PolicyDto();
        dto.setId(policy.getId());
        dto.setTitle(policy.getTitle());
        dto.setContent(policy.getContent());
        dto.setVersion(policy.getVersion());
        dto.setEffectiveDate(policy.getEffectiveDate());
        dto.setExpiryDate(policy.getExpiryDate());
        dto.setActive(policy.getActive());
        dto.setCreatedAt(policy.getCreatedAt());
        dto.setUpdatedAt(policy.getUpdatedAt());
        return dto;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

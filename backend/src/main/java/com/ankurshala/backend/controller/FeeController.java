package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.fee.FeeApplicationDto;
import com.ankurshala.backend.dto.fee.FeeWaiverRequest;
import com.ankurshala.backend.dto.fee.FeeWaiverDto;
import com.ankurshala.backend.entity.FeeApplication;
import com.ankurshala.backend.entity.FeeWaiver;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.FeeService;
import com.ankurshala.backend.service.LoggingService;
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
@RequestMapping("/api/fees")
public class FeeController {

    @Autowired
    private FeeService feeService;
    @Autowired
    private LoggingService loggingService;

    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FeeApplicationDto>> calculateFee(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam Long amountCents,
            @RequestParam String feeCategoryName,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("entityType", entityType);
        context.put("entityId", entityId);
        context.put("amountCents", amountCents);
        context.put("feeCategoryName", feeCategoryName);
        loggingService.logBusinessOperationStart("CALCULATE_FEE", userId.toString(), context);

        try {
            FeeApplication feeApplication = feeService.calculateFee(entityType, entityId, amountCents, feeCategoryName);
            FeeApplicationDto feeApplicationDto = convertToDto(feeApplication);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CALCULATE_FEE", userId.toString(), true, executionTime);

            ApiResponse<FeeApplicationDto> apiResponse = ApiResponse.success(feeApplicationDto, "Fee calculated successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("CALCULATE_FEE", userId.toString(), false, executionTime);
            loggingService.logError("CALCULATE_FEE", e, context);
            throw e;
        }
    }

    @PostMapping("/apply/{feeApplicationId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FeeApplicationDto>> applyFee(
            @PathVariable Long feeApplicationId,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("feeApplicationId", feeApplicationId);
        loggingService.logBusinessOperationStart("APPLY_FEE", userId.toString(), context);

        try {
            FeeApplication feeApplication = feeService.applyFee(feeApplicationId);
            FeeApplicationDto feeApplicationDto = convertToDto(feeApplication);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("APPLY_FEE", userId.toString(), true, executionTime);

            ApiResponse<FeeApplicationDto> apiResponse = ApiResponse.success(feeApplicationDto, "Fee applied successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("APPLY_FEE", userId.toString(), false, executionTime);
            loggingService.logError("APPLY_FEE", e, context);
            throw e;
        }
    }

    @PostMapping("/waiver/request")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FeeWaiverDto>> requestFeeWaiver(
            @Valid @RequestBody FeeWaiverRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("feeApplicationId", request.getFeeApplicationId());
        context.put("waiverType", request.getWaiverType());
        loggingService.logBusinessOperationStart("REQUEST_FEE_WAIVER", userId.toString(), context);

        try {
            FeeWaiver feeWaiver = feeService.requestFeeWaiver(
                    userId,
                    request.getFeeApplicationId(),
                    request.getReason(),
                    request.getWaiverType(),
                    request.getWaiverAmountCents(),
                    request.getWaiverPercentage()
            );
            FeeWaiverDto feeWaiverDto = convertToDto(feeWaiver);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REQUEST_FEE_WAIVER", userId.toString(), true, executionTime);

            ApiResponse<FeeWaiverDto> apiResponse = ApiResponse.success(feeWaiverDto, "Fee waiver requested successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REQUEST_FEE_WAIVER", userId.toString(), false, executionTime);
            loggingService.logError("REQUEST_FEE_WAIVER", e, context);
            throw e;
        }
    }

    @PostMapping("/waiver/{waiverId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeWaiverDto>> approveFeeWaiver(
            @PathVariable Long waiverId,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("waiverId", waiverId);
        loggingService.logBusinessOperationStart("APPROVE_FEE_WAIVER", userId.toString(), context);

        try {
            FeeWaiver feeWaiver = feeService.approveFeeWaiver(waiverId, userId);
            FeeWaiverDto feeWaiverDto = convertToDto(feeWaiver);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("APPROVE_FEE_WAIVER", userId.toString(), true, executionTime);

            ApiResponse<FeeWaiverDto> apiResponse = ApiResponse.success(feeWaiverDto, "Fee waiver approved successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("APPROVE_FEE_WAIVER", userId.toString(), false, executionTime);
            loggingService.logError("APPROVE_FEE_WAIVER", e, context);
            throw e;
        }
    }

    @PostMapping("/waiver/{waiverId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FeeWaiverDto>> rejectFeeWaiver(
            @PathVariable Long waiverId,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("waiverId", waiverId);
        loggingService.logBusinessOperationStart("REJECT_FEE_WAIVER", userId.toString(), context);

        try {
            FeeWaiver feeWaiver = feeService.rejectFeeWaiver(waiverId, userId);
            FeeWaiverDto feeWaiverDto = convertToDto(feeWaiver);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REJECT_FEE_WAIVER", userId.toString(), true, executionTime);

            ApiResponse<FeeWaiverDto> apiResponse = ApiResponse.success(feeWaiverDto, "Fee waiver rejected successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("REJECT_FEE_WAIVER", userId.toString(), false, executionTime);
            loggingService.logError("REJECT_FEE_WAIVER", e, context);
            throw e;
        }
    }

    @GetMapping("/applications")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeApplicationDto>>> getFeeApplicationsByEntity(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("entityType", entityType);
        context.put("entityId", entityId);
        loggingService.logBusinessOperationStart("GET_FEE_APPLICATIONS", userId.toString(), context);

        try {
            List<FeeApplication> feeApplications = feeService.getFeeApplicationsByEntity(entityType, entityId);
            List<FeeApplicationDto> feeApplicationDtos = feeApplications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_FEE_APPLICATIONS", userId.toString(), true, executionTime);

            ApiResponse<List<FeeApplicationDto>> apiResponse = ApiResponse.success(feeApplicationDtos, "Fee applications fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_FEE_APPLICATIONS", userId.toString(), false, executionTime);
            loggingService.logError("GET_FEE_APPLICATIONS", e, context);
            throw e;
        }
    }

    @GetMapping("/waivers/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeWaiverDto>>> getMyFeeWaivers(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_MY_FEE_WAIVERS", userId.toString(), context);

        try {
            List<FeeWaiver> feeWaivers = feeService.getFeeWaiversByUser(userId);
            List<FeeWaiverDto> feeWaiverDtos = feeWaivers.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_MY_FEE_WAIVERS", userId.toString(), true, executionTime);

            ApiResponse<List<FeeWaiverDto>> apiResponse = ApiResponse.success(feeWaiverDtos, "Fee waivers fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_MY_FEE_WAIVERS", userId.toString(), false, executionTime);
            loggingService.logError("GET_MY_FEE_WAIVERS", e, context);
            throw e;
        }
    }

    @GetMapping("/waivers/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeeWaiverDto>>> getPendingFeeWaivers(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_PENDING_FEE_WAIVERS", userId.toString(), context);

        try {
            List<FeeWaiver> feeWaivers = feeService.getPendingFeeWaivers();
            List<FeeWaiverDto> feeWaiverDtos = feeWaivers.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PENDING_FEE_WAIVERS", userId.toString(), true, executionTime);

            ApiResponse<List<FeeWaiverDto>> apiResponse = ApiResponse.success(feeWaiverDtos, "Pending fee waivers fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_PENDING_FEE_WAIVERS", userId.toString(), false, executionTime);
            loggingService.logError("GET_PENDING_FEE_WAIVERS", e, context);
            throw e;
        }
    }

    private FeeApplicationDto convertToDto(FeeApplication feeApplication) {
        FeeApplicationDto dto = new FeeApplicationDto();
        dto.setId(feeApplication.getId());
        dto.setEntityType(feeApplication.getEntityType());
        dto.setEntityId(feeApplication.getEntityId());
        dto.setAmountCents(feeApplication.getAmountCents());
        dto.setFeeAmountCents(feeApplication.getFeeAmountCents());
        dto.setCalculatedAt(feeApplication.getCalculatedAt());
        dto.setAppliedAt(feeApplication.getAppliedAt());
        dto.setStatus(feeApplication.getStatus());
        dto.setCreatedAt(feeApplication.getCreatedAt());
        dto.setUpdatedAt(feeApplication.getUpdatedAt());
        return dto;
    }

    private FeeWaiverDto convertToDto(FeeWaiver feeWaiver) {
        FeeWaiverDto dto = new FeeWaiverDto();
        dto.setId(feeWaiver.getId());
        dto.setUserId(feeWaiver.getUserId());
        dto.setFeeApplicationId(feeWaiver.getFeeApplicationId());
        dto.setWaiverType(feeWaiver.getWaiverType());
        dto.setWaiverAmountCents(feeWaiver.getWaiverAmountCents());
        dto.setWaiverPercentage(feeWaiver.getWaiverPercentage());
        dto.setReason(feeWaiver.getReason());
        dto.setApprovedBy(feeWaiver.getApprovedBy() != null ? feeWaiver.getApprovedBy().getId() : null);
        dto.setApprovedAt(feeWaiver.getApprovedAt());
        dto.setStatus(feeWaiver.getStatus());
        dto.setExpiresAt(feeWaiver.getExpiresAt());
        dto.setCreatedAt(feeWaiver.getCreatedAt());
        dto.setUpdatedAt(feeWaiver.getUpdatedAt());
        return dto;
    }
}

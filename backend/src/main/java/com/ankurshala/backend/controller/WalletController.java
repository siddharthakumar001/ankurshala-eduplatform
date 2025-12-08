package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.common.ApiResponse;
import com.ankurshala.backend.dto.wallet.WalletBalanceDto;
import com.ankurshala.backend.dto.wallet.WalletTransactionDto;
import com.ankurshala.backend.dto.wallet.WalletTransferRequest;
import com.ankurshala.backend.entity.StudentWallet;
import com.ankurshala.backend.entity.TeacherWallet;
import com.ankurshala.backend.entity.WalletTransaction;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.LoggingService;
import com.ankurshala.backend.service.WalletService;
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
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;
    @Autowired
    private LoggingService loggingService;

    @GetMapping("/student/balance")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<WalletBalanceDto>> getStudentWalletBalance(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_STUDENT_WALLET_BALANCE", userId.toString(), context);

        try {
            StudentWallet wallet = walletService.getStudentWallet(userId);
            WalletBalanceDto balanceDto = convertToBalanceDto(wallet);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_WALLET_BALANCE", userId.toString(), true, executionTime);

            ApiResponse<WalletBalanceDto> apiResponse = ApiResponse.success(balanceDto, "Student wallet balance fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_WALLET_BALANCE", userId.toString(), false, executionTime);
            loggingService.logError("GET_STUDENT_WALLET_BALANCE", e, context);
            throw e;
        }
    }

    @GetMapping("/teacher/balance")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<WalletBalanceDto>> getTeacherWalletBalance(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_TEACHER_WALLET_BALANCE", userId.toString(), context);

        try {
            TeacherWallet wallet = walletService.getTeacherWallet(userId);
            WalletBalanceDto balanceDto = convertToBalanceDto(wallet);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_WALLET_BALANCE", userId.toString(), true, executionTime);

            ApiResponse<WalletBalanceDto> apiResponse = ApiResponse.success(balanceDto, "Teacher wallet balance fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_WALLET_BALANCE", userId.toString(), false, executionTime);
            loggingService.logError("GET_TEACHER_WALLET_BALANCE", e, context);
            throw e;
        }
    }

    @GetMapping("/student/transactions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<WalletTransactionDto>>> getStudentWalletTransactions(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_STUDENT_WALLET_TRANSACTIONS", userId.toString(), context);

        try {
            List<WalletTransaction> transactions = walletService.getStudentWalletTransactions(userId);
            List<WalletTransactionDto> transactionDtos = transactions.stream()
                    .map(this::convertToTransactionDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_WALLET_TRANSACTIONS", userId.toString(), true, executionTime);

            ApiResponse<List<WalletTransactionDto>> apiResponse = ApiResponse.success(transactionDtos, "Student wallet transactions fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_STUDENT_WALLET_TRANSACTIONS", userId.toString(), false, executionTime);
            loggingService.logError("GET_STUDENT_WALLET_TRANSACTIONS", e, context);
            throw e;
        }
    }

    @GetMapping("/teacher/transactions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<WalletTransactionDto>>> getTeacherWalletTransactions(
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        loggingService.logBusinessOperationStart("GET_TEACHER_WALLET_TRANSACTIONS", userId.toString(), context);

        try {
            List<WalletTransaction> transactions = walletService.getTeacherWalletTransactions(userId);
            List<WalletTransactionDto> transactionDtos = transactions.stream()
                    .map(this::convertToTransactionDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_WALLET_TRANSACTIONS", userId.toString(), true, executionTime);

            ApiResponse<List<WalletTransactionDto>> apiResponse = ApiResponse.success(transactionDtos, "Teacher wallet transactions fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_TEACHER_WALLET_TRANSACTIONS", userId.toString(), false, executionTime);
            loggingService.logError("GET_TEACHER_WALLET_TRANSACTIONS", e, context);
            throw e;
        }
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<WalletTransactionDto>> transferBetweenWallets(
            @Valid @RequestBody WalletTransferRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("fromUserId", request.getFromUserId());
        context.put("toUserId", request.getToUserId());
        context.put("amountCents", request.getAmountCents());
        loggingService.logBusinessOperationStart("WALLET_TRANSFER", userId.toString(), context);

        try {
            WalletTransaction transaction = walletService.transferBetweenWallets(
                    request.getFromUserId(),
                    request.getToUserId(),
                    request.getAmountCents(),
                    request.getFromUserType(),
                    request.getToUserType(),
                    request.getDescription()
            );
            WalletTransactionDto transactionDto = convertToTransactionDto(transaction);

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("WALLET_TRANSFER", userId.toString(), true, executionTime);

            ApiResponse<WalletTransactionDto> apiResponse = ApiResponse.success(transactionDto, "Wallet transfer completed successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("WALLET_TRANSFER", userId.toString(), false, executionTime);
            loggingService.logError("WALLET_TRANSFER", e, context);
            throw e;
        }
    }

    @GetMapping("/transactions/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<WalletTransactionDto>>> getWalletTransactionsByBooking(
            @PathVariable Long bookingId,
            Authentication authentication,
            HttpServletRequest request) {
        String traceId = TraceUtil.getTraceId();
        String requestId = TraceUtil.getRequestId();
        long startTime = System.currentTimeMillis();

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("bookingId", bookingId);
        loggingService.logBusinessOperationStart("GET_WALLET_TRANSACTIONS_BY_BOOKING", userId.toString(), context);

        try {
            List<WalletTransaction> transactions = walletService.getWalletTransactionsByBooking(bookingId);
            List<WalletTransactionDto> transactionDtos = transactions.stream()
                    .map(this::convertToTransactionDto)
                    .collect(Collectors.toList());

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_WALLET_TRANSACTIONS_BY_BOOKING", userId.toString(), true, executionTime);

            ApiResponse<List<WalletTransactionDto>> apiResponse = ApiResponse.success(transactionDtos, "Wallet transactions fetched successfully");
            apiResponse.setTraceId(traceId);
            apiResponse.setRequestId(requestId);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logBusinessOperationComplete("GET_WALLET_TRANSACTIONS_BY_BOOKING", userId.toString(), false, executionTime);
            loggingService.logError("GET_WALLET_TRANSACTIONS_BY_BOOKING", e, context);
            throw e;
        }
    }

    private WalletBalanceDto convertToBalanceDto(StudentWallet wallet) {
        WalletBalanceDto dto = new WalletBalanceDto();
        dto.setOwnerType("STUDENT");
        dto.setOwnerId(wallet.getStudentId());
        dto.setBalanceCents(wallet.getBalanceCents());
        dto.setCreatedAt(wallet.getCreatedAt());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }

    private WalletBalanceDto convertToBalanceDto(TeacherWallet wallet) {
        WalletBalanceDto dto = new WalletBalanceDto();
        dto.setOwnerType("TEACHER");
        dto.setOwnerId(wallet.getTeacherId());
        dto.setBalanceCents(wallet.getBalanceCents());
        dto.setCreatedAt(wallet.getCreatedAt());
        dto.setUpdatedAt(wallet.getUpdatedAt());
        return dto;
    }

    private WalletTransactionDto convertToTransactionDto(WalletTransaction transaction) {
        WalletTransactionDto dto = new WalletTransactionDto();
        dto.setId(transaction.getId());
        dto.setOwnerType(transaction.getOwnerType());
        dto.setOwnerId(transaction.getOwnerId());
        dto.setBookingId(transaction.getBookingId());
        dto.setType(transaction.getType());
        dto.setAmountCents(transaction.getAmountCents());
        dto.setSource(transaction.getSource());
        // Note: Meta field conversion not implemented
        dto.setMeta(null);
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}
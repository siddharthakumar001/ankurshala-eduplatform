package com.ankurshala.backend.controller;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.security.UserPrincipal;
import com.ankurshala.backend.service.StudentPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STUDENT')")
public class StudentPaymentController {

    private final StudentPaymentService paymentService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentBillingSummaryDto> getBillingSummary(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting billing summary for student {}", userPrincipal.getId());
        
        StudentBillingSummaryDto summary = paymentService.getBillingSummary(userPrincipal);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<StudentPaymentDto>> getPaymentHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        
        log.info("Getting payment history for student {}", userPrincipal.getId());
        
        Page<StudentPaymentDto> payments = paymentService.getPaymentHistory(userPrincipal, pageable);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/process")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProcessPaymentResponse> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Processing payment for student {}", userPrincipal.getId());
        
        ProcessPaymentResponse response = paymentService.processPayment(request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/methods")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<StudentPaymentMethodDto>> getPaymentMethods(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting payment methods for student {}", userPrincipal.getId());
        
        List<StudentPaymentMethodDto> methods = paymentService.getPaymentMethods(userPrincipal);
        return ResponseEntity.ok(methods);
    }

    @PostMapping("/create-order")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CreatePaymentOrderResponse> createPaymentOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Creating payment order for student {} and booking {}", userPrincipal.getId(), request.getBookingId());
        
        CreatePaymentOrderResponse response = paymentService.createPaymentOrder(request, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Verifying payment for student {} and booking {}", userPrincipal.getId(), request.getBookingId());
        
        VerifyPaymentResponse response = paymentService.verifyPayment(request, userPrincipal);
        return ResponseEntity.ok(response);
    }
}

package com.ankurshala.backend.service;

import com.ankurshala.backend.dto.student.*;
import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentPaymentService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FeeWaiverRepository feeWaiverRepository;
    private final NotificationRepository notificationRepository;
    private final RazorpayService razorpayService;
    private final PaymentIntentRepository paymentIntentRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public StudentBillingSummaryDto getBillingSummary(UserPrincipal userPrincipal) {
        log.info("Getting billing summary for student {}", userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        // Get all bookings for the student
        List<Booking> allBookings = bookingRepository.findByStudentOrderByStartTsDesc(student, 
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        
        // Calculate billing summary (mock implementation)
        BigDecimal totalAmount = BigDecimal.valueOf(5000);
        BigDecimal paidAmount = BigDecimal.valueOf(3000);
        BigDecimal pendingAmount = BigDecimal.valueOf(1500);
        BigDecimal overdueAmount = BigDecimal.valueOf(500);
        
        int totalInvoices = 10;
        int paidInvoices = 6;
        int pendingInvoices = 3;
        int overdueInvoices = 1;
        
        LocalDateTime lastPaymentDate = LocalDateTime.now().minusDays(2);
        BigDecimal monthlySpending = BigDecimal.valueOf(2000);
        BigDecimal yearlySpending = BigDecimal.valueOf(5000);
        
        // Mock default payment method
        StudentPaymentMethodDto defaultPaymentMethod = new StudentPaymentMethodDto();
        defaultPaymentMethod.setId(1L);
        defaultPaymentMethod.setType("CARD");
        defaultPaymentMethod.setLastFourDigits("1234");
        defaultPaymentMethod.setCardBrand("VISA");
        defaultPaymentMethod.setDefault(true);
        defaultPaymentMethod.setCreatedAt(LocalDateTime.now().minusMonths(1));
        defaultPaymentMethod.setUpdatedAt(LocalDateTime.now());
        
        StudentBillingSummaryDto summary = new StudentBillingSummaryDto();
        summary.setTotalAmount(totalAmount);
        summary.setPaidAmount(paidAmount);
        summary.setPendingAmount(pendingAmount);
        summary.setOverdueAmount(overdueAmount);
        summary.setTotalInvoices(totalInvoices);
        summary.setPaidInvoices(paidInvoices);
        summary.setPendingInvoices(pendingInvoices);
        summary.setOverdueInvoices(overdueInvoices);
        summary.setLastPaymentDate(lastPaymentDate);
        summary.setMonthlySpending(monthlySpending);
        summary.setYearlySpending(yearlySpending);
        summary.setDefaultPaymentMethod(defaultPaymentMethod);
        
        return summary;
    }

    public Page<StudentPaymentDto> getPaymentHistory(UserPrincipal userPrincipal, Pageable pageable) {
        log.info("Getting payment history for student {}", userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        // Get bookings for the student
        Page<Booking> bookings = bookingRepository.findByStudentOrderByStartTsDesc(student, pageable);
        
        return bookings.map(this::convertToPaymentDto);
    }

    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request, UserPrincipal userPrincipal) {
        log.info("Processing payment for student {} for booking {}", userPrincipal.getId(), request.getBookingId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        // Verify student owns this booking
        if (!booking.getStudent().getId().equals(student.getId())) {
            return new ProcessPaymentResponse() {{
                setSuccess(false);
                setMessage("You are not authorized to pay for this booking");
                setStatus("FAILED");
            }};
        }
        
        // Check if booking is completed
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            return new ProcessPaymentResponse() {{
                setSuccess(false);
                setMessage("Payment can only be processed for completed sessions");
                setStatus("FAILED");
            }};
        }
        
        // Check for fee waivers
        boolean hasFeeWaiver = feeWaiverRepository.existsByUser_IdAndStatus(student.getId(), FeeWaiver.WaiverStatus.APPROVED);
        if (hasFeeWaiver) {
            return new ProcessPaymentResponse() {{
                setSuccess(true);
                setTransactionId("WAIVER_" + booking.getId());
                setAmount(BigDecimal.ZERO);
                setStatus("WAIVED");
                setMessage("Payment waived due to fee waiver");
                setProcessedAt(LocalDateTime.now());
            }};
        }
        
        // Process payment (mock implementation)
        String transactionId = "TXN_" + System.currentTimeMillis();
        String paymentUrl = "https://payments.ankurshala.com/process/" + transactionId;
        
        // Send payment notification
        sendPaymentNotification(booking, student, request.getAmount());
        
        // Send Kafka event
        kafkaTemplate.send("payment-events", "payment-initiated", new Object() {{
            // Payment event data
        }});
        
        return new ProcessPaymentResponse() {{
            setSuccess(true);
            setTransactionId(transactionId);
            setPaymentUrl(paymentUrl);
            setAmount(request.getAmount());
            setStatus("PENDING");
            setMessage("Payment initiated successfully");
            setProcessedAt(LocalDateTime.now());
        }};
    }

    public List<StudentPaymentMethodDto> getPaymentMethods(UserPrincipal userPrincipal) {
        log.info("Getting payment methods for student {}", userPrincipal.getId());
        
        // Mock payment methods
        return List.of(
                new StudentPaymentMethodDto() {{
                    setId(1L);
                    setType("CARD");
                    setLastFourDigits("1234");
                    setCardBrand("VISA");
                    setDefault(true);
                    setCreatedAt(LocalDateTime.now().minusMonths(1));
                    setUpdatedAt(LocalDateTime.now());
                }},
                new StudentPaymentMethodDto() {{
                    setId(2L);
                    setType("UPI");
                    setUpiId("student@paytm");
                    setDefault(false);
                    setCreatedAt(LocalDateTime.now().minusWeeks(2));
                    setUpdatedAt(LocalDateTime.now());
                }}
        );
    }

    private StudentPaymentDto convertToPaymentDto(Booking booking) {
        BigDecimal amount = BigDecimal.valueOf(500); // Mock amount
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = amount.subtract(discountAmount);
        
        // Mock payment status
        String paymentStatus = booking.getId() % 2 == 0 ? "PAID" : "PENDING";
        String paymentMethod = booking.getId() % 2 == 0 ? "CARD" : "UPI";
        String transactionId = booking.getId() % 2 == 0 ? "TXN_" + booking.getId() : null;
        LocalDateTime paidAt = booking.getId() % 2 == 0 ? booking.getEndTs().toLocalDateTime() : null;
        
        return new StudentPaymentDto() {{
            setId(booking.getId());
            setBookingId(booking.getId());
            setBookingTitle(booking.getTopic().getTitle());
            setTeacherName(booking.getTeacher().getName());
            setSessionDate(booking.getStartTs().toLocalDateTime());
            setSessionDuration(booking.getDurationMinutes());
            setAmount(amount);
            setDiscountAmount(discountAmount);
            setFinalAmount(finalAmount);
            setPaymentStatus(paymentStatus);
            setPaymentMethod(paymentMethod);
            setTransactionId(transactionId);
            setPaidAt(paidAt);
            setDueDate(booking.getEndTs().plusDays(7).toLocalDateTime());
            setInvoiceUrl("/invoices/" + booking.getId() + ".pdf");
            setReceiptUrl(paidAt != null ? "/receipts/" + booking.getId() + ".pdf" : null);
        }};
    }

    private void sendPaymentNotification(Booking booking, User student, BigDecimal amount) {
        try {
            // Create notification for student
            Notification notification = new Notification();
            notification.setUser(student);
            notification.setTitle("Payment Processed");
            notification.setBody("Payment of ₹" + amount + " has been processed for your session with " + booking.getTeacher().getName());
            notification.setAudience(NotificationAudience.STUDENT);
            notification.setDelivery(NotificationDelivery.IN_APP);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            log.error("Failed to send payment notification", e);
        }
    }

    public CreatePaymentOrderResponse createPaymentOrder(CreatePaymentOrderRequest request, UserPrincipal userPrincipal) {
        log.info("Creating payment order for student {} and booking {}", userPrincipal.getId(), request.getBookingId());

        // Verify student and booking
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Verify student owns this booking
        if (!booking.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You are not authorized to pay for this booking");
        }

        // Create payment intent
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setUserId(userPrincipal.getId());
        paymentIntent.setBookingId(booking.getId());
        paymentIntent.setAmountCents(request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        paymentIntent.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        paymentIntent.setStatus(PaymentIntentStatus.CREATED);
        paymentIntent = paymentIntentRepository.save(paymentIntent);

        // Create Razorpay order
        String receipt = "booking_" + booking.getId() + "_" + System.currentTimeMillis();
        Map<String, String> notes = new HashMap<>();
        notes.put("booking_id", booking.getId().toString());
        notes.put("student_id", student.getId().toString());
        notes.put("teacher_id", booking.getTeacher().getId().toString());
        if (request.getNotes() != null) {
            notes.put("notes", request.getNotes());
        }

        Map<String, Object> razorpayOrder = razorpayService.createOrder(
                request.getAmount(),
                request.getCurrency(),
                receipt,
                notes
        );

        // Update payment intent with Razorpay order ID
        paymentIntent.setProviderOrderId((String) razorpayOrder.get("id"));
        paymentIntentRepository.save(paymentIntent);

        // Build response
        return CreatePaymentOrderResponse.builder()
                .orderId((String) razorpayOrder.get("id"))
                .amount(request.getAmount())
                .currency((String) razorpayOrder.get("currency"))
                .keyId(razorpayService.getKeyId())
                .receipt(receipt)
                .status("created")
                .orderDetails(razorpayOrder)
                .bookingId(booking.getId())
                .message("Payment order created successfully")
                .build();
    }

    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request, UserPrincipal userPrincipal) {
        log.info("Verifying payment for student {} and booking {}", userPrincipal.getId(), request.getBookingId());

        // Verify student and booking
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Verify student owns this booking
        if (!booking.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You are not authorized to verify payment for this booking");
        }

        // Verify payment signature
        boolean signatureValid = razorpayService.verifyPaymentSignature(
                request.getOrderId(),
                request.getPaymentId(),
                request.getSignature()
        );

        if (!signatureValid) {
            log.error("Payment signature verification failed for booking {}", booking.getId());
            return VerifyPaymentResponse.builder()
                    .success(false)
                    .status("FAILED")
                    .message("Payment verification failed. Invalid signature.")
                    .bookingId(booking.getId())
                    .build();
        }

        // Find payment intent
        PaymentIntent paymentIntent = paymentIntentRepository
                .findByBookingIdAndProviderOrderId(booking.getId(), request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found"));

        // Fetch payment details from Razorpay
        Map<String, Object> paymentDetails = razorpayService.fetchPayment(request.getPaymentId());

        // Update payment intent
        paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
        paymentIntent.setProviderPaymentId(request.getPaymentId());
        paymentIntent.setProviderResponse(paymentDetails);
        paymentIntent.setUpdatedAt(LocalDateTime.now());
        paymentIntentRepository.save(paymentIntent);

        // Update booking status if needed
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        // Send payment confirmation notification
        BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmountCents()).divide(BigDecimal.valueOf(100));
        sendPaymentNotification(booking, student, amount);

        // Send Kafka event
        kafkaTemplate.send("payment-events", "payment-success", paymentDetails);

        return VerifyPaymentResponse.builder()
                .success(true)
                .status("VERIFIED")
                .message("Payment verified successfully")
                .transactionId("TXN_" + paymentIntent.getId())
                .paymentId(request.getPaymentId())
                .orderId(request.getOrderId())
                .amount(amount)
                .currency(paymentIntent.getCurrency())
                .paidAt(LocalDateTime.now())
                .bookingId(booking.getId())
                .receiptUrl("/receipts/" + booking.getId() + ".pdf")
                .build();
    }
}

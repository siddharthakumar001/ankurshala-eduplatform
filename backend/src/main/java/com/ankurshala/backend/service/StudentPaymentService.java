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
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final PaymentMethodRepository paymentMethodRepository;
    private final WalletService walletService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired(required = false)
    private WebSocketNotificationService webSocketNotificationService;

    public StudentBillingSummaryDto getBillingSummary(UserPrincipal userPrincipal) {
        log.info("Getting billing summary for student {}", userPrincipal.getId());
        
        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        
        List<Booking> allBookings = bookingRepository.findByStudentOrderByStartTsDesc(
                student, PageRequest.of(0, 1000)).getContent();

        List<PaymentIntent> intents = paymentIntentRepository.findByUserIdOrderByCreatedAtDesc(student.getId());
        Map<Long, PaymentIntent> latestIntentByBooking = new HashMap<>();
        for (PaymentIntent intent : intents) {
            if (intent.getBookingId() != null && !latestIntentByBooking.containsKey(intent.getBookingId())) {
                latestIntentByBooking.put(intent.getBookingId(), intent);
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;

        int totalInvoices = allBookings.size();
        int paidInvoices = 0;
        int overdueInvoices = 0;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal monthlySpending = BigDecimal.ZERO;
        BigDecimal yearlySpending = BigDecimal.ZERO;

        for (PaymentIntent intent : intents) {
            if (intent.getStatus() == PaymentIntentStatus.COMPLETED) {
                BigDecimal amount = centsToRupees(intent.getAmountCents());
                paidAmount = paidAmount.add(amount);
                LocalDateTime timestamp = intent.getUpdatedAt() != null ? intent.getUpdatedAt() : intent.getCreatedAt();
                if (timestamp != null && !timestamp.isBefore(yearStart)) {
                    yearlySpending = yearlySpending.add(amount);
                }
                if (timestamp != null && !timestamp.isBefore(monthStart)) {
                    monthlySpending = monthlySpending.add(amount);
                }
            }
        }

        for (Booking booking : allBookings) {
            BigDecimal bookingAmount = centsToRupees(resolveBookingAmountCents(booking));
            totalAmount = totalAmount.add(bookingAmount);

            PaymentIntent intent = latestIntentByBooking.get(booking.getId());
            boolean isPaid = intent != null && intent.getStatus() == PaymentIntentStatus.COMPLETED;
            if (isPaid) {
                paidInvoices += 1;
            } else if (booking.getEndTs() != null &&
                    booking.getEndTs().toLocalDateTime().isBefore(now.minusDays(7))) {
                overdueInvoices += 1;
                overdueAmount = overdueAmount.add(bookingAmount);
            }
        }

        int pendingInvoices = Math.max(totalInvoices - paidInvoices, 0);
        BigDecimal pendingAmount = totalAmount.subtract(paidAmount);
        if (pendingAmount.compareTo(BigDecimal.ZERO) < 0) {
            pendingAmount = BigDecimal.ZERO;
        }

        LocalDateTime lastPaymentDate = intents.stream()
                .filter(intent -> intent.getStatus() == PaymentIntentStatus.COMPLETED)
                .map(intent -> intent.getUpdatedAt() != null ? intent.getUpdatedAt() : intent.getCreatedAt())
                .filter(date -> date != null)
                .findFirst()
                .orElse(null);

        StudentPaymentMethodDto defaultPaymentMethod = getDefaultPaymentMethod(student.getId());
        
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
        List<PaymentIntent> intents = paymentIntentRepository.findByUserIdOrderByCreatedAtDesc(student.getId());
        Map<Long, PaymentIntent> latestIntentByBooking = new HashMap<>();
        for (PaymentIntent intent : intents) {
            if (intent.getBookingId() != null && !latestIntentByBooking.containsKey(intent.getBookingId())) {
                latestIntentByBooking.put(intent.getBookingId(), intent);
            }
        }
        Map<Long, PaymentMethod> paymentMethods = paymentMethodRepository
                .findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(student.getId())
                .stream()
                .collect(Collectors.toMap(PaymentMethod::getId, method -> method, (a, b) -> a));

        return bookings.map(booking -> convertToPaymentDto(
                booking,
                latestIntentByBooking.get(booking.getId()),
                paymentMethods
        ));
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
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return new ProcessPaymentResponse() {{
                setSuccess(false);
                setMessage("Payment cannot be processed for cancelled sessions");
                setStatus("FAILED");
            }};
        }

        if (isBookingPaid(booking.getId(), student.getId())) {
            return new ProcessPaymentResponse() {{
                setSuccess(false);
                setMessage("This booking is already paid");
                setStatus("FAILED");
            }};
        }

        BigDecimal bookingAmount = centsToRupees(resolveBookingAmountCents(booking));
        
        // Check for fee waivers
        boolean hasFeeWaiver = feeWaiverRepository.existsByUser_IdAndStatus(student.getId(), FeeWaiver.WaiverStatus.APPROVED);
        if (hasFeeWaiver) {
            PaymentIntent paymentIntent = new PaymentIntent();
            paymentIntent.setUserId(student.getId());
            paymentIntent.setBookingId(booking.getId());
            paymentIntent.setAmountCents(0);
            paymentIntent.setCurrency("INR");
            paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
            paymentIntent.setProviderPaymentId("WAIVER_" + booking.getId());
            paymentIntent.setProviderResponse(Map.of(
                "waiverApplied", true,
                "originalAmountCents", resolveBookingAmountCents(booking)
            ));
            paymentIntentRepository.save(paymentIntent);

            if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.ACCEPTED) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
            }

            return new ProcessPaymentResponse() {{
                setSuccess(true);
                setTransactionId("WAIVER_" + booking.getId());
                setAmount(BigDecimal.ZERO);
                setStatus("WAIVED");
                setMessage("Payment waived due to fee waiver");
                setProcessedAt(LocalDateTime.now());
            }};
        }
        
        CreatePaymentOrderResponse orderResponse = createPaymentOrder(new CreatePaymentOrderRequest() {{
            setBookingId(request.getBookingId());
            setAmount(bookingAmount);
            setCurrency("INR");
            setNotes(request.getNotes());
        }}, userPrincipal);

        kafkaTemplate.send("payment-events", "payment-initiated", new Object() {{
            // Payment event data
        }});

        return new ProcessPaymentResponse() {{
            setSuccess(true);
            setTransactionId(orderResponse.getOrderId());
            setPaymentUrl(orderResponse.getOrderId());
            setAmount(bookingAmount);
            setStatus("PENDING");
            setMessage("Payment order created successfully");
            setProcessedAt(LocalDateTime.now());
        }};
    }

    public List<StudentPaymentMethodDto> getPaymentMethods(UserPrincipal userPrincipal) {
        log.info("Getting payment methods for student {}", userPrincipal.getId());
        List<PaymentMethod> methods = paymentMethodRepository
                .findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userPrincipal.getId());
        return methods.stream()
                .map(this::toStudentPaymentMethod)
                .collect(Collectors.toList());
    }

    public WalletPaymentResponse payBookingWithWallet(WalletPaymentRequest request, UserPrincipal userPrincipal) {
        log.info("Processing wallet payment for student {} and booking {}", userPrincipal.getId(), request.getBookingId());

        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You are not authorized to pay for this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Payment cannot be processed for cancelled bookings");
        }

        if (isBookingPaid(booking.getId(), student.getId())) {
            throw new IllegalArgumentException("This booking is already paid");
        }

        Integer amountCents = resolveBookingAmountCents(booking);
        BigDecimal amount = centsToRupees(amountCents);

        walletService.debitStudentWallet(
                student.getId(),
                amountCents.longValue(),
                WalletTransactionSource.BOOKING_PAYMENT,
                booking.getId(),
                "Booking payment"
        );

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setUserId(student.getId());
        paymentIntent.setBookingId(booking.getId());
        paymentIntent.setAmountCents(amountCents);
        paymentIntent.setCurrency("INR");
        paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
        paymentIntent.setProviderPaymentId("WALLET_" + booking.getId());
        paymentIntent.setProviderResponse(Map.of("method", "WALLET"));
        paymentIntentRepository.save(paymentIntent);

        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.ACCEPTED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        sendPaymentNotification(booking, student, amount);

        WalletPaymentResponse response = new WalletPaymentResponse();
        response.setSuccess(true);
        response.setStatus("PAID");
        response.setMessage("Wallet payment completed");
        response.setBookingId(booking.getId());
        response.setAmount(amount);
        response.setBalanceCents(walletService.getStudentWalletBalance(student.getId()));
        response.setPaidAt(LocalDateTime.now());
        return response;
    }

    public WalletTopupOrderResponse createWalletTopupOrder(WalletTopupOrderRequest request, UserPrincipal userPrincipal) {
        log.info("Creating wallet top-up order for student {}", userPrincipal.getId());

        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Top-up amount must be greater than zero");
        }

        if (request.getCurrency() != null && !"INR".equalsIgnoreCase(request.getCurrency())) {
            throw new IllegalArgumentException("Only INR is supported for wallet top-ups");
        }
        String currency = "INR";

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setUserId(student.getId());
        paymentIntent.setAmountCents(amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue());
        paymentIntent.setCurrency(currency);
        paymentIntent.setStatus(PaymentIntentStatus.CREATED);
        paymentIntent = paymentIntentRepository.save(paymentIntent);

        String receipt = "wallet_topup_" + student.getId() + "_" + System.currentTimeMillis();
        Map<String, String> notes = new HashMap<>();
        notes.put("student_id", student.getId().toString());
        notes.put("payment_intent_id", paymentIntent.getId().toString());
        if (request.getNotes() != null) {
            notes.put("notes", request.getNotes());
        }

        Map<String, Object> razorpayOrder = razorpayService.createOrder(
                amount,
                currency,
                receipt,
                notes
        );

        paymentIntent.setProviderOrderId((String) razorpayOrder.get("id"));
        paymentIntentRepository.save(paymentIntent);

        WalletTopupOrderResponse response = new WalletTopupOrderResponse();
        response.setOrderId((String) razorpayOrder.get("id"));
        response.setAmount(amount);
        response.setCurrency(currency);
        response.setKeyId(razorpayService.getKeyId());
        response.setReceipt(receipt);
        response.setStatus("created");
        response.setOrderDetails(razorpayOrder);
        response.setMessage("Wallet top-up order created successfully");
        return response;
    }

    public WalletTopupVerifyResponse verifyWalletTopup(WalletTopupVerifyRequest request, UserPrincipal userPrincipal) {
        log.info("Verifying wallet top-up for student {}", userPrincipal.getId());

        User student = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        boolean signatureValid = razorpayService.verifyPaymentSignature(
                request.getOrderId(),
                request.getPaymentId(),
                request.getSignature()
        );

        if (!signatureValid) {
            WalletTopupVerifyResponse response = new WalletTopupVerifyResponse();
            response.setSuccess(false);
            response.setStatus("FAILED");
            response.setMessage("Wallet top-up verification failed. Invalid signature.");
            return response;
        }

        PaymentIntent paymentIntent = paymentIntentRepository
                .findByProviderOrderId(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Payment intent not found"));

        if (!paymentIntent.getUserId().equals(student.getId())) {
            throw new IllegalArgumentException("You are not authorized to verify this top-up");
        }

        if (paymentIntent.getStatus() == PaymentIntentStatus.COMPLETED) {
            WalletTopupVerifyResponse response = new WalletTopupVerifyResponse();
            response.setSuccess(true);
            response.setStatus("VERIFIED");
            response.setMessage("Wallet top-up already verified");
            response.setTransactionId(paymentIntent.getProviderPaymentId());
            response.setAmount(centsToRupees(paymentIntent.getAmountCents()));
            response.setCurrency(paymentIntent.getCurrency());
            response.setBalanceCents(walletService.getStudentWalletBalance(student.getId()));
            response.setPaidAt(paymentIntent.getUpdatedAt() != null ? paymentIntent.getUpdatedAt() : LocalDateTime.now());
            return response;
        }

        Map<String, Object> paymentDetails = razorpayService.fetchPayment(request.getPaymentId());

        paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
        paymentIntent.setProviderPaymentId(request.getPaymentId());
        paymentIntent.setProviderResponse(paymentDetails);
        paymentIntent.setUpdatedAt(LocalDateTime.now());
        paymentIntentRepository.save(paymentIntent);

        walletService.creditStudentWallet(
                student.getId(),
                paymentIntent.getAmountCents().longValue(),
                WalletTransactionSource.WALLET_TOPUP,
                null,
                "Wallet top-up"
        );

        WalletTopupVerifyResponse response = new WalletTopupVerifyResponse();
        response.setSuccess(true);
        response.setStatus("VERIFIED");
        response.setMessage("Wallet top-up verified successfully");
        response.setTransactionId(request.getPaymentId());
        response.setAmount(centsToRupees(paymentIntent.getAmountCents()));
        response.setCurrency(paymentIntent.getCurrency());
        response.setBalanceCents(walletService.getStudentWalletBalance(student.getId()));
        response.setPaidAt(LocalDateTime.now());
        return response;
    }

    private StudentPaymentDto convertToPaymentDto(Booking booking, PaymentIntent paymentIntent, Map<Long, PaymentMethod> paymentMethods) {
        BigDecimal amount = centsToRupees(resolveBookingAmountCents(booking));
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = amount;

        String paymentStatus = "PENDING";
        String paymentMethod = null;
        String transactionId = null;
        LocalDateTime paidAt = null;

        if (paymentIntent != null) {
            switch (paymentIntent.getStatus()) {
                case COMPLETED:
                    paymentStatus = "PAID";
                    paidAt = paymentIntent.getUpdatedAt();
                    break;
                case FAILED:
                    paymentStatus = "FAILED";
                    break;
                case CANCELLED:
                    paymentStatus = "REFUNDED";
                    break;
                default:
                    paymentStatus = "PENDING";
            }

            transactionId = paymentIntent.getProviderPaymentId() != null
                    ? paymentIntent.getProviderPaymentId()
                    : "TXN_" + paymentIntent.getId();

            if (paymentIntent.getProviderResponse() != null &&
                Boolean.TRUE.equals(paymentIntent.getProviderResponse().get("waiverApplied"))) {
                discountAmount = amount;
                finalAmount = BigDecimal.ZERO;
            }

            if (paymentIntent.getPaymentMethodId() != null) {
                PaymentMethod method = paymentMethods.get(paymentIntent.getPaymentMethodId());
                if (method != null) {
                    paymentMethod = method.getMethodType().name();
                }
            } else if (paymentIntent.getProviderResponse() != null) {
                Object method = paymentIntent.getProviderResponse().get("method");
                if (method != null) {
                    paymentMethod = method.toString().toUpperCase();
                }
            }
        }

        StudentPaymentDto dto = new StudentPaymentDto();
        dto.setId(booking.getId());
        dto.setBookingId(booking.getId());
        dto.setBookingTitle(booking.getTopic() != null ? booking.getTopic().getTitle() : "Session");
        dto.setTeacherName(booking.getTeacher() != null ? booking.getTeacher().getName() : "TBD");
        dto.setSessionDate(booking.getStartTs().toLocalDateTime());
        dto.setSessionDuration(booking.getDurationMinutes());
        dto.setAmount(amount);
        dto.setDiscountAmount(discountAmount);
        dto.setFinalAmount(finalAmount);
        dto.setPaymentStatus(paymentStatus);
        dto.setPaymentMethod(paymentMethod);
        dto.setTransactionId(transactionId);
        dto.setPaidAt(paidAt);
        dto.setDueDate(booking.getEndTs().plusDays(7).toLocalDateTime());
        dto.setInvoiceUrl("/invoices/" + booking.getId() + ".pdf");
        dto.setReceiptUrl(paidAt != null ? "/receipts/" + booking.getId() + ".pdf" : null);
        return dto;
    }

    private boolean isBookingPaid(Long bookingId, Long studentId) {
        List<PaymentIntent> intents = paymentIntentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        for (PaymentIntent intent : intents) {
            if (intent.getUserId() != null && !intent.getUserId().equals(studentId)) {
                continue;
            }
            if (intent.getStatus() == PaymentIntentStatus.COMPLETED) {
                return true;
            }
        }
        return false;
    }

    private void sendPaymentNotification(Booking booking, User student, BigDecimal amount) {
        try {
            // Create notification for student
            Notification notification = new Notification();
            notification.setUser(student);
            notification.setTitle("Payment Processed");
            notification.setBody("Payment of INR " + amount + " has been processed for your session with " + booking.getTeacher().getName());
            notification.setAudience(NotificationAudience.STUDENT);
            notification.setDelivery(NotificationDelivery.IN_APP);
            notification.setStatus(NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            log.error("Failed to send payment notification", e);
        }
    }

    private BigDecimal centsToRupees(Integer cents) {
        if (cents == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(cents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private Integer resolveBookingAmountCents(Booking booking) {
        if (booking.getPriceMinCents() != null) {
            return booking.getPriceMinCents();
        }
        if (booking.getPriceMin() != null) {
            return booking.getPriceMin()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }
        return 0;
    }

    private StudentPaymentMethodDto getDefaultPaymentMethod(Long userId) {
        List<PaymentMethod> methods = paymentMethodRepository
                .findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId);
        if (methods.isEmpty()) {
            return null;
        }
        return toStudentPaymentMethod(methods.get(0));
    }

    private StudentPaymentMethodDto toStudentPaymentMethod(PaymentMethod method) {
        StudentPaymentMethodDto dto = new StudentPaymentMethodDto();
        dto.setId(method.getId());
        dto.setType(method.getMethodType().name());
        dto.setDefault(method.getIsDefault() != null && method.getIsDefault());
        dto.setCreatedAt(method.getCreatedAt());
        dto.setUpdatedAt(method.getUpdatedAt());

        String masked = method.getMaskedDetails();
        if (masked != null && masked.length() >= 4) {
            dto.setLastFourDigits(masked.substring(masked.length() - 4));
        }

        if (method.getMethodType() == PaymentMethodType.UPI) {
            dto.setUpiId(masked);
        }

        return dto;
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

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Payment cannot be processed for cancelled bookings");
        }

        if (isBookingPaid(booking.getId(), student.getId())) {
            throw new IllegalArgumentException("This booking is already paid");
        }

        if (request.getCurrency() != null && !"INR".equalsIgnoreCase(request.getCurrency())) {
            throw new IllegalArgumentException("Only INR is supported for booking payments");
        }
        String currency = "INR";

        BigDecimal bookingAmount = centsToRupees(resolveBookingAmountCents(booking));

        // Create payment intent
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setUserId(userPrincipal.getId());
        paymentIntent.setBookingId(booking.getId());
        paymentIntent.setAmountCents(resolveBookingAmountCents(booking));
        paymentIntent.setCurrency(currency);
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
                bookingAmount,
                currency,
                receipt,
                notes
        );

        // Update payment intent with Razorpay order ID
        paymentIntent.setProviderOrderId((String) razorpayOrder.get("id"));
        paymentIntentRepository.save(paymentIntent);

        // Build response
        return CreatePaymentOrderResponse.builder()
                .orderId((String) razorpayOrder.get("id"))
                .amount(bookingAmount)
                .currency(currency)
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
        if (booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.ACCEPTED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            
            // Notify student via WebSocket that booking is confirmed
            if (webSocketNotificationService != null) {
                webSocketNotificationService.notifyBookingConfirmed(booking);
            }
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

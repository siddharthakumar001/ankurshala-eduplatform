package com.ankurshala.backend.service;

import com.ankurshala.backend.entity.StudentWallet;
import com.ankurshala.backend.entity.TeacherWallet;
import com.ankurshala.backend.entity.WalletTransaction;
import com.ankurshala.backend.entity.WalletTransactionSource;
import com.ankurshala.backend.entity.WalletTransactionType;
import com.ankurshala.backend.entity.WalletOwnerType;
import com.ankurshala.backend.exception.BusinessException;
import com.ankurshala.backend.repository.StudentWalletRepository;
import com.ankurshala.backend.repository.TeacherWalletRepository;
import com.ankurshala.backend.repository.WalletTransactionRepository;
import com.ankurshala.backend.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class WalletService {

    @Autowired
    private StudentWalletRepository studentWalletRepository;
    @Autowired
    private TeacherWalletRepository teacherWalletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    public StudentWallet getStudentWallet(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching student wallet - TraceId: {}, StudentId: {}", traceId, studentId);

        return studentWalletRepository.findByStudentId(studentId)
                .orElseGet(() -> createStudentWallet(studentId));
    }

    public TeacherWallet getTeacherWallet(Long teacherId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching teacher wallet - TraceId: {}, TeacherId: {}", traceId, teacherId);

        return teacherWalletRepository.findByTeacherId(teacherId)
                .orElseGet(() -> createTeacherWallet(teacherId));
    }

    public WalletTransaction creditStudentWallet(Long studentId, Long amountCents, WalletTransactionSource source, Long bookingId, String description) {
        String traceId = TraceUtil.getTraceId();
        log.info("Crediting student wallet - TraceId: {}, StudentId: {}, Amount: {}, Source: {}", traceId, studentId, amountCents, source);

        StudentWallet wallet = getStudentWallet(studentId);
        
        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setOwnerType(WalletOwnerType.STUDENT);
        transaction.setOwnerId(studentId);
        transaction.setBookingId(bookingId);
        transaction.setType(WalletTransactionType.CREDIT);
        transaction.setAmountCents(amountCents);
        transaction.setSource(source);
        transaction.setMeta("{\"description\":\"" + description + "\"}");
        transaction.setCreatedAt(LocalDateTime.now());

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // Update wallet balance
        wallet.setBalanceCents(wallet.getBalanceCents() + amountCents);
        wallet.setUpdatedAt(LocalDateTime.now());
        studentWalletRepository.save(wallet);

        log.info("Student wallet credited - TraceId: {}, TransactionId: {}, NewBalance: {}", traceId, savedTransaction.getId(), wallet.getBalanceCents());
        return savedTransaction;
    }

    public WalletTransaction debitStudentWallet(Long studentId, Long amountCents, WalletTransactionSource source, Long bookingId, String description) {
        String traceId = TraceUtil.getTraceId();
        log.info("Debiting student wallet - TraceId: {}, StudentId: {}, Amount: {}, Source: {}", traceId, studentId, amountCents, source);

        StudentWallet wallet = getStudentWallet(studentId);
        
        if (wallet.getBalanceCents() < amountCents) {
            throw new BusinessException("Insufficient wallet balance", HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE");
        }

        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setOwnerType(WalletOwnerType.STUDENT);
        transaction.setOwnerId(studentId);
        transaction.setBookingId(bookingId);
        transaction.setType(WalletTransactionType.DEBIT);
        transaction.setAmountCents(amountCents);
        transaction.setSource(source);
        transaction.setMeta("{\"description\":\"" + description + "\"}");
        transaction.setCreatedAt(LocalDateTime.now());

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // Update wallet balance
        wallet.setBalanceCents(wallet.getBalanceCents() - amountCents);
        wallet.setUpdatedAt(LocalDateTime.now());
        studentWalletRepository.save(wallet);

        log.info("Student wallet debited - TraceId: {}, TransactionId: {}, NewBalance: {}", traceId, savedTransaction.getId(), wallet.getBalanceCents());
        return savedTransaction;
    }

    public WalletTransaction creditTeacherWallet(Long teacherId, Long amountCents, WalletTransactionSource source, Long bookingId, String description) {
        String traceId = TraceUtil.getTraceId();
        log.info("Crediting teacher wallet - TraceId: {}, TeacherId: {}, Amount: {}, Source: {}", traceId, teacherId, amountCents, source);

        TeacherWallet wallet = getTeacherWallet(teacherId);
        
        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setOwnerType(WalletOwnerType.TEACHER);
        transaction.setOwnerId(teacherId);
        transaction.setBookingId(bookingId);
        transaction.setType(WalletTransactionType.CREDIT);
        transaction.setAmountCents(amountCents);
        transaction.setSource(source);
        transaction.setMeta("{\"description\":\"" + description + "\"}");
        transaction.setCreatedAt(LocalDateTime.now());

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // Update wallet balance
        wallet.setBalanceCents(wallet.getBalanceCents() + amountCents);
        wallet.setUpdatedAt(LocalDateTime.now());
        teacherWalletRepository.save(wallet);

        log.info("Teacher wallet credited - TraceId: {}, TransactionId: {}, NewBalance: {}", traceId, savedTransaction.getId(), wallet.getBalanceCents());
        return savedTransaction;
    }

    public WalletTransaction debitTeacherWallet(Long teacherId, Long amountCents, WalletTransactionSource source, Long bookingId, String description) {
        String traceId = TraceUtil.getTraceId();
        log.info("Debiting teacher wallet - TraceId: {}, TeacherId: {}, Amount: {}, Source: {}", traceId, teacherId, amountCents, source);

        TeacherWallet wallet = getTeacherWallet(teacherId);
        
        if (wallet.getBalanceCents() < amountCents) {
            throw new BusinessException("Insufficient wallet balance", HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE");
        }

        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setOwnerType(WalletOwnerType.TEACHER);
        transaction.setOwnerId(teacherId);
        transaction.setBookingId(bookingId);
        transaction.setType(WalletTransactionType.DEBIT);
        transaction.setAmountCents(amountCents);
        transaction.setSource(source);
        transaction.setMeta("{\"description\":\"" + description + "\"}");
        transaction.setCreatedAt(LocalDateTime.now());

        WalletTransaction savedTransaction = walletTransactionRepository.save(transaction);

        // Update wallet balance
        wallet.setBalanceCents(wallet.getBalanceCents() - amountCents);
        wallet.setUpdatedAt(LocalDateTime.now());
        teacherWalletRepository.save(wallet);

        log.info("Teacher wallet debited - TraceId: {}, TransactionId: {}, NewBalance: {}", traceId, savedTransaction.getId(), wallet.getBalanceCents());
        return savedTransaction;
    }

    public List<WalletTransaction> getStudentWalletTransactions(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching student wallet transactions - TraceId: {}, StudentId: {}", traceId, studentId);

        return walletTransactionRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
                WalletOwnerType.STUDENT, studentId);
    }

    public Long getStudentWalletBalance(Long studentId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting student wallet balance - TraceId: {}, StudentId: {}", traceId, studentId);

        StudentWallet wallet = getStudentWallet(studentId);
        return wallet.getBalanceCents();
    }

    public Long getTeacherWalletBalance(Long teacherId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Getting teacher wallet balance - TraceId: {}, TeacherId: {}", traceId, teacherId);

        TeacherWallet wallet = getTeacherWallet(teacherId);
        return wallet.getBalanceCents();
    }

    public List<WalletTransaction> getTeacherWalletTransactions(Long teacherId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching teacher wallet transactions - TraceId: {}, TeacherId: {}", traceId, teacherId);

        return walletTransactionRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
                WalletOwnerType.TEACHER, teacherId);
    }

    public List<WalletTransaction> getWalletTransactionsByBooking(Long bookingId) {
        String traceId = TraceUtil.getTraceId();
        log.info("Fetching wallet transactions by booking - TraceId: {}, BookingId: {}", traceId, bookingId);

        return walletTransactionRepository.findByBookingId(bookingId);
    }

    private StudentWallet createStudentWallet(Long studentId) {
        StudentWallet wallet = new StudentWallet();
        wallet.setStudentId(studentId);
        wallet.setBalanceCents(0L);
        wallet.setUpdatedAt(LocalDateTime.now());
        return studentWalletRepository.save(wallet);
    }

    private TeacherWallet createTeacherWallet(Long teacherId) {
        TeacherWallet wallet = new TeacherWallet();
        wallet.setTeacherId(teacherId);
        wallet.setBalanceCents(0L);
        wallet.setUpdatedAt(LocalDateTime.now());
        return teacherWalletRepository.save(wallet);
    }

    public WalletTransaction transferBetweenWallets(Long fromUserId, Long toUserId, Long amountCents, String fromUserType, String toUserType, String description) {
        String traceId = TraceUtil.getTraceId();
        log.info("Transferring between wallets - TraceId: {}, From: {}, To: {}, Amount: {}", traceId, fromUserId, toUserId, amountCents);

        // Debit from source wallet
        if ("STUDENT".equals(fromUserType)) {
            debitStudentWallet(fromUserId, amountCents, WalletTransactionSource.TRANSFER, null, description);
        } else if ("TEACHER".equals(fromUserType)) {
            debitTeacherWallet(fromUserId, amountCents, WalletTransactionSource.TRANSFER, null, description);
        }

        // Credit to destination wallet
        if ("STUDENT".equals(toUserType)) {
            return creditStudentWallet(toUserId, amountCents, WalletTransactionSource.TRANSFER, null, description);
        } else if ("TEACHER".equals(toUserType)) {
            return creditTeacherWallet(toUserId, amountCents, WalletTransactionSource.TRANSFER, null, description);
        }

        throw new BusinessException("Invalid user type for transfer", HttpStatus.BAD_REQUEST, "INVALID_USER_TYPE");
    }
}

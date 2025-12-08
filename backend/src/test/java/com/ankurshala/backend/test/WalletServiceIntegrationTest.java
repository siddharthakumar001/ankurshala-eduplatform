package com.ankurshala.backend.test;

import com.ankurshala.backend.entity.*;
import com.ankurshala.backend.repository.*;
import com.ankurshala.backend.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WalletServiceIntegrationTest {

    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StudentWalletRepository studentWalletRepository;
    
    @Autowired
    private TeacherWalletRepository teacherWalletRepository;
    
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    private User student;
    private User teacher;

    @BeforeEach
    void setUp() {
        // Clean up
        walletTransactionRepository.deleteAll();
        studentWalletRepository.deleteAll();
        teacherWalletRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test users
        student = createTestStudent();
        teacher = createTestTeacher();
    }

    @Test
    void testGetStudentWalletBalance() {
        // Given
        Long studentId = student.getId();

        // When
        Long balance = walletService.getStudentWalletBalance(studentId);

        // Then
        assertNotNull(balance);
        assertEquals(0L, balance); // Initial balance should be 0
    }

    @Test
    void testGetTeacherWalletBalance() {
        // Given
        Long teacherId = teacher.getId();

        // When
        Long balance = walletService.getTeacherWalletBalance(teacherId);

        // Then
        assertNotNull(balance);
        assertEquals(0L, balance); // Initial balance should be 0
    }

    @Test
    void testCreditStudentWallet() {
        // Given
        Long studentId = student.getId();
        Long amountCents = 10000L; // ₹100
        String reason = "Test credit";

        // When
        walletService.creditStudentWallet(studentId, amountCents, WalletTransactionSource.ADMIN_ADJUSTMENT, null, reason);

        // Then
        Long balance = walletService.getStudentWalletBalance(studentId);
        assertEquals(amountCents, balance);
        
        // Verify transaction is recorded
        List<WalletTransaction> transactions = walletTransactionRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
                WalletOwnerType.STUDENT, studentId);
        assertFalse(transactions.isEmpty());
        assertEquals(WalletTransactionType.CREDIT, transactions.get(0).getType());
        assertEquals(amountCents, transactions.get(0).getAmountCents());
    }

    @Test
    void testDebitStudentWallet() {
        // Given
        Long studentId = student.getId();
        Long creditAmount = 10000L;
        Long debitAmount = 5000L;
        
        // First credit the wallet
        walletService.creditStudentWallet(studentId, creditAmount, WalletTransactionSource.ADMIN_ADJUSTMENT, null, "Initial credit");

        // When
        walletService.debitStudentWallet(studentId, debitAmount, WalletTransactionSource.ADMIN_ADJUSTMENT, null, "Test debit");

        // Then
        Long balance = walletService.getStudentWalletBalance(studentId);
        assertEquals(creditAmount - debitAmount, balance);
        
        // Verify transaction is recorded
        List<WalletTransaction> transactions = walletTransactionRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
                WalletOwnerType.STUDENT, studentId);
        assertEquals(2, transactions.size());
        assertEquals(WalletTransactionType.DEBIT, transactions.get(0).getType());
        assertEquals(debitAmount, transactions.get(0).getAmountCents());
    }

    @Test
    void testDebitStudentWalletInsufficientFunds() {
        // Given
        Long studentId = student.getId();
        Long debitAmount = 5000L; // Try to debit more than available

        // When & Then
        assertThrows(Exception.class, () -> 
            walletService.debitStudentWallet(studentId, debitAmount, WalletTransactionSource.ADMIN_ADJUSTMENT, null, "Test debit"));
    }

    @Test
    void testCreditTeacherWallet() {
        // Given
        Long teacherId = teacher.getId();
        Long amountCents = 15000L; // ₹150
        String reason = "Test earning";

        // When
        walletService.creditTeacherWallet(teacherId, amountCents, WalletTransactionSource.ADMIN_ADJUSTMENT, null, reason);

        // Then
        Long balance = walletService.getTeacherWalletBalance(teacherId);
        assertEquals(amountCents, balance);
        
        // Verify transaction is recorded
        List<WalletTransaction> transactions = walletTransactionRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(
                WalletOwnerType.TEACHER, teacherId);
        assertFalse(transactions.isEmpty());
        assertEquals(WalletTransactionType.CREDIT, transactions.get(0).getType());
        assertEquals(amountCents, transactions.get(0).getAmountCents());
    }

    @Test
    void testGetStudentWalletTransactions() {
        // Given
        Long studentId = student.getId();
        walletService.creditStudentWallet(studentId, 10000L, WalletTransactionSource.ADMIN_ADJUSTMENT, null, "Credit 1");
        walletService.creditStudentWallet(studentId, 5000L, WalletTransactionSource.ADMIN_ADJUSTMENT, null, "Credit 2");

        // When
        List<WalletTransaction> transactions = walletService.getStudentWalletTransactions(studentId);

        // Then
        assertEquals(2, transactions.size());
        assertEquals(WalletOwnerType.STUDENT, transactions.get(0).getOwnerType());
        assertEquals(studentId, transactions.get(0).getOwnerId());
    }

    @Test
    void testGetTeacherWalletTransactions() {
        // Given
        Long teacherId = teacher.getId();
        walletService.creditTeacherWallet(teacherId, 20000L, WalletTransactionSource.ADMIN_ADJUSTMENT, null, "Earning 1");
        walletService.creditTeacherWallet(teacherId, 10000L, WalletTransactionSource.ADMIN_ADJUSTMENT, null, "Earning 2");

        // When
        List<WalletTransaction> transactions = walletService.getTeacherWalletTransactions(teacherId);

        // Then
        assertEquals(2, transactions.size());
        assertEquals(WalletOwnerType.TEACHER, transactions.get(0).getOwnerType());
        assertEquals(teacherId, transactions.get(0).getOwnerId());
    }

    private User createTestStudent() {
        User user = new User();
        user.setName("Test Student");
        user.setEmail("student@test.com");
        user.setPassword("password");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private User createTestTeacher() {
        User user = new User();
        user.setName("Test Teacher");
        user.setEmail("teacher@test.com");
        user.setPassword("password");
        user.setRole(Role.TEACHER);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}

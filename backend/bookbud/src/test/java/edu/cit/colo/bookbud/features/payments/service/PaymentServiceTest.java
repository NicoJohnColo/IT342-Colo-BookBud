package edu.cit.colo.bookbud.features.payments.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.features.payments.entity.Payment;
import edu.cit.colo.bookbud.features.payments.repository.PaymentRepository;
import edu.cit.colo.bookbud.features.transactions.entity.Transaction;
import edu.cit.colo.bookbud.features.transactions.repository.TransactionRepository;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void testGetPaymentById_Success() {
        // Given
        User testUser = User.builder().userId("test-user-id").build();
        Transaction testTransaction = Transaction.builder()
                .transactionId("test-transaction-id")
                .owner(testUser)
                .user(testUser)
                .build();
        Payment testPayment = Payment.builder()
                .paymentId("test-payment-id")
                .amount(new BigDecimal("29.99"))
                .transaction(testTransaction)
                .paymentMethod(Payment.PaymentMethod.Cash)
                .paymentStatus(Payment.PaymentStatus.Paid)
                .build();
        when(paymentRepository.findById("test-payment-id")).thenReturn(Optional.of(testPayment));

        // When
        var result = paymentService.getPaymentById("test-payment-id", "test-user-id");

        // Then
        assertNotNull(result);
        assertEquals("test-payment-id", result.getPaymentId());
        verify(paymentRepository, times(1)).findById("test-payment-id");
    }

    @Test
    void testGetPaymentById_NotFound() {
        // Given
        when(paymentRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentById("non-existent-id", "test-user-id");
        });
        verify(paymentRepository, times(1)).findById("non-existent-id");
    }

    @Test
    void testGetPaymentsByUserId_Success() {
        // Given
        User testUser = User.builder().userId("test-user-id").build();
        Transaction testTransaction = Transaction.builder()
                .transactionId("test-transaction-id")
                .owner(testUser)
                .user(testUser)
                .build();
        Payment testPayment = Payment.builder()
                .paymentId("test-payment-id")
                .amount(new BigDecimal("29.99"))
                .transaction(testTransaction)
                .paymentMethod(Payment.PaymentMethod.Cash)
                .paymentStatus(Payment.PaymentStatus.Paid)
                .build();
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findPaymentsReceivedByUser("test-user-id", PageRequest.of(0, 10))).thenReturn(new PageImpl<>(payments, PageRequest.of(0, 10), 1));

        // When
        var result = paymentService.getPaymentsReceivedByUser("test-user-id", 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(paymentRepository, times(1)).findPaymentsReceivedByUser("test-user-id", PageRequest.of(0, 10));
    }

    @Test
    void testGetPaymentsByUserId_Empty() {
        // Given
        when(paymentRepository.findPaymentsReceivedByUser("test-user-id", PageRequest.of(0, 10))).thenReturn(new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0));

        // When
        var result = paymentService.getPaymentsReceivedByUser("test-user-id", 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(paymentRepository, times(1)).findPaymentsReceivedByUser("test-user-id", PageRequest.of(0, 10));
    }
}

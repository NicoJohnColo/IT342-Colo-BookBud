package edu.cit.colo.bookbud.features.transactions.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import edu.cit.colo.bookbud.features.books.entity.Book;
import edu.cit.colo.bookbud.features.books.repository.BookRepository;
import edu.cit.colo.bookbud.features.notifications.service.NotificationService;
import edu.cit.colo.bookbud.features.payments.repository.PaymentRepository;
import edu.cit.colo.bookbud.features.transactions.dto.CreateTransactionRequest;
import edu.cit.colo.bookbud.features.transactions.entity.Transaction;
import edu.cit.colo.bookbud.features.transactions.repository.TransactionRepository;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
                .transactionId("test-transaction-id")
                .amount(29.99)
                .build();
    }

    @Test
    void testGetTransactionsByUser_Success() {
        // Given
        testTransaction = Transaction.builder()
                .transactionId("test-transaction-id")
                .amount(29.99)
                .build();
        User testUser = User.builder()
                .userId("test-user-id")
                .build();
        testTransaction.setOwner(testUser);
        testTransaction.setUser(testUser);
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findByUserUserIdOrOwnerUserId("test-user-id", "test-user-id", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(transactions, PageRequest.of(0, 10), 1));

        // When
        var result = transactionService.getMyTransactions("test-user-id", null, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(transactionRepository, times(1)).findByUserUserIdOrOwnerUserId("test-user-id", "test-user-id", PageRequest.of(0, 10));
    }

    @Test
    void testGetTransactionsByUser_Empty() {
        // Given
        when(transactionRepository.findByUserUserIdOrOwnerUserId("test-user-id", "test-user-id", PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0));

        // When
        var result = transactionService.getMyTransactions("test-user-id", null, 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(transactionRepository, times(1)).findByUserUserIdOrOwnerUserId("test-user-id", "test-user-id", PageRequest.of(0, 10));
    }

    @Test
    void testCreateTransaction_Success() {
        // Given
        User testUser = User.builder().userId("test-user-id").build();
        User bookOwner = User.builder().userId("owner-id").build();
        Book testBook = Book.builder().bookId("test-book-id").status(Book.Status.Available).owner(bookOwner).build();
        
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById("test-book-id")).thenReturn(Optional.of(testBook));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        var result = transactionService.createTransaction("test-user-id", new CreateTransactionRequest("test-book-id", null, null));

        // Then
        assertNotNull(result);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_NotFound() {
        // Given
        when(userRepository.findById("test-user-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction("test-user-id", new CreateTransactionRequest("test-book-id", null, null));
        });
    }
}

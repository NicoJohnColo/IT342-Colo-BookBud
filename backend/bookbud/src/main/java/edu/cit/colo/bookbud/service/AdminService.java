package edu.cit.colo.bookbud.service;

import edu.cit.colo.bookbud.dto.book.BookDTO;
import edu.cit.colo.bookbud.dto.notification.NotificationDTO;
import edu.cit.colo.bookbud.dto.PaginatedResponse;
import edu.cit.colo.bookbud.dto.transaction.TransactionDTO;
import edu.cit.colo.bookbud.dto.user.UserProfileDTO;
import edu.cit.colo.bookbud.entity.Book;
import edu.cit.colo.bookbud.entity.Notification;
import edu.cit.colo.bookbud.entity.Transaction;
import edu.cit.colo.bookbud.entity.User;
import edu.cit.colo.bookbud.exception.BusinessException;
import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PaginatedResponse<BookDTO> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findAll(pageable);

        return PaginatedResponse.<BookDTO>builder()
                .content(books.getContent().stream().map(this::mapBookToDTO).collect(Collectors.toList()))
                .page(books.getNumber())
                .size(books.getSize())
                .totalElements(books.getTotalElements())
                .build();
    }

    @Transactional
    public BookDTO updateBookStatus(String bookId, String status) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        book.setStatus(Book.Status.valueOf(status));
        book = bookRepository.save(book);

        if ("Unavailable".equals(status)) {
            notificationService.createNotification(book.getOwner().getUserId(), 
                "Your book has been set to Unavailable by admin: " + book.getTitle());
        }

        return mapBookToDTO(book);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<UserProfileDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);

        return PaginatedResponse.<UserProfileDTO>builder()
                .content(users.getContent().stream().map(this::mapUserToDTO).collect(Collectors.toList()))
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .build();
    }

    @Transactional
    public UserProfileDTO updateUserStatus(String userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        user.setAccountStatus(status);
        user = userRepository.save(user);

        return mapUserToDTO(user);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<TransactionDTO> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        return PaginatedResponse.<TransactionDTO>builder()
                .content(transactions.getContent().stream()
                        .map(t -> mapTransactionToDTO(t, t.getUser().getUserId()))
                        .collect(Collectors.toList()))
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .build();
    }

    @Transactional
    public TransactionDTO cancelTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));

        if (transaction.getStatus() == Transaction.Status.Completed) {
            throw new BusinessException("BUSINESS-004", "Transaction already completed");
        }

        transaction.setStatus(Transaction.Status.Cancelled);
        transaction = transactionRepository.save(transaction);

        // Update book status
        Book book = transaction.getBook();
        book.setStatus(Book.Status.Available);
        bookRepository.save(book);

        // Notify both parties
        notificationService.createNotification(transaction.getUser().getUserId(), 
            "Your transaction has been cancelled by admin for: " + book.getTitle());
        notificationService.createNotification(transaction.getOwner().getUserId(), 
            "Transaction cancelled by admin for: " + book.getTitle());

        return mapTransactionToDTO(transaction, transaction.getUser().getUserId());
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<NotificationDTO> getAllNotifications(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findAll(pageable);

        return PaginatedResponse.<NotificationDTO>builder()
                .content(notifications.getContent().stream().map(this::mapNotificationToDTO).collect(Collectors.toList()))
                .page(notifications.getNumber())
                .size(notifications.getSize())
                .totalElements(notifications.getTotalElements())
                .build();
    }

    private BookDTO mapBookToDTO(Book book) {
        return BookDTO.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .condition(book.getCondition() != null ? book.getCondition().name() : null)
                .priceRent(book.getPriceRent())
                .priceSale(book.getPriceSale())
                .transactionType(book.getTransactionType() != null ? book.getTransactionType().name() : null)
                .status(book.getStatus() != null ? book.getStatus().name() : null)
                .ownerId(book.getOwner() != null ? book.getOwner().getUserId() : null)
                .createdAt(book.getCreatedAt() != null ? book.getCreatedAt().toString() : null)
                .build();
    }

    private UserProfileDTO mapUserToDTO(User user) {
        return UserProfileDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .rating(user.getRating())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    private TransactionDTO mapTransactionToDTO(Transaction transaction, String requestingUserId) {
        String role = transaction.getUser().getUserId().equals(requestingUserId) ? "renter" : "owner";
        
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .bookId(transaction.getBook().getBookId())
                .userId(transaction.getUser().getUserId())
                .ownerId(transaction.getOwner().getUserId())
                .startDate(transaction.getStartDate())
                .endDate(transaction.getEndDate())
                .status(transaction.getStatus().name())
                .createdAt(transaction.getCreatedAt().toString())
                .userRole(role)
                .build();
    }

    private NotificationDTO mapNotificationToDTO(Notification notification) {
        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getUserId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt().toString())
                .build();
    }
}

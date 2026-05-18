package edu.cit.colo.bookbud.features.transactions.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.colo.bookbud.features.books.entity.Book;
import edu.cit.colo.bookbud.features.books.repository.BookRepository;
import edu.cit.colo.bookbud.features.notifications.service.NotificationService;
import edu.cit.colo.bookbud.features.payments.entity.Payment;
import edu.cit.colo.bookbud.features.payments.repository.PaymentRepository;
import edu.cit.colo.bookbud.features.transactions.dto.CreateTransactionRequest;
import edu.cit.colo.bookbud.features.transactions.dto.RatingResponse;
import edu.cit.colo.bookbud.features.transactions.dto.TransactionDTO;
import edu.cit.colo.bookbud.features.transactions.entity.Transaction;
import edu.cit.colo.bookbud.features.transactions.repository.TransactionRepository;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;
import edu.cit.colo.bookbud.shared.dto.PaginatedResponse;
import edu.cit.colo.bookbud.shared.exception.BusinessException;
import edu.cit.colo.bookbud.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Transactional
    public TransactionDTO createTransaction(String userId, CreateTransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        if (book.getStatus() != Book.Status.Available) {
            throw new BusinessException("BUSINESS-002", "Book is not Available");
        }

        if (book.getOwner().getUserId().equals(userId)) {
            throw new BusinessException("BUSINESS-003", "Cannot transact own listing");
        }

        // Calculate transaction amount based on book price and transaction type
        Double transactionAmount;
        if (isPurchaseRequest(book, request)) {
            // For sale/purchase transactions
            transactionAmount = book.getPriceSale() != null ? book.getPriceSale().doubleValue() : 0.0;
        } else {
            // For rental transactions
            transactionAmount = book.getPriceRent() != null ? book.getPriceRent().doubleValue() : 0.0;
        }

        Transaction transaction = Transaction.builder()
                .book(book)
                .user(user)
                .owner(book.getOwner())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .amount(transactionAmount)
                .status(Transaction.Status.Pending)
                .paymentMethod(request.getPaymentMethod())
                .build();

        transaction = transactionRepository.save(transaction);

    // Book is managed in this transaction; dirty checking will persist the change.
    book.setStatus(isPurchaseRequest(book, request) ? Book.Status.Unavailable : Book.Status.Rented);

        // Send notifications
        notificationService.createNotification(book.getOwner().getUserId(), 
            "New transaction request for your book: " + book.getTitle());
        notificationService.createNotification(userId, 
            "Your transaction request has been submitted for: " + book.getTitle());

        return mapToDTO(transaction, userId);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<TransactionDTO> getMyTransactions(String userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions;

        if (status != null) {
            Transaction.Status transactionStatus = Transaction.Status.valueOf(status);
            // Get transactions where user is either renter or owner with specific status
            transactions = transactionRepository.findByUserUserIdAndStatus(userId, transactionStatus, pageable);
        } else {
            transactions = transactionRepository.findByUserUserIdOrOwnerUserId(userId, userId, pageable);
        }

        return PaginatedResponse.<TransactionDTO>builder()
                .content(transactions.getContent().stream()
                        .map(t -> mapToDTO(t, userId))
                        .collect(Collectors.toList()))
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransaction(String transactionId, String userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));

        // Null-safe authorization check
        User user = transaction.getUser();
        User owner = transaction.getOwner();
        boolean isUser = user != null && user.getUserId() != null && user.getUserId().equals(userId);
        boolean isOwner = owner != null && owner.getUserId() != null && owner.getUserId().equals(userId);
        
        if (!isUser && !isOwner) {
            throw new BusinessException("AUTH-003", "Not a party to this transaction");
        }

        return mapToDTO(transaction, userId);
    }

    @Transactional
    public TransactionDTO updateTransactionStatus(String transactionId, String userId, String newStatus) {
        log.info("Updating transaction status: transactionId={}, userId={}, newStatus={}", transactionId, userId, newStatus);
        
        if (transactionId == null || transactionId.isBlank()) {
            throw new BusinessException("VALID-001", "Transaction ID is required");
        }
        if (newStatus == null || newStatus.isBlank()) {
            throw new BusinessException("VALID-001", "Status is required");
        }

        // Fetch transaction using standard method (simpler approach)
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));
        
        log.debug("Transaction found: id={}, ownerId={}, userId={}, currentStatus={}", 
            transaction.getTransactionId(),
            transaction.getOwner() != null ? transaction.getOwner().getUserId() : "null",
            transaction.getUser() != null ? transaction.getUser().getUserId() : "null",
            transaction.getStatus());

        // Validate that required entities are not null
        if (transaction.getOwner() == null || transaction.getUser() == null) {
            log.error("Transaction data is corrupted: owner or user is null");
            throw new BusinessException("SYSTEM-002", "Transaction data is corrupted");
        }

        // Parse and validate the new status
        Transaction.Status status;
        try {
            status = Transaction.Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("VALID-001", "Invalid status. Must be one of: Pending, Active, Completed, Cancelled");
        }
        
        Transaction.Status currentStatus = transaction.getStatus();

        // Get authorization info
        boolean isOwner = transaction.getOwner().getUserId().equals(userId);
        boolean isUser = transaction.getUser().getUserId().equals(userId);
        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);

        // Validate status transitions
        switch (status) {
            case Active:
                if (!isOwner) {
                    throw new BusinessException("AUTH-003", "Only owner can activate transaction");
                }
                if (currentStatus != Transaction.Status.Pending) {
                    throw new BusinessException("BUSINESS-004", "Invalid status transition from " + currentStatus + " to " + status);
                }
                break;
            case Completed:
                if (!isOwner) {
                    throw new BusinessException("AUTH-003", "Only owner can complete transaction");
                }
                // Allow transition from Active to Completed, or Completed to Completed (for sync)
                if (currentStatus != Transaction.Status.Active && currentStatus != Transaction.Status.Completed) {
                    throw new BusinessException("BUSINESS-004", "Invalid status transition from " + currentStatus + " to " + status);
                }
                break;
            case Cancelled:
                if (!isOwner && !isUser && !isAdmin) {
                    throw new BusinessException("AUTH-003", "Not authorized for this transition");
                }
                if (currentStatus == Transaction.Status.Completed) {
                    throw new BusinessException("BUSINESS-004", "Cannot cancel completed transaction");
                }
                break;
            case Pending:
                throw new BusinessException("BUSINESS-004", "Cannot transition to Pending status");
        }

        // Update transaction status
        transaction.setStatus(status);
        transaction = transactionRepository.save(transaction);

        // Handle book status updates
        try {
            handleBookStatusUpdate(transaction, status);
        } catch (Exception e) {
            // Log error but continue processing
        }

        // Handle payment creation/sync for completed transactions
        if (status == Transaction.Status.Completed) {
            try {
                log.info("Ensuring payment record is synchronized for completed transaction: {}", transactionId);
                createPaymentForTransaction(transaction);
            } catch (Exception e) {
                log.error("Failed to synchronize payment for transaction {}: {}", transactionId, e.getMessage(), e);
            }
        }

        // Send notifications
        try {
            sendTransactionNotifications(transaction, status);
        } catch (Exception e) {
            // Log error but continue processing
        }

        return mapToDTO(transaction, userId);
    }

    private void handleBookStatusUpdate(Transaction transaction, Transaction.Status status) {
        Book book = transaction.getBook();
        if (book == null) {
            return;
        }

        if (status == Transaction.Status.Completed) {
            book.setStatus(isPurchaseTransaction(transaction) ? Book.Status.Sold : Book.Status.Available);
            bookRepository.save(book);
        } else if (status == Transaction.Status.Cancelled) {
            book.setStatus(Book.Status.Available);
            bookRepository.save(book);
        }
    }

    private void sendTransactionNotifications(Transaction transaction, Transaction.Status status) {
        Book book = transaction.getBook();
        String bookTitle = (book != null && book.getTitle() != null) ? book.getTitle() : "Unknown Book";
        
        User user = transaction.getUser();
        User owner = transaction.getOwner();
        
        if (user != null && user.getUserId() != null) {
            notificationService.createNotification(
                user.getUserId(),
                "Transaction status updated to " + status + " for: " + bookTitle
            );
        }
        if (owner != null && owner.getUserId() != null) {
            notificationService.createNotification(
                owner.getUserId(),
                "Transaction status updated to " + status + " for: " + bookTitle
            );
        }
    }

    @Transactional
    public RatingResponse submitRating(String transactionId, String userId, BigDecimal rating) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));

        // Null-safe authorization check
        User user = transaction.getUser();
        User owner = transaction.getOwner();
        boolean isUser = user != null && user.getUserId() != null && user.getUserId().equals(userId);
        boolean isOwner = owner != null && owner.getUserId() != null && owner.getUserId().equals(userId);
        
        if (!isUser && !isOwner) {
            throw new BusinessException("AUTH-003", "Not a party to this transaction");
        }

        if (transaction.getStatus() != Transaction.Status.Completed) {
            throw new BusinessException("BUSINESS-007", "Transaction is not yet Completed");
        }

        if (isOwner && transaction.getOwnerRated()) {
            throw new BusinessException("BUSINESS-008", "Rating already submitted");
        }

        if (isUser && transaction.getRenterRated()) {
            throw new BusinessException("BUSINESS-008", "Rating already submitted");
        }

        // Update rating flags
        if (isOwner) {
            transaction.setOwnerRated(true);
        } else {
            transaction.setRenterRated(true);
        }
        transactionRepository.save(transaction);

        // Calculate new aggregate rating for the rated user
        User ratedUser = isOwner ? transaction.getUser() : transaction.getOwner();
        if (ratedUser == null || ratedUser.getUserId() == null) {
            throw new BusinessException("SYSTEM-002", "Cannot rate: rated user data is corrupted");
        }
        BigDecimal newRating = calculateNewRating(ratedUser, rating);
        ratedUser.setRating(newRating);
        userRepository.save(ratedUser);

        return RatingResponse.builder()
                .transactionId(transactionId)
                .ratedUserId(ratedUser.getUserId())
                .rating(rating)
                .newAggregateRating(newRating)
                .build();
    }

    private BigDecimal calculateNewRating(User user, BigDecimal newRating) {
        // Get all completed transactions where this user was rated
        List<Transaction> completedTransactions = transactionRepository.findByUserUserId(user.getUserId()).stream()
                .filter(t -> t.getStatus() == Transaction.Status.Completed && t.getRenterRated())
                .collect(Collectors.toList());
        
        completedTransactions.addAll(transactionRepository.findByOwnerUserId(user.getUserId()).stream()
                .filter(t -> t.getStatus() == Transaction.Status.Completed && t.getOwnerRated())
                .collect(Collectors.toList()));

        int ratingCount = completedTransactions.size() + 1;
        BigDecimal sum = completedTransactions.stream()
                .map(t -> user.getRating() != null ? user.getRating() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(newRating);

        return sum.divide(BigDecimal.valueOf(ratingCount), 2, RoundingMode.HALF_UP);
    }

    private boolean isPurchaseRequest(Book book, CreateTransactionRequest request) {
        if (book.getTransactionType() == Book.TransactionType.Sale) {
            return true;
        }
        return request.getEndDate() == null;
    }

    private boolean isPurchaseTransaction(Transaction transaction) {
        Book book = transaction.getBook();
        if (book == null) {
            return transaction.getEndDate() == null;
        }
        if (book.getTransactionType() == Book.TransactionType.Sale) {
            return true;
        }
        return transaction.getEndDate() == null;
    }

    private TransactionDTO mapToDTO(Transaction transaction, String requestingUserId) {
        // Null-safe access to user and owner entities
        User user = transaction.getUser();
        User owner = transaction.getOwner();
        
        String role = (user != null && user.getUserId() != null && user.getUserId().equals(requestingUserId)) ? "renter" : "owner";
        
        Book book = transaction.getBook();
        String bookId = book != null ? book.getBookId() : "Unknown";
        String bookTitle = book != null ? book.getTitle() : "Unknown Book";
        
        // Fetch payment info for this transaction
        String paymentStatus = null;
        String paymentMethod = null;
        try {
            Optional<Payment> paymentOpt = paymentRepository.findByTransactionTransactionId(transaction.getTransactionId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                paymentStatus = payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : null;
                paymentMethod = payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null;
            }
        } catch (Exception e) {
            // Payment info is optional, ignore errors
            log.debug("Could not fetch payment for transaction {}", transaction.getTransactionId());
        }
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .bookId(bookId)
                .bookTitle(bookTitle)
                .userId(user != null ? user.getUserId() : "Unknown")
                .renterUsername(user != null ? user.getUsername() : "Unknown")
                .ownerId(owner != null ? owner.getUserId() : "Unknown")
                .ownerUsername(owner != null ? owner.getUsername() : "Unknown")
                .startDate(transaction.getStartDate())
                .endDate(transaction.getEndDate())
                .amount(transaction.getAmount())
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : "Unknown")
                .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null)
                .userRole(role)
                .paymentStatus(paymentStatus != null ? paymentStatus : "PENDING")
                .paymentMethod(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod() : paymentMethod)
                .ownerRated(transaction.getOwnerRated())
                .renterRated(transaction.getRenterRated())
                .build();
    }
    

    /**
     * Automatically create a payment record when transaction is completed.
     * This ensures the payment table is populated with transaction data.
     */
    private void createPaymentForTransaction(Transaction transaction) {
        if (transaction == null || transaction.getTransactionId() == null) {
            log.error("Cannot create payment: transaction or transactionId is null");
            return;
        }
        
        log.info("Creating or updating payment for transaction: {}, status: {}, amount: {}", 
            transaction.getTransactionId(), transaction.getStatus(), transaction.getAmount());

        // Check if a payment already exists for this transaction
        Optional<edu.cit.colo.bookbud.features.payments.entity.Payment> existingOpt =
                paymentRepository.findByTransactionTransactionId(transaction.getTransactionId());

        if (existingOpt.isPresent()) {
            edu.cit.colo.bookbud.features.payments.entity.Payment existing = existingOpt.get();
            log.info("Found existing payment for transaction: {}, current payment status: {}", 
                transaction.getTransactionId(), existing.getPaymentStatus());

            // If transaction is completed, the payment should be marked as Paid
            if (transaction.getStatus() == Transaction.Status.Completed) {
                if (existing.getPaymentStatus() != edu.cit.colo.bookbud.features.payments.entity.Payment.PaymentStatus.Paid) {
                    log.info("Updating existing payment status from {} to Paid for transaction: {}", 
                        existing.getPaymentStatus(), transaction.getTransactionId());
                    
                    existing.setPaymentStatus(edu.cit.colo.bookbud.features.payments.entity.Payment.PaymentStatus.Paid);
                    
                    // Set payment date if missing
                    if (existing.getPaymentDate() == null) {
                        existing.setPaymentDate(java.time.LocalDate.now());
                    }
                    
                    paymentRepository.save(existing);
                    log.info("Successfully updated existing payment to Paid for transaction: {}", transaction.getTransactionId());
                } else {
                    log.debug("Payment is already marked as Paid for transaction: {}", transaction.getTransactionId());
                }
            }
            return;
        }

        // No existing payment: create a new Paid payment record for the completed transaction
        // (Only if the transaction is actually completed)
        if (transaction.getStatus() == Transaction.Status.Completed) {
            log.info("Creating new Paid payment record for completed transaction: {}", transaction.getTransactionId());
            
            edu.cit.colo.bookbud.features.payments.entity.Payment payment =
                    edu.cit.colo.bookbud.features.payments.entity.Payment.builder()
                    .transaction(transaction)
                    .amount(transaction.getAmount() != null ?
                        new java.math.BigDecimal(transaction.getAmount()) : java.math.BigDecimal.ZERO)
                    .paymentMethod(edu.cit.colo.bookbud.features.payments.entity.Payment.PaymentMethod.Cash)
                    .paymentDate(java.time.LocalDate.now())
                    .paymentStatus(edu.cit.colo.bookbud.features.payments.entity.Payment.PaymentStatus.Paid)
                    .build();

            try {
                paymentRepository.save(payment);
                log.info("New payment record saved successfully for transaction: {}", transaction.getTransactionId());
                
                // Notify owner about the payment
                String bookTitle = (transaction.getBook() != null && transaction.getBook().getTitle() != null)
                    ? transaction.getBook().getTitle()
                    : "Unknown Book";
                User owner = transaction.getOwner();
                if (owner != null && owner.getUserId() != null) {
                    notificationService.createNotification(
                        owner.getUserId(),
                        "Payment received for completed transaction: " + bookTitle
                    );
                }
            } catch (Exception e) {
                log.error("Error saving new payment for transaction {}: {}", transaction.getTransactionId(), e.getMessage());
            }
        } else {
            log.warn("createPaymentForTransaction called for non-completed transaction: {}. Doing nothing.", 
                transaction.getTransactionId());
        }
    }
}

package edu.cit.colo.bookbud.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.colo.bookbud.dto.PaginatedResponse;
import edu.cit.colo.bookbud.dto.transaction.CreateTransactionRequest;
import edu.cit.colo.bookbud.dto.transaction.RatingResponse;
import edu.cit.colo.bookbud.dto.transaction.TransactionDTO;
import edu.cit.colo.bookbud.entity.Book;
import edu.cit.colo.bookbud.entity.Transaction;
import edu.cit.colo.bookbud.entity.User;
import edu.cit.colo.bookbud.exception.BusinessException;
import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.repository.BookRepository;
import edu.cit.colo.bookbud.repository.TransactionRepository;
import edu.cit.colo.bookbud.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
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

        Transaction transaction = Transaction.builder()
                .book(book)
                .user(user)
                .owner(book.getOwner())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Transaction.Status.Pending)
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

        if (!transaction.getUser().getUserId().equals(userId) && 
            !transaction.getOwner().getUserId().equals(userId)) {
            throw new BusinessException("AUTH-003", "Not a party to this transaction");
        }

        return mapToDTO(transaction, userId);
    }

    @Transactional
    public TransactionDTO updateTransactionStatus(String transactionId, String userId, String newStatus) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));

        Transaction.Status status = Transaction.Status.valueOf(newStatus);
        Transaction.Status currentStatus = transaction.getStatus();

        boolean isOwner = transaction.getOwner().getUserId().equals(userId);
        boolean isUser = transaction.getUser().getUserId().equals(userId);
        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);

        // Validate status transitions
        if (status == Transaction.Status.Active) {
            if (!isOwner) {
                throw new BusinessException("AUTH-003", "Only owner can activate transaction");
            }
            if (currentStatus != Transaction.Status.Pending) {
                throw new BusinessException("BUSINESS-004", "Invalid status transition");
            }
        } else if (status == Transaction.Status.Completed) {
            if (!isOwner) {
                throw new BusinessException("AUTH-003", "Only owner can complete transaction");
            }
            if (currentStatus != Transaction.Status.Active) {
                throw new BusinessException("BUSINESS-004", "Invalid status transition");
            }
        } else if (status == Transaction.Status.Cancelled) {
            if (!isOwner && !isUser && !isAdmin) {
                throw new BusinessException("AUTH-003", "Not authorized for this transition");
            }
            if (currentStatus == Transaction.Status.Completed) {
                throw new BusinessException("BUSINESS-004", "Cannot cancel completed transaction");
            }
        }

        transaction.setStatus(status);
        transaction = transactionRepository.save(transaction);

        // Update book status
        Book book = transaction.getBook();
        if (status == Transaction.Status.Completed) {
            book.setStatus(isPurchaseTransaction(transaction) ? Book.Status.Sold : Book.Status.Available);
        } else if (status == Transaction.Status.Cancelled) {
            book.setStatus(Book.Status.Available);
        }

        // Send notifications
        notificationService.createNotification(transaction.getUser().getUserId(), 
            "Transaction status updated to " + status + " for: " + book.getTitle());
        notificationService.createNotification(transaction.getOwner().getUserId(), 
            "Transaction status updated to " + status + " for: " + book.getTitle());

        return mapToDTO(transaction, userId);
    }

    @Transactional
    public RatingResponse submitRating(String transactionId, String userId, BigDecimal rating) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Transaction not found"));

        if (!transaction.getUser().getUserId().equals(userId) && 
            !transaction.getOwner().getUserId().equals(userId)) {
            throw new BusinessException("AUTH-003", "Not a party to this transaction");
        }

        if (transaction.getStatus() != Transaction.Status.Completed) {
            throw new BusinessException("BUSINESS-007", "Transaction is not yet Completed");
        }

        boolean isOwner = transaction.getOwner().getUserId().equals(userId);
        boolean isUser = transaction.getUser().getUserId().equals(userId);

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
        if (book.getTransactionType() == Book.TransactionType.Sale) {
            return true;
        }
        return transaction.getEndDate() == null;
    }

    private TransactionDTO mapToDTO(Transaction transaction, String requestingUserId) {
        String role = transaction.getUser().getUserId().equals(requestingUserId) ? "renter" : "owner";
        
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .bookId(transaction.getBook().getBookId())
                .bookTitle(transaction.getBook().getTitle())
                .userId(transaction.getUser().getUserId())
                .renterUsername(transaction.getUser().getUsername())
                .ownerId(transaction.getOwner().getUserId())
                .ownerUsername(transaction.getOwner().getUsername())
                .startDate(transaction.getStartDate())
                .endDate(transaction.getEndDate())
                .status(transaction.getStatus().name())
            .createdAt(transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null)
                .userRole(role)
                .build();
    }
}

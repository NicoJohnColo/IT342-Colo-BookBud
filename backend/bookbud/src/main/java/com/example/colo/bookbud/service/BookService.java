package com.example.colo.bookbud.service;

import com.example.colo.bookbud.dto.book.BookDTO;
import com.example.colo.bookbud.dto.book.CreateBookRequest;
import com.example.colo.bookbud.dto.book.ExternalBookDTO;
import com.example.colo.bookbud.dto.book.UpdateBookRequest;
import com.example.colo.bookbud.dto.PaginatedResponse;
import com.example.colo.bookbud.entity.Book;
import com.example.colo.bookbud.entity.Transaction;
import com.example.colo.bookbud.entity.User;
import com.example.colo.bookbud.exception.BusinessException;
import com.example.colo.bookbud.exception.ResourceNotFoundException;
import com.example.colo.bookbud.repository.BookRepository;
import com.example.colo.bookbud.repository.TransactionRepository;
import com.example.colo.bookbud.repository.UserRepository;
import com.example.colo.bookbud.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WishlistRepository wishlistRepository;

    @Transactional(readOnly = true)
    public PaginatedResponse<BookDTO> getAllBooks(String q, String genre, String condition, 
                                                   String type, BigDecimal minPrice, BigDecimal maxPrice, 
                                                   String status, int page, int size) {
        Book.Status bookStatus = status != null ? Book.Status.valueOf(status) : Book.Status.Available;
        Book.Condition bookCondition = condition != null ? Book.Condition.valueOf(condition) : null;
        Book.TransactionType transactionType = type != null ? Book.TransactionType.valueOf(type) : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByFilters(q, genre, bookCondition, transactionType, 
                bookStatus, minPrice, maxPrice, pageable);

        return PaginatedResponse.<BookDTO>builder()
                .content(books.getContent().stream().map(this::mapToDTO).collect(Collectors.toList()))
                .page(books.getNumber())
                .size(books.getSize())
                .totalElements(books.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public BookDTO getBookById(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));
        return mapToDTO(book);
    }

    @Transactional
    public BookDTO createBook(String ownerId, CreateBookRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        Book book = Book.builder()
                .bookId(UUID.randomUUID().toString())
                .title(request.getTitle())
                .author(request.getAuthor())
                .genre(request.getGenre())
                .condition(Book.Condition.valueOf(request.getCondition()))
                .transactionType(Book.TransactionType.valueOf(request.getTransactionType()))
                .priceRent(request.getPriceRent())
                .priceSale(request.getPriceSale())
                .status(Book.Status.Available)
                .owner(owner)
                .build();

        book = bookRepository.save(book);
        return mapToDTO(book);
    }

    @Transactional
    public BookDTO updateBook(String bookId, String requestingUserId, UpdateBookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        boolean isOwner = book.getOwner().getUserId().equals(requestingUserId);
        boolean isAdmin = userRepository.findById(requestingUserId)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("AUTH-003", "Not owner or admin");
        }

        // Check if book is locked by active transaction
        List<Transaction> activeTransactions = transactionRepository.findByBookBookId(bookId).stream()
                .filter(t -> t.getStatus() == Transaction.Status.Active || t.getStatus() == Transaction.Status.Pending)
                .collect(Collectors.toList());

        if (!activeTransactions.isEmpty()) {
            throw new BusinessException("BUSINESS-001", "Book status is locked by an active transaction");
        }

        if (request.getTitle() != null) book.setTitle(request.getTitle());
        if (request.getAuthor() != null) book.setAuthor(request.getAuthor());
        if (request.getGenre() != null) book.setGenre(request.getGenre());
        if (request.getCondition() != null) book.setCondition(Book.Condition.valueOf(request.getCondition()));
        if (request.getTransactionType() != null) book.setTransactionType(Book.TransactionType.valueOf(request.getTransactionType()));
        if (request.getPriceRent() != null) book.setPriceRent(request.getPriceRent());
        if (request.getPriceSale() != null) book.setPriceSale(request.getPriceSale());

        book = bookRepository.save(book);
        return mapToDTO(book);
    }

    @Transactional
    public void deleteBook(String bookId, String requestingUserId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        boolean isOwner = book.getOwner().getUserId().equals(requestingUserId);
        boolean isAdmin = userRepository.findById(requestingUserId)
                .map(u -> u.getRole() == User.Role.ADMIN)
                .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new BusinessException("AUTH-003", "Insufficient permissions");
        }

        List<Transaction> activeTransactions = transactionRepository.findByBookBookId(bookId).stream()
                .filter(t -> t.getStatus() == Transaction.Status.Active || t.getStatus() == Transaction.Status.Pending)
                .collect(Collectors.toList());

        if (!activeTransactions.isEmpty()) {
            throw new BusinessException("BUSINESS-001", "Book status is locked by an active transaction");
        }

        if (isAdmin && !isOwner) {
            // Admin deletion - set status to Unavailable
            book.setStatus(Book.Status.Unavailable);
            bookRepository.save(book);
            // TODO: Send notification to owner
        } else {
            // Owner deletion - cascade delete wishlist entries
            wishlistRepository.deleteByBookBookId(bookId);
            bookRepository.delete(book);
        }
    }

    @Transactional(readOnly = true)
    public List<ExternalBookDTO> searchExternalBooks(String query) {
        // TODO: Implement Google Books API integration
        // For now, return empty list or mock data
        return List.of();
    }

    private BookDTO mapToDTO(Book book) {
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
}

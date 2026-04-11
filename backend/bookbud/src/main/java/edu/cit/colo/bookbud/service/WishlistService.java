package edu.cit.colo.bookbud.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.colo.bookbud.dto.book.BookDTO;
import edu.cit.colo.bookbud.dto.wishlist.AddToWishlistRequest;
import edu.cit.colo.bookbud.dto.wishlist.WishlistDTO;
import edu.cit.colo.bookbud.entity.Book;
import edu.cit.colo.bookbud.entity.User;
import edu.cit.colo.bookbud.entity.Wishlist;
import edu.cit.colo.bookbud.exception.BusinessException;
import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.repository.BookRepository;
import edu.cit.colo.bookbud.repository.UserRepository;
import edu.cit.colo.bookbud.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WishlistDTO> getMyWishlist(String userId) {
        return wishlistRepository.findByUserUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public WishlistDTO addToWishlist(String userId, AddToWishlistRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "User not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("DB-001", "Book not found"));

        if (book.getOwner().getUserId().equals(userId)) {
            throw new BusinessException("BUSINESS-006", "Cannot wishlist own book");
        }

        if (wishlistRepository.existsByUserUserIdAndBookBookId(userId, request.getBookId())) {
            throw new BusinessException("DB-002", "Book already in wishlist");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .book(book)
                .build();

        wishlist = wishlistRepository.save(wishlist);
        return mapToDTO(wishlist);
    }

    @Transactional
    public void removeFromWishlist(String wishlistId, String userId) {
        Wishlist wishlist = wishlistRepository.findByWishlistIdAndUserUserId(wishlistId, userId)
                .orElseThrow(() -> new BusinessException("AUTH-003", "Not own entry"));

        wishlistRepository.delete(wishlist);
    }

    private WishlistDTO mapToDTO(Wishlist wishlist) {
        String userId = wishlist.getUser() != null ? wishlist.getUser().getUserId() : null;
        String bookId = wishlist.getBook() != null ? wishlist.getBook().getBookId() : null;
        BookDTO book = mapBookToDTO(wishlist.getBook());

        return WishlistDTO.builder()
                .wishlistId(wishlist.getWishlistId())
            .userId(userId)
            .bookId(bookId)
            .book(book)
            .dateAdded(wishlist.getDateAdded() != null ? wishlist.getDateAdded().toString() : null)
                .build();
    }

    private BookDTO mapBookToDTO(Book book) {
        if (book == null) {
            return null;
        }

        String imageUrl = null;
        if (book.getImageFileName() != null && !book.getImageFileName().isBlank() && book.getBookId() != null) {
            imageUrl = "/api/v1/books/" + book.getBookId() + "/image";
        }

        return BookDTO.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .genre(book.getGenre())
                .description(book.getDescription())
                .imageUrl(imageUrl)
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

package com.example.colo.bookbud.service;

import com.example.colo.bookbud.dto.wishlist.AddToWishlistRequest;
import com.example.colo.bookbud.dto.wishlist.WishlistDTO;
import com.example.colo.bookbud.entity.Book;
import com.example.colo.bookbud.entity.User;
import com.example.colo.bookbud.entity.Wishlist;
import com.example.colo.bookbud.exception.BusinessException;
import com.example.colo.bookbud.exception.ResourceNotFoundException;
import com.example.colo.bookbud.repository.BookRepository;
import com.example.colo.bookbud.repository.UserRepository;
import com.example.colo.bookbud.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .wishlistId(UUID.randomUUID().toString())
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
        return WishlistDTO.builder()
                .wishlistId(wishlist.getWishlistId())
                .userId(wishlist.getUser().getUserId())
                .bookId(wishlist.getBook().getBookId())
                .dateAdded(wishlist.getDateAdded().toString())
                .build();
    }
}

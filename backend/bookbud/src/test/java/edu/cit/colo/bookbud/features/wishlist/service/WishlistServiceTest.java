package edu.cit.colo.bookbud.features.wishlist.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.cit.colo.bookbud.shared.exception.BusinessException;
import edu.cit.colo.bookbud.features.books.entity.Book;
import edu.cit.colo.bookbud.features.books.repository.BookRepository;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;
import edu.cit.colo.bookbud.features.wishlist.entity.Wishlist;
import edu.cit.colo.bookbud.features.wishlist.repository.WishlistRepository;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void testGetMyWishlist_Success() {
        // Given
        Wishlist testWishlist = Wishlist.builder()
                .wishlistId("test-wishlist-id")
                .build();
        List<Wishlist> wishlists = Arrays.asList(testWishlist);
        when(wishlistRepository.findByUserUserId("test-user-id")).thenReturn(wishlists);

        // When
        var result = wishlistService.getMyWishlist("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(wishlistRepository, times(1)).findByUserUserId("test-user-id");
    }

    @Test
    void testGetMyWishlist_Empty() {
        // Given
        when(wishlistRepository.findByUserUserId("test-user-id")).thenReturn(Arrays.asList());

        // When
        var result = wishlistService.getMyWishlist("test-user-id");

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(wishlistRepository, times(1)).findByUserUserId("test-user-id");
    }

    @Test
    void testAddToWishlist_Success() {
        // Given
        User testUser = User.builder().userId("test-user-id").build();
        Book testBook = Book.builder().bookId("test-book-id").owner(User.builder().userId("different-user-id").build()).build();
        
        when(userRepository.findById("test-user-id")).thenReturn(Optional.of(testUser));
        when(bookRepository.findById("test-book-id")).thenReturn(Optional.of(testBook));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(Wishlist.builder().build());

        // When
        var result = wishlistService.addToWishlist("test-user-id", 
                new edu.cit.colo.bookbud.features.wishlist.dto.AddToWishlistRequest("test-book-id"));

        // Then
        assertNotNull(result);
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    void testRemoveFromWishlist_Success() {
        // Given
        Wishlist testWishlist = Wishlist.builder()
                .wishlistId("test-wishlist-id")
                .build();
        when(wishlistRepository.findByWishlistIdAndUserUserId("test-wishlist-id", "test-user-id"))
                .thenReturn(Optional.of(testWishlist));
        doNothing().when(wishlistRepository).delete(testWishlist);

        // When
        wishlistService.removeFromWishlist("test-wishlist-id", "test-user-id");

        // Then
        verify(wishlistRepository, times(1)).findByWishlistIdAndUserUserId("test-wishlist-id", "test-user-id");
        verify(wishlistRepository, times(1)).delete(testWishlist);
    }

    @Test
    void testRemoveFromWishlist_NotFound() {
        // Given
        when(wishlistRepository.findByWishlistIdAndUserUserId("test-wishlist-id", "test-user-id"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessException.class, () -> {
            wishlistService.removeFromWishlist("test-wishlist-id", "test-user-id");
        });
        verify(wishlistRepository, times(1)).findByWishlistIdAndUserUserId("test-wishlist-id", "test-user-id");
        verify(wishlistRepository, never()).delete(any(Wishlist.class));
    }
}


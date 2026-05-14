package edu.cit.colo.bookbud.service;

import java.util.Arrays;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import edu.cit.colo.bookbud.shared.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.features.admin.service.AdminService;
import edu.cit.colo.bookbud.features.books.dto.BookDTO;
import edu.cit.colo.bookbud.features.books.entity.Book;
import edu.cit.colo.bookbud.features.books.repository.BookRepository;
import edu.cit.colo.bookbud.features.notifications.service.NotificationService;
import edu.cit.colo.bookbud.features.users.entity.User;
import edu.cit.colo.bookbud.features.users.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminService adminService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testBook = Book.builder()
                .bookId("test-book-id")
                .title("Test Book")
                .owner(User.builder().userId("test-owner-id").build())
                .build();
    }

    @Test
    void testGetAllBooks_Success() {
        // Given
        Page<Book> books = new PageImpl<>(Arrays.asList(testBook), PageRequest.of(0, 10), 1);
        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(books);

        // When
        var result = adminService.getAllBooks(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void testUpdateBookStatus_Success() {
        // Given
        when(bookRepository.findById("test-book-id")).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        BookDTO result = adminService.updateBookStatus("test-book-id", "Available");

        // Then
        assertNotNull(result);
        verify(bookRepository, times(1)).findById("test-book-id");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testUpdateBookStatus_NotFound() {
        // Given
        when(bookRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            adminService.updateBookStatus("non-existent-id", "Available");
        });
        verify(bookRepository, times(1)).findById("non-existent-id");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testDeleteBook_Success() {
        // Given
        when(bookRepository.findById("test-book-id")).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);
        when(notificationService.createNotification(any(), any())).thenReturn(null);

        // When
        adminService.deleteBook("test-book-id");

        // Then
        verify(bookRepository, times(1)).findById("test-book-id");
        verify(bookRepository, times(1)).delete(testBook);
        verify(notificationService, times(1)).createNotification(any(), any());
    }

    @Test
    void testDeleteBook_NotFound() {
        // Given
        when(bookRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            adminService.deleteBook("non-existent-id");
        });
        verify(bookRepository, times(1)).findById("non-existent-id");
        verify(bookRepository, never()).delete(any(Book.class));
    }
}


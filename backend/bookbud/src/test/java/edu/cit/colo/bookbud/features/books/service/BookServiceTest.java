package edu.cit.colo.bookbud.features.books.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.cit.colo.bookbud.exception.ResourceNotFoundException;
import edu.cit.colo.bookbud.features.books.entity.Book;
import edu.cit.colo.bookbud.features.books.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .bookId("test-book-id")
                .title("Test Book")
                .author("Test Author")
                .genre("Fiction")
                .description("Test Description")
                .priceSale(new BigDecimal("29.99"))
                .priceRent(new BigDecimal("5.99"))
                .status(Book.Status.Available)
                .build();
    }

    @Test
    void testGetBookById_Success() {
        // Given
        when(bookRepository.findById("test-book-id")).thenReturn(Optional.of(testBook));

        // When
        var result = bookService.getBookById("test-book-id");

        // Then
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
        verify(bookRepository, times(1)).findById("test-book-id");
    }

    @Test
    void testGetBookById_NotFound() {
        // Given
        when(bookRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.getBookById("non-existent-id");
        });
        verify(bookRepository, times(1)).findById("non-existent-id");
    }
}

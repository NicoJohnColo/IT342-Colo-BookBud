package edu.cit.colo.bookbud.controller;

import edu.cit.colo.bookbud.dto.ApiResponse;
import edu.cit.colo.bookbud.dto.PaginatedResponse;
import edu.cit.colo.bookbud.dto.book.BookDTO;
import edu.cit.colo.bookbud.dto.book.CreateBookRequest;
import edu.cit.colo.bookbud.dto.book.ExternalBookDTO;
import edu.cit.colo.bookbud.dto.book.UpdateBookRequest;
import edu.cit.colo.bookbud.security.JwtUtil;
import edu.cit.colo.bookbud.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<BookDTO>>> getAllBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "Available") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getAllBooks(q, genre, condition, type, 
                minPrice, maxPrice, status, page, size)));
    }

    @GetMapping("/search-external")
    public ResponseEntity<ApiResponse<List<ExternalBookDTO>>> searchExternalBooks(
            @RequestParam String q,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(ApiResponse.success(bookService.searchExternalBooks(q)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookDTO>> createBook(
            @Valid @RequestBody CreateBookRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String ownerId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(bookService.createBook(ownerId, request)));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable String bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookService.getBookById(bookId)));
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable String bookId,
            @Valid @RequestBody UpdateBookRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String requestingUserId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(bookService.updateBook(bookId, requestingUserId, request)));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable String bookId,
            @RequestHeader("Authorization") String authHeader) {
        String requestingUserId = jwtUtil.extractUserId(authHeader.substring(7));
        bookService.deleteBook(bookId, requestingUserId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

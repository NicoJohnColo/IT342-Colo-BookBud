package edu.cit.colo.bookbud.features.books.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.cit.colo.bookbud.dto.ApiResponse;
import edu.cit.colo.bookbud.dto.PaginatedResponse;
import edu.cit.colo.bookbud.features.books.dto.BookDTO;
import edu.cit.colo.bookbud.features.books.dto.CreateBookRequest;
import edu.cit.colo.bookbud.features.books.dto.ExternalBookDTO;
import edu.cit.colo.bookbud.features.books.dto.UpdateBookRequest;
import edu.cit.colo.bookbud.features.books.service.BookService;
import edu.cit.colo.bookbud.features.users.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
            @RequestParam String q) {
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

    @PostMapping(value = "/{bookId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookDTO>> uploadBookImage(
            @PathVariable String bookId,
            @RequestPart("image") MultipartFile image,
            @RequestHeader("Authorization") String authHeader) {
        String requestingUserId = jwtUtil.extractUserId(authHeader.substring(7));
        return ResponseEntity.ok(ApiResponse.success(bookService.uploadBookImage(bookId, requestingUserId, image)));
    }

    @GetMapping(value = "/{bookId}/image")
    public ResponseEntity<byte[]> getBookImage(@PathVariable String bookId) {
        BookService.BookImageFile image = bookService.getBookImage(bookId);
        String contentType = image.contentType() != null ? image.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(image.content());
    }
}

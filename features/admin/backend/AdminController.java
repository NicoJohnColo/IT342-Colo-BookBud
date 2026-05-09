package edu.cit.colo.bookbud.controller;

import edu.cit.colo.bookbud.dto.ApiResponse;
import edu.cit.colo.bookbud.dto.PaginatedResponse;
import edu.cit.colo.bookbud.dto.book.BookDTO;
import edu.cit.colo.bookbud.dto.notification.NotificationDTO;
import edu.cit.colo.bookbud.dto.transaction.TransactionDTO;
import edu.cit.colo.bookbud.dto.user.UserProfileDTO;
import edu.cit.colo.bookbud.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ==================== BOOK MANAGEMENT ====================
    
    @GetMapping("/books")
    public ResponseEntity<ApiResponse<PaginatedResponse<BookDTO>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllBooks(page, size)));
    }

    @PutMapping("/books/{bookId}/status")
    public ResponseEntity<ApiResponse<BookDTO>> updateBookStatus(
            @PathVariable String bookId,
            @RequestBody String status) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateBookStatus(bookId, status)));
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @PathVariable String bookId) {
        adminService.deleteBook(bookId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ==================== USER MANAGEMENT ====================
    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserProfileDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllUsers(page, size)));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody String status) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserStatus(userId, status)));
    }

    // ==================== TRANSACTION MANAGEMENT ====================
    
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PaginatedResponse<TransactionDTO>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllTransactions(page, size)));
    }

    @PutMapping("/transactions/{transactionId}/cancel")
    public ResponseEntity<ApiResponse<TransactionDTO>> cancelTransaction(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.cancelTransaction(transactionId)));
    }

    // ==================== NOTIFICATION MANAGEMENT ====================
    
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<PaginatedResponse<NotificationDTO>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAllNotifications(page, size)));
    }
}
